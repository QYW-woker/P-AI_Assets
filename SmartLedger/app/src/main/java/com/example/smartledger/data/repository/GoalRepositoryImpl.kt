package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.GoalDao
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
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
}
