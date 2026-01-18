package com.example.smartledger.presentation.ui.report

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.domain.repository.CategorySummary
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 财务报告页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "财务报告",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新",
                            tint = AppColors.TextPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 周期选择器
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.selectPeriod(it) }
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AppColors.Primary)
                            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                            Text(
                                text = "正在生成报告...",
                                style = AppTypography.BodyMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "加载失败",
                            style = AppTypography.BodyMedium,
                            color = AppColors.Accent
                        )
                    }
                }

                else -> {
                    ReportContent(uiState = uiState)
                }
            }
        }
    }
}

/**
 * 周期选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    onPeriodSelected: (ReportPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.PaddingL),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
    ) {
        ReportPeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.label, style = AppTypography.LabelMedium) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.Card,
                    labelColor = AppColors.TextSecondary
                )
            )
        }
    }
}

/**
 * 报告内容
 */
@Composable
private fun ReportContent(uiState: ReportUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
    ) {
        // 报告标题
        item {
            Text(
                text = uiState.periodTitle,
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(horizontal = AppDimens.PaddingL),
                textAlign = TextAlign.Center
            )
        }

        // 核心指标卡片
        item {
            ReportSummaryCard(uiState = uiState)
        }

        // 对比变化
        item {
            ComparisonCard(uiState = uiState)
        }

        // 支出分类排行
        if (uiState.expenseByCategory.isNotEmpty()) {
            item {
                Text(
                    text = "支出分类",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            item {
                CategoryRankingCard(
                    categories = uiState.expenseByCategory,
                    type = "expense"
                )
            }
        }

        // 收入分类排行
        if (uiState.incomeByCategory.isNotEmpty()) {
            item {
                Text(
                    text = "收入分类",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            item {
                CategoryRankingCard(
                    categories = uiState.incomeByCategory,
                    type = "income"
                )
            }
        }

        // 数据洞察
        item {
            InsightsCard(uiState = uiState)
        }

        item {
            Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
        }
    }
}

/**
 * 报告汇总卡片
 */
@Composable
private fun ReportSummaryCard(uiState: ReportUiState) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL)
    ) {
        Column {
            // 收支总览
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(
                    label = "总收入",
                    value = "¥${String.format("%.0f", uiState.totalIncome)}",
                    color = AppColors.Success
                )
                SummaryMetric(
                    label = "总支出",
                    value = "¥${String.format("%.0f", uiState.totalExpense)}",
                    color = AppColors.Accent
                )
                SummaryMetric(
                    label = "结余",
                    value = "¥${String.format("%.0f", uiState.balance)}",
                    color = if (uiState.balance >= 0) AppColors.Info else AppColors.Accent
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // 其他指标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(
                    label = "储蓄率",
                    value = "${String.format("%.1f", uiState.savingsRate)}%",
                    color = if (uiState.savingsRate >= 20) AppColors.Success else AppColors.Warning
                )
                SummaryMetric(
                    label = "交易笔数",
                    value = "${uiState.transactionCount}笔",
                    color = AppColors.Primary
                )
                SummaryMetric(
                    label = "日均支出",
                    value = "¥${String.format("%.0f", uiState.avgDailyExpense)}",
                    color = AppColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = AppTypography.Caption,
            color = AppColors.TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = AppTypography.NumberSmall,
            color = color
        )
    }
}

/**
 * 对比变化卡片
 */
@Composable
private fun ComparisonCard(uiState: ReportUiState) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL)
    ) {
        Column {
            Text(
                text = "环比变化",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonItem(
                    label = "收入",
                    change = uiState.incomeChange,
                    isPositiveGood = true
                )
                ComparisonItem(
                    label = "支出",
                    change = uiState.expenseChange,
                    isPositiveGood = false
                )
            }
        }
    }
}

