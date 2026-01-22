package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.data.local.entity.GoalTransactionEntity
import com.example.smartledger.data.local.entity.GoalTransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 储蓄目标数据仓库接口
 */
interface GoalRepository {

    /**
     * 获取所有活跃目标（未完成）
     */
    fun getActiveGoals(): Flow<List<GoalEntity>>

    /**
     * 获取已完成目标
     */
    fun getCompletedGoals(): Flow<List<GoalEntity>>

    /**
     * 获取所有目标
     */
    fun getAllGoals(): Flow<List<GoalEntity>>

    /**
     * 根据ID获取目标
     */
    suspend fun getGoalById(id: Long): GoalEntity?

    /**
     * 插入目标
     */
    suspend fun insertGoal(goal: GoalEntity): Long

    /**
     * 更新目标
     */
    suspend fun updateGoal(goal: GoalEntity)

    /**
     * 删除目标
     */
    suspend fun deleteGoal(goal: GoalEntity)

    /**
     * 标记目标为完成
     */
    suspend fun markGoalCompleted(goalId: Long)

    /**
     * 更新目标当前金额
     */
    suspend fun updateCurrentAmount(goalId: Long, amount: Double)

    /**
     * 增加目标当前金额
     */
    suspend fun addToCurrentAmount(goalId: Long, delta: Double)

    /**
     * 获取所有目标（用于备份）
     */
    suspend fun getAllGoalsForBackup(): List<GoalEntity>

    /**
     * 删除所有目标
     */
    suspend fun deleteAllGoals()

    // ==================== 目标交易记录相关 ====================

    /**
     * 获取目标的交易记录
     */
    fun getGoalTransactions(goalId: Long): Flow<List<GoalTransactionEntity>>

    /**
     * 获取目标的交易记录（一次性查询）
     */
    suspend fun getGoalTransactionsOnce(goalId: Long): List<GoalTransactionEntity>

    /**
     * 添加存款记录并更新目标金额
     */
    suspend fun depositToGoal(goalId: Long, amount: Double, note: String = "")

    /**
     * 添加取款记录并更新目标金额
     */
    suspend fun withdrawFromGoal(goalId: Long, amount: Double, note: String = "")

    /**
     * 获取目标的存款总额
     */
    suspend fun getTotalDeposits(goalId: Long): Double

    /**
     * 获取目标的取款总额
     */
    suspend fun getTotalWithdrawals(goalId: Long): Double
}
