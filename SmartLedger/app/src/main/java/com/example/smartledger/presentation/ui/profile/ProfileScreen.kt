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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    onNavigateToCategoryManage: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    // 编辑用户名对话框
    if (showEditDialog) {
        EditUsernameDialog(
            currentUsername = uiState.username,
            onDismiss = { showEditDialog = false },
            onConfirm = { newUsername ->
                viewModel.updateUsername(newUsername)
                showEditDialog = false
            }
        )
    }

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
                    username = uiState.username,
                    daysSinceStart = uiState.daysSinceStart,
                    totalTransactions = uiState.totalTransactions,
                    onEditClick = { showEditDialog = true },
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
    username: String,
    daysSinceStart: Int,
    totalTransactions: Int,
    onEditClick: () -> Unit,
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
                    text = username.firstOrNull()?.uppercase() ?: "U",
                    style = AppTypography.TitleLarge,
                    color = AppColors.Accent
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingL))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = AppTypography.TitleMedium,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (daysSinceStart > 0) "记账 $daysSinceStart 天 | 共 $totalTransactions 笔" else "开始记账吧",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "编辑",
                    tint = AppColors.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 编辑用户名对话框
 */
@Composable
private fun EditUsernameDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑用户名",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("输入用户名", color = AppColors.TextMuted)
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(username) },
                enabled = username.isNotBlank()
            ) {
                Text("保存", color = AppColors.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextMuted)
            }
        }
    )
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
