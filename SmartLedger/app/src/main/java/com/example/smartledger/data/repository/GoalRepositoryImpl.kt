package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.GoalDao
import com.example.smartledger.data.local.dao.GoalTransactionDao
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.data.local.entity.GoalTransactionEntity
import com.example.smartledger.data.local.entity.GoalTransactionType
import com.example.smartledger.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val goalTransactionDao: GoalTransactionDao
) : GoalRepository {

    override fun getActiveGoals(): Flow<List<GoalEntity>> {
        return goalDao.getActiveGoals()
    }

    override fun getCompletedGoals(): Flow<List<GoalEntity>> {
        return goalDao.getCompletedGoals()
    }

    override fun getAllGoals(): Flow<List<GoalEntity>> {
        return goalDao.getAllGoals()
    }

    override suspend fun getGoalById(id: Long): GoalEntity? {
        return goalDao.getGoalById(id)
    }

    override suspend fun insertGoal(goal: GoalEntity): Long {
        return goalDao.insert(goal)
    }

    override suspend fun updateGoal(goal: GoalEntity) {
        goalDao.update(goal)
    }

    override suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.delete(goal)
    }

    override suspend fun markGoalCompleted(goalId: Long) {
        goalDao.markAsCompleted(goalId)
    }

    override suspend fun updateCurrentAmount(goalId: Long, amount: Double) {
        goalDao.setCurrentAmount(goalId, amount)
    }

    override suspend fun addToCurrentAmount(goalId: Long, delta: Double) {
        goalDao.addToCurrentAmount(goalId, delta)
    }

    override suspend fun getAllGoalsForBackup(): List<GoalEntity> {
        return goalDao.getAllGoalsForBackup()
    }

    override suspend fun deleteAllGoals() {
        goalDao.clearAll()
    }

    // ==================== 目标交易记录相关 ====================

    override fun getGoalTransactions(goalId: Long): Flow<List<GoalTransactionEntity>> {
        return goalTransactionDao.getTransactionsByGoalId(goalId)
    }

    override suspend fun getGoalTransactionsOnce(goalId: Long): List<GoalTransactionEntity> {
        return goalTransactionDao.getTransactionsByGoalIdOnce(goalId)
    }

    override suspend fun depositToGoal(goalId: Long, amount: Double, note: String) {
        // 先更新目标金额
        goalDao.addToCurrentAmount(goalId, amount)

        // 获取更新后的目标以记录余额
        val goal = goalDao.getGoalById(goalId)
        val balanceAfter = goal?.currentAmount ?: 0.0

        // 记录交易
        goalTransactionDao.insert(
            GoalTransactionEntity(
                goalId = goalId,
                type = GoalTransactionType.DEPOSIT,
                amount = amount,
                balanceAfter = balanceAfter,
                note = note
            )
        )

        // 检查是否达成目标
        if (goal != null && goal.currentAmount >= goal.targetAmount) {
            goalDao.markAsCompleted(goalId)
        }
    }

    override suspend fun withdrawFromGoal(goalId: Long, amount: Double, note: String) {
        // 先更新目标金额
        goalDao.addToCurrentAmount(goalId, -amount)

        // 获取更新后的目标以记录余额
        val goal = goalDao.getGoalById(goalId)
        val balanceAfter = goal?.currentAmount ?: 0.0

        // 记录交易
        goalTransactionDao.insert(
            GoalTransactionEntity(
                goalId = goalId,
                type = GoalTransactionType.WITHDRAW,
                amount = amount,
                balanceAfter = balanceAfter,
                note = note
            )
        )
    }

    override suspend fun getTotalDeposits(goalId: Long): Double {
        return goalTransactionDao.getTotalDeposits(goalId)
    }

    override suspend fun getTotalWithdrawals(goalId: Long): Double {
        return goalTransactionDao.getTotalWithdrawals(goalId)
    }
}
