package com.example.smartledger.presentation.ui.recurring

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.local.entity.RecurringFrequency
import com.example.smartledger.data.local.entity.RecurringTransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 固定收支管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "固定收支",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.processAllDue() }) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "执行到期",
                            tint = AppColors.TextPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = AppColors.Primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 汇总卡片
            SummaryCard(
                totalIncome = uiState.totalFixedIncome,
                totalExpense = uiState.totalFixedExpense
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }

                uiState.recurringTransactions.isEmpty() -> {
                    EmptyState()
                }

                else -> {
                    RecurringList(
                        items = uiState.recurringTransactions,
                        onEdit = { viewModel.showEditDialog(it) },
                        onToggle = { viewModel.toggleActive(it) },
                        onDelete = { viewModel.deleteRecurring(it) }
                    )
                }
            }
        }
    }

    // 添加/编辑对话框
    if (uiState.showDialog) {
        RecurringFormDialog(
            formState = formState,
            onDismiss = { viewModel.dismissDialog() },
            onNameChange = { viewModel.updateFormName(it) },
            onAmountChange = { viewModel.updateFormAmount(it) },
            onTypeChange = { viewModel.updateFormType(it) },
            onCategoryChange = { viewModel.updateFormCategory(it) },
            onAccountChange = { viewModel.updateFormAccount(it) },
            onFrequencyChange = { viewModel.updateFormFrequency(it) },
            onDayChange = { viewModel.updateFormDayOfPeriod(it) },
            onNoteChange = { viewModel.updateFormNote(it) },
            onAutoExecuteChange = { viewModel.updateFormAutoExecute(it) },
            onSave = { viewModel.saveRecurring() }
        )
    }
}

/**
 * 汇总卡片
 */
@Composable
private fun SummaryCard(
    totalIncome: Double,
    totalExpense: Double
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.PaddingL)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "固定收入",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${String.format("%.0f", totalIncome)}/月",
                    style = AppTypography.NumberSmall,
                    color = AppColors.Success
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "固定支出",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${String.format("%.0f", totalExpense)}/月",
                    style = AppTypography.NumberSmall,
                    color = AppColors.Accent
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "每月结余",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                val balance = totalIncome - totalExpense
                Text(
                    text = "¥${String.format("%.0f", balance)}",
                    style = AppTypography.NumberSmall,
                    color = if (balance >= 0) AppColors.Info else AppColors.Accent
                )
            }
        }
    }
}

/**
 * 固定收支列表
 */
@Composable
private fun RecurringList(
    items: List<RecurringTransactionEntity>,
    onEdit: (RecurringTransactionEntity) -> Unit,
    onToggle: (RecurringTransactionEntity) -> Unit,
    onDelete: (RecurringTransactionEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = AppDimens.PaddingL,
            vertical = AppDimens.SpacingS
        )
    ) {
        items(items) { recurring ->
            RecurringItemCard(
                recurring = recurring,
                dateFormat = dateFormat,
                onEdit = { onEdit(recurring) },
                onToggle = { onToggle(recurring) },
                onDelete = { onDelete(recurring) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * 固定收支项卡片
 */
@Composable
private fun RecurringItemCard(
    recurring: RecurringTransactionEntity,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = if (recurring.type == TransactionType.EXPENSE) AppColors.Accent else AppColors.Success

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 类型标识
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (recurring.type == TransactionType.EXPENSE) "支" else "收",
                        style = AppTypography.LabelMedium,
                        color = typeColor
                    )
                }

                Spacer(modifier = Modifier.width(AppDimens.SpacingM))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recurring.name,
                        style = AppTypography.BodyMedium,
                        color = if (recurring.isActive) AppColors.TextPrimary else AppColors.TextMuted
                    )
                    Text(
                        text = "${recurring.frequency.label} · 下次: ${dateFormat.format(Date(recurring.nextExecutionDate))}",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }

                Text(
                    text = "${if (recurring.type == TransactionType.EXPENSE) "-" else "+"}¥${String.format("%.0f", recurring.amount)}",
                    style = AppTypography.NumberSmall,
                    color = if (recurring.isActive) typeColor else AppColors.TextMuted
                )

                Spacer(modifier = Modifier.width(AppDimens.SpacingS))

                Switch(
                    checked = recurring.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.Primary,
                        checkedTrackColor = AppColors.Primary.copy(alpha = 0.3f)
                    )
                )
            }

            // 操作按钮
            if (recurring.isActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppDimens.SpacingS),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Info)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑", style = AppTypography.Caption)
                    }

                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Accent)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除", style = AppTypography.Caption)
                    }
                }
            }
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🔄",
                style = AppTypography.NumberLarge
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            Text(
                text = "暂无固定收支",
                style = AppTypography.TitleSmall,
                color = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = "添加固定收支，自动记录定期交易",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }
    }
}

