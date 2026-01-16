package com.example.smartledger.presentation.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 预算页面ViewModel
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    // TODO: 注入Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            // TODO: 从Repository加载真实数据
            _uiState.value = BudgetUiState(
                totalBudget = BudgetUiModel(
                    id = 0,
                    categoryId = null,
                    categoryName = null,
                    categoryIcon = null,
                    categoryColor = null,
                    amount = 8000.00,
                    used = 5320.00,
                    remaining = 2680.00,
                    dailyAvailable = 89.33
                ),
                budgets = generateMockBudgets(),
                isLoading = false
            )
        }
    }

    private fun generateMockBudgets(): List<BudgetUiModel> {
        return listOf(
            BudgetUiModel(
                id = 1,
                categoryId = 1,
                categoryName = "餐饮美食",
                categoryIcon = "\uD83C\uDF5C",
                categoryColor = "#FFF3E0",
                amount = 2000.00,
                used = 1580.00,
                remaining = 420.00,
                dailyAvailable = 14.00
            ),
            BudgetUiModel(
                id = 2,
                categoryId = 3,
                categoryName = "购物消费",
                categoryIcon = "\uD83D\uDED2",
                categoryColor = "#FCE4EC",
                amount = 1500.00,
                used = 1200.00,
                remaining = 300.00,
                dailyAvailable = 10.00
            ),
            BudgetUiModel(
                id = 3,
                categoryId = 2,
                categoryName = "交通出行",
                categoryIcon = "\uD83D\uDE87",
                categoryColor = "#E3F2FD",
                amount = 1000.00,
                used = 850.00,
                remaining = 150.00,
                dailyAvailable = 5.00
            ),
            BudgetUiModel(
                id = 4,
                categoryId = 4,
                categoryName = "娱乐休闲",
                categoryIcon = "\uD83C\uDFAC",
                categoryColor = "#F3E5F5",
                amount = 800.00,
                used = 690.00,
                remaining = 110.00,
                dailyAvailable = 3.67
            )
        )
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
