package com.example.smartledger.presentation.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
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
 * 资产页面ViewModel
 */
@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    init {
        loadAssetsData()
    }

    private fun loadAssetsData() {
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

                // 获取总资产
                val totalAssets = accountRepository.getTotalBalance().first()

                // 获取所有账户
                val accounts = accountRepository.getAllActiveAccounts().first()
                val accountModels = accounts.map { account ->
                    val typeName = when (account.type.name) {
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

                // 计算储蓄率
                val savingsRate = if (monthlyIncome > 0) {
                    ((monthlyIncome - monthlyExpense) / monthlyIncome).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }

                // 计算投资收益（基于投资类账户）
                val investmentAccounts = accounts.filter {
                    it.type.name.startsWith("INVESTMENT")
                }
                val investmentCurrentValue = investmentAccounts.sumOf { it.balance }
                val investmentPrincipal = investmentAccounts.sumOf { it.initialBalance }
                val investmentReturn = investmentCurrentValue - investmentPrincipal
                val investmentReturnRate = if (investmentPrincipal > 0) {
                    (investmentReturn / investmentPrincipal).toFloat()
                } else {
                    0f
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
                    savingsRate = savingsRate,
                    investmentPrincipal = investmentPrincipal,
                    investmentCurrentValue = investmentCurrentValue,
                    investmentReturn = investmentReturn,
                    investmentReturnRate = investmentReturnRate,
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
    val savingsRate: Float = 0f,
    val investmentPrincipal: Double = 0.0,
    val investmentCurrentValue: Double = 0.0,
    val investmentReturn: Double = 0.0,
    val investmentReturnRate: Float = 0f,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
