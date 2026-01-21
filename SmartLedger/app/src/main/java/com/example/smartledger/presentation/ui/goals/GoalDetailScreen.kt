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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// iOS风格颜色
private val iOSBackground = Color(0xFFF2F2F7)
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSRed = Color(0xFFFF3B30)
private val iOSPurple = Color(0xFFAF52DE)

/**
 * 目标详情页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: Long,
    onNavigateBack: () -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) {
        viewModel.loadGoal(goalId)
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        IOSAlertDialog(
            icon = "🗑️",
            title = "确认删除",
            message = "删除后无法恢复，确定要删除这个储蓄目标吗？",
            confirmText = "删除",
            confirmColor = iOSRed,
            onConfirm = {
                viewModel.deleteGoal()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // 存入金额对话框
    if (showDepositDialog && uiState.goal != null) {
        DepositToGoalDialog(
            goalName = uiState.goal!!.name,
            currentAmount = uiState.goal!!.currentAmount,
            targetAmount = uiState.goal!!.targetAmount,
            onDismiss = { showDepositDialog = false },
            onConfirm = { amount ->
                viewModel.addToGoal(amount)
                showDepositDialog = false
            }
        )
    }

    // 取出金额对话框
    if (showWithdrawDialog && uiState.goal != null) {
        WithdrawFromGoalDialog(
            goalName = uiState.goal!!.name,
            currentAmount = uiState.goal!!.currentAmount,
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount ->
                viewModel.withdrawFromGoal(amount)
                showWithdrawDialog = false
            }
        )
    }

    // 编辑目标对话框
    if (showEditDialog && uiState.goal != null) {
        EditGoalDialog(
            goal = uiState.goal!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, targetAmount, note ->
                viewModel.updateGoal(name, targetAmount, note)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        containerColor = iOSBackground
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⏳", fontSize = 48.sp)
            }
        } else if (uiState.goal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "😕", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "目标不存在",
                        fontSize = 16.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(iOSBackground)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 顶部栏
                item {
                    IOSTopBar(
                        title = "🎯 目标详情",
                        onBack = onNavigateBack,
                        onEdit = { showEditDialog = true },
                        onDelete = { showDeleteDialog = true }
                    )
                }

                // 进度卡片
                item {
                    ProgressCard(
                        goal = uiState.goal!!,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 快捷操作
                if (!uiState.goal!!.isCompleted) {
                    item {
                        QuickActionsCard(
                            onDeposit = { showDepositDialog = true },
                            onWithdraw = { showWithdrawDialog = true },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 完成庆祝
                if (uiState.goal!!.isCompleted) {
                    item {
                        CompletedCard(
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 金额详情
                item {
                    AmountDetailsCard(
                        goal = uiState.goal!!,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 存款历史
                if (uiState.depositHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "📜 存款记录",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    items(uiState.depositHistory) { record ->
                        DepositHistoryItem(
                            record = record,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 目标信息
                item {
                    GoalInfoCard(
                        goal = uiState.goal!!,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
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
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E5EA))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "←", fontSize = 18.sp, color = Color(0xFF8E8E93))
            }

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Row {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iOSAccent.copy(alpha = 0.1f))
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✏️", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iOSRed.copy(alpha = 0.1f))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🗑️", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * 进度卡片
 */
@Composable
private fun ProgressCard(
    goal: GoalDetailUiModel,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1500),
        label = "progress"
    )

    LaunchedEffect(progress) {
        animatedProgress = progress
    }

    val progressColor = when {
        goal.isCompleted -> iOSGreen
        progress >= 0.8f -> iOSOrange
        else -> iOSAccent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = if (goal.isCompleted) {
                        listOf(Color(0xFF34C759), Color(0xFF30D158))
                    } else {
                        listOf(Color(0xFF667eea), Color(0xFF764ba2))
                    }
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 目标图标
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = goal.icon, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = goal.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (goal.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "🎉", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "已完成",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 圆形进度
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // 背景圆
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 进度圆
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedValue * 100).toInt()}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "完成度",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 快捷操作卡片
 */
