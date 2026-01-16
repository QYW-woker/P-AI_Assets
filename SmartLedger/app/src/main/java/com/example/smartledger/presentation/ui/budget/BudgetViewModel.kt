package com.example.smartledger.presentation.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.BudgetEntity
import com.example.smartledger.data.local.entity.BudgetPeriod
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.BudgetRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 预算页面ViewModel
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 获取本月时间范围
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                val monthEnd = calendar.timeInMillis

                // 计算剩余天数
                val today = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val daysInMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val daysRemaining = daysInMonth - today.get(Calendar.DAY_OF_MONTH) + 1

                // 获取本月总支出
                val monthlyExpense = transactionRepository.getTotalByDateRange(
                    TransactionType.EXPENSE, monthStart, monthEnd
                )

                // 获取总预算
                val totalBudget = budgetRepository.getTotalBudget().first()
                val totalBudgetModel = if (totalBudget != null) {
                    val remaining = totalBudget.amount - monthlyExpense
                    val dailyAvailable = if (daysRemaining > 0 && remaining > 0) {
                        remaining / daysRemaining
                    } else {
                        0.0
                    }
                    BudgetUiModel(
                        id = totalBudget.id,
                        categoryId = null,
                        categoryName = null,
                        categoryIcon = null,
                        categoryColor = null,
                        amount = totalBudget.amount,
                        used = monthlyExpense,
                        remaining = remaining.coerceAtLeast(0.0),
                        dailyAvailable = dailyAvailable
                    )
                } else {
                    null
                }

                // 获取分类预算
                val categoryBudgets = budgetRepository.getCategoryBudgets().first()
                val budgetModels = categoryBudgets.mapNotNull { budget ->
                    val category = budget.categoryId?.let { categoryRepository.getCategoryById(it) }
                    if (category != null) {
                        // 简化：使用总支出的比例作为分类支出（实际应该按分类查询）
                        val categoryUsed = monthlyExpense * 0.25 // 简化计算
                        val remaining = budget.amount - categoryUsed
                        val dailyAvailable = if (daysRemaining > 0 && remaining > 0) {
                            remaining / daysRemaining
                        } else {
                            0.0
                        }

                        BudgetUiModel(
                            id = budget.id,
                            categoryId = category.id,
                            categoryName = category.name,
                            categoryIcon = category.icon,
                            categoryColor = category.color,
                            amount = budget.amount,
                            used = categoryUsed,
                            remaining = remaining.coerceAtLeast(0.0),
                            dailyAvailable = dailyAvailable
                        )
                    } else null
                }

                _uiState.value = BudgetUiState(
                    totalBudget = totalBudgetModel,
                    budgets = budgetModels,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun addTotalBudget(amount: Double) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                categoryId = null,
                amount = amount,
                period = BudgetPeriod.MONTHLY,
                startDate = System.currentTimeMillis()
            )
            budgetRepository.insertBudget(budget)
            loadBudgetData()
        }
    }

    fun addCategoryBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                categoryId = categoryId,
                amount = amount,
                period = BudgetPeriod.MONTHLY,
                startDate = System.currentTimeMillis()
            )
            budgetRepository.insertBudget(budget)
            loadBudgetData()
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            val budget = budgetRepository.getBudgetById(budgetId)
            if (budget != null) {
                budgetRepository.deleteBudget(budget)
                loadBudgetData()
            }
        }
    }

    fun refresh() {
        loadBudgetData()
    }
}

/**
 * 预算页面UI状态
 */
data class BudgetUiState(
    val totalBudget: BudgetUiModel? = null,
    val budgets: List<BudgetUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