/**
 * 表单对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringFormDialog(
    formState: RecurringFormState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (Long) -> Unit,
    onAccountChange: (Long) -> Unit,
    onFrequencyChange: (RecurringFrequency) -> Unit,
    onDayChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onAutoExecuteChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAccountDropdown by remember { mutableStateOf(false) }

    val filteredCategories = formState.availableCategories.filter { it.type == formState.type }
    val selectedCategory = formState.availableCategories.find { it.id == formState.categoryId }
    val selectedAccount = formState.availableAccounts.find { it.id == formState.accountId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (formState.isEditing) "编辑固定收支" else "添加固定收支",
                style = AppTypography.TitleMedium
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                // 类型选择
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)) {
                        FilterChip(
                            selected = formState.type == TransactionType.EXPENSE,
                            onClick = { onTypeChange(TransactionType.EXPENSE) },
                            label = { Text("支出") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.Accent,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = formState.type == TransactionType.INCOME,
                            onClick = { onTypeChange(TransactionType.INCOME) },
                            label = { Text("收入") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.Success,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // 名称
                item {
                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = onNameChange,
                        label = { Text("名称") },
                        placeholder = { Text("如：房租、工资") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.Border
                        )
                    )
                }

                // 金额
                item {
                    OutlinedTextField(
                        value = formState.amount,
                        onValueChange = onAmountChange,
                        label = { Text("金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.Border
                        )
                    )
                }

                // 分类选择
                item {
                    Box {
                        OutlinedTextField(
                            value = selectedCategory?.let { "${it.icon} ${it.name}" } ?: "",
                            onValueChange = {},
                            label = { Text("分类") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Border
                            )
                        )
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            filteredCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text("${category.icon} ${category.name}") },
                                    onClick = {
                                        onCategoryChange(category.id)
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 账户选择
                item {
                    Box {
                        OutlinedTextField(
                            value = selectedAccount?.let { "${it.icon} ${it.name}" } ?: "",
                            onValueChange = {},
                            label = { Text("账户") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAccountDropdown = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Border
                            )
                        )
                        DropdownMenu(
                            expanded = showAccountDropdown,
                            onDismissRequest = { showAccountDropdown = false }
                        ) {
                            formState.availableAccounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text("${account.icon} ${account.name}") },
                                    onClick = {
                                        onAccountChange(account.id)
                                        showAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 频率选择
                item {
                    Column {
                        Text(
                            text = "重复频率",
                            style = AppTypography.LabelMedium,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                        Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)) {
                            RecurringFrequency.entries.forEach { freq ->
                                FilterChip(
                                    selected = formState.frequency == freq,
                                    onClick = { onFrequencyChange(freq) },
                                    label = { Text(freq.label, style = AppTypography.Caption) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppColors.Primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // 日期选择（根据频率）
                item {
                    when (formState.frequency) {
                        RecurringFrequency.MONTHLY -> {
                            OutlinedTextField(
                                value = formState.dayOfPeriod.toString(),
                                onValueChange = { it.toIntOrNull()?.let { day -> onDayChange(day.coerceIn(1, 31)) } },
                                label = { Text("每月几号") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        RecurringFrequency.WEEKLY -> {
                            Column {
                                Text("每周几", style = AppTypography.LabelMedium, color = AppColors.TextSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("日" to 1, "一" to 2, "二" to 3, "三" to 4, "四" to 5, "五" to 6, "六" to 7)
                                        .forEach { (label, value) ->
                                            FilterChip(
                                                selected = formState.dayOfPeriod == value,
                                                onClick = { onDayChange(value) },
                                                label = { Text(label, style = AppTypography.Caption) },
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // 备注
                item {
                    OutlinedTextField(
                        value = formState.note,
                        onValueChange = onNoteChange,
                        label = { Text("备注") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 自动执行开关
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "自动记账",
                                style = AppTypography.BodyMedium,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "到期自动创建交易记录",
                                style = AppTypography.Caption,
                                color = AppColors.TextMuted
                            )
                        }
                        Switch(
                            checked = formState.autoExecute,
                            onCheckedChange = onAutoExecuteChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Primary,
                                checkedTrackColor = AppColors.Primary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = formState.isValid,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
