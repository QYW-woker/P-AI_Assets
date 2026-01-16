package com.example.smartledger.presentation.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 统计页面ViewModel
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var currentPeriod = "月"

    init {
        loadStatsData()
    }

    fun setPeriod(period: String) {
        currentPeriod = period
        loadStatsData()
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 获取时间范围
                val (startDate, endDate) = getDateRange(currentPeriod)

                // 获取收支总额
                val totalIncome = transactionRepository.getTotalByDateRange(
                    TransactionType.INCOME, startDate, endDate
                )
                val totalExpense = transactionRepository.getTotalByDateRange(
                    TransactionType.EXPENSE, startDate, endDate
                )

                // 获取分类统计
                val categorySummaries = transactionRepository.getCategorySummary(
                    TransactionType.EXPENSE, startDate, endDate
                )

                val categoryRanking = categorySummaries.map { summary ->
                    val category = categoryRepository.getCategoryById(summary.categoryId)
                    CategoryRankingUiModel(
                        id = summary.categoryId,
                        name = category?.name ?: "未分类",
                        icon = category?.icon ?: "📦",
                        color = category?.color ?: "#CCCCCC",
                        amount = summary.totalAmount,
                        percent = summary.percent
                    )
                }

                _uiState.value = StatsUiState(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = totalIncome - totalExpense,
                    categoryRanking = categoryRanking,
                    selectedPeriod = currentPeriod,
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

    private fun getDateRange(period: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        when (period) {
            "日" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "周" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "月" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "年" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }

        return Pair(calendar.timeInMillis, endDate)
    }

    fun refresh() {
        loadStatsData()
    }
}

/**
 * 统计页面UI状态
 */
data class StatsUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryRanking: List<CategoryRankingUiModel> = emptyList(),
    val selectedPeriod: String = "月",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
