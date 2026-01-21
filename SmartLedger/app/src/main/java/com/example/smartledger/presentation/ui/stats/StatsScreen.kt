package com.example.smartledger.presentation.ui.stats

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.presentation.ui.components.BarChart
import com.example.smartledger.presentation.ui.components.BarChartData
import com.example.smartledger.presentation.ui.components.ChartLegend
import com.example.smartledger.presentation.ui.components.DonutChart
import com.example.smartledger.presentation.ui.components.LineChart
import com.example.smartledger.presentation.ui.components.LineChartPoint
import com.example.smartledger.presentation.ui.components.PieChartData
import com.example.smartledger.utils.toColor

// iOS风格颜色
private val iOSBackground = Color(0xFFF2F2F7)
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSRed = Color(0xFFFF3B30)
private val iOSPurple = Color(0xFFAF52DE)

/**
 * 统计页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedChartType by remember { mutableStateOf("pie") }

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
            // 顶部标题
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "📊 统计分析",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            // 时间筛选
            item {
                TimeFilterTabs(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 周期标签
            if (uiState.periodLabel.isNotEmpty()) {
                item {
                    Text(
                        text = uiState.periodLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E8E93),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // 汇总卡片
            item {
                SummaryCards(
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense,
                    balance = uiState.balance,
                    incomeChange = uiState.incomeChange,
                    expenseChange = uiState.expenseChange,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 月度概览指标
            item {
                MonthlyMetricsCard(
                    transactionCount = uiState.transactionCount,
                    avgDailyExpense = uiState.avgDailyExpense,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 图表切换
            item {
                ChartTypeToggle(
                    selectedType = selectedChartType,
                    onTypeSelected = { selectedChartType = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 图表区域
            item {
                ChartArea(
                    chartType = selectedChartType,
                    categoryRanking = uiState.categoryRanking,
                    totalExpense = if (uiState.showIncome) uiState.totalIncome else uiState.totalExpense,
                    dailyTrend = uiState.dailyTrend,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 分类排行
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.showIncome) "📈 收入分类排行" else "📉 支出分类排行",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(iOSAccent.copy(alpha = 0.1f))
                            .clickable { viewModel.toggleIncomeExpense() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (uiState.showIncome) "查看支出" else "查看收入",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = iOSAccent
                        )
                    }
                }
            }

            if (uiState.categoryRanking.isEmpty()) {
                item {
                    EmptyDataCard(
                        message = "暂无数据",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                items(uiState.categoryRanking) { item ->
                    CategoryRankingItem(
                        item = item,
                        maxAmount = uiState.categoryRanking.firstOrNull()?.amount ?: 1.0,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // 账户变动
            if (uiState.accountChanges.isNotEmpty()) {
                item {
                    Text(
                        text = "🏦 账户变动",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                items(uiState.accountChanges) { account ->
                    AccountChangeItem(
                        account = account,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // 最近交易
            if (uiState.recentTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "📝 最近交易",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                items(uiState.recentTransactions) { transaction ->
                    RecentTransactionItem(
                        transaction = transaction,
                        onClick = { onNavigateToTransactionDetail(transaction.id) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

/**
 * 时间筛选标签
 */
@Composable
private fun TimeFilterTabs(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf("周", "月", "季", "年", "全部")

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
            periods.forEach { period ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedPeriod == period)
                                iOSAccent
                            else
                                Color.Transparent
                        )
                        .clickable { onPeriodSelected(period) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period,
                        fontSize = 13.sp,
                        fontWeight = if (selectedPeriod == period) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedPeriod == period) Color.White else Color(0xFF8E8E93)
                    )
                }
            }
        }
    }
}

/**
 * 汇总卡片
 */
@Composable
private fun SummaryCards(
    income: Double,
    expense: Double,
    balance: Double,
    incomeChange: Float,
    expenseChange: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 收入卡片
        SummaryCard(
            icon = "📈",
            label = "收入",
            amount = income,
            change = incomeChange,
            gradientColors = listOf(Color(0xFF34C759), Color(0xFF30D158)),
            modifier = Modifier.weight(1f)
        )

        // 支出卡片
        SummaryCard(
            icon = "📉",
            label = "支出",
            amount = expense,
            change = expenseChange,
            gradientColors = listOf(Color(0xFFFF9500), Color(0xFFFF6B6B)),
            positiveIsBad = true,
            modifier = Modifier.weight(1f)
        )

        // 结余卡片
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = if (balance >= 0)
                            listOf(Color(0xFF007AFF), Color(0xFF5AC8FA))
                        else
                            listOf(Color(0xFFFF3B30), Color(0xFFFF6B6B))
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Text(text = "💰", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "结余",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "¥${String.format("%.0f", balance)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 汇总卡片项
 */
@Composable
private fun SummaryCard(
    icon: String,
    label: String,
    amount: Double,
    change: Float,
    gradientColors: List<Color>,
    positiveIsBad: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors = gradientColors))
            .padding(12.dp)
    ) {
        Column {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "¥${String.format("%.0f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isPositive = change >= 0
                val changeIcon = if (isPositive) "↑" else "↓"
                Text(
                    text = "$changeIcon${String.format("%.1f", kotlin.math.abs(change))}%",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * 月度指标卡片
 */
@Composable
private fun MonthlyMetricsCard(
    transactionCount: Int,
    avgDailyExpense: Double,
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
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📝", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "交易笔数",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
                Text(
                    text = "$transactionCount 笔",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(Color(0xFFE5E5EA))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📅", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "日均支出",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
                Text(
                    text = "¥${String.format("%.2f", avgDailyExpense)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        }
    }
}

/**
 * 图表类型切换
 */
@Composable
private fun ChartTypeToggle(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        listOf(
            "pie" to "🥧",
            "bar" to "📊",
            "line" to "📈"
        ).forEach { (type, icon) ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selectedType == type)
                            iOSAccent.copy(alpha = 0.15f)
                        else
                            Color.Transparent
                    )
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 22.sp
                )
            }
        }
    }
}

