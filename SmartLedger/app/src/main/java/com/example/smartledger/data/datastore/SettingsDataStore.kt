package com.example.smartledger.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val DEFAULT_ACCOUNT_ID = longPreferencesKey("default_account_id")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
        // AI API 配置
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_API_BASE_URL = stringPreferencesKey("ai_api_base_url")
        val AI_MODEL_NAME = stringPreferencesKey("ai_model_name")
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
            defaultAccountId = preferences[PreferencesKeys.DEFAULT_ACCOUNT_ID] ?: 0L,
            isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true,
            lastBackupTime = preferences[PreferencesKeys.LAST_BACKUP_TIME],
            // AI 配置
            aiProvider = preferences[PreferencesKeys.AI_PROVIDER] ?: AiProvider.FREE.name,
            aiApiKey = preferences[PreferencesKeys.AI_API_KEY] ?: "",
            aiApiBaseUrl = preferences[PreferencesKeys.AI_API_BASE_URL] ?: "",
            aiModelName = preferences[PreferencesKeys.AI_MODEL_NAME] ?: ""
        )
    }

    /**
     * 获取AI配置
     */
    val aiConfigFlow: Flow<AiConfig> = context.dataStore.data.map { preferences ->
        AiConfig(
            provider = try {
                AiProvider.valueOf(preferences[PreferencesKeys.AI_PROVIDER] ?: AiProvider.FREE.name)
            } catch (e: Exception) {
                AiProvider.FREE
            },
            apiKey = preferences[PreferencesKeys.AI_API_KEY] ?: "",
            baseUrl = preferences[PreferencesKeys.AI_API_BASE_URL] ?: "",
            modelName = preferences[PreferencesKeys.AI_MODEL_NAME] ?: ""
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
    suspend fun setDefaultAccountId(accountId: Long) {
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

    /**
     * 设置AI提供商
     */
    suspend fun setAiProvider(provider: AiProvider) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_PROVIDER] = provider.name
        }
    }

    /**
     * 设置AI API配置
     */
    suspend fun setAiConfig(config: AiConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_PROVIDER] = config.provider.name
            preferences[PreferencesKeys.AI_API_KEY] = config.apiKey
            preferences[PreferencesKeys.AI_API_BASE_URL] = config.baseUrl
            preferences[PreferencesKeys.AI_MODEL_NAME] = config.modelName
        }
    }

    /**
     * 清除AI API配置
     */
    suspend fun clearAiConfig() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_PROVIDER] = AiProvider.FREE.name
            preferences[PreferencesKeys.AI_API_KEY] = ""
            preferences[PreferencesKeys.AI_API_BASE_URL] = ""
            preferences[PreferencesKeys.AI_MODEL_NAME] = ""
        }
    }
}

/**
 * AI提供商枚举
 */
enum class AiProvider(val displayName: String, val description: String) {
    FREE("免费AI", "使用内置免费AI服务，功能有限"),
    OPENAI("OpenAI", "使用OpenAI API (GPT系列模型)"),
    AZURE_OPENAI("Azure OpenAI", "使用Azure OpenAI服务"),
    ANTHROPIC("Anthropic", "使用Anthropic API (Claude系列模型)"),
    CUSTOM("自定义API", "使用兼容OpenAI格式的自定义API")
}

/**
 * AI配置数据类
 */
data class AiConfig(
    val provider: AiProvider = AiProvider.FREE,
    val apiKey: String = "",
    val baseUrl: String = "",
    val modelName: String = ""
) {
    val isConfigured: Boolean
        get() = when (provider) {
            AiProvider.FREE -> true
            AiProvider.OPENAI -> apiKey.isNotBlank()
            AiProvider.AZURE_OPENAI -> apiKey.isNotBlank() && baseUrl.isNotBlank()
            AiProvider.ANTHROPIC -> apiKey.isNotBlank()
            AiProvider.CUSTOM -> apiKey.isNotBlank() && baseUrl.isNotBlank()
        }

    val defaultModel: String
        get() = when (provider) {
            AiProvider.FREE -> "free-model"
            AiProvider.OPENAI -> "gpt-3.5-turbo"
            AiProvider.AZURE_OPENAI -> "gpt-35-turbo"
            AiProvider.ANTHROPIC -> "claude-3-haiku-20240307"
            AiProvider.CUSTOM -> modelName.ifBlank { "default" }
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
    val defaultAccountId: Long = 0L,
    val isFirstLaunch: Boolean = true,
    val lastBackupTime: String? = null,
    // AI 配置
    val aiProvider: String = AiProvider.FREE.name,
    val aiApiKey: String = "",
    val aiApiBaseUrl: String = "",
    val aiModelName: String = ""
)
