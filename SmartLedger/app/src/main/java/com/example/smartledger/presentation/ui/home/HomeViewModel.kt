package com.example.smartledger.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * 首页ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: 注入Repository
    // private val transactionRepository: TransactionRepository,
    // private val accountRepository: AccountRepository,
    // private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // TODO: 从Repository加载真实数据
            // 目前使用模拟数据
            val currentMonth = SimpleDateFormat("yyyy年M月", Locale.CHINA).format(Date())

            _uiState.value = HomeUiState(
                currentMonth = currentMonth,
                totalAssets = 128650.00,
                assetsChange = 3280.00,
                assetsChangePercent = 2.6f,
                budgetTotal = 8000.00,
                budgetUsed = 5320.00,
                dailyAvailable = 89.33,
                monthlyIncome = 15000.00,
                monthlyExpense = 5320.00,
                monthlyInvestmentReturn = 580.00,
                recentTransactions = generateMockTransactions(),
                isLoading = false
            )
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadHomeData()
    }

    private fun generateMockTransactions(): List<TransactionUiModel> {
        return listOf(
            TransactionUiModel(
                id = 1,
                categoryName = "餐饮美食",
                categoryIcon = "\uD83C\uDF5C",
                categoryColor = "#FFF3E0",
                amount = 35.00,
                note = "午餐",
                isExpense = true,
                date = System.currentTimeMillis()
            ),
            TransactionUiModel(
                id = 2,
                categoryName = "交通出行",
                categoryIcon = "\uD83D\uDE87",
                categoryColor = "#E3F2FD",
                amount = 6.00,
                note = "地铁",
                isExpense = true,
                date = System.currentTimeMillis() - 3600000
            ),
            TransactionUiModel(
                id = 3,
                categoryName = "工资薪酬",
                categoryIcon = "\uD83D\uDCB0",
                categoryColor = "#E8F5E9",
                amount = 15000.00,
                note = "1月工资",
                isExpense = false,
                date = System.currentTimeMillis() - 86400000
            ),
            TransactionUiModel(
                id = 4,
                categoryName = "购物消费",
                categoryIcon = "\uD83D\uDED2",
                categoryColor = "#FCE4EC",
                amount = 299.00,
                note = "淘宝购物",
                isExpense = true,
                date = System.currentTimeMillis() - 172800000
            ),
            TransactionUiModel(
                id = 5,
                categoryName = "娱乐休闲",
                categoryIcon = "\uD83C\uDFAC",
                categoryColor = "#F3E5F5",
                amount = 89.00,
                note = "电影票",
                isExpense = true,
                date = System.currentTimeMillis() - 259200000
            )
        )
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
