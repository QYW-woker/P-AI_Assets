package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * 目标DAO
 */
@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY deadline")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY deadline DESC")
    fun getCompletedGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY isCompleted, deadline")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Query("UPDATE goals SET currentAmount = currentAmount + :delta WHERE id = :goalId")
    suspend fun addToCurrentAmount(goalId: Long, delta: Double)

    @Query("UPDATE goals SET currentAmount = :amount WHERE id = :goalId")
    suspend fun setCurrentAmount(goalId: Long, amount: Double)

    @Query("UPDATE goals SET isCompleted = 1 WHERE id = :goalId")
    suspend fun markAsCompleted(goalId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM goals ORDER BY id")
    suspend fun getAllGoalsForBackup(): List<GoalEntity>

    @Query("DELETE FROM goals")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM goals WHERE isCompleted = 0")
    suspend fun getActiveGoalCount(): Int
}
