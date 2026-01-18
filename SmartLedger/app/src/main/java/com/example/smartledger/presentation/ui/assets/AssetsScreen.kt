package com.example.smartledger.presentation.ui.assets

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBar
import com.example.smartledger.presentation.ui.components.DonutChart
import com.example.smartledger.presentation.ui.components.GradientCard
import com.example.smartledger.presentation.ui.components.LineChart
import com.example.smartledger.presentation.ui.components.LineChartPoint
import com.example.smartledger.presentation.ui.components.PieChartData
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography
import com.example.smartledger.utils.toColor

/**
 * 资产页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    onNavigateToAccountDetail: (Long) -> Unit,
    onNavigateToAccountManage: () -> Unit,
    onNavigateToAccountAdd: () -> Unit = {},
    onNavigateToAssetHistory: () -> Unit = {},
    onNavigateToInvestmentHolding: () -> Unit = {},
    viewModel: AssetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("资产", "收支", "投资")

    Scaffold(
        topBar = {
            AppTopBar(title = "资产")
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = onNavigateToAccountAdd,
                    containerColor = AppColors.Accent,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "添加账户")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
        ) {
            // 模块Tab
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = AppColors.Background,
                    contentColor = AppColors.Accent,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(3.dp)
                                .padding(horizontal = 32.dp)
                                .clip(AppShapes.Full)
                                .background(AppColors.Accent)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
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
            }

            when (selectedTabIndex) {
                0 -> {
                    // 资产模块
                    // 总资产概览
                    item {
                        TotalAssetsCard(
                            totalAssets = uiState.totalAssets,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    item {
                        HealthScoreCard(
                            score = uiState.healthScore,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    // 快捷入口：历史资产记录
                    item {
                        AppCard(
                            modifier = Modifier
                                .padding(horizontal = AppDimens.PaddingL)
                                .clickable { onNavigateToAssetHistory() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "历史资产记录",
                                        style = AppTypography.BodyMedium,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        text = "查看每月资产快照与变化趋势",
                                        style = AppTypography.Caption,
                                        color = AppColors.TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.PaddingL),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "我的账户",
                                style = AppTypography.TitleSmall,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "管理",
                                style = AppTypography.LabelMedium,
                                color = AppColors.Accent,
                                modifier = Modifier.clickable { onNavigateToAccountManage() }
                            )
                        }
                    }

                    if (uiState.accounts.isEmpty()) {
                        item {
                            EmptyAccountsCard(
                                onAddClick = onNavigateToAccountAdd,
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }
                    } else {
                        items(uiState.accounts) { account ->
                            AccountItem(
                                account = account,
                                onClick = { onNavigateToAccountDetail(account.id) },
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }
                    }
                }

                1 -> {
                    // 收支模块 - 增强版
                    item {
                        IncomeExpenseOverview(
                            income = uiState.monthlyIncome,
                            expense = uiState.monthlyExpense,
                            savingsRate = uiState.savingsRate,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    // 月度收支对比
                    item {
                        MonthlyComparisonCard(
                            currentIncome = uiState.monthlyIncome,
                            currentExpense = uiState.monthlyExpense,
                            lastMonthIncome = uiState.lastMonthIncome,
                            lastMonthExpense = uiState.lastMonthExpense,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    // 收支趋势图
                    if (uiState.dailyExpenseTrend.isNotEmpty()) {
                        item {
                            ExpenseTrendCard(
                                dailyTrend = uiState.dailyExpenseTrend,
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }
                    }

                    // 本月分类支出
                    if (uiState.categoryExpenses.isNotEmpty()) {
                        item {
                            Text(
                                text = "本月支出分类",
                                style = AppTypography.TitleSmall,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }

                        item {
                            CategoryExpenseCard(
                                categories = uiState.categoryExpenses,
                                totalExpense = uiState.monthlyExpense,
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }
                    }
                }

                2 -> {
                    // 投资模块 - 增强版
                    item {
                        InvestmentOverview(
                            principal = uiState.investmentPrincipal,
                            currentValue = uiState.investmentCurrentValue,
                            totalReturn = uiState.investmentReturn,
                            returnRate = uiState.investmentReturnRate,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    // 快捷入口：投资明细
                    item {
                        AppCard(
                            modifier = Modifier
                                .padding(horizontal = AppDimens.PaddingL)
                                .clickable { onNavigateToInvestmentHolding() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "投资明细",
                                        style = AppTypography.BodyMedium,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        text = "查看具体持仓与收益详情",
                                        style = AppTypography.Caption,
                                        color = AppColors.TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 投资账户列表
                    if (uiState.investmentAccounts.isNotEmpty()) {
                        item {
                            Text(
                                text = "投资账户",
                                style = AppTypography.TitleSmall,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }

                        items(uiState.investmentAccounts) { account ->
                            InvestmentAccountItem(
                                account = account,
                                onClick = { onNavigateToAccountDetail(account.id) },
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }

                        // 投资组合分布
                        item {
                            InvestmentAllocationCard(
                                accounts = uiState.investmentAccounts,
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }
                    } else {
                        item {
                            EmptyInvestmentCard(
                                onAddClick = onNavigateToAccountAdd,
                                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
            }
        }
    }
}

/**
 * 健康评分卡片
 */
