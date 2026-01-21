package com.example.smartledger.presentation.ui.assets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
private val iOSPink = Color(0xFFFF2D55)

/**
 * 资产页面 - iOS卡通风格
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
    val tabs = listOf("💰 资产", "📊 收支", "📈 投资")

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
                        text = "💎 我的资产",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            // iOS风格Tab
            item {
                IOSTabRow(
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // 资产模块
                    item {
                        TotalAssetsCard(
                            totalAssets = uiState.totalAssets,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        HealthScoreCard(
                            score = uiState.healthScore,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // 快捷入口
                    item {
                        QuickEntryCard(
                            icon = "📅",
                            title = "历史资产记录",
                            subtitle = "查看每月资产快照与变化趋势",
                            onClick = onNavigateToAssetHistory,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // 我的账户标题
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏦 我的账户",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E)
                            )
                            Text(
                                text = "管理 →",
                                fontSize = 14.sp,
                                color = iOSAccent,
                                modifier = Modifier.clickable { onNavigateToAccountManage() }
                            )
                        }
                    }

                    if (uiState.accounts.isEmpty()) {
                        item {
                            EmptyAccountsCard(
                                onAddClick = onNavigateToAccountAdd,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    } else {
                        items(uiState.accounts) { account ->
                            AccountItem(
                                account = account,
                                onClick = { onNavigateToAccountDetail(account.id) },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }

                    // 添加账户按钮
                    item {
                        AddAccountButton(
                            onClick = onNavigateToAccountAdd,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                1 -> {
                    // 收支模块
                    item {
                        IncomeExpenseCard(
                            income = uiState.monthlyIncome,
                            expense = uiState.monthlyExpense,
                            savingsRate = uiState.savingsRate,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        MonthlyComparisonCard(
                            currentIncome = uiState.monthlyIncome,
                            currentExpense = uiState.monthlyExpense,
                            lastMonthIncome = uiState.lastMonthIncome,
                            lastMonthExpense = uiState.lastMonthExpense,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    if (uiState.dailyExpenseTrend.isNotEmpty()) {
                        item {
                            ExpenseTrendCard(
                                dailyTrend = uiState.dailyExpenseTrend,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }

                    if (uiState.categoryExpenses.isNotEmpty()) {
                        item {
                            Text(
                                text = "🏷️ 本月支出分类",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            CategoryExpenseCard(
                                categories = uiState.categoryExpenses,
                                totalExpense = uiState.monthlyExpense,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }

                2 -> {
                    // 投资模块
                    item {
                        InvestmentOverviewCard(
                            principal = uiState.investmentPrincipal,
                            currentValue = uiState.investmentCurrentValue,
                            totalReturn = uiState.investmentReturn,
                            returnRate = uiState.investmentReturnRate,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        QuickEntryCard(
                            icon = "📋",
                            title = "投资明细",
                            subtitle = "查看具体持仓与收益详情",
                            onClick = onNavigateToInvestmentHolding,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    if (uiState.investmentAccounts.isNotEmpty()) {
                        item {
                            Text(
                                text = "💼 投资账户",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        items(uiState.investmentAccounts) { account ->
                            InvestmentAccountItem(
                                account = account,
                                onClick = { onNavigateToAccountDetail(account.id) },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            InvestmentAllocationCard(
                                accounts = uiState.investmentAccounts,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    } else {
                        item {
                            EmptyInvestmentCard(
                                onAddClick = onNavigateToAccountAdd,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedTabIndex == index)
                                iOSAccent
                            else
                                Color.Transparent
                        )
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
 * 总资产卡片
 */
@Composable
private fun TotalAssetsCard(
    totalAssets: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "总资产",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "¥ ${String.format("%,.2f", totalAssets)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
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
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedScore by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000),
        label = "score"
    )

    LaunchedEffect(score) {
        animatedProgress = score / 100f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "❤️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "财务健康分",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = score.toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = getScoreColor(score)
                    )
                    Text(
                        text = " / 100",
                        fontSize = 16.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getHealthScoreDescription(score),
                    fontSize = 14.sp,
                    color = getScoreColor(score)
                )
            }

            // 圆形进度
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // 背景圆
                    drawCircle(
                        color = Color(0xFFE5E5EA),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 进度圆
                    drawArc(
                        color = getScoreColor(score),
                        startAngle = -90f,
                        sweepAngle = animatedScore * 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(animatedScore * 100).toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(score)
                )
            }
        }
    }
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF34C759)
        score >= 60 -> Color(0xFFFF9500)
        else -> Color(0xFFFF3B30)
    }
}

