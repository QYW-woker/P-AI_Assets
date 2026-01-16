package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 预算数据仓库接口
 */
interface BudgetRepository {

    /**
     * 获取所有活跃预算
     */
    fun getAllActiveBudgets(): Flow<List<BudgetEntity>>

    /**
     * 获取总预算（categoryId为null的）
     */
    fun getTotalBudget(): Flow<BudgetEntity?>

    /**
     * 获取分类预算
     */
    fun getCategoryBudgets(): Flow<List<BudgetEntity>>

    /**
     * 根据分类获取预算
     */
    suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity?

    /**
     * 根据ID获取预算
     */
    suspend fun getBudgetById(id: Long): BudgetEntity?

    /**
     * 插入预算
     */
    suspend fun insertBudget(budget: BudgetEntity): Long

    /**
     * 更新预算
     */
    suspend fun updateBudget(budget: BudgetEntity)

    /**
     * 删除预算
     */
    suspend fun deleteBudget(budget: BudgetEntity)

    /**
     * 获取所有预算（用于备份）
     */
    suspend fun getAllBudgets(): List<BudgetEntity>

    /**
     * 删除所有预算
     */
    suspend fun deleteAllBudgets()
}
