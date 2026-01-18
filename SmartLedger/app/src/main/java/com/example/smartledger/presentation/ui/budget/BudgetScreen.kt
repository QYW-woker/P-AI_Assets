package com.example.smartledger.presentation.ui.budget

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.components.NoBudgetState
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography
import com.example.smartledger.utils.toColor

/**
 * 预算页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddBudget: () -> Unit = {}, // 保留参数以兼容旧代码，但不再使用
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    // 添加预算对话框
    if (showAddBudgetDialog) {
        AddBudgetDialog(
            categories = expenseCategories,
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { categoryId, amount ->
                if (categoryId == null) {
                    viewModel.addTotalBudget(amount)
                } else {
                    viewModel.addCategoryBudget(categoryId, amount)
                }
                showAddBudgetDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "预算管理",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBudgetDialog = true },
                containerColor = AppColors.Accent,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加预算"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.budgets.isEmpty() && uiState.totalBudget == null) {
            NoBudgetState(
                onAddBudget = { showAddBudgetDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
            ) {
                // 总预算卡片
                uiState.totalBudget?.let { totalBudget ->
                    item {
                        TotalBudgetCard(
                            budget = totalBudget,
                            modifier = Modifier.padding(
                                start = AppDimens.PaddingL,
                                end = AppDimens.PaddingL,
                                top = AppDimens.PaddingL
                            )
                        )
                    }
                }

                // 分类预算标题
                if (uiState.budgets.isNotEmpty()) {
                    item {
                        Text(
                            text = "分类预算",
                            style = AppTypography.TitleSmall,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    items(uiState.budgets) { budget ->
                        CategoryBudgetItem(
                            budget = budget,
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

/**
 * 总预算卡片
 */
@Composable
private fun TotalBudgetCard(
    budget: BudgetUiModel,
    modifier: Modifier = Modifier
) {
    val progress = if (budget.amount > 0) (budget.used / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val progressColor = when {
        progress < 0.6f -> AppColors.Success
        progress < 0.8f -> AppColors.Warning
        else -> AppColors.Accent
    }

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月总预算",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "${String.format("%.0f", progress * 100)}%",
                    style = AppTypography.LabelMedium,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Text(
                text = "¥${String.format("%.2f", budget.amount)}",
                style = AppTypography.NumberLarge,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.ProgressBarHeight)
                    .clip(AppShapes.Full),
                color = progressColor,
                trackColor = AppColors.Border
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "已用",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", budget.used)}",
                        style = AppTypography.NumberSmall,
                        color = progressColor
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "剩余",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", budget.remaining)}",
                        style = AppTypography.NumberSmall,
                        color = if (budget.remaining >= 0) AppColors.Success else AppColors.Accent
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "日均可用",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", budget.dailyAvailable)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * 分类预算项
 */
@Composable
private fun CategoryBudgetItem(
    budget: BudgetUiModel,
    modifier: Modifier = Modifier
) {
    val progress = if (budget.amount > 0) (budget.used / budget.amount).toFloat().coerceIn(0f, 1.2f) else 0f
    val progressColor = when {
        progress < 0.6f -> AppColors.Success
        progress < 0.8f -> AppColors.Warning
        else -> AppColors.Accent
    }

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background((budget.categoryColor ?: "#ECEFF1").toColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = budget.categoryIcon ?: "\uD83D\uDCB0",
                    style = AppTypography.BodyLarge
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = budget.categoryName ?: "总预算",
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "¥${String.format("%.0f", budget.used)} / ¥${String.format("%.0f", budget.amount)}",
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = progress.coerceAtMost(1f),
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(AppShapes.Full),
                        color = progressColor,
                        trackColor = AppColors.Border
                    )
                    Spacer(modifier = Modifier.width(AppDimens.SpacingS))
                    Text(
                        text = "${String.format("%.0f", progress * 100)}%",
                        style = AppTypography.Caption,
                        color = progressColor
                    )
                }
            }
        }
    }
}

/**
 * 预算UI模型
 */
data class BudgetUiModel(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val amount: Double,
    val used: Double,
    val remaining: Double,
    val dailyAvailable: Double
)
