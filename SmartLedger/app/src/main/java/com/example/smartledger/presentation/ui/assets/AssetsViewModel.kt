package com.example.smartledger.presentation.ui.assets

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
 * 资产页面ViewModel
 */
@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    init {
        observeAssetsData()
    }

    /**
     * 持续观察资产数据变化，实时更新UI
     */
    private fun observeAssetsData() {
        viewModelScope.launch {
            // 使用 combine 持续观察账户数据变化
            combine(
                accountRepository.getTotalBalance(),
                accountRepository.getAllActiveAccounts()
            ) { totalAssets, accounts ->
                Pair(totalAssets, accounts)
            }.collectLatest { (totalAssets, accounts) ->
                try {
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

                    // 获取上月时间范围
                    calendar.time = Date()
                    calendar.add(Calendar.MONTH, -1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val lastMonthStart = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, 1)
                    val lastMonthEnd = calendar.timeInMillis

                    // 转换账户列表
                    val accountModels = accounts.map { account ->
                        val typeName = getAccountTypeName(account.type.name)
                        AccountUiModel(
                            id = account.id,
                            name = account.name,
                            icon = account.icon,
                            color = account.color,
                            typeName = typeName,
                            balance = account.balance
                        )
                    }

                    // 获取本月收支
                    val monthlyIncome = transactionRepository.getTotalByDateRange(
                        TransactionType.INCOME, monthStart, monthEnd
                    )
                    val monthlyExpense = transactionRepository.getTotalByDateRange(
                        TransactionType.EXPENSE, monthStart, monthEnd
                    )

                    // 获取上月收支
                    val lastMonthIncome = transactionRepository.getTotalByDateRange(
                        TransactionType.INCOME, lastMonthStart, lastMonthEnd
                    )
                    val lastMonthExpense = transactionRepository.getTotalByDateRange(
                        TransactionType.EXPENSE, lastMonthStart, lastMonthEnd
                    )

                    // 计算储蓄率
                    val savingsRate = if (monthlyIncome > 0) {
                        ((monthlyIncome - monthlyExpense) / monthlyIncome).toFloat().coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    // 获取每日支出趋势
                    val dailyTotals = transactionRepository.getDailyTotals(
                        TransactionType.EXPENSE, monthStart, monthEnd
                    )
                    val dailyExpenseTrend = dailyTotals.map { daily ->
                        DailyTrendUiModel(
                            date = daily.date,
                            amount = daily.amount.toFloat(),
                            label = dateFormat.format(Date(daily.date))
                        )
                    }

                    // 获取本月分类支出
                    val categorySummaries = transactionRepository.getCategorySummary(
                        TransactionType.EXPENSE, monthStart, monthEnd
                    )
                    val categoryExpenses = categorySummaries.map { summary ->
                        val category = categoryRepository.getCategoryById(summary.categoryId)
                        CategoryExpenseUiModel(
                            id = summary.categoryId,
                            name = category?.name ?: "未分类",
                            icon = category?.icon ?: "📦",
                            color = category?.color ?: "#CCCCCC",
                            amount = summary.totalAmount,
                            percent = summary.percent
                        )
                    }

                    // 计算投资收益（基于投资类账户）
                    val investmentAccountEntities = accounts.filter {
                        it.type.name.startsWith("INVESTMENT")
                    }
                    val investmentCurrentValue = investmentAccountEntities.sumOf { it.balance }
                    val investmentPrincipal = investmentAccountEntities.sumOf { it.initialBalance }
                    val investmentReturn = investmentCurrentValue - investmentPrincipal
                    val investmentReturnRate = if (investmentPrincipal > 0) {
                        (investmentReturn / investmentPrincipal).toFloat()
                    } else {
                        0f
                    }

                    // 投资账户列表
                    val investmentAccountModels = investmentAccountEntities.map { account ->
                        val typeName = getAccountTypeName(account.type.name)
                        InvestmentAccountUiModel(
                            id = account.id,
                            name = account.name,
                            icon = account.icon,
                            color = account.color,
                            typeName = typeName,
                            principal = account.initialBalance,
                            currentValue = account.balance
                        )
                    }

                    // 计算健康评分（简化算法）
                    val healthScore = calculateHealthScore(
                        savingsRate = savingsRate,
                        hasEmergencyFund = totalAssets > monthlyExpense * 3,
                        investmentReturnRate = investmentReturnRate
                    )

                    _uiState.value = AssetsUiState(
                        totalAssets = totalAssets,
                        healthScore = healthScore,
                        accounts = accountModels,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense,
                        lastMonthIncome = lastMonthIncome,
                        lastMonthExpense = lastMonthExpense,
                        savingsRate = savingsRate,
                        dailyExpenseTrend = dailyExpenseTrend,
                        categoryExpenses = categoryExpenses,
                        investmentPrincipal = investmentPrincipal,
                        investmentCurrentValue = investmentCurrentValue,
                        investmentReturn = investmentReturn,
                        investmentReturnRate = investmentReturnRate,
                        investmentAccounts = investmentAccountModels,
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
    }

    private fun getAccountTypeName(typeName: String): String {
        return when (typeName) {
            "CASH" -> "现金"
            "DEBIT_CARD" -> "储蓄卡"
            "CREDIT_CARD" -> "信用卡"
            "ALIPAY" -> "支付宝"
            "WECHAT" -> "微信"
            "INVESTMENT_STOCK" -> "股票"
            "INVESTMENT_FUND" -> "基金"
            "INVESTMENT_DEPOSIT" -> "定期存款"
            else -> "其他"
        }
    }

    private fun calculateHealthScore(
        savingsRate: Float,
        hasEmergencyFund: Boolean,
        investmentReturnRate: Float
    ): Int {
        var score = 50 // 基础分

        // 储蓄率评分（最高30分）
        score += (savingsRate * 30).toInt()

        // 应急基金评分（10分）
        if (hasEmergencyFund) score += 10

        // 投资收益率评分（最高10分）
        score += (investmentReturnRate * 100).toInt().coerceIn(0, 10)

        return score.coerceIn(0, 100)
    }

    fun refresh() {
        loadAssetsData()
    }
}

/**
 * 资产页面UI状态
 */
data class AssetsUiState(
    val totalAssets: Double = 0.0,
    val healthScore: Int = 0,
    val accounts: List<AccountUiModel> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val lastMonthIncome: Double = 0.0,
    val lastMonthExpense: Double = 0.0,
    val savingsRate: Float = 0f,
    val dailyExpenseTrend: List<DailyTrendUiModel> = emptyList(),
    val categoryExpenses: List<CategoryExpenseUiModel> = emptyList(),
    val investmentPrincipal: Double = 0.0,
    val investmentCurrentValue: Double = 0.0,
    val investmentReturn: Double = 0.0,
    val investmentReturnRate: Float = 0f,
    val investmentAccounts: List<InvestmentAccountUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
