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

    private val _expenseCategories = MutableStateFlow<List<BudgetCategoryItem>>(emptyList())
    val expenseCategories: StateFlow<List<BudgetCategoryItem>> = _expenseCategories.asStateFlow()

    init {
        loadBudgetData()
        loadExpenseCategories()
    }

    private fun loadExpenseCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE).collect { categories ->
                _expenseCategories.value = categories.map { category ->
                    BudgetCategoryItem(
                        id = category.id,
                        name = category.name,
                        icon = category.icon,
                        color = category.color
                    )
                }
            }
        }
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 获取总预算
                val totalBudget = budgetRepository.getTotalBudget().first()
                val totalBudgetModel = if (totalBudget != null) {
                    calculateBudgetUsage(totalBudget, null)
                } else {
                    null
                }

                // 获取分类预算
                val categoryBudgets = budgetRepository.getCategoryBudgets().first()
                val budgetModels = categoryBudgets.mapNotNull { budget ->
                    val category = budget.categoryId?.let { categoryRepository.getCategoryById(it) }
                    if (category != null) {
                        calculateBudgetUsage(budget, category.id)?.copy(
                            categoryName = category.name,
                            categoryIcon = category.icon,
                            categoryColor = category.color
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

    /**
     * 根据预算周期计算使用情况
     */
    private suspend fun calculateBudgetUsage(budget: BudgetEntity, categoryId: Long?): BudgetUiModel? {
        // 获取预算周期的时间范围
        val (periodStart, periodEnd, daysRemaining) = getDateRangeForPeriod(budget.period, budget.startDate)

        // 获取该周期内的支出
        val expense = if (categoryId != null) {
            // 分类支出
            val summary = transactionRepository.getCategorySummary(
                TransactionType.EXPENSE, periodStart, periodEnd
            )
            summary.find { it.categoryId == categoryId }?.totalAmount ?: 0.0
        } else {
            // 总支出
            transactionRepository.getTotalByDateRange(
                TransactionType.EXPENSE, periodStart, periodEnd
            )
        }

        val remaining = budget.amount - expense
        val dailyAvailable = if (daysRemaining > 0 && remaining > 0) {
            remaining / daysRemaining
        } else {
            0.0
        }

        return BudgetUiModel(
            id = budget.id,
            categoryId = categoryId,
            categoryName = null,
            categoryIcon = null,
            categoryColor = null,
            amount = budget.amount,
            used = expense,
            remaining = remaining.coerceAtLeast(0.0),
            dailyAvailable = dailyAvailable,
            period = budget.period,
            periodDisplayName = budget.period.displayName,
            daysRemaining = daysRemaining,
            alertThreshold = budget.alertThreshold,
            name = budget.name
        )
    }

    /**
     * 根据预算周期获取时间范围
     * @return Triple<开始时间, 结束时间, 剩余天数>
     */
    private fun getDateRangeForPeriod(period: BudgetPeriod, startDate: Long): Triple<Long, Long, Int> {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()

        return when (period) {
            BudgetPeriod.WEEKLY -> {
                // 本周开始（周一）
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val weekStart = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 7)
                val weekEnd = calendar.timeInMillis

                val daysRemaining = ((weekEnd - now.timeInMillis) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
                Triple(weekStart, weekEnd, daysRemaining)
            }

            BudgetPeriod.BIWEEKLY -> {
                // 双周预算，从周一开始
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // 计算当前是哪个双周周期
                val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                if (weekOfYear % 2 != 0) {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                val biweekStart = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 14)
                val biweekEnd = calendar.timeInMillis

                val daysRemaining = ((biweekEnd - now.timeInMillis) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
                Triple(biweekStart, biweekEnd, daysRemaining)
            }

            BudgetPeriod.MONTHLY -> {
                // 本月开始
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                val monthEnd = calendar.timeInMillis

                val lastDayOfMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
                val currentDay = now.get(Calendar.DAY_OF_MONTH)
                val daysRemaining = lastDayOfMonth - currentDay + 1

                Triple(monthStart, monthEnd, daysRemaining)
            }

            BudgetPeriod.QUARTERLY -> {
                // 本季度开始
                val currentMonth = calendar.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                calendar.set(Calendar.MONTH, quarterStartMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val quarterStart = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 3)
                val quarterEnd = calendar.timeInMillis

                val daysRemaining = ((quarterEnd - now.timeInMillis) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
                Triple(quarterStart, quarterEnd, daysRemaining)
            }

            BudgetPeriod.SEMI_ANNUAL -> {
                // 半年周期
                val currentMonth = calendar.get(Calendar.MONTH)
                val halfYearStartMonth = if (currentMonth < 6) 0 else 6
                calendar.set(Calendar.MONTH, halfYearStartMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val halfYearStart = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 6)
                val halfYearEnd = calendar.timeInMillis

                val daysRemaining = ((halfYearEnd - now.timeInMillis) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
                Triple(halfYearStart, halfYearEnd, daysRemaining)
            }

            BudgetPeriod.YEARLY -> {
                // 本年开始
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val yearStart = calendar.timeInMillis

                calendar.add(Calendar.YEAR, 1)
                val yearEnd = calendar.timeInMillis

                val lastDayOfYear = now.getActualMaximum(Calendar.DAY_OF_YEAR)
                val currentDayOfYear = now.get(Calendar.DAY_OF_YEAR)
                val daysRemaining = lastDayOfYear - currentDayOfYear + 1

                Triple(yearStart, yearEnd, daysRemaining)
            }
        }
    }

    /**
     * 添加总预算（支持选择周期）
     */
    fun addTotalBudget(amount: Double, period: BudgetPeriod = BudgetPeriod.MONTHLY, name: String = "") {
        viewModelScope.launch {
            val budget = BudgetEntity(
                categoryId = null,
                amount = amount,
                period = period,
                startDate = System.currentTimeMillis(),
                name = name
            )
            budgetRepository.insertBudget(budget)
            loadBudgetData()
        }
    }

    /**
     * 添加分类预算（支持选择周期）
     */
    fun addCategoryBudget(categoryId: Long, amount: Double, period: BudgetPeriod = BudgetPeriod.MONTHLY, name: String = "") {
        viewModelScope.launch {
            val budget = BudgetEntity(
                categoryId = categoryId,
                amount = amount,
                period = period,
                startDate = System.currentTimeMillis(),
                name = name
            )
            budgetRepository.insertBudget(budget)
            loadBudgetData()
        }
    }

    /**
     * 更新预算
     */
    fun updateBudget(budgetId: Long, amount: Double, period: BudgetPeriod, alertThreshold: Float = 0.8f) {
        viewModelScope.launch {
            val budget = budgetRepository.getBudgetById(budgetId)
            if (budget != null) {
                budgetRepository.updateBudget(
                    budget.copy(
                        amount = amount,
                        period = period,
                        alertThreshold = alertThreshold
                    )
                )
                loadBudgetData()
            }
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

/**
 * 预算UI模型
 */
data class BudgetUiModel(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val amount: Double,
    val used: Double,
    val remaining: Double,
    val dailyAvailable: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val periodDisplayName: String = "每月",
    val daysRemaining: Int = 0,
    val alertThreshold: Float = 0.8f,
    val name: String = ""
) {
    /** 使用百分比 */
    val usagePercent: Float
        get() = if (amount > 0) (used / amount).toFloat().coerceIn(0f, 1f) else 0f

    /** 是否超支 */
    val isOverBudget: Boolean
        get() = used > amount

    /** 是否达到警告阈值 */
    val isWarning: Boolean
        get() = usagePercent >= alertThreshold && !isOverBudget
}

/**
 * 预算分类项
 */
data class BudgetCategoryItem(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String
)
