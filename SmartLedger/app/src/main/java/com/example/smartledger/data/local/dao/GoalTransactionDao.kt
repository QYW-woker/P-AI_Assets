package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.smartledger.data.local.entity.GoalTransactionEntity
import com.example.smartledger.data.local.entity.GoalTransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 储蓄目标交易记录DAO
 */
@Dao
interface GoalTransactionDao {

    /**
     * 获取某个目标的所有交易记录（按时间倒序）
     */
    @Query("SELECT * FROM goal_transactions WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun getTransactionsByGoalId(goalId: Long): Flow<List<GoalTransactionEntity>>

    /**
     * 获取某个目标的所有交易记录（一次性查询）
     */
    @Query("SELECT * FROM goal_transactions WHERE goalId = :goalId ORDER BY createdAt DESC")
    suspend fun getTransactionsByGoalIdOnce(goalId: Long): List<GoalTransactionEntity>

    /**
     * 获取某个目标在指定时间范围内的交易记录
     */
    @Query("""
        SELECT * FROM goal_transactions
        WHERE goalId = :goalId
        AND createdAt >= :startDate
        AND createdAt <= :endDate
        ORDER BY createdAt DESC
    """)
    fun getTransactionsByDateRange(
        goalId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<GoalTransactionEntity>>

    /**
     * 获取某个目标的存入总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM goal_transactions
        WHERE goalId = :goalId AND type = 'DEPOSIT'
    """)
    suspend fun getTotalDeposits(goalId: Long): Double

    /**
     * 获取某个目标的取出总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM goal_transactions
        WHERE goalId = :goalId AND type = 'WITHDRAW'
    """)
    suspend fun getTotalWithdrawals(goalId: Long): Double

    /**
     * 获取某个目标的交易次数
     */
    @Query("SELECT COUNT(*) FROM goal_transactions WHERE goalId = :goalId")
    suspend fun getTransactionCount(goalId: Long): Int

    /**
     * 获取某个目标的最新一条交易记录
     */
    @Query("SELECT * FROM goal_transactions WHERE goalId = :goalId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestTransaction(goalId: Long): GoalTransactionEntity?

    /**
     * 插入交易记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: GoalTransactionEntity): Long

    /**
     * 批量插入交易记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<GoalTransactionEntity>)

    /**
     * 删除交易记录
     */
    @Delete
    suspend fun delete(transaction: GoalTransactionEntity)

    /**
     * 删除某个目标的所有交易记录
     */
    @Query("DELETE FROM goal_transactions WHERE goalId = :goalId")
    suspend fun deleteAllByGoalId(goalId: Long)

    /**
     * 获取所有交易记录（用于备份）
     */
    @Query("SELECT * FROM goal_transactions ORDER BY id")
    suspend fun getAllForBackup(): List<GoalTransactionEntity>

    /**
     * 清空所有交易记录
     */
    @Query("DELETE FROM goal_transactions")
    suspend fun clearAll()
}
