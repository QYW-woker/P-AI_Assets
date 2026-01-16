package com.example.smartledger.presentation.ui.profile

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBar
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 我的页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAiChat: () -> Unit,
    onNavigateToCategoryManage: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(title = "我的")
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
        ) {
            // 用户信息卡片
            item {
                UserInfoCard(
                    modifier = Modifier.padding(
                        start = AppDimens.PaddingL,
                        end = AppDimens.PaddingL,
                        top = AppDimens.PaddingL
                    )
                )
            }

            // 功能入口
            item {
                AppCard(
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "预算管理",
                        subtitle = "设置和跟踪您的预算",
                        onClick = onNavigateToBudget
                    )

                    Divider(
                        modifier = Modifier.padding(start = 40.dp),
                        color = AppColors.Divider
                    )

                    ProfileMenuItem(
                        icon = Icons.Filled.Flag,
                        title = "储蓄目标",
                        subtitle = "创建和追踪储蓄目标",
                        onClick = onNavigateToGoals
                    )

                    Divider(
                        modifier = Modifier.padding(start = 40.dp),
                        color = AppColors.Divider
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.SmartToy,
                        title = "AI助手",
                        subtitle = "智能记账，轻松管理财务",
                        onClick = onNavigateToAiChat
                    )

                    Divider(
                        modifier = Modifier.padding(start = 40.dp),
                        color = AppColors.Divider
                    )

                    ProfileMenuItem(
                        icon = Icons.Filled.Category,
                        title = "分类管理",
                        subtitle = "自定义收支分类",
                        onClick = onNavigateToCategoryManage
                    )
                }
            }

            // 其他功能
            item {
                AppCard(
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Filled.Backup,
                        title = "备份与恢复",
                        subtitle = "保护您的数据安全",
                        onClick = onNavigateToBackup
                    )

                    Divider(
                        modifier = Modifier.padding(start = 40.dp),
                        color = AppColors.Divider
                    )

                    ProfileMenuItem(
                        icon = Icons.Filled.Settings,
                        title = "设置",
                        subtitle = "货币、提醒、主题等",
                        onClick = onNavigateToSettings
                    )
                }
            }

            // 版本信息
            item {
                Text(
                    text = "智能记账 v1.0.0",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.PaddingL),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
            }
        }
    }
}

/**
 * 用户信息卡片
 */
@Composable
private fun UserInfoCard(
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppColors.AccentLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = AppTypography.TitleLarge,
                    color = AppColors.Accent
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingL))

            Column {
                Text(
                    text = "用户",
                    style = AppTypography.TitleMedium,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "记账 30 天 | 共 156 笔",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }
        }
    }
}

/**
 * 菜单项
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppDimens.PaddingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppColors.Accent
        )

        Spacer(modifier = Modifier.width(AppDimens.SpacingL))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.BodyMedium,
                color = AppColors.TextPrimary
            )
            Text(
                text = subtitle,
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = AppColors.TextMuted
        )
    }
}
