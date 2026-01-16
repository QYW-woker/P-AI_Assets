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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppTopBarWithClose
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

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
                    .padding(AppDimens.PaddingXL)
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
                    .padding(AppDimens.PaddingL)
            )

            // 扩展字段
            ExpandedFields(
                date = uiState.dateText,
                accountName = uiState.accountName,
                note = uiState.note,
                onDateClick = { /* TODO: 打开日期选择器 */ },
                onAccountClick = { /* TODO: 打开账户选择器 */ },
                onNoteClick = { /* TODO: 打开备注输入 */ },
                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
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
                    .padding(AppDimens.PaddingL)
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
                style = AppTypography.TitleLarge,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = amount.ifEmpty { "0" },
                style = AppTypography.NumberLarge.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(48f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                color = AppColors.TextPrimary
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
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
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
        targetValue = if (isSelected) AppColors.Accent else Color.Transparent,
        label = "borderColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(category.color)))
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                style = AppTypography.TitleMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
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
 * 数字键盘
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

    Column(modifier = modifier) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
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
                // 确认按钮
                if (row == keys.last()) {
                    ConfirmButton(
                        onClick = onConfirmClick,
                        enabled = isConfirmEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
        }
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
            .aspectRatio(1.5f)
            .clip(AppShapes.Medium)
            .background(AppColors.Background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == "backspace") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "退格",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(24.dp)
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
 * 确认按钮
 */
@Composable
private fun ConfirmButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.75f)
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
 * 分类UI模型
 */
data class CategoryUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String
)
