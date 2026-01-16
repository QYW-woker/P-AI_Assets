package com.example.smartledger.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 饼图数据项
 */
data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * 饼图组件
 */
@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 30.dp,
    animationDuration: Int = 1000
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    Canvas(
        modifier = modifier.size(size)
    ) {
        val canvasSize = this.size.minDimension
        val radius = canvasSize / 2
        val strokeWidthPx = strokeWidth.toPx()

        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f * animatedProgress.value

            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = Size(canvasSize - strokeWidthPx, canvasSize - strokeWidthPx),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }
    }
}

/**
 * 带中心文字的环形图
 */
@Composable
fun DonutChart(
    data: List<PieChartData>,
    centerText: String,
    centerSubText: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 24.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            data = data,
            size = size,
            strokeWidth = strokeWidth
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = centerText,
                style = AppTypography.TitleLarge,
                color = AppColors.TextPrimary
            )
            if (centerSubText.isNotEmpty()) {
                Text(
                    text = centerSubText,
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }
        }
    }
}

/**
 * 柱状图数据项
 */
data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color = AppColors.Primary
)

/**
 * 柱状图组件
 */
@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    barWidth: Dp = 24.dp,
    spacing: Dp = 16.dp,
    animationDuration: Int = 1000
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.value }
    if (maxValue == 0f) return

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            val barHeight = (item.value / maxValue) * height.value * animatedProgress.value

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 数值标签
                Text(
                    text = String.format("%.0f", item.value),
                    style = AppTypography.LabelSmall,
                    color = AppColors.TextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 柱子
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeight.dp.coerceAtLeast(4.dp))
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(item.color)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 标签
                Text(
                    text = item.label,
                    style = AppTypography.Caption,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 折线图数据点
 */
data class LineChartPoint(
    val x: Float,
    val y: Float,
    val label: String = ""
)

/**
 * 折线图组件
 */
@Composable
fun LineChart(
    points: List<LineChartPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    lineColor: Color = AppColors.Primary,
    fillColor: Color = AppColors.Primary.copy(alpha = 0.1f),
    strokeWidth: Float = 3f,
    showDots: Boolean = true,
    showGrid: Boolean = true,
    animationDuration: Int = 1000
) {
    if (points.size < 2) return

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    val maxY = points.maxOf { it.y }
    val minY = points.minOf { it.y }
    val yRange = if (maxY == minY) 1f else maxY - minY

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 16.dp.toPx()

        val chartWidth = canvasWidth - padding * 2
        val chartHeight = canvasHeight - padding * 2

        // 绘制网格线
        if (showGrid) {
            val gridColor = Color.Gray.copy(alpha = 0.2f)
            repeat(5) { i ->
                val y = padding + (chartHeight / 4) * i
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(canvasWidth - padding, y),
                    strokeWidth = 1f
                )
            }
        }

        // 计算点的位置
        val pointPositions = points.mapIndexed { index, point ->
            val x = padding + (chartWidth / (points.size - 1)) * index
            val normalizedY = (point.y - minY) / yRange
            val y = padding + chartHeight * (1 - normalizedY)
            Offset(x, y)
        }

        // 绘制填充区域
        if (pointPositions.isNotEmpty()) {
            val fillPath = Path().apply {
                moveTo(pointPositions.first().x, padding + chartHeight)
                pointPositions.forEachIndexed { index, pos ->
                    val animatedX = pointPositions.first().x + (pos.x - pointPositions.first().x) * animatedProgress.value
                    val animatedY = (padding + chartHeight) + (pos.y - (padding + chartHeight)) * animatedProgress.value
                    lineTo(animatedX, animatedY)
                }
                lineTo(pointPositions.first().x + (pointPositions.last().x - pointPositions.first().x) * animatedProgress.value, padding + chartHeight)
                close()
            }
            drawPath(fillPath, fillColor)
        }

        // 绘制折线
        for (i in 0 until pointPositions.size - 1) {
            val start = pointPositions[i]
            val end = pointPositions[i + 1]

            val animatedEndX = start.x + (end.x - start.x) * animatedProgress.value
            val animatedEndY = start.y + (end.y - start.y) * animatedProgress.value

            drawLine(
                color = lineColor,
                start = start,
                end = Offset(animatedEndX, animatedEndY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // 绘制数据点
        if (showDots) {
            pointPositions.forEachIndexed { index, pos ->
                val progress = (animatedProgress.value * points.size).toInt()
                if (index <= progress) {
                    // 外圈
                    drawCircle(
                        color = lineColor,
                        radius = 6.dp.toPx(),
                        center = pos
                    )
                    // 内圈
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = pos
                    )
                }
            }
        }
    }
}

/**
 * 图例组件
 */
@Composable
fun ChartLegend(
    items: List<Pair<String, Color>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (label, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = AppTypography.Caption,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

/**
 * 进度条图表
 */
@Composable
fun ProgressBarChart(
    items: List<Triple<String, Float, Color>>, // label, progress (0-1), color
    modifier: Modifier = Modifier,
    barHeight: Dp = 8.dp,
    animationDuration: Int = 1000
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { (label, progress, color) ->
            val animatedProgress = remember { Animatable(0f) }

            LaunchedEffect(progress) {
                animatedProgress.animateTo(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = animationDuration)
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = AppTypography.Caption,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(barHeight / 2))
                        .background(AppColors.Border)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.value)
                            .height(barHeight)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(barHeight / 2))
                            .background(color)
                    )
                }
            }
        }
    }
}
