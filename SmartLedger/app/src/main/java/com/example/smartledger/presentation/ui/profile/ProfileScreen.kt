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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

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
 * 我的页面 - iOS卡通风格
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
    onNavigateToFinancialHealth: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToRecurring: () -> Unit = {},
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
        containerColor = iOSBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(iOSBackground)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部标题
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "👤 我的",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            // 用户信息卡片
            item {
                UserInfoCard(
                    username = uiState.username,
                    daysSinceStart = uiState.daysSinceStart,
                    totalTransactions = uiState.totalTransactions,
                    onEditClick = { showEditDialog = true },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 功能入口 - 记账工具
            item {
                Text(
                    text = "📱 记账工具",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                MenuSection(
                    items = listOf(
                        MenuItemData("💰", "预算管理", "设置和跟踪您的预算", iOSGreen, onNavigateToBudget),
                        MenuItemData("🎯", "储蓄目标", "创建和追踪储蓄目标", iOSOrange, onNavigateToGoals),
                        MenuItemData("🔄", "固定收支", "管理定期自动记账", iOSAccent, onNavigateToRecurring),
                        MenuItemData("🤖", "AI助手", "智能记账，轻松管理财务", iOSPurple, onNavigateToAiChat),
                        MenuItemData("🏷️", "分类管理", "自定义收支分类", iOSPink, onNavigateToCategoryManage)
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 分析与报告
            item {
                Text(
                    text = "📊 分析与报告",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                MenuSection(
                    items = listOf(
                        MenuItemData("❤️", "财务健康诊断", "全面分析您的财务状况", iOSRed, onNavigateToFinancialHealth),
                        MenuItemData("📈", "财务报告", "周报、月报、年报", iOSGreen, onNavigateToReport)
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 其他功能
            item {
                Text(
                    text = "⚙️ 设置",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                MenuSection(
                    items = listOf(
                        MenuItemData("☁️", "备份与恢复", "保护您的数据安全", iOSAccent, onNavigateToBackup),
                        MenuItemData("⚙️", "设置", "货币、提醒、主题等", Color(0xFF8E8E93), onNavigateToSettings)
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 版本信息
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱 智能记账",
                        fontSize = 14.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "v1.0.0",
                        fontSize = 12.sp,
                        color = Color(0xFFC7C7CC)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
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
            // 头像
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.firstOrNull()?.uppercase() ?: "😊",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = username,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable(onClick = onEditClick)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✏️",
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (daysSinceStart > 0)
                        "📅 记账 $daysSinceStart 天 · 📝 共 $totalTransactions 笔"
                    else
                        "✨ 开始记账吧",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 菜单项数据
 */
private data class MenuItemData(
    val icon: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val onClick: () -> Unit
)

/**
 * 菜单组
 */
@Composable
private fun MenuSection(
    items: List<MenuItemData>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                MenuItem(
                    icon = item.icon,
                    title = item.title,
                    subtitle = item.subtitle,
                    iconColor = item.color,
                    onClick = item.onClick
                )
                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 68.dp)
                            .height(1.dp)
                            .background(Color(0xFFE5E5EA))
                    )
                }
            }
        }
    }
}

/**
 * 菜单项
 */
@Composable
private fun MenuItem(
    icon: String,
    title: String,
    subtitle: String,
    iconColor: Color,
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
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF8E8E93)
            )
        }

        Text(
            text = "→",
            fontSize = 18.sp,
            color = Color(0xFFC7C7CC)
        )
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
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "✏️ 编辑用户名",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("输入用户名", color = Color(0xFF8E8E93))
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iOSAccent,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(username) },
                enabled = username.isNotBlank()
            ) {
                Text(
                    text = "保存",
                    color = if (username.isNotBlank()) iOSAccent else Color(0xFFC7C7CC),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFF8E8E93))
            }
        }
    )
}