@Composable
private fun HealthScoreCard(
    score: Int,
    modifier: Modifier = Modifier
) {
    GradientCard(
        modifier = modifier.fillMaxWidth(),
        gradientColors = AppColors.GradientAssetCard
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "财务健康分",
                    style = AppTypography.BodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = score.toString(),
                        style = AppTypography.NumberLarge,
                        color = Color.White
                    )
                    Text(
                        text = " / 100",
                        style = AppTypography.BodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Text(
                    text = getHealthScoreDescription(score),
                    style = AppTypography.LabelSmall,
                    color = AppColors.Success
                )
            }

            // 圆环进度
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // TODO: 添加圆环进度指示器
                Text(
                    text = "${score}%",
                    style = AppTypography.TitleMedium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 账户项
 */
@Composable
private fun AccountItem(
    account: AccountUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
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
                        .background(Color(android.graphics.Color.parseColor(account.color))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.icon, style = AppTypography.BodyLarge)
                }
                Spacer(modifier = Modifier.width(AppDimens.SpacingM))
                Column {
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
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¥${String.format("%.2f", account.balance)}",
                    style = AppTypography.NumberSmall,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.TextMuted
                )
            }
        }
    }
}

/**
 * 收支概览
 */
@Composable
private fun IncomeExpenseOverview(
    income: Double,
    expense: Double,
    savingsRate: Float,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "本月收支",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "收入",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", income)}",
                        style = AppTypography.NumberMedium,
                        color = AppColors.Success
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "支出",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", expense)}",
                        style = AppTypography.NumberMedium,
                        color = AppColors.Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "储蓄率",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "${String.format("%.1f", savingsRate * 100)}%",
                    style = AppTypography.NumberSmall,
                    color = if (savingsRate >= 0.2) AppColors.Success else AppColors.Warning
                )
            }
        }
    }
}

/**
 * 投资概览
 */
@Composable
private fun InvestmentOverview(
    principal: Double,
    currentValue: Double,
    totalReturn: Double,
    returnRate: Float,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "投资概览",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总本金",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", principal)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "当前市值",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", currentValue)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "总收益",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "${if (totalReturn >= 0) "+" else ""}¥${String.format("%.2f", totalReturn)}",
                        style = AppTypography.NumberSmall,
                        color = if (totalReturn >= 0) AppColors.Success else AppColors.Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Text(
                text = "收益率: ${if (returnRate >= 0) "+" else ""}${String.format("%.2f", returnRate * 100)}%",
                style = AppTypography.LabelMedium,
                color = if (returnRate >= 0) AppColors.Success else AppColors.Accent
            )
        }
    }
}

