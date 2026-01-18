package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.RecurringTransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 固定收支仓库接口
 */
interface RecurringTransactionRepository {

    /**
     * 获取所有活跃的固定收支
     */
    fun getAllActive(): Flow<List<RecurringTransactionEntity>>

    /**
     * 获取所有固定收支
     */
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    /**
     * 按类型获取
     */
    fun getByType(type: TransactionType): Flow<List<RecurringTransactionEntity>>

    /**
     * 获取需要执行的固定收支
     */
    suspend fun getDueForExecution(): List<RecurringTransactionEntity>

    /**
     * 获取需要提醒的固定收支
     */
    suspend fun getDueForReminder(): List<RecurringTransactionEntity>

    /**
     * 根据ID获取
     */
    suspend fun getById(id: Long): RecurringTransactionEntity?

    /**
     * 创建固定收支
     */
    suspend fun create(entity: RecurringTransactionEntity): Long

    /**
     * 更新固定收支
     */
    suspend fun update(entity: RecurringTransactionEntity)

    /**
     * 删除固定收支
     */
    suspend fun delete(entity: RecurringTransactionEntity)

    /**
     * 标记已执行并更新下次执行日期
     */
    suspend fun markExecuted(id: Long)

    /**
     * 切换启用状态
     */
    suspend fun setActive(id: Long, isActive: Boolean)

    /**
     * 获取固定支出总额
     */
    suspend fun getTotalFixedExpense(): Double

    /**
     * 获取固定收入总额
     */
    suspend fun getTotalFixedIncome(): Double

    /**
     * 处理所有到期的固定收支（自动记账）
     */
    suspend fun processAllDue(): Int

    /**
     * 获取所有数据（用于备份）
     */
    suspend fun getAllForBackup(): List<RecurringTransactionEntity>

    /**
     * 清空所有数据
     */
    suspend fun deleteAll()
}
