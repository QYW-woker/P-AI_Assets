package com.example.smartledger.presentation.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 统计页面ViewModel
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var currentPeriod = "月"
    private var showIncome = false // false = 支出, true = 收入
    private val dateFormat = SimpleDateFormat("MM月dd日", Locale.CHINESE)

    init {
        loadStatsData()
    }

    fun setPeriod(period: String) {
        currentPeriod = period
        loadStatsData()
    }

    fun toggleIncomeExpense() {
        showIncome = !showIncome
        loadStatsData()
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 获取时间范围
                val (startDate, endDate) = getDateRange(currentPeriod)

                // 获取上一个周期的时间范围（用于对比）
                val (lastPeriodStart, lastPeriodEnd) = getLastPeriodDateRange(currentPeriod)

                // 获取收支总额
                val totalIncome = transactionRepository.getTotalByDateRange(
                    TransactionType.INCOME, startDate, endDate
                )
                val totalExpense = transactionRepository.getTotalByDateRange(
                    TransactionType.EXPENSE, startDate, endDate
                )

                // 获取上期收支（用于环比）
                val lastPeriodIncome = transactionRepository.getTotalByDateRange(
                    TransactionType.INCOME, lastPeriodStart, lastPeriodEnd
                )
                val lastPeriodExpense = transactionRepository.getTotalByDateRange(
                    TransactionType.EXPENSE, lastPeriodStart, lastPeriodEnd
                )

                // 计算环比变化
                val incomeChange = if (lastPeriodIncome > 0) {
                    ((totalIncome - lastPeriodIncome) / lastPeriodIncome * 100).toFloat()
                } else if (totalIncome > 0) 100f else 0f

                val expenseChange = if (lastPeriodExpense > 0) {
                    ((totalExpense - lastPeriodExpense) / lastPeriodExpense * 100).toFloat()
                } else if (totalExpense > 0) 100f else 0f

                // 获取交易笔数
                val transactionCount = transactionRepository.getTransactionCountByDateRange(startDate, endDate)

                // 计算日均支出
                val daysDiff = ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                val avgDailyExpense = totalExpense / daysDiff

                // 获取分类统计（根据收入/支出切换）
                val transactionType = if (showIncome) TransactionType.INCOME else TransactionType.EXPENSE
                val categorySummaries = transactionRepository.getCategorySummary(
                    transactionType, startDate, endDate
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

                // 获取最近交易记录
                val recentTransactions = transactionRepository.getTransactionsByDateRange(startDate, endDate)
                    .take(10)
                    .map { transaction ->
                        val category = categoryRepository.getCategoryById(transaction.categoryId)
                        RecentTransactionUiModel(
                            id = transaction.id,
                            categoryName = category?.name ?: "未分类",
                            categoryIcon = category?.icon ?: "📦",
                            amount = transaction.amount,
                            type = transaction.type,
                            date = dateFormat.format(Date(transaction.date)),
                            note = transaction.note
                        )
                    }

                // 获取账户余额变动
                val accounts = accountRepository.getAllActiveAccounts().first()
                val accountChanges = accounts.map { account ->
                    // 计算本期内该账户的收支变动
                    val accountIncome = transactionRepository.getAccountTotalByDateRange(
                        account.id, TransactionType.INCOME, startDate, endDate
                    )
                    val accountExpense = transactionRepository.getAccountTotalByDateRange(
                        account.id, TransactionType.EXPENSE, startDate, endDate
                    )
                    AccountChangeUiModel(
                        id = account.id,
                        name = account.name,
                        icon = account.icon,
                        color = account.color,
                        currentBalance = account.balance,
                        periodChange = accountIncome - accountExpense
                    )
                }.filter { it.periodChange != 0.0 }

                _uiState.value = StatsUiState(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = totalIncome - totalExpense,
                    incomeChange = incomeChange,
                    expenseChange = expenseChange,
                    transactionCount = transactionCount,
                    avgDailyExpense = avgDailyExpense,
                    categoryRanking = categoryRanking,
                    dailyTrend = dailyTrend,
                    recentTransactions = recentTransactions,
                    accountChanges = accountChanges,
                    showIncome = showIncome,
                    selectedPeriod = currentPeriod,
                    periodLabel = getPeriodLabel(currentPeriod, startDate, endDate),
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

    private fun getPeriodLabel(period: String, startDate: Long, endDate: Long): String {
        val sdf = SimpleDateFormat("yyyy年MM月", Locale.CHINESE)
        return when (period) {
            "月" -> sdf.format(Date(startDate))
            "周" -> {
                val weekFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                "${weekFormat.format(Date(startDate))} - ${weekFormat.format(Date(endDate))}"
            }
            "季" -> {
                val cal = Calendar.getInstance()
                cal.timeInMillis = startDate
                val quarter = (cal.get(Calendar.MONTH) / 3) + 1
                "${cal.get(Calendar.YEAR)}年第${quarter}季度"
            }
            "年" -> {
                val yearFormat = SimpleDateFormat("yyyy年", Locale.CHINESE)
                yearFormat.format(Date(startDate))
            }
            else -> period
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
                calendar.set(2020, Calendar.JANUARY, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "自定义" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -30)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }

        return Pair(calendar.timeInMillis, endDate)
    }

    private fun getLastPeriodDateRange(period: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        when (period) {
            "日" -> {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                return Pair(startDate, calendar.timeInMillis)
            }
            "周" -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                return Pair(startDate, calendar.timeInMillis)
            }
            "月" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                return Pair(startDate, calendar.timeInMillis)
            }
            "季" -> {
                calendar.add(Calendar.MONTH, -3)
                val currentMonth = calendar.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                calendar.set(Calendar.MONTH, quarterStartMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 3)
                return Pair(startDate, calendar.timeInMillis)
            }
            "年" -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.YEAR, 1)
                return Pair(startDate, calendar.timeInMillis)
            }
            else -> {
                // 默认上个月
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                return Pair(startDate, calendar.timeInMillis)
            }
        }
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
    val incomeChange: Float = 0f,
    val expenseChange: Float = 0f,
    val transactionCount: Int = 0,
    val avgDailyExpense: Double = 0.0,
    val categoryRanking: List<CategoryRankingUiModel> = emptyList(),
    val dailyTrend: List<DailyTrendUiModel> = emptyList(),
    val recentTransactions: List<RecentTransactionUiModel> = emptyList(),
    val accountChanges: List<AccountChangeUiModel> = emptyList(),
    val showIncome: Boolean = false,
    val selectedPeriod: String = "月",
    val periodLabel: String = "",
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

/**
 * 最近交易UI模型
 */
data class RecentTransactionUiModel(
    val id: Long,
    val categoryName: String,
    val categoryIcon: String,
    val amount: Double,
    val type: TransactionType,
    val date: String,
    val note: String?
)

/**
 * 账户变动UI模型
 */
data class AccountChangeUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val currentBalance: Double,
    val periodChange: Double
)