@Composable
private fun QuickActionsCard(
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 存入按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(iOSGreen)
                .clickable(onClick = onDeposit)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💰", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "存入",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // 取出按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(iOSOrange)
                .clickable(onClick = onWithdraw)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💸", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "取出",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 完成庆祝卡片
 */
@Composable
private fun CompletedCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🏆", fontSize = 40.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "恭喜完成目标！",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "你太棒了！继续保持这份坚持",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * 金额详情卡片
 */
@Composable
private fun AmountDetailsCard(
    goal: GoalDetailUiModel,
    modifier: Modifier = Modifier
) {
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val daysRemaining = goal.deadline?.let {
        ((it - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
    val dailyTarget = if (daysRemaining != null && daysRemaining > 0) {
        remaining / daysRemaining
    } else null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "💵 金额详情",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AmountItem(
                    icon = "🎯",
                    label = "目标金额",
                    amount = goal.targetAmount,
                    color = iOSAccent
                )
                AmountItem(
                    icon = "💰",
                    label = "已存入",
                    amount = goal.currentAmount,
                    color = iOSGreen
                )
                AmountItem(
                    icon = "📊",
                    label = "还需存",
                    amount = remaining,
                    color = iOSOrange
                )
            }

            if (dailyTarget != null && !goal.isCompleted) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(iOSAccent.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📅", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "日均需存",
                                fontSize = 14.sp,
                                color = Color(0xFF8E8E93)
                            )
                        }
                        Text(
                            text = "¥${String.format("%.2f", dailyTarget)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = iOSAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountItem(
    icon: String,
    label: String,
    amount: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8E8E93)
        )
        Text(
            text = "¥${formatAmount(amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 存款历史项
 */
@Composable
private fun DepositHistoryItem(
    record: DepositRecord,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

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
                        .background(
                            if (record.isDeposit)
                                iOSGreen.copy(alpha = 0.15f)
                            else
                                iOSOrange.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (record.isDeposit) "💰" else "💸",
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (record.isDeposit) "存入" else "取出",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = dateFormat.format(Date(record.date)),
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }

            Text(
                text = "${if (record.isDeposit) "+" else "-"}¥${String.format("%.2f", record.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (record.isDeposit) iOSGreen else iOSOrange
            )
        }
    }
}

/**
 * 目标信息卡片
 */
@Composable
private fun GoalInfoCard(
    goal: GoalDetailUiModel,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(iOSCardBackground)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "ℹ️ 目标信息",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(icon = "📅", label = "创建时间", value = dateFormat.format(Date(goal.createdAt)))

            if (goal.deadline != null) {
                InfoRow(icon = "⏰", label = "截止日期", value = dateFormat.format(Date(goal.deadline)))
                val daysRemaining = ((goal.deadline - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                InfoRow(
                    icon = "📆",
                    label = "剩余天数",
                    value = if (daysRemaining > 0) "${daysRemaining}天" else "已到期"
                )
            }

            if (goal.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF2F2F7))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📝", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "备注",
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = goal.note,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF8E8E93)
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E)
        )
    }
}

/**
 * iOS风格提示对话框
 */
@Composable
private fun IOSAlertDialog(
    icon: String,
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = icon, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = confirmColor, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFF8E8E93))
            }
        }
    )
}

/**
 * 编辑目标对话框
 */
@Composable
private fun EditGoalDialog(
    goal: GoalDetailUiModel,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var nameText by remember { mutableStateOf(goal.name) }
    var targetText by remember { mutableStateOf(goal.targetAmount.toString()) }
    var noteText by remember { mutableStateOf(goal.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "✏️ 编辑目标",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("目标名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSAccent,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("目标金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSAccent,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("备注") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSAccent,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    targetText.toDoubleOrNull()?.let { target ->
                        onConfirm(nameText, target, noteText)
                    }
                },
                enabled = nameText.isNotBlank() && targetText.toDoubleOrNull() != null
            ) {
                Text("保存", color = iOSAccent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color(0xFF8E8E93))
            }
        }
    )
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 10000) {
        String.format("%.2f万", amount / 10000)
    } else {
        String.format("%.2f", amount)
    }
}

/**
 * 目标详情UI模型
 */
data class GoalDetailUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: Long?,
    val note: String,
    val isCompleted: Boolean,
    val createdAt: Long
)

/**
 * 存款记录
 */
data class DepositRecord(
    val id: Long,
    val amount: Double,
    val isDeposit: Boolean,
    val date: Long
)

/**
 * 目标详情ViewModel
 */
@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    private var currentGoalId: Long = 0

    fun loadGoal(goalId: Long) {
        currentGoalId = goalId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val goal = goalRepository.getGoalById(goalId)
            if (goal != null) {
                _uiState.value = GoalDetailUiState(
                    goal = GoalDetailUiModel(
                        id = goal.id,
                        name = goal.name,
                        icon = goal.icon,
                        targetAmount = goal.targetAmount,
                        currentAmount = goal.currentAmount,
                        deadline = goal.deadline,
                        note = goal.note,
                        isCompleted = goal.isCompleted,
                        createdAt = goal.createdAt
                    ),
                    depositHistory = emptyList(), // TODO: 从数据库加载存款历史
                    isLoading = false
                )
            } else {
                _uiState.value = GoalDetailUiState(isLoading = false)
            }
        }
    }

    fun addToGoal(amount: Double) {
        viewModelScope.launch {
            goalRepository.addToCurrentAmount(currentGoalId, amount)

            // 检查是否达成目标
            val goal = goalRepository.getGoalById(currentGoalId)
            if (goal != null && goal.currentAmount >= goal.targetAmount) {
                goalRepository.markGoalCompleted(currentGoalId)
            }

            loadGoal(currentGoalId)
        }
    }

    fun withdrawFromGoal(amount: Double) {
        viewModelScope.launch {
            goalRepository.addToCurrentAmount(currentGoalId, -amount)
            loadGoal(currentGoalId)
        }
    }

    fun updateGoal(name: String, targetAmount: Double, note: String) {
        viewModelScope.launch {
            val goal = goalRepository.getGoalById(currentGoalId)
            if (goal != null) {
                goalRepository.updateGoal(
                    goal.copy(
                        name = name,
                        targetAmount = targetAmount,
                        note = note,
                        isCompleted = goal.currentAmount >= targetAmount
                    )
                )
                loadGoal(currentGoalId)
            }
        }
    }

    fun deleteGoal() {
        viewModelScope.launch {
            val goal = goalRepository.getGoalById(currentGoalId)
            if (goal != null) {
                goalRepository.deleteGoal(goal)
            }
        }
    }
}

/**
 * 目标详情UI状态
 */
data class GoalDetailUiState(
    val goal: GoalDetailUiModel? = null,
    val depositHistory: List<DepositRecord> = emptyList(),
    val isLoading: Boolean = true
)
