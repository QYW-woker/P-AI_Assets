package com.example.smartledger.presentation.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.utils.toColor

/**
 * iOS风格首页
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

    // iOS风格背景色
    val backgroundColor = Color(0xFFF2F2F7)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部问候区域
        item {
            HomeHeader(
                currentMonth = uiState.currentMonth,
                onNotificationClick = { }
            )
        }

        // 总资产卡片
        item {
            TotalAssetCard(
                totalAssets = uiState.totalAssets,
                assetsChange = uiState.assetsChange,
                assetsChangePercent = uiState.assetsChangePercent,
                onClick = onNavigateToAssets,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // 快捷操作按钮
        item {
            QuickActionButtons(
                onExpenseClick = onNavigateToRecord,
                onIncomeClick = onNavigateToRecord,
                onTransferClick = onNavigateToRecord,
                onAiClick = onNavigateToAiChat,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // 预算进度卡片
        item {
            BudgetCard(
                budgetTotal = uiState.budgetTotal,
                budgetUsed = uiState.budgetUsed,
                dailyAvailable = uiState.dailyAvailable,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // 月度概览
        item {
            MonthOverview(
                income = uiState.monthlyIncome,
                expense = uiState.monthlyExpense,
                investmentReturn = uiState.monthlyInvestmentReturn,
                modifier = Modifier.padding(start = 20.dp)
            )
        }

        // 近期账目标题
        item {
            SectionHeader(
                title = "近期账目",
                actionText = "全部",
                onActionClick = onNavigateToTransactionList,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // 交易列表
        if (uiState.recentTransactions.isEmpty()) {
            item {
                EmptyTransactionCard(
                    onAddClick = onNavigateToRecord,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        } else {
            items(
                items = uiState.recentTransactions,
                key = { it.id }
            ) { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    onClick = { onNavigateToTransactionDetail(transaction.id) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // 底部间距
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * 首页顶部区域 - iOS风格
 */
@Composable
private fun HomeHeader(
    currentMonth: String,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 60.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = currentMonth,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        }

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "通知",
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 总资产卡片 - iOS风格渐变
 */
@Composable
private fun TotalAssetCard(
    totalAssets: Double,
    assetsChange: Double,
    assetsChangePercent: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF1A1A2E).copy(alpha = 0.3f),
                spotColor = Color(0xFF1A1A2E).copy(alpha = 0.3f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2D2D44),
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "总资产",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¥${formatAmount(totalAssets)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isPositive = assetsChange >= 0
                    val changeIcon = if (isPositive) "📈" else "📉"
                    val changeColor = if (isPositive) Color(0xFF00D9A5) else Color(0xFFE94560)

                    Text(text = changeIcon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (isPositive) "+" else ""}${formatAmount(assetsChange)} (${String.format("%.1f", assetsChangePercent)}%)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = changeColor
                    )
                    Text(
                        text = " 本月",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * 快捷操作按钮组 - 卡通风格
 */
@Composable
private fun QuickActionButtons(
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
        QuickActionItem(
            icon = "📉",
            label = "支出",
            backgroundColor = Color(0xFFFFF0F3),
            onClick = onExpenseClick
        )
        QuickActionItem(
            icon = "📈",
            label = "收入",
            backgroundColor = Color(0xFFE6FFF7),
            onClick = onIncomeClick
        )
        QuickActionItem(
            icon = "🔄",
            label = "转账",
            backgroundColor = Color(0xFFEEF0FF),
            onClick = onTransferClick
        )
        QuickActionItem(
            icon = "🤖",
            label = "AI记账",
            backgroundColor = Color(0xFFFFF8E6),
            onClick = onAiClick
        )
    }
}

/**
 * 单个快捷操作按钮
 */
@Composable
private fun QuickActionItem(
    icon: String,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = backgroundColor.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8E8E93),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 预算进度卡片 - 带圆环进度
 */
@Composable
private fun BudgetCard(
    budgetTotal: Double,
    budgetUsed: Double,
    dailyAvailable: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (budgetTotal > 0) (budgetUsed / budgetTotal).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = budgetTotal - budgetUsed

    // 动画进度
    var animatedProgress by remember { mutableStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    LaunchedEffect(progress) {
        animatedProgress = progress
    }

    val progressColor = when {
        progress < 0.6f -> Color(0xFF00D9A5)
        progress < 0.8f -> Color(0xFFFFB020)
        else -> Color(0xFFE94560)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "预算进度",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 圆环进度指示器
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // 背景圆环
                        drawCircle(
                            color = Color(0xFFF2F2F7),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 进度圆环
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = animatedValue * 360f,
                            useCenter = false,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Text(
                        text = "${(animatedValue * 100).toInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "已用",
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93)
                            )
                            Text(
                                text = "¥${formatAmount(budgetUsed)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "剩余",
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93)
                            )
                            Text(
                                text = "¥${formatAmount(remaining)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (remaining >= 0) Color(0xFF00D9A5) else Color(0xFFE94560)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💰 日均可用 ¥${formatAmount(dailyAvailable)}",
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        }
    }
}

/**
 * 月度概览
 */
@Composable
private fun MonthOverview(
    income: Double,
    expense: Double,
    investmentReturn: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "月度概览",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonthOverviewItem(
                icon = "💵",
                title = "收入",
                amount = income,
                backgroundColor = Color(0xFFE6FFF7),
                amountColor = Color(0xFF00D9A5)
            )
            MonthOverviewItem(
                icon = "💸",
                title = "支出",
                amount = expense,
                backgroundColor = Color(0xFFFFF0F3),
                amountColor = Color(0xFFE94560)
            )
            MonthOverviewItem(
                icon = "📊",
                title = "投资收益",
                amount = investmentReturn,
                backgroundColor = Color(0xFFEEF0FF),
                amountColor = Color(0xFF667EEA)
            )
            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

/**
 * 月度概览项
 */
@Composable
private fun MonthOverviewItem(
    icon: String,
    title: String,
    amount: Double,
    backgroundColor: Color,
    amountColor: Color
) {
    Card(
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color(0xFF8E8E93)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¥${formatAmount(amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

/**
 * 区域标题
 */
@Composable
private fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E)
        )
        Text(
            text = actionText,
            fontSize = 15.sp,
            color = Color(0xFFE94560),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

/**
 * 空交易状态卡片
 */
@Composable
private fun EmptyTransactionCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📝", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "还没有记录",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击这里开始记账吧",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93)
            )
        }
    }
}

/**
 * 交易列表项 - iOS风格
 */
@Composable
private fun TransactionListItem(
    transaction: TransactionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 分类图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(transaction.categoryColor.toColor().copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.categoryIcon,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = transaction.categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    if (transaction.note.isNotEmpty()) {
                        Text(
                            text = transaction.note,
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Text(
                text = "${if (transaction.isExpense) "-" else "+"}¥${formatAmount(transaction.amount)}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.isExpense) Color(0xFFE94560) else Color(0xFF00D9A5)
            )
        }
    }
}

/**
 * 格式化金额
 */
private fun formatAmount(amount: Double): String {
    val absAmount = kotlin.math.abs(amount)
    return if (absAmount >= 10000) {
        String.format("%.2f万", absAmount / 10000)
    } else {
        String.format("%.2f", absAmount)
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
