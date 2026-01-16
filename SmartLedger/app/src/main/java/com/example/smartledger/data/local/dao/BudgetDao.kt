package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 预算DAO
 */
@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE isActive = 1")
    fun getAllActiveBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND isActive = 1 LIMIT 1")
    fun getTotalBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND isActive = 1 LIMIT 1")
    suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId IS NOT NULL AND isActive = 1")
    fun getCategoryBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("UPDATE budgets SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY id")
    suspend fun getAllBudgetsForBackup(): List<BudgetEntity>

    @Query("DELETE FROM budgets")
    suspend fun clearAll()
}
