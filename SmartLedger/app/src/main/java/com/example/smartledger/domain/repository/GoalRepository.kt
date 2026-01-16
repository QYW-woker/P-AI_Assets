package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.GoalEntity
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
}
