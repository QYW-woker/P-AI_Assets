package com.example.smartledger.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页面ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: 注入SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // TODO: 从DataStore加载设置
            _uiState.value = SettingsUiState(
                currency = "CNY ¥",
                monthStartDay = 1,
                weekStartDay = "周一",
                isDarkMode = false,
                isDailyReminderEnabled = true,
                reminderTime = "21:00",
                isBudgetAlertEnabled = true,
                isLoading = false
            )
        }
    }

    fun setCurrency(currency: String) {
        _uiState.update { it.copy(currency = currency) }
        // TODO: 保存到DataStore
    }

    fun setMonthStartDay(day: Int) {
        _uiState.update { it.copy(monthStartDay = day) }
        // TODO: 保存到DataStore
    }

    fun setWeekStartDay(day: String) {
        _uiState.update { it.copy(weekStartDay = day) }
        // TODO: 保存到DataStore
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        // TODO: 保存到DataStore并更新主题
    }

    fun setDailyReminder(enabled: Boolean) {
        _uiState.update { it.copy(isDailyReminderEnabled = enabled) }
        // TODO: 保存到DataStore并配置提醒
    }

    fun setReminderTime(time: String) {
        _uiState.update { it.copy(reminderTime = time) }
        // TODO: 保存到DataStore并更新提醒时间
    }

    fun setBudgetAlert(enabled: Boolean) {
        _uiState.update { it.copy(isBudgetAlertEnabled = enabled) }
        // TODO: 保存到DataStore
    }
}

/**
 * 设置页面UI状态
 */
data class SettingsUiState(
    val currency: String = "CNY ¥",
    val monthStartDay: Int = 1,
    val weekStartDay: String = "周一",
    val isDarkMode: Boolean = false,
    val isDailyReminderEnabled: Boolean = false,
    val reminderTime: String = "21:00",
    val isBudgetAlertEnabled: Boolean = true,
    val isLoading: Boolean = true
)
