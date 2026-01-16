package com.example.smartledger.presentation.ui.accounts

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 账户管理页面
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
        topBar = {
            AppTopBarWithBack(
                title = "账户管理",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AppColors.Accent,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加账户"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.assetAccounts.isEmpty() && uiState.investmentAccounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💳",
                        style = AppTypography.NumberLarge
                    )
                    Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                    Text(
                        text = "暂无账户",
                        style = AppTypography.TitleSmall,
                        color = AppColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                    Text(
                        text = "点击右下角按钮添加账户",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(AppDimens.PaddingL)
            ) {
                // 资产账户汇总
                if (uiState.assetAccounts.isNotEmpty()) {
                    item {
                        SummaryCard(
                            title = "资产账户",
                            total = uiState.totalAssets,
                            icon = "💰"
                        )
                    }

                    items(uiState.assetAccounts) { account ->
                        AccountItem(
                            account = account,
                            onEdit = { editingAccount = account },
                            onDelete = { viewModel.deleteAccount(account.id) },
                            onUpdateBalance = { showBalanceDialog = account }
                        )
                    }
                }

                // 投资账户汇总
                if (uiState.investmentAccounts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                        SummaryCard(
                            title = "投资账户",
                            total = uiState.totalInvestments,
                            icon = "📈"
                        )
                    }

                    items(uiState.investmentAccounts) { account ->
                        AccountItem(
                            account = account,
                            onEdit = { editingAccount = account },
                            onDelete = { viewModel.deleteAccount(account.id) },
                            onUpdateBalance = { showBalanceDialog = account }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // 添加账户对话框
    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, icon, color, balance, note ->
                viewModel.addAccount(name, type, icon, color, balance, note)
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

@Composable
private fun SummaryCard(
    title: String,
    total: Double,
    icon: String
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = icon,
                    style = AppTypography.TitleMedium
                )
                Spacer(modifier = Modifier.width(AppDimens.SpacingM))
                Text(
                    text = title,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )
            }
            Text(
                text = "¥${String.format("%.2f", total)}",
                style = AppTypography.NumberMedium,
                color = AppColors.Success
            )
        }
    }
}

@Composable
private fun AccountItem(
    account: AccountUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateBalance: () -> Unit
) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(account.color))
    } catch (e: Exception) {
        AppColors.Primary
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onUpdateBalance
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accountColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.icon,
                    style = AppTypography.TitleSmall
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = account.typeName,
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }

            Text(
                text = "¥${String.format("%.2f", account.balance)}",
                style = AppTypography.NumberSmall,
                color = if (account.balance >= 0) AppColors.TextPrimary else AppColors.Accent
            )

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "编辑",
                    tint = AppColors.TextMuted
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = AppColors.Accent
                )
            }
        }
    }
}