/**
 * 图表区域
 */
@Composable
private fun ChartArea(
    chartType: String,
    categoryRanking: List<CategoryRankingUiModel>,
    totalExpense: Double,
    dailyTrend: List<DailyTrendUiModel>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
        when (chartType) {
            "pie" -> {
                if (categoryRanking.isNotEmpty()) {
                    val pieData = categoryRanking.take(6).map { item ->
                        PieChartData(
                            label = item.name,
                            value = item.amount.toFloat(),
                            color = item.color.toColor()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutChart(
                            data = pieData,
                            centerText = "¥${String.format("%.0f", totalExpense)}",
                            centerSubText = "总计",
                            size = 160.dp,
                            strokeWidth = 20.dp
                        )

                        ChartLegend(
                            items = pieData.map { it.label to it.color }
                        )
                    }
                } else {
                    EmptyChartPlaceholder("暂无数据")
                }
            }

            "bar" -> {
                if (categoryRanking.isNotEmpty()) {
                    val barData = categoryRanking.take(5).map { item ->
                        BarChartData(
                            label = item.name.take(4),
                            value = item.amount.toFloat(),
                            color = item.color.toColor()
                        )
                    }

                    BarChart(
                        data = barData,
                        modifier = Modifier,
                        height = 180.dp
                    )
                } else {
                    EmptyChartPlaceholder("暂无数据")
                }
            }

            "line" -> {
                if (dailyTrend.isNotEmpty()) {
                    val linePoints = dailyTrend.mapIndexed { index, daily ->
                        LineChartPoint(index.toFloat(), daily.amount, daily.label)
                    }

                    LineChart(
                        points = linePoints,
                        modifier = Modifier,
                        height = 180.dp,
                        lineColor = iOSAccent,
                        showGrid = true
                    )
                } else {
                    EmptyChartPlaceholder("暂无趋势数据")
                }
            }
        }
    }
}

/**
 * 空图表占位
 */
@Composable
private fun EmptyChartPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📭", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color(0xFF8E8E93)
            )
        }
    }
}

/**
 * 空数据卡片
 */
@Composable
private fun EmptyDataCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSCardBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF8E8E93)
        )
    }
}

/**
 * 分类排行项
 */
@Composable
private fun CategoryRankingItem(
    item: CategoryRankingUiModel,
    maxAmount: Double,
    modifier: Modifier = Modifier
) {
    val progress = (item.amount / maxAmount).toFloat().coerceIn(0f, 1f)

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color.toColor().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "¥${String.format("%.2f", item.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = item.color.toColor(),
                        trackColor = Color(0xFFE5E5EA),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.1f", item.percent)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        }
    }
}

/**
 * 账户变动项
 */
@Composable
private fun AccountChangeItem(
    account: AccountChangeUiModel,
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
                        .background(account.color.toColor().copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = account.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "余额: ¥${String.format("%.2f", account.currentBalance)}",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (account.periodChange >= 0) "📈" else "📉",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (account.periodChange >= 0) "+" else ""}¥${String.format("%.2f", account.periodChange)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (account.periodChange >= 0) iOSGreen else iOSRed
                )
            }
        }
    }
}

/**
 * 最近交易项
 */
@Composable
private fun RecentTransactionItem(
    transaction: RecentTransactionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSCardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (transaction.type == TransactionType.EXPENSE)
                                iOSOrange.copy(alpha = 0.15f)
                            else
                                iOSGreen.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = transaction.categoryIcon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.categoryName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Row {
                        Text(
                            text = transaction.date,
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                        if (!transaction.note.isNullOrEmpty()) {
                            Text(
                                text = " · ${transaction.note}",
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Text(
                text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${String.format("%.2f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.type == TransactionType.EXPENSE) iOSOrange else iOSGreen
            )
        }
    }
}

/**
 * 分类排行UI模型
 */
data class CategoryRankingUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val amount: Double,
    val percent: Float
)
