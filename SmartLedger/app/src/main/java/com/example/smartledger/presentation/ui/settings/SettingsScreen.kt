package com.example.smartledger.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "设置",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
        ) {
            // 基本设置
            item {
                SettingsSection(title = "基本设置") {
                    SettingsItem(
                        icon = Icons.Filled.Language,
                        title = "货币单位",
                        value = uiState.currency,
                        onClick = { /* TODO: 选择货币 */ }
                    )

                    Divider(
                        modifier = Modifier.padding(start = 40.dp),
                        color = AppColors.Divider
                    )

                    SettingsItem(
                        icon = Icons.Filled.Schedule,
                        title = "每月起始日",
                        value = "每月${uiState.monthStartDay}日",
                        onClick = { /* TODO: 选择日期 */ }
                    )

                    Divider(
                        modifier = Modifier.padding(start = 40.dp),
                        color = AppColors.Divider
                    )

                    SettingsItem(
                        icon = Icons.Filled.Schedule,
                        title = "每周起始日",
                        value = uiState.weekStartDay,
                        onClick = { /* TODO: 选择星期 */ }
                    )
                }
            }

            // 外观设置
            item {
                SettingsSection(title = "外观") {
                    SettingsSwitchItem(
                        icon = Icons.Filled.DarkMode,
                        title = "深色模式",
                        isChecked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
            }

            // 提醒设置
            item {
                SettingsSection(title = "提醒") {
                    SettingsSwitchItem(
                        icon = Icons.Filled.Notifications,
                        title = "每日记账提醒",
                        subtitle = if (uiState.isDailyReminderEnabled) "每天 ${uiState.reminderTime}" else null,
                        isChecked = uiState.isDailyReminderEnabled,
                        onCheckedChange = { viewModel.setDailyReminder(it) }
                    )

                    Divider(
                        modifier = Modifier.padding(start = 40.dp),
                        color = AppColors.Divider
                    )

                    SettingsSwitchItem(
                        icon = Icons.Filled.Notifications,
                        title = "预算超支提醒",
                        subtitle = "当预算使用超过80%时提醒",
                        isChecked = uiState.isBudgetAlertEnabled,
                        onCheckedChange = { viewModel.setBudgetAlert(it) }
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        title = "版本",
                        value = "1.0.0",
                        onClick = { }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
            }
        }
    }
}

/**
 * 设置分组
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
    ) {
        Text(
            text = title,
            style = AppTypography.LabelMedium,
            color = AppColors.TextMuted,
            modifier = Modifier.padding(
                start = AppDimens.PaddingS,
                bottom = AppDimens.SpacingS
            )
        )
        AppCard(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * 设置项
 */
@Composable
private fun SettingsItem(
    icon: ImageVector? = null,
    title: String,
    value: String? = null,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppDimens.PaddingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(AppDimens.SpacingL))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.BodyMedium,
                color = AppColors.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }
        }

        if (value != null) {
            Text(
                text = value,
                style = AppTypography.BodyMedium,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(AppDimens.SpacingS))
        }

        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = AppColors.TextMuted
        )
    }
}

/**
 * 带开关的设置项
 */
@Composable
private fun SettingsSwitchItem(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.PaddingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(AppDimens.SpacingL))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.BodyMedium,
                color = AppColors.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.Card,
                checkedTrackColor = AppColors.Accent,
                uncheckedThumbColor = AppColors.Card,
                uncheckedTrackColor = AppColors.Border
            )
        )
    }
}
