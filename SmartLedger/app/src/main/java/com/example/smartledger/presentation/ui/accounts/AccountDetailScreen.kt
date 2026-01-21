package com.example.smartledger.presentation.ui.accounts

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.TransactionRepository
import com.example.smartledger.utils.toColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
 * 账户详情页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditBalanceDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        IOSAlertDialog(
            icon = "🗑️",
            title = "确认删除",
            message = "删除账户后，相关的交易记录不会被删除。确定要删除吗？",
            confirmText = "删除",
            confirmColor = iOSRed,
            onConfirm = {
                viewModel.deleteAccount()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // 编辑余额对话框
    if (showEditBalanceDialog && uiState.account != null) {
        EditBalanceDialog(
            currentBalance = uiState.account!!.balance,
            onConfirm = { newBalance ->
                viewModel.updateBalance(newBalance)
                showEditBalanceDialog = false
            },
            onDismiss = { showEditBalanceDialog = false }
        )
    }

    // 编辑名称对话框
    if (showEditNameDialog && uiState.account != null) {
        EditNameDialog(
            currentName = uiState.account!!.name,
            onConfirm = { newName ->
                viewModel.updateAccountName(newName)
                showEditNameDialog = false
            },
            onDismiss = { showEditNameDialog = false }
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
        } else if (uiState.account == null) {
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
                        text = "账户不存在",
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
                        title = "💳 账户详情",
                        onBack = onNavigateBack,
                        onDelete = { showDeleteDialog = true }
                    )
                }

                // 账户信息卡片
                item {
                    AccountInfoCard(
                        account = uiState.account!!,
                        onEditBalance = { showEditBalanceDialog = true },
                        onEditName = { showEditNameDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 本月统计
                item {
                    MonthlyStatisticsCard(
                        income = uiState.account!!.monthlyIncome,
                        expense = uiState.account!!.monthlyExpense,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 余额趋势图
                if (uiState.balanceHistory.isNotEmpty()) {
                    item {
                        BalanceTrendCard(
                            history = uiState.balanceHistory,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 收支占比
                item {
                    IncomeExpenseRatioCard(
                        income = uiState.account!!.monthlyIncome,
                        expense = uiState.account!!.monthlyExpense,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 最近交易
                if (uiState.recentTransactions.isNotEmpty()) {
                    item {
                        Text(
                            text = "📝 最近交易",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    items(uiState.recentTransactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    item {
                        EmptyTransactionsCard(
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 账户信息详情
                item {
                    AccountDetailsCard(
                        account = uiState.account!!,
                        createdDays = uiState.createdDays,
                        transactionCount = uiState.transactionCount,
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

/**
 * 账户信息卡片
 */
@Composable
private fun AccountInfoCard(
    account: AccountDetailUiModel,
    onEditBalance: () -> Unit,
    onEditName: () -> Unit,
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 账户图标
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = account.icon, fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 账户名称（可编辑）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onEditName)
            ) {
                Text(
                    text = account.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "✏️", fontSize = 14.sp)
            }

            Text(
                text = account.typeName,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 余额（可编辑）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onEditBalance)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "当前余额",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¥ ${String.format("%,.2f", account.balance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "✏️", fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * 本月统计卡片
 */
@Composable
private fun MonthlyStatisticsCard(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val netIncome = income - expense

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
                text = "📊 本月统计",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = "📈",
                    label = "收入",
                    amount = income,
                    color = iOSGreen
                )
                StatItem(
                    icon = "📉",
                    label = "支出",
                    amount = expense,
                    color = iOSOrange
                )
                StatItem(
                    icon = "💰",
                    label = "净收入",
                    amount = netIncome,
                    color = if (netIncome >= 0) iOSGreen else iOSRed
                )
            }
        }
    }
}

@Composable
private fun StatItem(
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
            text = "¥${String.format("%.0f", amount)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 余额趋势卡片
 */
@Composable
private fun BalanceTrendCard(
    history: List<BalanceHistoryItem>,
    modifier: Modifier = Modifier
) {
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
                text = "📈 余额趋势",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 简单的折线图
            if (history.size >= 2) {
                val maxBalance = history.maxOf { it.balance }
                val minBalance = history.minOf { it.balance }
                val range = (maxBalance - minBalance).coerceAtLeast(1.0)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (history.size - 1).coerceAtLeast(1)

                    val path = Path()
                    history.forEachIndexed { index, item ->
                        val x = index * stepX
                        val y = height - ((item.balance - minBalance) / range * height).toFloat()

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = iOSAccent,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 绘制数据点
                    history.forEachIndexed { index, _ ->
                        val x = index * stepX
                        val item = history[index]
                        val y = height - ((item.balance - minBalance) / range * height).toFloat()
                        drawCircle(
                            color = iOSAccent,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 日期标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = history.firstOrNull()?.dateLabel ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                    Text(
                        text = history.lastOrNull()?.dateLabel ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        }
    }
}

/**
 * 收支占比卡片
 */
@Composable
private fun IncomeExpenseRatioCard(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val total = income + expense
    val incomeRatio = if (total > 0) (income / total).toFloat() else 0.5f

    var animatedRatio by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedRatio,
        animationSpec = tween(1000),
        label = "ratio"
    )

    LaunchedEffect(incomeRatio) {
        animatedRatio = incomeRatio
    }

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
                text = "⚖️ 收支占比",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iOSOrange.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedValue)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iOSGreen)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(iOSGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "收入 ${String.format("%.1f", animatedValue * 100)}%",
                        fontSize = 14.sp,
                        color = Color(0xFF1C1C1E)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(iOSOrange)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "支出 ${String.format("%.1f", (1 - animatedValue) * 100)}%",
                        fontSize = 14.sp,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }
        }
    }
}

/**
 * 交易项
 */
@Composable
private fun TransactionItem(
    transaction: AccountTransactionUiModel,
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
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (transaction.isExpense)
                                iOSOrange.copy(alpha = 0.15f)
                            else
                                iOSGreen.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (transaction.isExpense) "📉" else "📈",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = dateFormat.format(Date(transaction.date)),
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }

            Text(
                text = "${if (transaction.isExpense) "-" else "+"}¥${String.format("%.2f", transaction.amount)}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.isExpense) iOSOrange else iOSGreen
            )
        }
    }
}

/**
 * 空交易提示
 */
@Composable
private fun EmptyTransactionsCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(iOSCardBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📭", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "暂无交易记录",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93)
            )
        }
    }
}

/**
 * 账户详情卡片
 */
@Composable
private fun AccountDetailsCard(
    account: AccountDetailUiModel,
    createdDays: Int,
    transactionCount: Int,
    modifier: Modifier = Modifier
) {
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
                text = "ℹ️ 账户信息",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(icon = "🏷️", label = "账户类型", value = account.typeName)
            DetailRow(icon = "📅", label = "使用天数", value = "${createdDays}天")
            DetailRow(icon = "📝", label = "交易笔数", value = "${transactionCount}笔")
            DetailRow(
                icon = "💵",
                label = "本月净流入",
                value = "¥${String.format("%.2f", account.monthlyIncome - account.monthlyExpense)}"
            )
        }
    }
}

@Composable
private fun DetailRow(
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
 * 编辑余额对话框
 */
@Composable
private fun EditBalanceDialog(
    currentBalance: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var balanceText by remember { mutableStateOf(currentBalance.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "💰 修改余额",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("新余额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iOSAccent,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    balanceText.toDoubleOrNull()?.let { onConfirm(it) }
                }
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

/**
 * 编辑名称对话框
 */
@Composable
private fun EditNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameText by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "✏️ 修改名称",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("账户名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iOSAccent,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (nameText.isNotBlank()) onConfirm(nameText) },
                enabled = nameText.isNotBlank()
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

/**
 * 账户详情UI模型
 */
data class AccountDetailUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val typeName: String,
    val balance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double
)

/**
 * 账户交易UI模型
 */
data class AccountTransactionUiModel(
    val id: Long,
    val categoryName: String,
    val amount: Double,
    val isExpense: Boolean,
    val date: Long
)

/**
 * 余额历史项
 */
data class BalanceHistoryItem(
    val date: Long,
    val balance: Double,
    val dateLabel: String
)

/**
 * 账户详情ViewModel
 */
@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    private var currentAccountId: Long = 0

    fun loadAccount(accountId: Long) {
        currentAccountId = accountId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val account = accountRepository.getAccountById(accountId)
            if (account != null) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                val monthEnd = calendar.timeInMillis

                val transactions = transactionRepository
                    .getTransactionsByAccount(accountId, monthStart, monthEnd)
                    .first()

                val monthlyIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }

                val monthlyExpense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val recentTransactions = transactions.take(10).map { tx ->
                    AccountTransactionUiModel(
                        id = tx.id,
                        categoryName = "交易",
                        amount = tx.amount,
                        isExpense = tx.type == TransactionType.EXPENSE,
                        date = tx.date
                    )
                }

                // 生成余额历史（模拟过去7天）
                val balanceHistory = generateBalanceHistory(account.balance, monthlyExpense)

                // 计算使用天数
                val createdDays = ((System.currentTimeMillis() - account.createdAt) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)

                val typeName = when (account.type.name) {
                    "CASH" -> "现金"
                    "BANK" -> "银行卡"
                    "DEBIT_CARD" -> "储蓄卡"
                    "CREDIT_CARD" -> "信用卡"
                    "ALIPAY" -> "支付宝"
                    "WECHAT" -> "微信"
                    "INVESTMENT_STOCK" -> "股票"
                    "INVESTMENT_FUND" -> "基金"
                    "INVESTMENT_DEPOSIT" -> "定期存款"
                    else -> "其他"
                }

                _uiState.value = AccountDetailUiState(
                    account = AccountDetailUiModel(
                        id = account.id,
                        name = account.name,
                        icon = account.icon,
                        color = account.color,
                        typeName = typeName,
                        balance = account.balance,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense
                    ),
                    recentTransactions = recentTransactions,
                    balanceHistory = balanceHistory,
                    createdDays = createdDays,
                    transactionCount = transactions.size,
                    isLoading = false
                )
            } else {
                _uiState.value = AccountDetailUiState(isLoading = false)
            }
        }
    }

    private fun generateBalanceHistory(currentBalance: Double, monthlyExpense: Double): List<BalanceHistoryItem> {
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val history = mutableListOf<BalanceHistoryItem>()

        // 模拟过去7天的余额变化
        var balance = currentBalance
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dailyChange = monthlyExpense / 30 * (Math.random() * 2)
            balance -= dailyChange
            history.add(
                BalanceHistoryItem(
                    date = calendar.timeInMillis,
                    balance = balance.coerceAtLeast(0.0),
                    dateLabel = dateFormat.format(calendar.time)
                )
            )
        }
        // 最后一个是当前余额
        history[history.lastIndex] = history.last().copy(balance = currentBalance)

        return history
    }

    fun updateBalance(newBalance: Double) {
        viewModelScope.launch {
            accountRepository.setBalance(currentAccountId, newBalance)
            loadAccount(currentAccountId)
        }
    }

    fun updateAccountName(newName: String) {
        viewModelScope.launch {
            val account = accountRepository.getAccountById(currentAccountId)
            if (account != null) {
                accountRepository.updateAccount(account.copy(name = newName))
                loadAccount(currentAccountId)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val account = accountRepository.getAccountById(currentAccountId)
            if (account != null) {
                accountRepository.deleteAccount(account)
            }
        }
    }
}

/**
 * 账户详情UI状态
 */
data class AccountDetailUiState(
    val account: AccountDetailUiModel? = null,
    val recentTransactions: List<AccountTransactionUiModel> = emptyList(),
    val balanceHistory: List<BalanceHistoryItem> = emptyList(),
    val createdDays: Int = 0,
    val transactionCount: Int = 0,
    val isLoading: Boolean = true
)
