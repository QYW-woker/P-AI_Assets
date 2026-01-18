package com.example.smartledger.presentation.ui.health

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.domain.usecase.FinancialHealthReport
import com.example.smartledger.domain.usecase.FinancialSuggestion
import com.example.smartledger.domain.usecase.HealthLevel
import com.example.smartledger.domain.usecase.SuggestionPriority
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 财务健康诊断页面
 */
@Composable
fun FinancialHealthScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancialHealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "财务健康诊断",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
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
                                text = "正在分析您的财务数据...",
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "❌",
                                style = AppTypography.NumberLarge
                            )
                            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                            Text(
                                text = uiState.error ?: "加载失败",
                                style = AppTypography.BodyMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }

                uiState.report != null -> {
                    HealthReportContent(report = uiState.report!!)
                }
            }
        }
    }
}

@Composable
private fun HealthReportContent(report: FinancialHealthReport) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
    ) {
        // 总体评分卡片
        item {
            OverallScoreCard(
                score = report.overallScore,
                healthLevel = report.healthLevel
            )
        }

        // 核心指标
        item {
            KeyMetricsCard(report = report)
        }

        // 各维度评分
        item {
            DimensionScoresCard(report = report)
        }

        // 优势与短板
        item {
            StrengthsWeaknessesCard(
                strengths = report.strengths,
                weaknesses = report.weaknesses
            )
        }

        // 改进建议
        if (report.suggestions.isNotEmpty()) {
            item {
                Text(
                    text = "改进建议",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(
                        horizontal = AppDimens.PaddingL,
                        vertical = AppDimens.SpacingS
                    )
                )
            }

            items(report.suggestions) { suggestion ->
                SuggestionCard(suggestion = suggestion)
            }
        }

        item {
            Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
        }
    }
}

/**
 * 总体评分卡片
 */
