package com.example.smartledger.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.datastore.AiConfig
import com.example.smartledger.data.datastore.AiProvider

// iOS风格颜色
private val iOSBackground = Color(0xFFF2F2F7)
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSRed = Color(0xFFFF3B30)
private val iOSPurple = Color(0xFFAF52DE)
private val iOSPink = Color(0xFFFF2D55)

/**
 * 设置页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Dialog states
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showMonthStartDayDialog by remember { mutableStateOf(false) }
    var showWeekStartDayDialog by remember { mutableStateOf(false) }
    var showReminderTimeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAiConfigDialog by remember { mutableStateOf(false) }

    // Currency selection dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = uiState.currency,
            onDismiss = { showCurrencyDialog = false },
            onConfirm = { currency ->
                viewModel.setCurrency(currency)
                showCurrencyDialog = false
            }
        )
    }

    // Month start day dialog
    if (showMonthStartDayDialog) {
        MonthStartDayDialog(
            currentDay = uiState.monthStartDay,
            onDismiss = { showMonthStartDayDialog = false },
            onConfirm = { day ->
                viewModel.setMonthStartDay(day)
                showMonthStartDayDialog = false
            }
        )
    }

    // Week start day dialog
    if (showWeekStartDayDialog) {
        WeekStartDayDialog(
            currentDay = uiState.weekStartDay,
            onDismiss = { showWeekStartDayDialog = false },
            onConfirm = { day ->
                viewModel.setWeekStartDay(day)
                showWeekStartDayDialog = false
            }
        )
    }

    // Reminder time dialog
    if (showReminderTimeDialog) {
        ReminderTimeDialog(
            currentTime = uiState.reminderTime,
            onDismiss = { showReminderTimeDialog = false },
            onConfirm = { time ->
                viewModel.setReminderTime(time)
                showReminderTimeDialog = false
            }
        )
    }

    // Clear data confirmation dialog
    if (showClearDataDialog) {
        ConfirmClearDataDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            }
        )
    }

    // AI configuration dialog
    if (showAiConfigDialog) {
        AiConfigDialog(
            currentConfig = uiState.aiConfig,
            onDismiss = { showAiConfigDialog = false },
            onConfirm = { config ->
                viewModel.setAiConfig(config)
                showAiConfigDialog = false
            }
        )
    }

    Scaffold(
        containerColor = iOSBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(iOSBackground)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部导航栏
            item {
                IOSTopBar(
                    title = "⚙️ 设置",
                    onBackClick = onNavigateBack
                )
            }

            // 用户设置卡片
            item {
                UserSettingsCard(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 基本设置
            item {
                Text(
                    text = "🔧 基本设置",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                SettingsSection(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    SettingsItem(
                        icon = "💱",
                        iconColor = iOSGreen,
                        title = "货币单位",
                        value = uiState.currency,
                        onClick = { showCurrencyDialog = true }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = "📅",
                        iconColor = iOSAccent,
                        title = "每月起始日",
                        value = "每月${uiState.monthStartDay}日",
                        onClick = { showMonthStartDayDialog = true }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = "📆",
                        iconColor = iOSOrange,
                        title = "每周起始日",
                        value = uiState.weekStartDay,
                        onClick = { showWeekStartDayDialog = true }
                    )
                }
            }

            // 外观设置
            item {
                Text(
                    text = "🎨 外观",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                SettingsSection(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    SettingsSwitchItem(
                        icon = "🌙",
                        iconColor = iOSPurple,
                        title = "深色模式",
                        subtitle = "跟随系统或手动切换",
                        isChecked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
            }

            // 提醒设置
            item {
                Text(
                    text = "🔔 提醒",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                SettingsSection(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    SettingsSwitchItem(
                        icon = "⏰",
                        iconColor = iOSRed,
                        title = "每日记账提醒",
                        subtitle = if (uiState.isDailyReminderEnabled) "每天 ${uiState.reminderTime}" else "关闭",
                        isChecked = uiState.isDailyReminderEnabled,
                        onCheckedChange = { viewModel.setDailyReminder(it) },
                        onSubtitleClick = if (uiState.isDailyReminderEnabled) {
                            { showReminderTimeDialog = true }
                        } else null
                    )

                    SettingsDivider()

                    SettingsSwitchItem(
                        icon = "📊",
                        iconColor = iOSOrange,
                        title = "预算超支提醒",
                        subtitle = "当预算使用超过80%时提醒",
                        isChecked = uiState.isBudgetAlertEnabled,
                        onCheckedChange = { viewModel.setBudgetAlert(it) }
                    )
                }
            }

            // AI助手设置
            item {
                Text(
                    text = "🤖 AI助手",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                SettingsSection(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    SettingsItem(
                        icon = "🔌",
                        iconColor = iOSPurple,
                        title = "AI服务配置",
                        subtitle = uiState.aiConfig.provider.displayName,
                        value = if (uiState.aiConfig.isConfigured) "已配置" else "未配置",
                        onClick = { showAiConfigDialog = true }
                    )
                }
            }

            // 数据管理
            item {
                Text(
                    text = "💾 数据管理",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                SettingsSection(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    SettingsItem(
                        icon = "📤",
                        iconColor = iOSAccent,
                        title = "导出数据",
                        subtitle = "导出账单到本地",
                        onClick = { viewModel.exportData() }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = "📥",
                        iconColor = iOSGreen,
                        title = "导入数据",
                        subtitle = "从文件导入账单",
                        onClick = { viewModel.importData() }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = "🗑️",
                        iconColor = iOSRed,
                        title = "清除数据",
                        subtitle = "删除所有记账数据",
                        isDestructive = true,
                        onClick = { showClearDataDialog = true }
                    )
                }
            }

            // 关于
            item {
                Text(
                    text = "ℹ️ 关于",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                SettingsSection(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    SettingsItem(
                        icon = "📱",
                        iconColor = iOSAccent,
                        title = "版本",
                        value = "1.0.0",
                        onClick = { }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = "⭐",
                        iconColor = iOSOrange,
                        title = "给我们评分",
                        subtitle = "喜欢就给个好评吧",
                        onClick = { /* TODO: 跳转应用商店 */ }
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = "💬",
                        iconColor = iOSPink,
                        title = "意见反馈",
                        subtitle = "告诉我们你的想法",
                        onClick = { /* TODO: 反馈 */ }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

