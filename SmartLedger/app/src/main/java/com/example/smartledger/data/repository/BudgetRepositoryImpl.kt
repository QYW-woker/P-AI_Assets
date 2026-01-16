package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.BudgetDao
import com.example.smartledger.data.local.entity.BudgetEntity
import com.example.smartledger.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getAllActiveBudgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getAllActiveBudgets()
    }

    override fun getTotalBudget(): Flow<BudgetEntity?> {
        return budgetDao.getTotalBudget()
    }

    override fun getCategoryBudgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getCategoryBudgets()
    }

    override suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity? {
        return budgetDao.getBudgetByCategory(categoryId)
    }

    override suspend fun getBudgetById(id: Long): BudgetEntity? {
        return budgetDao.getBudgetById(id)
    }

    override suspend fun insertBudget(budget: BudgetEntity): Long {
        return budgetDao.insert(budget)
    }

    override suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.update(budget)
    }

    override suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.softDelete(budget.id)
    }

    override suspend fun getAllBudgets(): List<BudgetEntity> {
        return budgetDao.getAllBudgetsForBackup()
    }
}
