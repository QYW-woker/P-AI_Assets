package com.example.smartledger.presentation.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.CategorySummary
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 报告ViewModel
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    private val weekFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())

    init {
        loadReport(ReportPeriod.MONTH)
    }

    fun selectPeriod(period: ReportPeriod) {
        loadReport(period)
    }

    private fun loadReport(period: ReportPeriod) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, selectedPeriod = period)

                val (startDate, endDate, title) = getDateRange(period)
                val (lastStartDate, lastEndDate) = getLastPeriodDateRange(period)

                // 本期数据
                val income = transactionRepository.getTotalByDateRange(TransactionType.INCOME, startDate, endDate)
                val expense = transactionRepository.getTotalByDateRange(TransactionType.EXPENSE, startDate, endDate)
                val transactionCount = transactionRepository.getCountByDateRange(startDate, endDate)

                // 上期数据（用于对比）
                val lastIncome = transactionRepository.getTotalByDateRange(TransactionType.INCOME, lastStartDate, lastEndDate)
                val lastExpense = transactionRepository.getTotalByDateRange(TransactionType.EXPENSE, lastStartDate, lastEndDate)

                // 分类统计
                val expenseByCategory = transactionRepository.getCategorySummary(TransactionType.EXPENSE, startDate, endDate)
                val incomeByCategory = transactionRepository.getCategorySummary(TransactionType.INCOME, startDate, endDate)

                // 每日趋势
                val dailyExpenses = transactionRepository.getDailyTotals(TransactionType.EXPENSE, startDate, endDate)
                val dailyIncomes = transactionRepository.getDailyTotals(TransactionType.INCOME, startDate, endDate)

                // 计算变化率
                val incomeChange = if (lastIncome > 0) ((income - lastIncome) / lastIncome * 100) else 0.0
                val expenseChange = if (lastExpense > 0) ((expense - lastExpense) / lastExpense * 100) else 0.0

                // 计算日均支出
                val days = ((endDate - startDate) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
                val avgDailyExpense = expense / days

                // 找出最大支出日
                val maxExpenseDay = dailyExpenses.maxByOrNull { it.amount }

                // 储蓄率
                val savingsRate = if (income > 0) ((income - expense) / income * 100) else 0.0

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    periodTitle = title,
                    totalIncome = income,
                    totalExpense = expense,
                    balance = income - expense,
                    savingsRate = savingsRate,
                    transactionCount = transactionCount,
                    avgDailyExpense = avgDailyExpense,
                    incomeChange = incomeChange,
                    expenseChange = expenseChange,
                    expenseByCategory = expenseByCategory,
                    incomeByCategory = incomeByCategory,
                    topExpenseCategory = expenseByCategory.firstOrNull(),
                    topIncomeCategory = incomeByCategory.firstOrNull(),
                    dailyExpenses = dailyExpenses.map { ReportDayData(it.label, it.amount) },
                    dailyIncomes = dailyIncomes.map { ReportDayData(it.label, it.amount) },
                    maxExpenseDay = maxExpenseDay?.let { ReportDayData(it.label, it.amount) },
                    startDate = startDate,
                    endDate = endDate
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun getDateRange(period: ReportPeriod): Triple<Long, Long, String> {
        val calendar = Calendar.getInstance()
        val endDate: Long
        val startDate: Long
        val title: String

        when (period) {
            ReportPeriod.WEEK -> {
                // 本周
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                endDate = calendar.timeInMillis

                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                val weekStart = weekFormat.format(Date(startDate))
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val weekEnd = weekFormat.format(calendar.time)
                title = "周报 ($weekStart - $weekEnd)"
            }
            ReportPeriod.MONTH -> {
                // 本月
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                endDate = calendar.timeInMillis

                title = monthFormat.format(Date(startDate)) + " 月报"
            }
            ReportPeriod.YEAR -> {
                // 本年
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.YEAR, 1)
                endDate = calendar.timeInMillis

                title = "${calendar.get(Calendar.YEAR) - 1}年 年报"
            }
        }

        return Triple(startDate, endDate, title)
    }

    private fun getLastPeriodDateRange(period: ReportPeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        when (period) {
            ReportPeriod.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis

                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val endDate = calendar.timeInMillis

                return Pair(startDate, endDate)
            }
            ReportPeriod.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                val endDate = calendar.timeInMillis

                return Pair(startDate, endDate)
            }
            ReportPeriod.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.add(Calendar.YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis

                calendar.add(Calendar.YEAR, 1)
                val endDate = calendar.timeInMillis

                return Pair(startDate, endDate)
            }
        }
    }

    fun refresh() {
        loadReport(_uiState.value.selectedPeriod)
    }
}

/**
 * 报告周期
 */
enum class ReportPeriod(val label: String) {
    WEEK("周报"),
    MONTH("月报"),
    YEAR("年报")
}

/**
 * 报告UI状态
 */
data class ReportUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: ReportPeriod = ReportPeriod.MONTH,
    val periodTitle: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val savingsRate: Double = 0.0,
    val transactionCount: Int = 0,
    val avgDailyExpense: Double = 0.0,
    val incomeChange: Double = 0.0,
    val expenseChange: Double = 0.0,
    val expenseByCategory: List<CategorySummary> = emptyList(),
    val incomeByCategory: List<CategorySummary> = emptyList(),
    val topExpenseCategory: CategorySummary? = null,
    val topIncomeCategory: CategorySummary? = null,
    val dailyExpenses: List<ReportDayData> = emptyList(),
    val dailyIncomes: List<ReportDayData> = emptyList(),
    val maxExpenseDay: ReportDayData? = null,
    val startDate: Long = 0,
    val endDate: Long = 0,
    val error: String? = null
)

/**
 * 每日数据
 */
data class ReportDayData(
    val label: String,
    val amount: Double
)