@Composable
private fun ComparisonItem(
    label: String,
    change: Double,
    isPositiveGood: Boolean
) {
    val isPositive = change >= 0
    val isGood = if (isPositiveGood) isPositive else !isPositive
    val color = if (isGood) AppColors.Success else AppColors.Accent
    val arrow = if (isPositive) "↑" else "↓"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = AppTypography.LabelMedium,
            color = AppColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = arrow,
                style = AppTypography.BodyMedium,
                color = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${String.format("%.1f", kotlin.math.abs(change))}%",
                style = AppTypography.NumberSmall,
                color = color
            )
        }
    }
}

/**
 * 分类排行卡片
 */
@Composable
private fun CategoryRankingCard(
    categories: List<CategorySummary>,
    type: String
) {
    val color = if (type == "expense") AppColors.Accent else AppColors.Success

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL)
    ) {
        Column {
            categories.take(5).forEachIndexed { index, category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppDimens.SpacingS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 排名
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when (index) {
                                    0 -> Color(0xFFFFD700)
                                    1 -> Color(0xFFC0C0C0)
                                    2 -> Color(0xFFCD7F32)
                                    else -> AppColors.Border
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = AppTypography.Caption,
                            color = if (index < 3) Color.White else AppColors.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(AppDimens.SpacingM))

                    // 分类名称
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.categoryName,
                            style = AppTypography.BodyMedium,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "${category.count}笔",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                    }

                    // 金额和占比
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "¥${String.format("%.0f", category.totalAmount)}",
                            style = AppTypography.NumberSmall,
                            color = color
                        )
                        Text(
                            text = "${String.format("%.1f", category.percent)}%",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                    }
                }

                // 进度条
                LinearProgressIndicator(
                    progress = category.percent / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = color.copy(alpha = 0.7f),
                    trackColor = AppColors.Border
                )

                if (index < categories.size - 1 && index < 4) {
                    Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                }
            }
        }
    }
}

/**
 * 数据洞察卡片
 */
@Composable
private fun InsightsCard(uiState: ReportUiState) {
    val insights = buildList {
        // 储蓄率洞察
        if (uiState.savingsRate >= 30) {
            add("🎉 本期储蓄率达到${String.format("%.1f", uiState.savingsRate)}%，表现优秀！")
        } else if (uiState.savingsRate >= 20) {
            add("👍 本期储蓄率${String.format("%.1f", uiState.savingsRate)}%，继续保持！")
        } else if (uiState.savingsRate > 0) {
            add("💡 本期储蓄率${String.format("%.1f", uiState.savingsRate)}%，建议适当增加储蓄")
        } else {
            add("⚠️ 本期支出超过收入，请注意控制开支")
        }

        // 最大支出分类
        uiState.topExpenseCategory?.let {
            add("📊 最大支出分类：${it.categoryName}，占比${String.format("%.1f", it.percent)}%")
        }

        // 支出变化
        if (uiState.expenseChange > 20) {
            add("📈 支出较上期增长${String.format("%.1f", uiState.expenseChange)}%，建议关注")
        } else if (uiState.expenseChange < -10) {
            add("📉 支出较上期减少${String.format("%.1f", kotlin.math.abs(uiState.expenseChange))}%，控制得不错！")
        }

        // 最大支出日
        uiState.maxExpenseDay?.let {
            add("📅 单日最高支出：${it.label}，¥${String.format("%.0f", it.amount)}")
        }

        // 交易频率
        val avgTransactionsPerDay = uiState.transactionCount.toFloat() / 30
        if (avgTransactionsPerDay >= 2) {
            add("✅ 记账习惯良好，平均每日${String.format("%.1f", avgTransactionsPerDay)}笔")
        } else if (avgTransactionsPerDay >= 1) {
            add("📝 记账较为规律，可以继续坚持")
        }
    }

    if (insights.isNotEmpty()) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.PaddingL)
        ) {
            Column {
                Text(
                    text = "数据洞察",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                insights.forEach { insight ->
                    Text(
                        text = insight,
                        style = AppTypography.BodySmall,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
