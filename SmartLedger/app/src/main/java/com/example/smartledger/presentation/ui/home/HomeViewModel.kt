package com.example.smartledger.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.BudgetRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 首页ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeData()
    }

    /**
     * 持续监听数据变化，实现实时更新
     */
    private fun observeHomeData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 组合多个 Flow，任一变化都会触发更新
                combine(
                    accountRepository.getTotalBalance(),
                    budgetRepository.getTotalBudget(),
                    transactionRepository.getRecentTransactions(10)
                ) { totalAssets, totalBudget, recentTransactions ->
                    Triple(totalAssets, totalBudget, recentTransactions)
                }.collectLatest { (totalAssets, totalBudget, recentTransactions) ->

                    val currentMonth = SimpleDateFormat("yyyy年M月", Locale.CHINA).format(Date())

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
                    val lastDayOfMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val currentDay = today.get(Calendar.DAY_OF_MONTH)
                    val daysRemaining = lastDayOfMonth - currentDay + 1

                    // 获取本月收支（实时计算）
                    val monthlyIncome = transactionRepository.getTotalByDateRange(
                        TransactionType.INCOME, monthStart, monthEnd
                    )
                    val monthlyExpense = transactionRepository.getTotalByDateRange(
                        TransactionType.EXPENSE, monthStart, monthEnd
                    )

                    val budgetAmount = totalBudget?.amount ?: 0.0
                    val budgetUsed = monthlyExpense

                    // 计算日均可用
                    val dailyAvailable = if (daysRemaining > 0 && budgetAmount > budgetUsed) {
                        (budgetAmount - budgetUsed) / daysRemaining
                    } else {
                        0.0
                    }

                    // 转换交易为UI模型
                    val transactionUiModels = recentTransactions.map { transaction ->
                        val category = categoryRepository.getCategoryById(transaction.categoryId)
                        TransactionUiModel(
                            id = transaction.id,
                            categoryName = category?.name ?: "未分类",
                            categoryIcon = category?.icon ?: "📦",
                            categoryColor = category?.color ?: "#CCCCCC",
                            amount = transaction.amount,
                            note = transaction.note,
                            isExpense = transaction.type == TransactionType.EXPENSE,
                            date = transaction.date
                        )
                    }

                    _uiState.value = HomeUiState(
                        currentMonth = currentMonth,
                        totalAssets = totalAssets,
                        assetsChange = 0.0,
                        assetsChangePercent = 0f,
                        budgetTotal = budgetAmount,
                        budgetUsed = budgetUsed,
                        dailyAvailable = dailyAvailable,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense,
                        monthlyInvestmentReturn = 0.0,
                        recentTransactions = transactionUiModels,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun refresh() {
        observeHomeData()
    }
}

/**
 * 首页UI状态
 */
data class HomeUiState(
    val currentMonth: String = "",
    val totalAssets: Double = 0.0,
    val assetsChange: Double = 0.0,
    val assetsChangePercent: Float = 0f,
    val budgetTotal: Double = 0.0,
    val budgetUsed: Double = 0.0,
    val dailyAvailable: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyInvestmentReturn: Double = 0.0,
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
