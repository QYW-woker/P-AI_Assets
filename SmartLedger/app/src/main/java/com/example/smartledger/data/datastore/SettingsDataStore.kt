package com.example.smartledger.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 应用设置数据存储
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val CURRENCY = stringPreferencesKey("currency")
        val MONTH_START_DAY = intPreferencesKey("month_start_day")
        val WEEK_START_DAY = stringPreferencesKey("week_start_day")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_DAILY_REMINDER_ENABLED = booleanPreferencesKey("is_daily_reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val IS_BUDGET_ALERT_ENABLED = booleanPreferencesKey("is_budget_alert_enabled")
        val DEFAULT_ACCOUNT_ID = intPreferencesKey("default_account_id")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
    }

    /**
     * 获取所有设置
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            currency = preferences[PreferencesKeys.CURRENCY] ?: "CNY ¥",
            monthStartDay = preferences[PreferencesKeys.MONTH_START_DAY] ?: 1,
            weekStartDay = preferences[PreferencesKeys.WEEK_START_DAY] ?: "周一",
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
            isDailyReminderEnabled = preferences[PreferencesKeys.IS_DAILY_REMINDER_ENABLED] ?: false,
            reminderTime = preferences[PreferencesKeys.REMINDER_TIME] ?: "21:00",
            isBudgetAlertEnabled = preferences[PreferencesKeys.IS_BUDGET_ALERT_ENABLED] ?: true,
            defaultAccountId = preferences[PreferencesKeys.DEFAULT_ACCOUNT_ID] ?: 0,
            isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true,
            lastBackupTime = preferences[PreferencesKeys.LAST_BACKUP_TIME]
        )
    }

    /**
     * 设置货币
     */
    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY] = currency
        }
    }

    /**
     * 设置月份开始日
     */
    suspend fun setMonthStartDay(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MONTH_START_DAY] = day
        }
    }

    /**
     * 设置周开始日
     */
    suspend fun setWeekStartDay(day: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEK_START_DAY] = day
        }
    }

    /**
     * 设置深色模式
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }

    /**
     * 设置每日提醒
     */
    suspend fun setDailyReminder(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DAILY_REMINDER_ENABLED] = enabled
        }
    }

    /**
     * 设置提醒时间
     */
    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] = time
        }
    }

    /**
     * 设置预算提醒
     */
    suspend fun setBudgetAlert(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_BUDGET_ALERT_ENABLED] = enabled
        }
    }

    /**
     * 设置默认账户
     */
    suspend fun setDefaultAccountId(accountId: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_ACCOUNT_ID] = accountId
        }
    }

    /**
     * 设置首次启动标志
     */
    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = isFirst
        }
    }

    /**
     * 设置最后备份时间
     */
    suspend fun setLastBackupTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIME] = time
        }
    }

    /**
     * 清除所有设置
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

/**
 * 应用设置数据类
 */
data class AppSettings(
    val currency: String = "CNY ¥",
    val monthStartDay: Int = 1,
    val weekStartDay: String = "周一",
    val isDarkMode: Boolean = false,
    val isDailyReminderEnabled: Boolean = false,
    val reminderTime: String = "21:00",
    val isBudgetAlertEnabled: Boolean = true,
    val defaultAccountId: Int = 0,
    val isFirstLaunch: Boolean = true,
    val lastBackupTime: String? = null
)
