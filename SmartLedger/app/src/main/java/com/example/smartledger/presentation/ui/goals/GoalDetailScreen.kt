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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.domain.repository.GoalRepository
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 目标详情页面
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

    LaunchedEffect(goalId) {
        viewModel.loadGoal(goalId)
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除", style = AppTypography.TitleMedium) },
            text = { Text("确定要删除这个储蓄目标吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal()
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
                    Text("删除", color = AppColors.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消", color = AppColors.TextMuted)
                }
            }
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

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "目标详情",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = AppColors.Accent
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.goal != null && !uiState.goal!!.isCompleted) {
                FloatingActionButton(
                    onClick = { showDepositDialog = true },
                    containerColor = AppColors.Success,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "存入"
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Accent)
                }
            }
            uiState.goal == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("目标不存在", color = AppColors.TextMuted)
                }
            }
            else -> {
                GoalDetailContent(
                    goal = uiState.goal!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.Background)
                        .padding(paddingValues)
                        .padding(AppDimens.PaddingL)
                )
            }
        }
    }
}

@Composable
private fun GoalDetailContent(
    goal: GoalDetailUiModel,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    Column(modifier = modifier) {
        // 主卡片
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AppColors.AccentLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goal.icon,
                        style = AppTypography.TitleLarge
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                Text(
                    text = goal.name,
                    style = AppTypography.TitleMedium,
                    color = AppColors.TextPrimary
                )

                if (goal.isCompleted) {
                    Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                    Text(
                        text = "🎉 已完成",
                        style = AppTypography.LabelMedium,
                        color = AppColors.Success
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 进度条
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(AppShapes.Full),
                    color = if (goal.isCompleted) AppColors.Success else AppColors.Accent,
                    trackColor = AppColors.Border
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                Text(
                    text = "${String.format("%.1f", progress * 100)}%",
                    style = AppTypography.NumberMedium,
                    color = AppColors.Accent
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 金额信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "已存入",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${formatAmount(goal.currentAmount)}",
                            style = AppTypography.NumberMedium,
                            color = AppColors.Success
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "还需存入",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${formatAmount((goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0))}",
                            style = AppTypography.NumberMedium,
                            color = AppColors.Accent
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "目标金额",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¥${formatAmount(goal.targetAmount)}",
                            style = AppTypography.NumberMedium,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AppDimens.SpacingL))

        // 详情信息
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                if (goal.deadline != null) {
                    DetailRow(label = "截止日期", value = dateFormat.format(Date(goal.deadline)))
                }
                DetailRow(label = "创建时间", value = dateFormat.format(Date(goal.createdAt)))
                if (goal.note.isNotEmpty()) {
                    DetailRow(label = "备注", value = goal.note)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.SpacingS),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypography.BodyMedium,
            color = AppColors.TextMuted
        )
        Text(
            text = value,
            style = AppTypography.BodyMedium,
            color = AppColors.TextPrimary
        )
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

            // 重新加载
            loadGoal(currentGoalId)
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
    val isLoading: Boolean = true
)
