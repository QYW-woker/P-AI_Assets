package com.example.smartledger.presentation.ui.goals

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.components.NoGoalsState
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 目标页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddGoal: () -> Unit = {}, // 保留参数以兼容旧代码，但不再使用
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
        topBar = {
            AppTopBarWithBack(
                title = "储蓄目标",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = AppColors.Accent,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加目标"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.goals.isEmpty()) {
            NoGoalsState(
                onAddGoal = { showAddGoalDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(AppDimens.PaddingL)
            ) {
                items(uiState.goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onNavigateToGoalDetail(goal.id) }
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
 * 目标卡片
 */
@Composable
private fun GoalCard(
    goal: GoalUiModel,
    onClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppColors.AccentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goal.icon,
                        style = AppTypography.TitleMedium
                    )
                }

                Spacer(modifier = Modifier.width(AppDimens.SpacingM))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = AppTypography.TitleSmall,
                        color = AppColors.TextPrimary
                    )
                    goal.deadline?.let {
                        Text(
                            text = "截止日期: $it",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                    }
                }

                Text(
                    text = "${String.format("%.0f", progress * 100)}%",
                    style = AppTypography.NumberSmall,
                    color = AppColors.Accent
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.ProgressBarHeight)
                    .clip(AppShapes.Full),
                color = AppColors.Accent,
                trackColor = AppColors.Border
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "已存入",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${formatAmount(goal.currentAmount)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.Success
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "目标金额",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${formatAmount(goal.targetAmount)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.TextPrimary
                    )
                }
            }

            goal.estimatedCompletion?.let { estimated ->
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Text(
                    text = "预计完成: $estimated",
                    style = AppTypography.Caption,
                    color = AppColors.Info
                )
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