private fun getHealthScoreDescription(score: Int): String {
    return when {
        score >= 90 -> "财务状况优秀 🌟"
        score >= 70 -> "财务状况良好 👍"
        score >= 50 -> "财务状况一般 📊"
        else -> "需要改善 ⚠️"
    }
}

/**
 * 快捷入口卡片
 */
@Composable
private fun QuickEntryCard(
    icon: String,
    title: String,
    subtitle: String,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
            Text(
                text = "→",
                fontSize = 20.sp,
                color = Color(0xFFC7C7CC)
            )
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(android.graphics.Color.parseColor(account.color)).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.icon, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = account.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = account.typeName,
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¥${String.format("%,.2f", account.balance)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "→",
                    fontSize = 16.sp,
                    color = Color(0xFFC7C7CC)
                )
            }
        }
    }
}

/**
 * 添加账户按钮
 */
@Composable
private fun AddAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(iOSAccent.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "➕", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加账户",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = iOSAccent
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🏦", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "还没有账户",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = "添加银行卡、支付宝、微信等账户",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(iOSAccent)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "➕", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "添加账户",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 收支概览卡片
 */
@Composable
private fun IncomeExpenseCard(
    income: Double,
    expense: Double,
    savingsRate: Float,
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
        Column {
            Text(
                text = "📊 本月收支",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📈", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "收入",
                            fontSize = 14.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${String.format("%,.2f", income)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = iOSGreen
                    )
                }

                // 支出
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📉", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "支出",
                            fontSize = 14.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${String.format("%,.2f", expense)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = iOSOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 储蓄率
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 储蓄率",
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
                )
                Text(
                    text = "${String.format("%.1f", savingsRate * 100)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (savingsRate >= 0.2) iOSGreen else iOSOrange
                )
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
    val currentNet = currentIncome - currentExpense

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "📅 与上月对比",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 收入变化
                Column {
                    Text(
                        text = "收入变化",
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (incomeChange >= 0) "📈" else "📉",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (incomeChange >= 0) "+" else ""}${String.format("%.1f", incomeChange)}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (incomeChange >= 0) iOSGreen else iOSRed
                        )
                    }
                }

                // 支出变化
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "支出变化",
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (expenseChange <= 0) "📉" else "📈",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (expenseChange >= 0) "+" else ""}${String.format("%.1f", expenseChange)}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (expenseChange <= 0) iOSGreen else iOSRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 本月净结余",
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
                )
                Text(
                    text = "${if (currentNet >= 0) "+" else ""}¥${String.format("%,.2f", currentNet)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentNet >= 0) iOSGreen else iOSRed
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "📈 本月支出趋势",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val linePoints = dailyTrend.mapIndexed { index, daily ->
                LineChartPoint(index.toFloat(), daily.amount, daily.label)
            }

            LineChart(
                points = linePoints,
                modifier = Modifier.padding(vertical = 8.dp),
                height = 150.dp,
                lineColor = iOSAccent,
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
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

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 分类列表
            categories.take(5).forEach { category ->
                CategoryExpenseItem(
                    category = category,
                    maxAmount = categories.firstOrNull()?.amount ?: 1.0
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(category.color.toColor().copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.icon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = "¥${String.format("%.2f", category.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = category.color.toColor(),
                trackColor = Color(0xFFE5E5EA),
            )
        }
    }
}

/**
 * 投资概览卡片
 */
@Composable
private fun InvestmentOverviewCard(
    principal: Double,
    currentValue: Double,
    totalReturn: Double,
    returnRate: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF11998e),
                        Color(0xFF38ef7d)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📈", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "投资概览",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总本金",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "¥${String.format("%,.2f", principal)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "当前市值",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "¥${String.format("%,.2f", currentValue)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "总收益",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${if (totalReturn >= 0) "+" else ""}¥${String.format("%.2f", totalReturn)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (returnRate >= 0) "📈" else "📉",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "收益率: ${if (returnRate >= 0) "+" else ""}${String.format("%.2f", returnRate * 100)}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(android.graphics.Color.parseColor(account.color)).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.icon, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = account.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = account.typeName,
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "¥${String.format("%,.2f", account.currentValue)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (returnAmount >= 0) "📈" else "📉",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${if (returnRate >= 0) "+" else ""}${String.format("%.2f", returnRate)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (returnAmount >= 0) iOSGreen else iOSRed
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "🥧 投资组合分布",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(20.dp))

                accounts.forEach { account ->
                    val percent = (account.currentValue / totalValue * 100)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = account.name,
                                fontSize = 14.sp,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        Text(
                            text = "${String.format("%.1f", percent)}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8E8E93)
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📈", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "还没有投资账户",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = "添加股票、基金、定期存款等投资账户",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(iOSGreen)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "➕", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "添加投资账户",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
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
