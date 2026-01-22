package com.example.smartledger.presentation.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.datastore.SettingsDataStore
import com.example.smartledger.domain.repository.BudgetRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.GoalRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

/**
 * 设置页面ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { settings ->
                _uiState.value = SettingsUiState(
                    currency = settings.currency,
                    monthStartDay = settings.monthStartDay,
                    weekStartDay = settings.weekStartDay,
                    isDarkMode = settings.isDarkMode,
                    isDailyReminderEnabled = settings.isDailyReminderEnabled,
                    reminderTime = settings.reminderTime,
                    isBudgetAlertEnabled = settings.isBudgetAlertEnabled,
                    isLoading = false
                )
            }
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            settingsDataStore.setCurrency(currency)
            _uiState.update { it.copy(currency = currency) }
        }
    }

    fun setMonthStartDay(day: Int) {
        viewModelScope.launch {
            settingsDataStore.setMonthStartDay(day)
            _uiState.update { it.copy(monthStartDay = day) }
        }
    }

    fun setWeekStartDay(day: String) {
        viewModelScope.launch {
            settingsDataStore.setWeekStartDay(day)
            _uiState.update { it.copy(weekStartDay = day) }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(enabled)
            _uiState.update { it.copy(isDarkMode = enabled) }
        }
    }

    fun setDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDailyReminder(enabled)
            _uiState.update { it.copy(isDailyReminderEnabled = enabled) }
        }
    }

    fun setReminderTime(time: String) {
        viewModelScope.launch {
            settingsDataStore.setReminderTime(time)
            _uiState.update { it.copy(reminderTime = time) }
        }
    }

    fun setBudgetAlert(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setBudgetAlert(enabled)
            _uiState.update { it.copy(isBudgetAlertEnabled = enabled) }
        }
    }

    /**
     * 清除所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                transactionRepository.deleteAllTransactions()
                budgetRepository.deleteAllBudgets()
                goalRepository.deleteAllGoals()
                // 保留默认分类，只清除自定义分类
                categoryRepository.clearCustomCategories()
                settingsDataStore.clearAll()
                // 重新初始化默认分类
                categoryRepository.initDefaultCategories()
                _uiState.update { it.copy(isLoading = false) }
                Log.d(TAG, "All data cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing data", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * 导出数据
     */
    fun exportData() {
        viewModelScope.launch {
            try {
                // TODO: 实现数据导出功能
                Log.d(TAG, "Export data requested")
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting data", e)
            }
        }
    }

    /**
     * 导入数据
     */
    fun importData() {
        viewModelScope.launch {
            try {
                // TODO: 实现数据导入功能
                Log.d(TAG, "Import data requested")
            } catch (e: Exception) {
                Log.e(TAG, "Error importing data", e)
            }
        }
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
