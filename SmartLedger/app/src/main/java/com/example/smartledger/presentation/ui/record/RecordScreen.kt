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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.utils.toColor

// iOS风格颜色
private val iOSBackground = Color(0xFFF2F2F7)
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSRed = Color(0xFFFF3B30)

/**
 * 记账页面 - iOS卡通风格
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
    val tabs = listOf("📉 支出", "📈 收入", "🔄 转账")

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
        containerColor = iOSBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(iOSBackground)
                .padding(paddingValues)
        ) {
            // 顶部栏
            IOSTopBar(
                onClose = onNavigateBack
            )

            // Tab切换
            IOSTabRow(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { index ->
                    selectedTabIndex = index
                    viewModel.setTransactionType(index)
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // 金额显示
            AmountDisplay(
                amount = uiState.amountText,
                isExpense = selectedTabIndex == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            )

            // 分类选择网格
            Text(
                text = "🏷️ 选择分类",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            CategoryGrid(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { viewModel.selectCategory(it) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // 扩展字段
            ExpandedFields(
                date = uiState.dateText,
                accountName = uiState.accountName,
                note = uiState.note,
                onDateClick = { showDatePicker = true },
                onAccountClick = { showAccountPicker = true },
                onNoteClick = { showNoteInput = true },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
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
                isExpense = selectedTabIndex == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(iOSCardBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

/**
 * iOS风格顶部栏
 */
@Composable
private fun IOSTopBar(
    onClose: () -> Unit
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
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    fontSize = 16.sp,
                    color = Color(0xFF8E8E93)
                )
            }

            Text(
                text = "📝 记一笔",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            // 占位符保持居中
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

/**
 * iOS风格Tab栏
 */
@Composable
private fun IOSTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSCardBackground)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                val bgColor = when {
                    selectedTabIndex == index && index == 0 -> iOSOrange
                    selectedTabIndex == index && index == 1 -> iOSGreen
                    selectedTabIndex == index && index == 2 -> iOSAccent
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTabIndex == index) Color.White else Color(0xFF8E8E93)
                    )
                }
            }
        }
    }
}

/**
 * 金额显示
 */
@Composable
private fun AmountDisplay(
    amount: String,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isExpense) {
                        listOf(Color(0xFFFF9500), Color(0xFFFF6B6B))
                    } else {
                        listOf(Color(0xFF34C759), Color(0xFF30D158))
                    }
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "¥",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = amount.ifEmpty { "0" },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 分类选择网格
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
 * 分类项
 */
@Composable
private fun CategoryItem(
    category: CategoryUiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) iOSAccent else Color.Transparent,
        label = "borderColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(if (isSelected) 4.dp else 2.dp, CircleShape)
                .clip(CircleShape)
                .background(category.color.toColor().copy(alpha = 0.15f))
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) iOSAccent else Color(0xFF8E8E93),
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSCardBackground)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 日期
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F2F7))
                .clickable(onClick = onDateClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📅", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = date,
                    fontSize = 13.sp,
                    color = Color(0xFF1C1C1E)
                )
            }
        }

        // 账户
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F2F7))
                .clickable(onClick = onAccountClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🏦", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = accountName,
                    fontSize = 13.sp,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = " ▼",
                    fontSize = 10.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        }

        // 备注
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F2F7))
                .clickable(onClick = onNoteClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✏️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = note.ifEmpty { "备注" },
                    fontSize = 13.sp,
                    color = if (note.isEmpty()) Color(0xFF8E8E93) else Color(0xFF1C1C1E),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 数字键盘
 */
@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDotClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onConfirmClick: () -> Unit,
    isConfirmEnabled: Boolean,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Row(modifier = modifier) {
        // 数字键盘主体
        Column(modifier = Modifier.weight(3f)) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        KeypadButton(
                            key = key,
                            onClick = {
                                when (key) {
                                    "⌫" -> onBackspaceClick()
                                    "." -> onDotClick()
                                    else -> onNumberClick(key)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 确认按钮
        ConfirmButton(
            onClick = onConfirmClick,
            enabled = isConfirmEnabled,
            isExpense = isExpense,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 键盘按钮
 */
@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF2F2F7))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            fontSize = if (key == "⌫") 20.sp else 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E)
        )
    }
}

/**
 * 确认按钮
 */
@Composable
private fun ConfirmButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (enabled) {
        if (isExpense) iOSOrange else iOSGreen
    } else {
        Color(0xFFE5E5EA)
    }

    Box(
        modifier = modifier
            .height(228.dp) // 4 rows * 52dp + 3 gaps * 8dp
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "✓",
                fontSize = 32.sp,
                color = if (enabled) Color.White else Color(0xFF8E8E93)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "完成",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else Color(0xFF8E8E93)
            )
        }
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
                Text("确定", color = iOSAccent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFF8E8E93))
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
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "🏦 选择账户",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            LazyColumn {
                items(accounts) { account ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAccountSelected(account.id) }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF2F2F7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.icon,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = account.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFF8E8E93))
            }
        }
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
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "✏️ 添加备注",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
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
                        color = Color(0xFF8E8E93)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iOSAccent,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onNoteConfirmed(noteText) }
            ) {
                Text("确定", color = iOSAccent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFF8E8E93))
            }
        }
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