@Composable
private fun OverallScoreCard(
    score: Int,
    healthLevel: HealthLevel
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )

    LaunchedEffect(score) {
        animatedProgress = score / 100f
    }

    val scoreColor = when (healthLevel) {
        HealthLevel.EXCELLENT -> AppColors.Success
        HealthLevel.GOOD -> Color(0xFF4CAF50)
        HealthLevel.FAIR -> AppColors.Warning
        HealthLevel.POOR -> Color(0xFFFF9800)
        HealthLevel.CRITICAL -> AppColors.Accent
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.PaddingL)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "财务健康评分",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // 圆形进度指示器
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    // 背景圆
                    drawArc(
                        color = AppColors.Border,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 进度圆
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedValue,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedValue * 100).toInt()}",
                        style = AppTypography.NumberLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = scoreColor
                    )
                    Text(
                        text = healthLevel.label,
                        style = AppTypography.TitleSmall,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Text(
                text = healthLevel.description,
                style = AppTypography.BodyMedium,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 核心指标卡片
 */
@Composable
private fun KeyMetricsCard(report: FinancialHealthReport) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL)
    ) {
        Column {
            Text(
                text = "核心指标",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "月收入",
                    value = "¥${String.format("%.0f", report.monthlyIncome)}",
                    color = AppColors.Success
                )
                MetricItem(
                    label = "月支出",
                    value = "¥${String.format("%.0f", report.monthlyExpense)}",
                    color = AppColors.Accent
                )
                MetricItem(
                    label = "储蓄率",
                    value = "${String.format("%.1f", report.savingsRate)}%",
                    color = if (report.savingsRate >= 20) AppColors.Success else AppColors.Warning
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "总资产",
                    value = "¥${String.format("%.0f", report.totalAssets)}",
                    color = AppColors.Primary
                )
                MetricItem(
                    label = "应急资金",
                    value = "${String.format("%.1f", report.emergencyFundMonths)}个月",
                    color = if (report.emergencyFundMonths >= 3) AppColors.Success else AppColors.Warning
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
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
 * 各维度评分卡片
 */
@Composable
private fun DimensionScoresCard(report: FinancialHealthReport) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL)
    ) {
        Column {
            Text(
                text = "各维度评分",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            DimensionScoreItem(
                icon = "💰",
                label = "储蓄能力",
                score = report.savingsScore
            )
            DimensionScoreItem(
                icon = "📊",
                label = "收支稳定",
                score = report.stabilityScore
            )
            DimensionScoreItem(
                icon = "📋",
                label = "预算执行",
                score = report.budgetScore
            )
            DimensionScoreItem(
                icon = "🎯",
                label = "目标进度",
                score = report.goalScore
            )
            DimensionScoreItem(
                icon = "🏦",
                label = "账户多样",
                score = report.diversityScore
            )
            DimensionScoreItem(
                icon = "🛡️",
                label = "应急资金",
                score = report.emergencyFundScore
            )
        }
    }
}

@Composable
private fun DimensionScoreItem(
    icon: String,
    label: String,
    score: Int
) {
    val color = when {
        score >= 80 -> AppColors.Success
        score >= 60 -> AppColors.Warning
        else -> AppColors.Accent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = AppTypography.BodyMedium
        )

        Spacer(modifier = Modifier.width(AppDimens.SpacingS))

        Text(
            text = label,
            style = AppTypography.BodyMedium,
            color = AppColors.TextPrimary,
            modifier = Modifier.width(80.dp)
        )

        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = AppColors.Border
        )

        Spacer(modifier = Modifier.width(AppDimens.SpacingS))

        Text(
            text = "$score",
            style = AppTypography.LabelMedium,
            color = color,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 优势与短板卡片
 */
@Composable
private fun StrengthsWeaknessesCard(
    strengths: List<String>,
    weaknesses: List<String>
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 优势
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✅", style = AppTypography.BodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "优势",
                        style = AppTypography.LabelMedium,
                        color = AppColors.Success
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                strengths.forEach { strength ->
                    Text(
                        text = "• $strength",
                        style = AppTypography.Caption,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingL))

            // 短板
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚠️", style = AppTypography.BodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "待改善",
                        style = AppTypography.LabelMedium,
                        color = AppColors.Warning
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                if (weaknesses.isEmpty()) {
                    Text(
                        text = "• 暂无明显短板",
                        style = AppTypography.Caption,
                        color = AppColors.TextSecondary
                    )
                } else {
                    weaknesses.forEach { weakness ->
                        Text(
                            text = "• $weakness",
                            style = AppTypography.Caption,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 建议卡片
 */
@Composable
private fun SuggestionCard(suggestion: FinancialSuggestion) {
    val priorityColor = when (suggestion.priority) {
        SuggestionPriority.HIGH -> AppColors.Accent
        SuggestionPriority.MEDIUM -> AppColors.Warning
        SuggestionPriority.LOW -> AppColors.Info
    }

    val priorityLabel = when (suggestion.priority) {
        SuggestionPriority.HIGH -> "高优先"
        SuggestionPriority.MEDIUM -> "中优先"
        SuggestionPriority.LOW -> "低优先"
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = priorityLabel,
                            style = AppTypography.Caption,
                            color = priorityColor
                        )
                    }

                    Spacer(modifier = Modifier.width(AppDimens.SpacingS))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AppColors.Primary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = suggestion.category,
                            style = AppTypography.Caption,
                            color = AppColors.Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingS))

            Text(
                text = suggestion.title,
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = suggestion.description,
                style = AppTypography.BodySmall,
                color = AppColors.TextSecondary
            )

            if (suggestion.actionItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Column {
                    suggestion.actionItems.forEachIndexed { index, action ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = AppTypography.Caption,
                                    color = AppColors.Primary
                                )
                            }
                            Spacer(modifier = Modifier.width(AppDimens.SpacingS))
                            Text(
                                text = action,
                                style = AppTypography.Caption,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
