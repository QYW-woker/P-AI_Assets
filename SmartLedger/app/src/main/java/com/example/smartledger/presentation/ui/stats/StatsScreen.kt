package com.example.smartledger.presentation.ui.stats

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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBar
import com.example.smartledger.presentation.ui.components.BarChart
import com.example.smartledger.presentation.ui.components.BarChartData
import com.example.smartledger.presentation.ui.components.ChartLegend
import com.example.smartledger.presentation.ui.components.DonutChart
import com.example.smartledger.presentation.ui.components.LineChart
import com.example.smartledger.presentation.ui.components.LineChartPoint
import com.example.smartledger.presentation.ui.components.PieChartData
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography
import com.example.smartledger.utils.toColor

/**
 * 统计页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedChartType by remember { mutableStateOf("pie") }

    Scaffold(
        topBar = {
            AppTopBar(title = "统计")
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
        ) {
            // 时间筛选
            item {
                TimeFilterTabs(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            // 汇总卡片
            item {
                SummaryCards(
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense,
                    balance = uiState.balance,
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            // 图表切换
            item {
                ChartTypeToggle(
                    selectedType = selectedChartType,
                    onTypeSelected = { selectedChartType = it },
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            // 图表区域
            item {
                ChartArea(
                    chartType = selectedChartType,
                    categoryRanking = uiState.categoryRanking,
                    totalExpense = uiState.totalExpense,
                    dailyTrend = uiState.dailyTrend,
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            // 分类排行
            item {
                Text(
                    text = "分类排行",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            items(uiState.categoryRanking) { item ->
                CategoryRankingItem(
                    item = item,
                    maxAmount = uiState.categoryRanking.firstOrNull()?.amount ?: 1.0,
                    modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
            }
        }
    }
}

/**
 * 时间筛选标签
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFilterTabs(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf("周", "月", "季", "年", "全部", "自定义")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
    ) {
        periods.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = period,
                        style = AppTypography.LabelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Accent,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.Card,
                    labelColor = AppColors.TextSecondary
                )
            )
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
    ) {
        SummaryCard(
            label = "收入",
            amount = income,
            color = AppColors.Success,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "支出",
            amount = expense,
            color = AppColors.Accent,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "结余",
            amount = balance,
            color = if (balance >= 0) AppColors.Info else AppColors.Accent,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 汇总卡片项
 */
@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Text(
            text = label,
            style = AppTypography.Caption,
            color = AppColors.TextMuted
        )
        Spacer(modifier = Modifier.height(AppDimens.SpacingXS))
        Text(
            text = "¥${String.format("%.2f", amount)}",
            style = AppTypography.NumberSmall,
            color = color
        )
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
        IconButton(onClick = { onTypeSelected("pie") }) {
            Icon(
                imageVector = Icons.Filled.PieChart,
                contentDescription = "饼图",
                tint = if (selectedType == "pie") AppColors.Accent else AppColors.TextMuted
            )
        }
        IconButton(onClick = { onTypeSelected("bar") }) {
            Icon(
                imageVector = Icons.Filled.BarChart,
                contentDescription = "柱状图",
                tint = if (selectedType == "bar") AppColors.Accent else AppColors.TextMuted
            )
        }
        IconButton(onClick = { onTypeSelected("line") }) {
            Icon(
                imageVector = Icons.Filled.ShowChart,
                contentDescription = "折线图",
                tint = if (selectedType == "line") AppColors.Accent else AppColors.TextMuted
            )
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
    AppCard(modifier = modifier.fillMaxWidth()) {
        when (chartType) {
            "pie" -> {
                // 饼图
                if (categoryRanking.isNotEmpty()) {
                    val pieData = categoryRanking.take(6).map { item ->
                        PieChartData(
                            label = item.name,
                            value = item.amount.toFloat(),
                            color = item.color.toColor()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.PaddingM),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutChart(
                            data = pieData,
                            centerText = "¥${String.format("%.0f", totalExpense)}",
                            centerSubText = "总支出",
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
                // 柱状图
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
                        modifier = Modifier.padding(AppDimens.PaddingM),
                        height = 180.dp
                    )
                } else {
                    EmptyChartPlaceholder("暂无数据")
                }
            }

            "line" -> {
                // 折线图 - 使用真实趋势数据
                if (dailyTrend.isNotEmpty()) {
                    val linePoints = dailyTrend.mapIndexed { index, daily ->
                        LineChartPoint(index.toFloat(), daily.amount, daily.label)
                    }

                    LineChart(
                        points = linePoints,
                        modifier = Modifier.padding(AppDimens.PaddingM),
                        height = 180.dp,
                        lineColor = AppColors.Primary,
                        showGrid = true
                    )
                } else {
                    EmptyChartPlaceholder("暂无趋势数据")
                }
            }

            else -> {
                EmptyChartPlaceholder("选择图表类型")
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
        Text(
            text = text,
            style = AppTypography.BodyMedium,
            color = AppColors.TextMuted
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

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.color.toColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, style = AppTypography.BodyLarge)
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "¥${String.format("%.2f", item.amount)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingXS))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(AppShapes.Full),
                        color = item.color.toColor(),
                        trackColor = AppColors.Border
                    )
                    Spacer(modifier = Modifier.width(AppDimens.SpacingS))
                    Text(
                        text = "${String.format("%.1f", item.percent)}%",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }
            }
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
