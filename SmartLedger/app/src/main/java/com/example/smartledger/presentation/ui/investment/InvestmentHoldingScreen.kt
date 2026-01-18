package com.example.smartledger.presentation.ui.investment

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.local.entity.HoldingType
import com.example.smartledger.data.local.entity.InvestmentHoldingEntity
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentHoldingScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvestmentHoldingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    // 编辑/添加对话框
    if (uiState.showDialog) {
        HoldingFormDialog(
            formState = formState,
            accounts = uiState.investmentAccounts,
            isEditing = uiState.editingHolding != null,
            onFormStateChange = { viewModel.updateFormState(it) },
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { viewModel.saveHolding() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("投资明细") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.investmentAccounts.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.showAddDialog() },
                    containerColor = AppColors.Accent
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加持仓", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                // 汇总卡片
                item {
                    SummaryCard(
                        summary = uiState.summary,
                        currencyFormat = currencyFormat,
                        modifier = Modifier.padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.PaddingS)
                    )
                }

                // 类型筛选
                item {
                    TypeFilter(
                        selectedType = uiState.selectedType,
                        onTypeSelected = { viewModel.filterByType(it) }
                    )
                }

                if (uiState.holdings.isEmpty()) {
                    item {
                        EmptyState(
                            hasAccounts = uiState.investmentAccounts.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppDimens.PaddingXXL)
                        )
                    }
                } else {
                    // 持仓列表
                    items(uiState.filteredHoldings) { holding ->
                        HoldingCard(
                            holding = holding,
                            currencyFormat = currencyFormat,
                            onEdit = { viewModel.showEditDialog(holding) },
                            onDelete = { viewModel.deleteHolding(holding) },
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    summary: com.example.smartledger.domain.repository.InvestmentSummary,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.PaddingL)
        ) {
            Text(
                text = "投资组合",
                style = AppTypography.TitleSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "总市值",
                        style = AppTypography.Caption,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currencyFormat.format(summary.totalMarketValue),
                        style = AppTypography.TitleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "总收益",
                        style = AppTypography.Caption,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (summary.totalProfitLoss >= 0)
                                Icons.AutoMirrored.Filled.TrendingUp
                            else
                                Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currencyFormat.format(summary.totalProfitLoss)} (${String.format("%+.2f%%", summary.returnRate)})",
                            style = AppTypography.BodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill(label = "本金", value = currencyFormat.format(summary.totalPrincipal))
                StatPill(label = "持仓", value = "${summary.holdingCount}只")
                StatPill(
                    label = "盈/亏",
                    value = "${summary.profitableCount}/${summary.lossCount}"
                )
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.Caption,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = AppTypography.BodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun TypeFilter(
    selectedType: HoldingType?,
    onTypeSelected: (HoldingType?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text("全部") }
            )
        }
        items(HoldingType.entries.toList()) { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.label) }
            )
        }
    }
}

@Composable
private fun HoldingCard(
    holding: InvestmentHoldingEntity,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除 ${holding.name} 吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("删除", color = AppColors.Expense)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.PaddingM)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppColors.AccentLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = holding.holdingType.label,
                            style = AppTypography.Caption,
                            color = AppColors.Accent
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = holding.name,
                            style = AppTypography.BodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (holding.code.isNotEmpty()) {
                            Text(
                                text = holding.code,
                                style = AppTypography.Caption,
                                color = AppColors.TextMuted
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // 数据行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("市值", style = AppTypography.Caption, color = AppColors.TextMuted)
                    Text(
                        text = currencyFormat.format(holding.marketValue),
                        style = AppTypography.BodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("持仓", style = AppTypography.Caption, color = AppColors.TextMuted)
                    Text(
                        text = String.format("%.2f", holding.quantity),
                        style = AppTypography.BodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("成本", style = AppTypography.Caption, color = AppColors.TextMuted)
                    Text(
                        text = String.format("%.2f", holding.costPrice),
                        style = AppTypography.BodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("现价", style = AppTypography.Caption, color = AppColors.TextMuted)
                    Text(
                        text = String.format("%.2f", holding.currentPrice),
                        style = AppTypography.BodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("盈亏", style = AppTypography.Caption, color = AppColors.TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${if (holding.profitLoss >= 0) "+" else ""}${currencyFormat.format(holding.profitLoss)}",
                            style = AppTypography.BodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (holding.profitLoss >= 0) AppColors.Income else AppColors.Expense
                        )
                    }
                    Text(
                        text = String.format("%+.2f%%", holding.returnRate),
                        style = AppTypography.Caption,
                        color = if (holding.returnRate >= 0) AppColors.Income else AppColors.Expense
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingFormDialog(
    formState: HoldingFormState,
    accounts: List<com.example.smartledger.data.local.entity.AccountEntity>,
    isEditing: Boolean,
    onFormStateChange: (HoldingFormState) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var accountExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "编辑持仓" else "添加持仓") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 账户选择
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == formState.accountId }?.name ?: "选择账户",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("投资账户") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    onFormStateChange(formState.copy(accountId = account.id))
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                // 类型选择
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.holdingType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("持仓类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        HoldingType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    onFormStateChange(formState.copy(holdingType = type))
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // 名称
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { onFormStateChange(formState.copy(name = it)) },
                    label = { Text("名称 *") },
                    placeholder = { Text("如：贵州茅台") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 代码
                OutlinedTextField(
                    value = formState.code,
                    onValueChange = { onFormStateChange(formState.copy(code = it)) },
                    label = { Text("代码") },
                    placeholder = { Text("如：600519") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 数量和成本价
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formState.quantity,
                        onValueChange = { onFormStateChange(formState.copy(quantity = it)) },
                        label = { Text("数量 *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formState.costPrice,
                        onValueChange = { onFormStateChange(formState.copy(costPrice = it)) },
                        label = { Text("成本价 *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // 现价
                OutlinedTextField(
                    value = formState.currentPrice,
                    onValueChange = { onFormStateChange(formState.copy(currentPrice = it)) },
                    label = { Text("当前价格") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 备注
                OutlinedTextField(
                    value = formState.note,
                    onValueChange = { onFormStateChange(formState.copy(note = it)) },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = formState.isValid
            ) {
                Text("保存", color = if (formState.isValid) AppColors.Accent else AppColors.TextMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EmptyState(
    hasAccounts: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasAccounts) "暂无持仓记录" else "请先创建投资账户",
                style = AppTypography.BodyLarge,
                color = AppColors.TextMuted
            )
            if (hasAccounts) {
                Text(
                    text = "点击右下角按钮添加持仓",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }
        }
    }
}
