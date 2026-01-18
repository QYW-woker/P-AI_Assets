package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 交易数据仓库接口
 */
interface TransactionRepository {

    /**
     * 获取指定日期范围内的交易记录（Flow版本）
     */
    fun getTransactionsByDateRangeFlow(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * 获取指定类型的交易记录
     */
    fun getTransactionsByType(type: TransactionType, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * 获取指定分类的交易记录
     */
    fun getTransactionsByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * 获取指定账户的交易记录
     */
    fun getTransactionsByAccount(accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * 获取最近的交易记录
     */
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    /**
     * 获取分类汇总
     */
    suspend fun getCategorySummary(type: TransactionType, startDate: Long, endDate: Long): List<CategorySummary>

    /**
     * 获取日期范围内的总金额
     */
    suspend fun getTotalByDateRange(type: TransactionType, startDate: Long, endDate: Long): Double

    /**
     * 获取日期范围内的交易数量
     */
    suspend fun getCountByDateRange(startDate: Long, endDate: Long): Int

    /**
     * 获取日期范围内的交易数量（别名）
     */
    suspend fun getTransactionCountByDateRange(startDate: Long, endDate: Long): Int

    /**
     * 获取指定账户在日期范围内的总金额
     */
    suspend fun getAccountTotalByDateRange(accountId: Long, type: TransactionType, startDate: Long, endDate: Long): Double

    /**
     * 获取日期范围内的交易列表（非Flow）
     */
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<TransactionEntity>

    /**
     * 获取每日汇总（用于趋势图）
     */
    suspend fun getDailyTotals(type: TransactionType, startDate: Long, endDate: Long): List<DailyTotal>

    /**
     * 根据ID获取交易
     */
    suspend fun getTransactionById(id: Long): TransactionEntity?

    /**
     * 插入交易
     */
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    /**
     * 更新交易
     */
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * 删除交易
     */
    suspend fun deleteTransaction(transaction: TransactionEntity)

    /**
     * 批量删除交易
     */
    suspend fun deleteTransactions(transactions: List<TransactionEntity>)

    /**
     * 搜索交易（按备注）
     */
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    /**
     * 获取所有交易（用于备份）
     */
    suspend fun getAllTransactions(): List<TransactionEntity>

    /**
     * 删除所有交易
     */
    suspend fun deleteAllTransactions()
}

/**
 * 分类汇总数据类
 */
data class CategorySummary(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Double,
    val count: Int,
    val percent: Float = 0f
)

/**
 * 每日汇总数据类
 */
data class DailyTotal(
    val date: Long,
    val amount: Double,
    val label: String
)
