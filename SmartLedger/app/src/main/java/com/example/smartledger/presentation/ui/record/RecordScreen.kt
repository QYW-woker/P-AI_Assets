package com.example.smartledger.presentation.ui.record

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppTopBarWithClose
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography
import com.example.smartledger.utils.toColor

/**
 * 记账页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("支出", "收入", "转账")

    // 对话框状态
    var showDatePicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showNoteInput by remember { mutableStateOf(false) }

    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialogContent(
            onDateSelected = { timestamp ->
                viewModel.setDate(timestamp)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // 账户选择对话框
    if (showAccountPicker) {
        AccountPickerDialog(
            accounts = uiState.accounts,
            onAccountSelected = { accountId ->
                viewModel.selectAccount(accountId)
                showAccountPicker = false
            },
            onDismiss = { showAccountPicker = false }
        )
    }

    // 备注输入对话框
    if (showNoteInput) {
        NoteInputDialog(
            initialNote = uiState.note,
            onNoteConfirmed = { note ->
                viewModel.setNote(note)
                showNoteInput = false
            },
            onDismiss = { showNoteInput = false }
        )
    }

    Scaffold(
        topBar = {
            AppTopBarWithClose(
                title = "记账",
                onCloseClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // Tab切换
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = AppColors.Background,
                contentColor = AppColors.Accent,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .padding(horizontal = 24.dp)
                            .clip(AppShapes.Full)
                            .background(AppColors.Accent)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            viewModel.setTransactionType(index)
                        },
                        text = {
                            Text(
                                text = title,
                                style = AppTypography.LabelLarge,
                                color = if (selectedTabIndex == index) AppColors.Accent else AppColors.TextMuted
                            )
                        }
                    )
                }
            }

            // 金额显示
            AmountDisplay(
                amount = uiState.amountText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppDimens.PaddingM, horizontal = AppDimens.PaddingL)
            )

            // 分类选择网格
            Text(
                text = "选择分类",
                style = AppTypography.LabelMedium,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
            )

            CategoryGrid(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppDimens.PaddingM, vertical = AppDimens.PaddingS)
            )

            // 扩展字段
            ExpandedFields(
                date = uiState.dateText,
                accountName = uiState.accountName,
                note = uiState.note,
                onDateClick = { showDatePicker = true },
                onAccountClick = { showAccountPicker = true },
                onNoteClick = { showNoteInput = true },
                modifier = Modifier.padding(horizontal = AppDimens.PaddingM, vertical = AppDimens.PaddingS)
            )

            // 数字键盘
            NumericKeypad(
                onNumberClick = { viewModel.appendNumber(it) },
                onDotClick = { viewModel.appendDot() },
                onBackspaceClick = { viewModel.backspace() },
                onConfirmClick = {
                    viewModel.saveTransaction()
                    onSaveSuccess()
                },
                isConfirmEnabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Card)
                    .padding(horizontal = AppDimens.PaddingM, vertical = AppDimens.PaddingS)
            )
        }
    }
}

/**
 * 金额显示
 */
@Composable
private fun AmountDisplay(
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "¥",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = amount.ifEmpty { "0" },
                style = AppTypography.NumberLarge.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(36f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                color = AppColors.TextPrimary
            )
        }
    }
}

/**
 * 分类选择网格 - 紧凑版
 */
@Composable
private fun CategoryGrid(
    categories: List<CategoryUiModel>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

/**
 * 分类项 - 紧凑版
 */
@Composable
private fun CategoryItem(
    category: CategoryUiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Accent else Color.Transparent,
        label = "borderColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(category.color.toColor())
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                style = AppTypography.LabelLarge
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = category.name,
            style = AppTypography.LabelSmall,
            color = if (isSelected) AppColors.Accent else AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * 扩展字段
 */
@Composable
private fun ExpandedFields(
    date: String,
    accountName: String,
    note: String,
    onDateClick: () -> Unit,
    onAccountClick: () -> Unit,
    onNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 日期
        Row(
            modifier = Modifier.clickable(onClick = onDateClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "日期",
                modifier = Modifier.size(20.dp),
                tint = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = date,
                style = AppTypography.LabelMedium,
                color = AppColors.TextSecondary
            )
        }

        // 账户
        Row(
            modifier = Modifier.clickable(onClick = onAccountClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = accountName,
                style = AppTypography.LabelMedium,
                color = AppColors.TextSecondary
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "选择账户",
                modifier = Modifier.size(20.dp),
                tint = AppColors.TextSecondary
            )
        }

        // 备注
        Row(
            modifier = Modifier.clickable(onClick = onNoteClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "备注",
                modifier = Modifier.size(20.dp),
                tint = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = note.ifEmpty { "添加备注" },
                style = AppTypography.LabelMedium,
                color = AppColors.TextMuted
            )
        }
    }
}

/**
 * 数字键盘 - 紧凑版适配手机
 */
@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDotClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onConfirmClick: () -> Unit,
    isConfirmEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "backspace")
    )

    Row(modifier = modifier) {
        // 数字键盘主体
        Column(modifier = Modifier.weight(3f)) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    row.forEach { key ->
                        KeypadButton(
                            key = key,
                            onClick = {
                                when (key) {
                                    "backspace" -> onBackspaceClick()
                                    "." -> onDotClick()
                                    else -> onNumberClick(key)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            }
        }

        Spacer(modifier = Modifier.width(AppDimens.SpacingS))

        // 确认按钮 - 右侧独立
        ConfirmButton(
            onClick = onConfirmClick,
            enabled = isConfirmEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 键盘按钮 - 紧凑版
 */
@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(AppShapes.Medium)
            .background(AppColors.Background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == "backspace") {
            Icon(
                imageVector = Icons.Filled.Backspace,
                contentDescription = "退格",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = key,
                style = AppTypography.NumberMedium,
                color = AppColors.TextPrimary
            )
        }
    }
}

/**
 * 确认按钮 - 占满右侧高度
 */
@Composable
private fun ConfirmButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    // 4行按钮高度 + 3个间距 = 44*4 + 4*3 = 188dp
    Box(
        modifier = modifier
            .height(188.dp)
            .clip(AppShapes.Medium)
            .background(if (enabled) AppColors.Accent else AppColors.Accent.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "确认",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * 日期选择器对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContent(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                Text("确定", color = AppColors.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * 账户选择对话框
 */
@Composable
private fun AccountPickerDialog(
    accounts: List<AccountUiModel>,
    onAccountSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择账户",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            LazyColumn {
                items(accounts) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(account.id) }
                            .padding(vertical = AppDimens.PaddingM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = account.icon,
                            style = AppTypography.TitleMedium
                        )
                        Spacer(modifier = Modifier.width(AppDimens.SpacingM))
                        Text(
                            text = account.name,
                            style = AppTypography.BodyMedium,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextSecondary)
            }
        },
        containerColor = AppColors.Card
    )
}

/**
 * 备注输入对话框
 */
@Composable
private fun NoteInputDialog(
    initialNote: String,
    onNoteConfirmed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加备注",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "请输入备注...",
                        color = AppColors.TextMuted
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                maxLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onNoteConfirmed(noteText) }
            ) {
                Text("确定", color = AppColors.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextSecondary)
            }
        },
        containerColor = AppColors.Card
    )
}

/**
 * 分类UI模型
 */
data class CategoryUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String
)
