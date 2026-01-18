package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.RecurringTransactionDao
import com.example.smartledger.data.local.dao.TransactionDao
import com.example.smartledger.data.local.entity.RecurringFrequency
import com.example.smartledger.data.local.entity.RecurringTransactionEntity
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 固定收支仓库实现
 */
@Singleton
class RecurringTransactionRepositoryImpl @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao
) : RecurringTransactionRepository {

    override fun getAllActive(): Flow<List<RecurringTransactionEntity>> {
        return recurringTransactionDao.getAllActive()
    }

    override fun getAll(): Flow<List<RecurringTransactionEntity>> {
        return recurringTransactionDao.getAll()
    }

    override fun getByType(type: TransactionType): Flow<List<RecurringTransactionEntity>> {
        return recurringTransactionDao.getByType(type)
    }

    override suspend fun getDueForExecution(): List<RecurringTransactionEntity> {
        return recurringTransactionDao.getDueForExecution(System.currentTimeMillis())
    }

    override suspend fun getDueForReminder(): List<RecurringTransactionEntity> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.add(Calendar.DAY_OF_YEAR, 7) // 查找未来7天内需要提醒的
        val reminderDate = calendar.timeInMillis
        return recurringTransactionDao.getDueForReminder(now, reminderDate)
    }

    override suspend fun getById(id: Long): RecurringTransactionEntity? {
        return recurringTransactionDao.getById(id)
    }

    override suspend fun create(entity: RecurringTransactionEntity): Long {
        return recurringTransactionDao.insert(entity)
    }

    override suspend fun update(entity: RecurringTransactionEntity) {
        recurringTransactionDao.update(entity.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(entity: RecurringTransactionEntity) {
        recurringTransactionDao.delete(entity)
    }

    override suspend fun markExecuted(id: Long) {
        val entity = recurringTransactionDao.getById(id) ?: return
        val nextDate = calculateNextExecutionDate(entity)
        recurringTransactionDao.updateExecutionDates(
            id = id,
            lastDate = System.currentTimeMillis(),
            nextDate = nextDate
        )
    }

    override suspend fun setActive(id: Long, isActive: Boolean) {
        recurringTransactionDao.setActive(id, isActive)
    }

    override suspend fun getTotalFixedExpense(): Double {
        return recurringTransactionDao.getTotalByType(TransactionType.EXPENSE) ?: 0.0
    }

    override suspend fun getTotalFixedIncome(): Double {
        return recurringTransactionDao.getTotalByType(TransactionType.INCOME) ?: 0.0
    }

    override suspend fun processAllDue(): Int {
        val dueTransactions = getDueForExecution()
        var processedCount = 0

        dueTransactions.forEach { recurring ->
            try {
                // 创建实际交易记录
                val transaction = TransactionEntity(
                    amount = recurring.amount,
                    type = recurring.type,
                    categoryId = recurring.categoryId,
                    accountId = recurring.accountId,
                    date = System.currentTimeMillis(),
                    note = "[固定] ${recurring.name}: ${recurring.note}".trim(),
                    tags = "固定收支,${recurring.name}"
                )
                transactionDao.insert(transaction)

                // 更新下次执行日期
                markExecuted(recurring.id)
                processedCount++
            } catch (e: Exception) {
                // 继续处理其他项
            }
        }

        return processedCount
    }

    override suspend fun getAllForBackup(): List<RecurringTransactionEntity> {
        return recurringTransactionDao.getAllForBackup()
    }

    override suspend fun deleteAll() {
        recurringTransactionDao.clearAll()
    }

    /**
     * 计算下次执行日期
     */
    private fun calculateNextExecutionDate(entity: RecurringTransactionEntity): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = entity.nextExecutionDate

        when (entity.frequency) {
            RecurringFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            RecurringFrequency.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            RecurringFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                // 确保日期有效（如2月没有30号）
                val targetDay = entity.dayOfPeriod.coerceAtMost(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, targetDay)
            }
            RecurringFrequency.YEARLY -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.set(Calendar.MONTH, entity.month - 1)
                val targetDay = entity.dayOfPeriod.coerceAtMost(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.DAY_OF_MONTH, targetDay)
            }
        }

        return calendar.timeInMillis
    }
}