private fun getHealthScoreDescription(score: Int): String {
    return when {
        score >= 90 -> "财务状况优秀"
        score >= 70 -> "财务状况良好"
        score >= 50 -> "财务状况一般"
        else -> "需要改善财务状况"
    }
}

/**
 * 总资产卡片
 */
@Composable
private fun TotalAssetsCard(
    totalAssets: Double,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "总资产",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = "¥${String.format("%,.2f", totalAssets)}",
                style = AppTypography.NumberLarge,
                color = AppColors.TextPrimary
            )
        }
    }
}

/**
 * 空账户提示卡片
 */
@Composable
private fun EmptyAccountsCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.PaddingL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            Text(
                text = "还没有账户",
                style = AppTypography.BodyMedium,
                color = AppColors.TextSecondary
            )
            Text(
                text = "添加您的银行卡、支付宝、微信等账户",
                style = AppTypography.Caption,
                color = AppColors.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingL))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Accent
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(AppDimens.SpacingS))
                Text("添加账户")
            }
        }
    }
}

/**
 * 月度对比卡片
 */
@Composable
private fun MonthlyComparisonCard(
    currentIncome: Double,
    currentExpense: Double,
    lastMonthIncome: Double,
    lastMonthExpense: Double,
    modifier: Modifier = Modifier
) {
    val incomeChange = if (lastMonthIncome > 0) ((currentIncome - lastMonthIncome) / lastMonthIncome * 100) else 0.0
    val expenseChange = if (lastMonthExpense > 0) ((currentExpense - lastMonthExpense) / lastMonthExpense * 100) else 0.0

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "与上月对比",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入变化
                Column {
                    Text(
                        text = "收入变化",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (incomeChange >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (incomeChange >= 0) AppColors.Success else AppColors.Accent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (incomeChange >= 0) "+" else ""}${String.format("%.1f", incomeChange)}%",
                            style = AppTypography.NumberSmall,
                            color = if (incomeChange >= 0) AppColors.Success else AppColors.Accent
                        )
                    }
                }

                // 支出变化
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "支出变化",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (expenseChange <= 0) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (expenseChange <= 0) AppColors.Success else AppColors.Accent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (expenseChange >= 0) "+" else ""}${String.format("%.1f", expenseChange)}%",
                            style = AppTypography.NumberSmall,
                            color = if (expenseChange <= 0) AppColors.Success else AppColors.Accent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // 净结余
            val currentNet = currentIncome - currentExpense
            val lastMonthNet = lastMonthIncome - lastMonthExpense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月净结余",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "${if (currentNet >= 0) "+" else ""}¥${String.format("%.2f", currentNet)}",
                    style = AppTypography.NumberSmall,
                    color = if (currentNet >= 0) AppColors.Success else AppColors.Accent
                )
            }
        }
    }
}

/**
 * 支出趋势卡片
 */
@Composable
private fun ExpenseTrendCard(
    dailyTrend: List<DailyTrendUiModel>,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "本月支出趋势",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            val linePoints = dailyTrend.mapIndexed { index, daily ->
                LineChartPoint(index.toFloat(), daily.amount, daily.label)
            }

            LineChart(
                points = linePoints,
                modifier = Modifier.padding(vertical = AppDimens.PaddingS),
                height = 150.dp,
                lineColor = AppColors.Accent,
                showGrid = true
            )
        }
    }
}

/**
 * 分类支出卡片
 */
