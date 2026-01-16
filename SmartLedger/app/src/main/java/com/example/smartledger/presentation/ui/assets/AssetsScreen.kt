package com.example.smartledger.presentation.ui.assets

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.smartledger.presentation.ui.components.GradientCard
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 资产页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    onNavigateToAccountDetail: (Long) -> Unit,
    onNavigateToAccountManage: () -> Unit,
    viewModel: AssetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("资产", "收支", "投资")

    Scaffold(
        topBar = {
            AppTopBar(title = "资产")
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
        ) {
            // 模块Tab
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = AppColors.Background,
                    contentColor = AppColors.Accent,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(3.dp)
                                .padding(horizontal = 32.dp)
                                .clip(AppShapes.Full)
                                .background(AppColors.Accent)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = AppTypography.LabelLarge,
                                    color = if (selectedTabIndex == index) AppColors.Accent else AppColors.TextMuted
                                )
                            }
                        )
                    }
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    // 资产模块
                    item {
                        HealthScoreCard(
                            score = uiState.healthScore,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.PaddingL),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "我的账户",
                                style = AppTypography.TitleSmall,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "管理",
                                style = AppTypography.LabelMedium,
                                color = AppColors.Accent
                            )
                        }
                    }

                    items(uiState.accounts) { account ->
                        AccountItem(
                            account = account,
                            onClick = { onNavigateToAccountDetail(account.id) },
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }
                }

                1 -> {
                    // 收支模块
                    item {
                        IncomeExpenseOverview(
                            income = uiState.monthlyIncome,
                            expense = uiState.monthlyExpense,
                            savingsRate = uiState.savingsRate,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }
                }

                2 -> {
                    // 投资模块
                    item {
                        InvestmentOverview(
                            principal = uiState.investmentPrincipal,
                            currentValue = uiState.investmentCurrentValue,
                            totalReturn = uiState.investmentReturn,
                            returnRate = uiState.investmentReturnRate,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
            }
        }
    }
}

/**
 * 健康评分卡片
 */
@Composable
private fun HealthScoreCard(
    score: Int,
    modifier: Modifier = Modifier
) {
    GradientCard(
        modifier = modifier.fillMaxWidth(),
        gradientColors = AppColors.GradientAssetCard
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "财务健康分",
                    style = AppTypography.BodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = score.toString(),
                        style = AppTypography.NumberLarge,
                        color = Color.White
                    )
                    Text(
                        text = " / 100",
                        style = AppTypography.BodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Text(
                    text = getHealthScoreDescription(score),
                    style = AppTypography.LabelSmall,
                    color = AppColors.Success
                )
            }

            // 圆环进度
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // TODO: 添加圆环进度指示器
                Text(
                    text = "${score}%",
                    style = AppTypography.TitleMedium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 账户项
 */
@Composable
private fun AccountItem(
    account: AccountUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
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
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(account.color))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = account.icon, style = AppTypography.BodyLarge)
                }
                Spacer(modifier = Modifier.width(AppDimens.SpacingM))
                Column {
                    Text(
                        text = account.name,
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = account.typeName,
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¥${String.format("%.2f", account.balance)}",
                    style = AppTypography.NumberSmall,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.TextMuted
                )
            }
        }
    }
}

/**
 * 收支概览
 */
@Composable
private fun IncomeExpenseOverview(
    income: Double,
    expense: Double,
    savingsRate: Float,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "本月收支",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "收入",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", income)}",
                        style = AppTypography.NumberMedium,
                        color = AppColors.Success
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "支出",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", expense)}",
                        style = AppTypography.NumberMedium,
                        color = AppColors.Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "储蓄率",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "${String.format("%.1f", savingsRate * 100)}%",
                    style = AppTypography.NumberSmall,
                    color = if (savingsRate >= 0.2) AppColors.Success else AppColors.Warning
                )
            }
        }
    }
}

/**
 * 投资概览
 */
@Composable
private fun InvestmentOverview(
    principal: Double,
    currentValue: Double,
    totalReturn: Double,
    returnRate: Float,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "投资概览",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总本金",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", principal)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "当前市值",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "¥${String.format("%.2f", currentValue)}",
                        style = AppTypography.NumberSmall,
                        color = AppColors.TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "总收益",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    Text(
                        text = "${if (totalReturn >= 0) "+" else ""}¥${String.format("%.2f", totalReturn)}",
                        style = AppTypography.NumberSmall,
                        color = if (totalReturn >= 0) AppColors.Success else AppColors.Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Text(
                text = "收益率: ${if (returnRate >= 0) "+" else ""}${String.format("%.2f", returnRate * 100)}%",
                style = AppTypography.LabelMedium,
                color = if (returnRate >= 0) AppColors.Success else AppColors.Accent
            )
        }
    }
}

private fun getHealthScoreDescription(score: Int): String {
    return when {
        score >= 90 -> "财务状况优秀"
        score >= 70 -> "财务状况良好"
        score >= 50 -> "财务状况一般"
        else -> "需要改善财务状况"
    }
}

/**
 * 账户UI模型
 */
data class AccountUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val typeName: String,
    val balance: Double
)