/**
 * iOS风格顶部栏
 */
@Composable
private fun IOSTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iOSCardBackground)
                .shadow(2.dp, CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 20.sp,
                color = iOSAccent
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )
    }
}

/**
 * 用户设置卡片
 */
@Composable
private fun UserSettingsCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙️",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "个性化您的体验",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "调整应用设置以符合您的使用习惯",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 设置分组
 */
@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
    ) {
        Column {
            content()
        }
    }
}

/**
 * 设置项分割线
 */
@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp)
            .height(1.dp)
            .background(Color(0xFFE5E5EA))
    )
}

/**
 * 设置项
 */
@Composable
private fun SettingsItem(
    icon: String,
    iconColor: Color,
    title: String,
    value: String? = null,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) iOSRed else Color(0xFF1C1C1E)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        }

        if (value != null) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = Color(0xFF8E8E93)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = "→",
            fontSize = 18.sp,
            color = Color(0xFFC7C7CC)
        )
    }
}

/**
 * 带开关的设置项
 */
@Composable
private fun SettingsSwitchItem(
    icon: String,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onSubtitleClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (onSubtitleClick != null) iOSAccent else Color(0xFF8E8E93),
                    modifier = if (onSubtitleClick != null) {
                        Modifier.clickable(onClick = onSubtitleClick)
                    } else Modifier
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iOSGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5EA)
            )
        )
    }
}
