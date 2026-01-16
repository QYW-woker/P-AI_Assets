package com.example.smartledger.presentation.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 目标页面ViewModel
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    // TODO: 注入Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoalsData()
    }

    private fun loadGoalsData() {
        viewModelScope.launch {
            // TODO: 从Repository加载真实数据
            _uiState.value = GoalsUiState(
                goals = generateMockGoals(),
                isLoading = false
            )
        }
    }

    private fun generateMockGoals(): List<GoalUiModel> {
        return listOf(
            GoalUiModel(
                id = 1,
                name = "旅行基金",
                icon = "\u2708\uFE0F",
                targetAmount = 20000.00,
                currentAmount = 8500.00,
                deadline = "2025-06-30",
                estimatedCompletion = "2025-05-15",
                note = "计划去日本旅行"
            ),
            GoalUiModel(
                id = 2,
                name = "应急资金",
                icon = "\uD83D\uDEE1\uFE0F",
                targetAmount = 50000.00,
                currentAmount = 32000.00,
                deadline = null,
                estimatedCompletion = "2025-08-20",
                note = "6个月生活费储备"
            ),
            GoalUiModel(
                id = 3,
                name = "新电脑",
                icon = "\uD83D\uDCBB",
                targetAmount = 12000.00,
                currentAmount = 4800.00,
                deadline = "2025-03-31",
                estimatedCompletion = "2025-04-10",
                note = "MacBook Pro"
            )
        )
    }
}

/**
 * 目标页面UI状态
 */
data class GoalsUiState(
    val goals: List<GoalUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
