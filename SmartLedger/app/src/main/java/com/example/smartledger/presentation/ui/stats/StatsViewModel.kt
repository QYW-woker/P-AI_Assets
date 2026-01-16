package com.example.smartledger.presentation.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 统计页面ViewModel
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    // TODO: 注入Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStatsData()
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            // TODO: 从Repository加载真实数据
            _uiState.value = StatsUiState(
                totalIncome = 15000.00,
                totalExpense = 5320.00,
                balance = 9680.00,
                categoryRanking = generateMockRanking(),
                isLoading = false
            )
        }
    }

    private fun generateMockRanking(): List<CategoryRankingUiModel> {
        return listOf(
            CategoryRankingUiModel(
                id = 1,
                name = "餐饮美食",
                icon = "\uD83C\uDF5C",
                color = "#FFF3E0",
                amount = 1580.00,
                percent = 29.7f
            ),
            CategoryRankingUiModel(
                id = 2,
                name = "购物消费",
                icon = "\uD83D\uDED2",
                color = "#FCE4EC",
                amount = 1200.00,
                percent = 22.6f
            ),
            CategoryRankingUiModel(
                id = 3,
                name = "交通出行",
                icon = "\uD83D\uDE87",
                color = "#E3F2FD",
                amount = 850.00,
                percent = 16.0f
            ),
            CategoryRankingUiModel(
                id = 4,
                name = "娱乐休闲",
                icon = "\uD83C\uDFAC",
                color = "#F3E5F5",
                amount = 690.00,
                percent = 13.0f
            ),
            CategoryRankingUiModel(
                id = 5,
                name = "居住生活",
                icon = "\uD83C\uDFE0",
                color = "#E8F5E9",
                amount = 1000.00,
                percent = 18.8f
            )
        )
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
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
