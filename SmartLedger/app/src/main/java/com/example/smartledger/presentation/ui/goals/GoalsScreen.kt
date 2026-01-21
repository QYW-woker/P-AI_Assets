package com.example.smartledger.presentation.ui.goals

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// iOS风格颜色
private val iOSBackground = Color(0xFFF2F2F7)
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSRed = Color(0xFFFF3B30)
private val iOSPurple = Color(0xFFAF52DE)

/**
 * 储蓄目标页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddGoal: () -> Unit = {},
    onNavigateToGoalDetail: (Long) -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    // 添加目标对话框
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { name, icon, targetAmount, deadline, note ->
                viewModel.addGoal(name, icon, targetAmount, deadline, note)
                showAddGoalDialog = false
            }
        )
    }

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
            // 顶部导航栏
            item {
                IOSTopBar(
                    title = "🎯 储蓄目标",
                    onBackClick = onNavigateBack
                )
            }

            // 总览卡片
            item {
                GoalsSummaryCard(
                    goals = uiState.goals,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 添加新目标按钮
            item {
                AddGoalButton(
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // 目标列表
            if (uiState.goals.isEmpty()) {
                item {
                    EmptyGoalsState(
                        onAddGoal = { showAddGoalDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                item {
                    Text(
                        text = "📋 我的目标",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E8E93),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                items(uiState.goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onNavigateToGoalDetail(goal.id) },
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
 * iOS风格顶部栏
 */
@Composable
private fun IOSTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iOSCardBackground)
                .shadow(2.dp, CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 20.sp,
                color = iOSAccent
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1E)
        )
    }
}

/**
 * 目标总览卡片
 */
@Composable
private fun GoalsSummaryCard(
    goals: List<GoalUiModel>,
    modifier: Modifier = Modifier
) {
    val totalTarget = goals.sumOf { it.targetAmount }
    val totalCurrent = goals.sumOf { it.currentAmount }
    val overallProgress = if (totalTarget > 0) (totalCurrent / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
    val completedCount = goals.count { it.currentAmount >= it.targetAmount }

    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1500),
        label = "progress"
    )

    LaunchedEffect(overallProgress) {
        animatedProgress = overallProgress
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF9500),
                        Color(0xFFFF6B35)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 环形进度
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // 背景圆环
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // 进度圆环
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = animatedValue * 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedValue * 100).toInt()}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "整体进度",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "总目标金额",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "¥${formatAmount(totalTarget)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row {
                    StatItem(
                        label = "已存入",
                        value = "¥${formatAmount(totalCurrent)}",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = "已完成",
                        value = "$completedCount/${goals.size}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * 添加目标按钮
 */
@Composable
private fun AddGoalButton(
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
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iOSAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "➕",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "创建新目标",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = iOSAccent
            )
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyGoalsState(
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(iOSCardBackground)
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "还没有储蓄目标",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "设定一个目标，开始存钱吧！\n无论是旅行、买房还是投资",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(iOSAccent)
                    .clickable(onClick = onAddGoal)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "➕ 创建第一个目标",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 目标卡片
 */
@Composable
private fun GoalCard(
    goal: GoalUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val isCompleted = progress >= 1f

    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000),
        label = "progress"
    )

    LaunchedEffect(progress) {
        animatedProgress = progress
    }

    val progressColor = when {
        isCompleted -> iOSGreen
        progress >= 0.7f -> iOSOrange
        progress >= 0.3f -> iOSAccent
        else -> iOSPurple
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标和圆形进度
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(56.dp)) {
                        val strokeWidth = 4.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2

                        // 背景圆
                        drawCircle(
                            color = progressColor.copy(alpha = 0.2f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )

                        // 进度圆弧
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
                        text = goal.icon,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = goal.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )

                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(iOSGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "✅ 已完成",
                                    fontSize = 10.sp,
                                    color = iOSGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    goal.deadline?.let {
                        Text(
                            text = "📅 截止: $it",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }

                // 进度百分比
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${(animatedValue * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                    Text(
                        text = "→",
                        fontSize = 16.sp,
                        color = Color(0xFFC7C7CC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(progressColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedValue)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    progressColor.copy(alpha = 0.7f),
                                    progressColor
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 金额信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "已存入",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Text(
                        text = "¥${formatAmount(goal.currentAmount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = iOSGreen
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "还需",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Text(
                        text = "¥${formatAmount((goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0))}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) iOSGreen else iOSOrange
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "目标",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Text(
                        text = "¥${formatAmount(goal.targetAmount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }

            // 预计完成时间
            goal.estimatedCompletion?.let { estimated ->
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(iOSAccent.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "⏱️ 预计完成: $estimated",
                        fontSize = 12.sp,
                        color = iOSAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 10000) {
        String.format("%.2f万", amount / 10000)
    } else {
        String.format("%.2f", amount)
    }
}

/**
 * 目标UI模型
 */
data class GoalUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val progress: Float = 0f,
    val deadline: String?,
    val estimatedCompletion: String?,
    val note: String
)
