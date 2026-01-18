package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.RecurringTransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 固定收支数据访问对象
 */
@Dao
interface RecurringTransactionDao {

    /**
     * 获取所有活跃的固定收支
     */
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<RecurringTransactionEntity>>

    /**
     * 获取所有固定收支
     */
    @Query("SELECT * FROM recurring_transactions ORDER BY name ASC")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    /**
     * 按类型获取固定收支
     */
    @Query("SELECT * FROM recurring_transactions WHERE type = :type AND isActive = 1 ORDER BY name ASC")
    fun getByType(type: TransactionType): Flow<List<RecurringTransactionEntity>>

    /**
     * 获取需要执行的固定收支（下次执行日期已过）
     */
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND autoExecute = 1 AND nextExecutionDate <= :currentDate AND (endDate IS NULL OR endDate >= :currentDate)")
    suspend fun getDueForExecution(currentDate: Long): List<RecurringTransactionEntity>

    /**
     * 获取需要提醒的固定收支
     */
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND remindBefore = 1 AND nextExecutionDate <= :reminderDate AND nextExecutionDate > :currentDate AND (endDate IS NULL OR endDate >= :currentDate)")
    suspend fun getDueForReminder(currentDate: Long, reminderDate: Long): List<RecurringTransactionEntity>

    /**
     * 根据ID获取
     */
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    /**
     * 插入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecurringTransactionEntity): Long

    /**
     * 更新
     */
    @Update
    suspend fun update(entity: RecurringTransactionEntity)

    /**
     * 删除
     */
    @Delete
    suspend fun delete(entity: RecurringTransactionEntity)

    /**
     * 更新下次执行日期
     */
    @Query("UPDATE recurring_transactions SET nextExecutionDate = :nextDate, lastExecutionDate = :lastDate, executionCount = executionCount + 1, updatedAt = :now WHERE id = :id")
    suspend fun updateExecutionDates(id: Long, lastDate: Long, nextDate: Long, now: Long = System.currentTimeMillis())

    /**
     * 切换启用状态
     */
    @Query("UPDATE recurring_transactions SET isActive = :isActive, updatedAt = :now WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean, now: Long = System.currentTimeMillis())

    /**
     * 获取固定收支汇总（按类型）
     */
    @Query("SELECT SUM(amount) FROM recurring_transactions WHERE type = :type AND isActive = 1")
    suspend fun getTotalByType(type: TransactionType): Double?

    /**
     * 清空所有数据
     */
    @Query("DELETE FROM recurring_transactions")
    suspend fun clearAll()

    /**
     * 获取所有数据（用于备份）
     */
    @Query("SELECT * FROM recurring_transactions")
    suspend fun getAllForBackup(): List<RecurringTransactionEntity>
}
