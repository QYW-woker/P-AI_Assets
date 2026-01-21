package com.example.smartledger.presentation.ui.budget

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
import com.example.smartledger.utils.toColor

// iOS风格颜色
private val iOSBackground = Color(0xFFF2F2F7)
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSRed = Color(0xFFFF3B30)

/**
 * 预算页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddBudget: () -> Unit = {},
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    // 添加预算对话框
    if (showAddBudgetDialog) {
        AddBudgetDialog(
            categories = expenseCategories,
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { categoryId, amount ->
                if (categoryId == null) {
                    viewModel.addTotalBudget(amount)
                } else {
                    viewModel.addCategoryBudget(categoryId, amount)
                }
                showAddBudgetDialog = false
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
            // 顶部栏
            item {
                IOSTopBar(
                    title = "💰 预算管理",
                    onBack = onNavigateBack
                )
            }

            // 空状态
            if (uiState.budgets.isEmpty() && uiState.totalBudget == null) {
                item {
                    EmptyBudgetCard(
                        onAddClick = { showAddBudgetDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                // 总预算卡片
                uiState.totalBudget?.let { totalBudget ->
                    item {
                        TotalBudgetCard(
                            budget = totalBudget,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 预算建议
                item {
                    BudgetTipsCard(
                        totalBudget = uiState.totalBudget,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 分类预算标题
                if (uiState.budgets.isNotEmpty()) {
                    item {
                        Text(
                            text = "🏷️ 分类预算",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    items(uiState.budgets) { budget ->
                        CategoryBudgetItem(
                            budget = budget,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            // 添加预算按钮
            item {
                AddBudgetButton(
                    onClick = { showAddBudgetDialog = true },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
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
    onBack: () -> Unit
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
                Text(
                    text = "←",
                    fontSize = 18.sp,
                    color = Color(0xFF8E8E93)
                )
            }

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

/**
 * 总预算卡片
 */
@Composable
private fun TotalBudgetCard(
    budget: BudgetUiModel,
    modifier: Modifier = Modifier
) {
    val progress = if (budget.amount > 0) (budget.used / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val progressColor = when {
        progress < 0.6f -> iOSGreen
        progress < 0.8f -> iOSOrange
        else -> iOSRed
    }

    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000),
        label = "progress"
    )

    LaunchedEffect(progress) {
        animatedProgress = progress
    }

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📊", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "本月总预算",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¥${String.format("%,.2f", budget.amount)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "已用",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "¥${String.format("%.0f", budget.used)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "剩余",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "¥${String.format("%.0f", budget.remaining)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "日均可用",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "¥${String.format("%.0f", budget.dailyAvailable)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 右侧进度圆环
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val strokeWidth = 10.dp.toPx()
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "已使用",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 预算提示卡片
 */
@Composable
private fun BudgetTipsCard(
    totalBudget: BudgetUiModel?,
    modifier: Modifier = Modifier
) {
    val progress = totalBudget?.let {
        if (it.amount > 0) (it.used / it.amount).toFloat() else 0f
    } ?: 0f

    val (icon, message, bgColor) = when {
        totalBudget == null -> Triple("💡", "设置预算，轻松掌控财务状况", iOSAccent)
        progress < 0.5f -> Triple("😊", "预算充裕，保持良好消费习惯", iOSGreen)
        progress < 0.8f -> Triple("⚠️", "预算已过半，注意控制支出", iOSOrange)
        progress < 1f -> Triple("😰", "预算即将用完，请节制消费", iOSRed)
        else -> Triple("🚨", "预算已超支，请立即调整", iOSRed)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = bgColor
            )
        }
    }
}

/**
 * 分类预算项
 */
@Composable
private fun CategoryBudgetItem(
    budget: BudgetUiModel,
    modifier: Modifier = Modifier
) {
    val progress = if (budget.amount > 0) (budget.used / budget.amount).toFloat().coerceIn(0f, 1.2f) else 0f
    val progressColor = when {
        progress < 0.6f -> iOSGreen
        progress < 0.8f -> iOSOrange
        else -> iOSRed
    }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background((budget.categoryColor ?: "#ECEFF1").toColor().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = budget.categoryIcon ?: "💰",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = budget.categoryName ?: "总预算",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "¥${String.format("%.0f", budget.used)}",
                            fontSize = 14.sp,
                            color = progressColor
                        )
                        Text(
                            text = " / ¥${String.format("%.0f", budget.amount)}",
                            fontSize = 14.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progress.coerceAtMost(1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = Color(0xFFE5E5EA),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.0f", progress * 100)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor
                    )
                }

                if (budget.remaining < 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ 已超支 ¥${String.format("%.0f", -budget.remaining)}",
                        fontSize = 12.sp,
                        color = iOSRed
                    )
                }
            }
        }
    }
}

/**
 * 添加预算按钮
 */
@Composable
private fun AddBudgetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSAccent)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "➕", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加预算",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * 空预算提示卡片
 */
@Composable
private fun EmptyBudgetCard(
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
            Text(text = "💰", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "开始管理您的预算",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "设置预算限额，追踪消费情况\n轻松实现财务目标",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(iOSAccent)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "➕", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "设置预算",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 预算UI模型
 */
data class BudgetUiModel(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val amount: Double,
    val used: Double,
    val remaining: Double,
    val dailyAvailable: Double
)
