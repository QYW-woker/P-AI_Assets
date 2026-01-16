package com.example.smartledger.presentation.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 资产页面ViewModel
 */
@HiltViewModel
class AssetsViewModel @Inject constructor(
    // TODO: 注入Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    init {
        loadAssetsData()
    }

    private fun loadAssetsData() {
        viewModelScope.launch {
            // TODO: 从Repository加载真实数据
            _uiState.value = AssetsUiState(
                totalAssets = 128650.00,
                healthScore = 78,
                accounts = generateMockAccounts(),
                monthlyIncome = 15000.00,
                monthlyExpense = 5320.00,
                savingsRate = 0.645f,
                investmentPrincipal = 50000.00,
                investmentCurrentValue = 52800.00,
                investmentReturn = 2800.00,
                investmentReturnRate = 0.056f,
                isLoading = false
            )
        }
    }

    private fun generateMockAccounts(): List<AccountUiModel> {
        return listOf(
            AccountUiModel(
                id = 1,
                name = "招商银行",
                icon = "\uD83C\uDFE6",
                color = "#E8F5E9",
                typeName = "储蓄卡",
                balance = 45680.00
            ),
            AccountUiModel(
                id = 2,
                name = "支付宝",
                icon = "\uD83D\uDCB3",
                color = "#E3F2FD",
                typeName = "电子钱包",
                balance = 3250.00
            ),
            AccountUiModel(
                id = 3,
                name = "微信",
                icon = "\uD83D\uDCB5",
                color = "#FFF3E0",
                typeName = "电子钱包",
                balance = 1520.00
            ),
            AccountUiModel(
                id = 4,
                name = "现金",
                icon = "\uD83D\uDCB0",
                color = "#FCE4EC",
                typeName = "现金",
                balance = 500.00
            )
        )
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
