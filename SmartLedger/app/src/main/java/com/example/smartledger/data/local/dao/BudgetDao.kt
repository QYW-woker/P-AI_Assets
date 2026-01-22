package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.BudgetEntity
import com.example.smartledger.data.local.entity.BudgetPeriod
import kotlinx.coroutines.flow.Flow

/**
 * 预算DAO
 */
@Dao
interface BudgetDao {

    /**
     * 获取所有激活的预算
     */
    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveBudgets(): Flow<List<BudgetEntity>>

    /**
     * 获取总预算（不限周期）
     */
    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND isActive = 1 LIMIT 1")
    fun getTotalBudget(): Flow<BudgetEntity?>

    /**
     * 获取指定周期的总预算
     */
    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND period = :period AND isActive = 1 LIMIT 1")
    fun getTotalBudgetByPeriod(period: BudgetPeriod): Flow<BudgetEntity?>

    /**
     * 获取指定分类的预算
     */
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND isActive = 1 LIMIT 1")
    suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity?

    /**
     * 获取指定分类和周期的预算
     */
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND period = :period AND isActive = 1 LIMIT 1")
    suspend fun getBudgetByCategoryAndPeriod(categoryId: Long, period: BudgetPeriod): BudgetEntity?

    /**
     * 获取所有分类预算
     */
    @Query("SELECT * FROM budgets WHERE categoryId IS NOT NULL AND isActive = 1 ORDER BY createdAt DESC")
    fun getCategoryBudgets(): Flow<List<BudgetEntity>>

    /**
     * 获取指定周期的所有分类预算
     */
    @Query("SELECT * FROM budgets WHERE categoryId IS NOT NULL AND period = :period AND isActive = 1 ORDER BY createdAt DESC")
    fun getCategoryBudgetsByPeriod(period: BudgetPeriod): Flow<List<BudgetEntity>>

    /**
     * 获取指定周期的所有预算（总预算+分类预算）
     */
    @Query("SELECT * FROM budgets WHERE period = :period AND isActive = 1 ORDER BY createdAt DESC")
    fun getBudgetsByPeriod(period: BudgetPeriod): Flow<List<BudgetEntity>>

    /**
     * 根据ID获取预算
     */
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    /**
     * 获取当前有效的预算（在时间范围内）
     */
    @Query("""
        SELECT * FROM budgets
        WHERE isActive = 1
        AND (startDate <= :currentTime AND (endDate = 0 OR endDate >= :currentTime))
        ORDER BY createdAt DESC
    """)
    fun getCurrentValidBudgets(currentTime: Long): Flow<List<BudgetEntity>>

    /**
     * 插入预算
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    /**
     * 批量插入预算
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    /**
     * 更新预算
     */
    @Update
    suspend fun update(budget: BudgetEntity)

    /**
     * 软删除预算
     */
    @Query("UPDATE budgets SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    /**
     * 硬删除预算
     */
    @Delete
    suspend fun delete(budget: BudgetEntity)

    /**
     * 获取所有预算用于备份
     */
    @Query("SELECT * FROM budgets ORDER BY id")
    suspend fun getAllBudgetsForBackup(): List<BudgetEntity>

    /**
     * 清空所有预算
     */
    @Query("DELETE FROM budgets")
    suspend fun clearAll()

    /**
     * 检查是否存在相同分类和周期的预算
     */
    @Query("""
        SELECT COUNT(*) FROM budgets
        WHERE categoryId = :categoryId
        AND period = :period
        AND isActive = 1
    """)
    suspend fun countByCategoryAndPeriod(categoryId: Long?, period: BudgetPeriod): Int

    /**
     * 更新预算的结束日期（用于自动续期）
     */
    @Query("UPDATE budgets SET endDate = :endDate WHERE id = :id")
    suspend fun updateEndDate(id: Long, endDate: Long)
}
