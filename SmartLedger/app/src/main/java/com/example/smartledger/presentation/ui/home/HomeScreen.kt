package com.example.smartledger.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppCardSmall
import com.example.smartledger.presentation.ui.components.GradientCard
import com.example.smartledger.presentation.ui.components.NoTransactionsState
import com.example.smartledger.utils.toColor
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 首页
 */
@Composable
fun HomeScreen(
    onNavigateToRecord: () -> Unit,
    onNavigateToAiChat: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    onNavigateToTransactionList: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
    ) {
        // 顶部标题栏
        item {
            HomeTopBar(
                currentMonth = uiState.currentMonth,
                onNotificationClick = { /* TODO */ }
            )
        }

        // 资产卡片
        item {
            AssetCard(
                totalAssets = uiState.totalAssets,
                assetsChange = uiState.assetsChange,
                assetsChangePercent = uiState.assetsChangePercent,
                onClick = onNavigateToAssets,
                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
            )
        }

        // 快捷操作
        item {
            QuickActions(
                onExpenseClick = onNavigateToRecord,
                onIncomeClick = onNavigateToRecord,
                onTransferClick = onNavigateToRecord,
                onAiClick = onNavigateToAiChat,
                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
            )
        }

        // 预算进度
        item {
            BudgetProgressCard(
                budgetTotal = uiState.budgetTotal,
                budgetUsed = uiState.budgetUsed,
                dailyAvailable = uiState.dailyAvailable,
                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
            )
        }

        // 月度概览
        item {
            MonthlyOverviewSection(
                income = uiState.monthlyIncome,
                expense = uiState.monthlyExpense,
                investmentReturn = uiState.monthlyInvestmentReturn,
                modifier = Modifier.padding(start = AppDimens.PaddingL)
            )
        }

        // 近期账目标题
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.PaddingL),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "近期账目",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "全部",
                    style = AppTypography.LabelMedium,
                    color = AppColors.Accent,
                    modifier = Modifier.clickable { onNavigateToTransactionList() }
                )
            }
        }

        // 近期交易列表
        if (uiState.recentTransactions.isEmpty()) {
            item {
                NoTransactionsState(
                    onAddTransaction = onNavigateToRecord,
                    modifier = Modifier.padding(AppDimens.PaddingXXL)
                )
            }
        } else {
            items(
                items = uiState.recentTransactions,
                key = { it.id }
            ) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onNavigateToTransactionDetail(transaction.id) },
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }
        }

        // 底部间距
        item {
            Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
        }
    }
}

/**
 * 首页顶部栏
 */
@Composable
private fun HomeTopBar(
    currentMonth: String,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = AppDimens.PaddingL,
                end = AppDimens.PaddingL,
                top = AppDimens.PaddingXL,
                bottom = AppDimens.PaddingM
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = currentMonth,
                style = AppTypography.TitleLarge,
                color = AppColors.TextPrimary
            )
        }

        IconButton(onClick = onNotificationClick) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "通知",
                tint = AppColors.TextPrimary
            )
        }
    }
}

/**
 * 资产卡片
 */
@Composable
private fun AssetCard(
    totalAssets: Double,
    assetsChange: Double,
    assetsChangePercent: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GradientCard(
        modifier = modifier.fillMaxWidth(),
        gradientColors = AppColors.GradientAssetCard,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "总资产",
                    style = AppTypography.BodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Text(
                    text = "¥${formatAmount(totalAssets)}",
                    style = AppTypography.NumberLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isPositive = assetsChange >= 0
                    Icon(
                        imageVector = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isPositive) AppColors.Success else AppColors.Accent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (isPositive) "+" else ""}${formatAmount(assetsChange)} (${String.format("%.1f", assetsChangePercent)}%)",
                        style = AppTypography.LabelSmall,
                        color = if (isPositive) AppColors.Success else AppColors.Accent
                    )
                    Text(
                        text = " 本月",
                        style = AppTypography.LabelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "查看详情",
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * 快捷操作按钮组
 */
@Composable
private fun QuickActions(
    onExpenseClick: () -> Unit,
    onIncomeClick: () -> Unit,
    onTransferClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            icon = Icons.Filled.TrendingDown,
            label = "支出",
            backgroundColor = AppColors.AccentLight,
            iconColor = AppColors.Accent,
            onClick = onExpenseClick
        )
        QuickActionButton(
            icon = Icons.Filled.TrendingUp,
            label = "收入",
            backgroundColor = AppColors.SuccessLight,
            iconColor = AppColors.Success,
            onClick = onIncomeClick
        )
        QuickActionButton(
            icon = Icons.Filled.SwapHoriz,
            label = "转账",
            backgroundColor = AppColors.InfoLight,
            iconColor = AppColors.Info,
            onClick = onTransferClick
        )
        QuickActionButton(
            icon = Icons.Outlined.SmartToy,
            label = "AI记账",
            backgroundColor = AppColors.WarningLight,
            iconColor = AppColors.Warning,
            onClick = onAiClick
        )
    }
}