@Composable
private fun CategoryExpenseCard(
    categories: List<CategoryExpenseUiModel>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            // 饼图
            if (categories.isNotEmpty() && totalExpense > 0) {
                val pieData = categories.take(6).map { item ->
                    PieChartData(
                        label = item.name,
                        value = item.amount.toFloat(),
                        color = item.color.toColor()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DonutChart(
                        data = pieData,
                        centerText = "¥${String.format("%.0f", totalExpense)}",
                        centerSubText = "总支出",
                        size = 140.dp,
                        strokeWidth = 18.dp
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            }

            // 分类列表
            categories.take(5).forEach { category ->
                CategoryExpenseItem(
                    category = category,
                    maxAmount = categories.firstOrNull()?.amount ?: 1.0
                )
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            }
        }
    }
}

/**
 * 分类支出项
 */
@Composable
private fun CategoryExpenseItem(
    category: CategoryExpenseUiModel,
    maxAmount: Double
) {
    val progress = (category.amount / maxAmount).toFloat().coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(category.color.toColor()),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.icon, style = AppTypography.BodySmall)
        }

        Spacer(modifier = Modifier.width(AppDimens.SpacingM))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.name,
                    style = AppTypography.BodySmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "¥${String.format("%.2f", category.amount)}",
                    style = AppTypography.LabelSmall,
                    color = AppColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(AppShapes.Full),
                color = category.color.toColor(),
                trackColor = AppColors.Border
            )
        }
    }
}

/**
 * 投资账户项
 */
@Composable
private fun InvestmentAccountItem(
    account: InvestmentAccountUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val returnAmount = account.currentValue - account.principal
    val returnRate = if (account.principal > 0) (returnAmount / account.principal * 100) else 0.0

    AppCard(
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
                        .background(Color(android.graphics.Color.parseColor(account.color))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.icon, style = AppTypography.BodyLarge)
                }
                Spacer(modifier = Modifier.width(AppDimens.SpacingM))
                Column {
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
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "¥${String.format("%.2f", account.currentValue)}",
                    style = AppTypography.NumberSmall,
                    color = AppColors.TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (returnAmount >= 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (returnAmount >= 0) AppColors.Success else AppColors.Accent
                    )
                    Text(
                        text = "${if (returnRate >= 0) "+" else ""}${String.format("%.2f", returnRate)}%",
                        style = AppTypography.Caption,
                        color = if (returnAmount >= 0) AppColors.Success else AppColors.Accent
                    )
                }
            }
        }
    }
}

/**
 * 投资组合分布卡片
 */
@Composable
private fun InvestmentAllocationCard(
    accounts: List<InvestmentAccountUiModel>,
    modifier: Modifier = Modifier
) {
    val totalValue = accounts.sumOf { it.currentValue }

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "投资组合分布",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            if (accounts.isNotEmpty() && totalValue > 0) {
                val pieData = accounts.map { account ->
                    PieChartData(
                        label = account.name,
                        value = account.currentValue.toFloat(),
                        color = account.color.toColor()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    DonutChart(
                        data = pieData,
                        centerText = "¥${String.format("%.0f", totalValue)}",
                        centerSubText = "总市值",
                        size = 140.dp,
                        strokeWidth = 18.dp
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                accounts.forEach { account ->
                    val percent = (account.currentValue / totalValue * 100)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(account.color.toColor())
                            )
                            Spacer(modifier = Modifier.width(AppDimens.SpacingS))
                            Text(
                                text = account.name,
                                style = AppTypography.BodySmall,
                                color = AppColors.TextPrimary
                            )
                        }
                        Text(
                            text = "${String.format("%.1f", percent)}%",
                            style = AppTypography.LabelSmall,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * 空投资提示卡片
 */
@Composable
private fun EmptyInvestmentCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.PaddingL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            Text(
                text = "还没有投资账户",
                style = AppTypography.BodyMedium,
                color = AppColors.TextSecondary
            )
            Text(
                text = "添加股票、基金、定期存款等投资账户",
                style = AppTypography.Caption,
                color = AppColors.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingL))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Accent
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(AppDimens.SpacingS))
                Text("添加投资账户")
            }
        }
    }
}

/**
 * 账户UI模型
 */
data class AccountUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val typeName: String,
    val balance: Double
)

/**
 * 每日趋势UI模型
 */
data class DailyTrendUiModel(
    val date: Long,
    val amount: Float,
    val label: String
)

/**
 * 分类支出UI模型
 */
data class CategoryExpenseUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val amount: Double,
    val percent: Float
)

/**
 * 投资账户UI模型
 */
data class InvestmentAccountUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val typeName: String,
    val principal: Double,
    val currentValue: Double
)
