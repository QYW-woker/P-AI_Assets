package com.example.smartledger.presentation.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 目标页面ViewModel
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadGoalsData()
    }

    private fun loadGoalsData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val goals = goalRepository.getActiveGoals().first()
                val goalModels = goals.map { goal ->
                    val progress = if (goal.targetAmount > 0) {
                        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    // 估算完成日期
                    val estimatedCompletion = calculateEstimatedCompletion(goal)

                    GoalUiModel(
                        id = goal.id,
                        name = goal.name,
                        icon = goal.icon,
                        targetAmount = goal.targetAmount,
                        currentAmount = goal.currentAmount,
                        progress = progress,
                        deadline = goal.deadline?.let { dateFormat.format(Date(it)) },
                        estimatedCompletion = estimatedCompletion,
                        note = goal.note
                    )
                }

                _uiState.value = GoalsUiState(
                    goals = goalModels,
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

    private fun calculateEstimatedCompletion(goal: GoalEntity): String? {
        if (goal.currentAmount >= goal.targetAmount) {
            return "已完成"
        }

        val createdAt = goal.createdAt
        val now = System.currentTimeMillis()
        val daysPassed = ((now - createdAt) / (1000 * 60 * 60 * 24)).toInt()

        if (daysPassed <= 0 || goal.currentAmount <= 0) {
            return null
        }

        val dailyRate = goal.currentAmount / daysPassed
        val remaining = goal.targetAmount - goal.currentAmount
        val daysNeeded = (remaining / dailyRate).toLong()

        val completionDate = Date(now + daysNeeded * 24 * 60 * 60 * 1000)
        return dateFormat.format(completionDate)
    }

    fun addGoal(name: String, icon: String, targetAmount: Double, deadline: Long?, note: String) {
        viewModelScope.launch {
            val goal = GoalEntity(
                name = name,
                icon = icon,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                deadline = deadline,
                note = note,
                createdAt = System.currentTimeMillis()
            )
            goalRepository.insertGoal(goal)
            loadGoalsData()
        }
    }

    fun addToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            goalRepository.addToCurrentAmount(goalId, amount)

            // 检查是否达成目标
            val goal = goalRepository.getGoalById(goalId)
            if (goal != null && goal.currentAmount >= goal.targetAmount) {
                goalRepository.markGoalCompleted(goalId)
            }

            loadGoalsData()
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            val goal = goalRepository.getGoalById(goalId)
            if (goal != null) {
                goalRepository.deleteGoal(goal)
                loadGoalsData()
            }
        }
    }

    fun refresh() {
        loadGoalsData()
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