/**
 * 快捷操作按钮
 */
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(AppDimens.SpacingS))
        Text(
            text = label,
            style = AppTypography.LabelSmall,
            color = AppColors.TextSecondary
        )
    }
}

/**
 * 预算进度卡片
 */
@Composable
private fun BudgetProgressCard(
    budgetTotal: Double,
    budgetUsed: Double,
    dailyAvailable: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (budgetTotal > 0) (budgetUsed / budgetTotal).toFloat().coerceIn(0f, 1f) else 0f
    val progressColor = when {
        progress < 0.6f -> AppColors.Success
        progress < 0.8f -> AppColors.Warning
        else -> AppColors.Accent
    }
    val remaining = budgetTotal - budgetUsed

    AppCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "预算进度",
            style = AppTypography.TitleSmall,
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
                    text = "¥${formatAmount(budgetUsed)}",
                    style = AppTypography.NumberSmall,
                    color = AppColors.TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "剩余",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
                Text(
                    text = "¥${formatAmount(remaining)}",
                    style = AppTypography.NumberSmall,
                    color = if (remaining >= 0) AppColors.Success else AppColors.Accent
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimens.SpacingS))

        Text(
            text = "日均可用 ¥${formatAmount(dailyAvailable)}",
            style = AppTypography.Caption,
            color = AppColors.TextMuted
        )
    }
}

/**
 * 月度概览区域
 */
@Composable
private fun MonthlyOverviewSection(
    income: Double,
    expense: Double,
    investmentReturn: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "月度概览",
            style = AppTypography.TitleSmall,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(bottom = AppDimens.SpacingM)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
        ) {
            MonthlyOverviewCard(
                title = "收入",
                amount = income,
                backgroundColor = AppColors.SuccessLight,
                amountColor = AppColors.Success
            )
            MonthlyOverviewCard(
                title = "支出",
                amount = expense,
                backgroundColor = AppColors.AccentLight,
                amountColor = AppColors.Accent
            )
            MonthlyOverviewCard(
                title = "投资收益",
                amount = investmentReturn,
                backgroundColor = AppColors.InfoLight,
                amountColor = AppColors.Info
            )
            Spacer(modifier = Modifier.width(AppDimens.PaddingL))
        }
    }
}

/**
 * 月度概览卡片
 */
@Composable
private fun MonthlyOverviewCard(
    title: String,
    amount: Double,
    backgroundColor: Color,
    amountColor: Color
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(AppShapes.Medium)
            .background(backgroundColor)
            .padding(AppDimens.PaddingL)
    ) {
        Column {
            Text(
                text = title,
                style = AppTypography.LabelMedium,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = "¥${formatAmount(amount)}",
                style = AppTypography.NumberMedium,
                color = amountColor
            )
        }
    }
}

/**
 * 交易项
 */
@Composable
private fun TransactionItem(
    transaction: TransactionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCardSmall(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
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
                        .clip(CircleShape)
                        .background(transaction.categoryColor.toColor()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.categoryIcon,
                        style = AppTypography.BodyLarge
                    )
                }
                Spacer(modifier = Modifier.width(AppDimens.SpacingM))
                Column {
                    Text(
                        text = transaction.categoryName,
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextPrimary
                    )
                    if (transaction.note.isNotEmpty()) {
                        Text(
                            text = transaction.note,
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }

            Text(
                text = "${if (transaction.isExpense) "-" else "+"}¥${formatAmount(transaction.amount)}",
                style = AppTypography.NumberSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (transaction.isExpense) AppColors.Accent else AppColors.Success
            )
        }
    }
}

/**
 * 格式化金额
 */
private fun formatAmount(amount: Double): String {
    return if (amount >= 10000) {
        String.format("%.2f万", amount / 10000)
    } else {
        String.format("%.2f", amount)
    }
}

/**
 * 交易UI模型
 */
data class TransactionUiModel(
    val id: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val amount: Double,
    val note: String,
    val isExpense: Boolean,
    val date: Long
)
