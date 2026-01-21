package com.example.smartledger.presentation.ui.accounts

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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

/**
 * 账户管理页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountUiModel?>(null) }
    var showBalanceDialog by remember { mutableStateOf<AccountUiModel?>(null) }

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
            // 顶部栏
            item {
                IOSTopBar(
                    title = "💳 账户管理",
                    onBack = onNavigateBack
                )
            }

            // 总资产卡片
            item {
                TotalAssetsCard(
                    totalAssets = uiState.totalAssets,
                    totalCredit = uiState.totalCredit,
                    totalInvestments = uiState.totalInvestments,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 空状态
            if (uiState.assetAccounts.isEmpty() && uiState.creditAccounts.isEmpty() && uiState.investmentAccounts.isEmpty()) {
                item {
                    EmptyAccountsCard(
                        onAddClick = { showAddDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                // 资产账户
                if (uiState.assetAccounts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = "💰",
                            title = "资产账户",
                            total = uiState.totalAssets,
                            color = iOSGreen,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    items(uiState.assetAccounts) { account ->
                        AccountItem(
                            account = account,
                            onEdit = { editingAccount = account },
                            onDelete = { viewModel.deleteAccount(account.id) },
                            onUpdateBalance = { showBalanceDialog = account },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 信贷账户
                if (uiState.creditAccounts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            icon = "💳",
                            title = "信贷账户",
                            total = uiState.totalCredit,
                            color = iOSRed,
                            isDebt = true,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    items(uiState.creditAccounts) { account ->
                        AccountItem(
                            account = account,
                            onEdit = { editingAccount = account },
                            onDelete = { viewModel.deleteAccount(account.id) },
                            onUpdateBalance = { showBalanceDialog = account },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 投资账户
                if (uiState.investmentAccounts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            icon = "📈",
                            title = "投资账户",
                            total = uiState.totalInvestments,
                            color = iOSPurple,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    items(uiState.investmentAccounts) { account ->
                        AccountItem(
                            account = account,
                            onEdit = { editingAccount = account },
                            onDelete = { viewModel.deleteAccount(account.id) },
                            onUpdateBalance = { showBalanceDialog = account },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            // 添加账户按钮
            item {
                AddAccountButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // 添加账户对话框
    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, icon, color, balance, note, bankType, cardNumber, creditLimit ->
                viewModel.addAccount(name, type, icon, color, balance, note, bankType, cardNumber, creditLimit)
                showAddDialog = false
            }
        )
    }

    // 编辑账户对话框
    editingAccount?.let { account ->
        EditAccountDialog(
            account = account,
            onDismiss = { editingAccount = null },
            onConfirm = { name, icon, color, note ->
                viewModel.updateAccount(account.id, name, icon, color, note)
                editingAccount = null
            }
        )
    }

    // 更新余额对话框
    showBalanceDialog?.let { account ->
        UpdateBalanceDialog(
            accountName = account.name,
            currentBalance = account.balance,
            onDismiss = { showBalanceDialog = null },
            onConfirm = { newBalance ->
                viewModel.updateBalance(account.id, newBalance)
                showBalanceDialog = null
            }
        )
    }
}

/**
 * iOS风格顶部栏
 */
@Composable
private fun IOSTopBar(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E5EA))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 18.sp,
                    color = Color(0xFF8E8E93)
                )
            }

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

/**
 * 总资产卡片
 */
@Composable
private fun TotalAssetsCard(
    totalAssets: Double,
    totalCredit: Double,
    totalInvestments: Double,
    modifier: Modifier = Modifier
) {
    val netAssets = totalAssets + totalInvestments + totalCredit

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
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💎", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "净资产",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "¥ ${String.format("%,.2f", netAssets)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssetSummaryItem(
                    icon = "💰",
                    label = "资产",
                    amount = totalAssets,
                    color = iOSGreen
                )
                AssetSummaryItem(
                    icon = "💳",
                    label = "负债",
                    amount = if (totalCredit < 0) -totalCredit else 0.0,
                    color = iOSRed
                )
                AssetSummaryItem(
                    icon = "📈",
                    label = "投资",
                    amount = totalInvestments,
                    color = iOSPurple
                )
            }
        }
    }
}

/**
 * 资产汇总项
 */
@Composable
private fun AssetSummaryItem(
    icon: String,
    label: String,
    amount: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "¥${String.format("%.0f", amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * 分类标题
 */
@Composable
private fun SectionHeader(
    icon: String,
    title: String,
    total: Double,
    color: Color,
    isDebt: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSCardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
            }
            Text(
                text = if (isDebt && total < 0) "¥${String.format("%,.2f", -total)}" else "¥${String.format("%,.2f", total)}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * 账户项
 */
@Composable
private fun AccountItem(
    account: AccountUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(account.color))
    } catch (e: Exception) {
        iOSAccent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSCardBackground)
            .clickable(onClick = onUpdateBalance)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accountColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = account.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = account.typeName,
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "¥${String.format("%,.2f", account.balance)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (account.balance >= 0) Color(0xFF1C1C1E) else iOSRed
                )
                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF2F2F7))
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "✏️", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(iOSRed.copy(alpha = 0.1f))
                            .clickable(onClick = onDelete)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🗑️", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * 添加账户按钮
 */
@Composable
private fun AddAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSAccent)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "➕", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加账户",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * 空账户提示卡片
 */
@Composable
private fun EmptyAccountsCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💳", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无账户",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = "添加银行卡、支付宝、微信等账户",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(iOSAccent)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "➕", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "添加账户",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
