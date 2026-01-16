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

                // 获取每日趋势数据
                val dailyTotals = transactionRepository.getDailyTotals(
                    TransactionType.EXPENSE, startDate, endDate
                )
                val dailyTrend = dailyTotals.map { daily ->
                    DailyTrendUiModel(
                        date = daily.date,
                        amount = daily.amount.toFloat(),
                        label = daily.label
                    )
                }

                _uiState.value = StatsUiState(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = totalIncome - totalExpense,
                    categoryRanking = categoryRanking,
                    dailyTrend = dailyTrend,
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
            "季" -> {
                // 当前季度的第一天
                val currentMonth = calendar.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                calendar.set(Calendar.MONTH, quarterStartMonth)
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
            "全部" -> {
                // 从很早的时间开始（2020年1月1日）
                calendar.set(2020, Calendar.JANUARY, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "自定义" -> {
                // 自定义默认显示最近30天
                calendar.add(Calendar.DAY_OF_MONTH, -30)
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
    val dailyTrend: List<DailyTrendUiModel> = emptyList(),
    val selectedPeriod: String = "月",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * 每日趋势UI模型
 */
data class DailyTrendUiModel(
    val date: Long,
    val amount: Float,
    val label: String
)
