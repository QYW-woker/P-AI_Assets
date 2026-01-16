package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.CategoryDao
import com.example.smartledger.data.local.dao.TransactionDao
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.CategorySummary
import com.example.smartledger.domain.repository.DailyTotal
import com.example.smartledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : TransactionRepository {

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    override fun getTransactionsByType(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { transactions ->
            transactions.filter { it.type == type }
        }
    }

    override fun getTransactionsByCategory(
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(categoryId, startDate, endDate)
    }

    override fun getTransactionsByAccount(
        accountId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByAccount(accountId, startDate, endDate)
    }

    override fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }

    override suspend fun getCategorySummary(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): List<CategorySummary> {
        val summaries = transactionDao.getCategorySummary(type, startDate, endDate)
        val total = summaries.sumOf { it.totalAmount }

        return summaries.map { summary ->
            val category = categoryDao.getCategoryById(summary.categoryId)
            CategorySummary(
                categoryId = summary.categoryId,
                categoryName = category?.name ?: "未知分类",
                totalAmount = summary.totalAmount,
                count = summary.count,
                percent = if (total > 0) (summary.totalAmount / total * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.totalAmount }
    }

    override suspend fun getTotalByDateRange(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Double {
        return transactionDao.getTotalByDateRange(type, startDate, endDate) ?: 0.0
    }

    override suspend fun getCountByDateRange(startDate: Long, endDate: Long): Int {
        return transactionDao.getCountByDateRange(startDate, endDate)
    }

    override suspend fun getDailyTotals(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): List<DailyTotal> {
        val results = transactionDao.getDailyTotals(type, startDate, endDate)
        val dateFormat = SimpleDateFormat("M/d", Locale.getDefault())
        return results.map { result ->
            DailyTotal(
                date = result.dayTimestamp,
                amount = result.totalAmount,
                label = dateFormat.format(Date(result.dayTimestamp))
            )
        }
    }

    override suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    override suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insert(transaction)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }

    override suspend fun deleteTransactions(transactions: List<TransactionEntity>) {
        transactionDao.deleteAll(transactions)
    }

    override fun searchTransactions(query: String): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(1000).map { transactions ->
            transactions.filter {
                it.note.contains(query, ignoreCase = true) ||
                it.tags.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun getAllTransactions(): List<TransactionEntity> {
        return transactionDao.getAllTransactionsForBackup()
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.clearAll()
    }
}
