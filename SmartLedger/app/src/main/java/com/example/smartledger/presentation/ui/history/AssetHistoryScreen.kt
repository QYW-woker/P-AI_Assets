package com.example.smartledger.presentation.ui.history

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.local.entity.MonthlySnapshotEntity
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssetHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史资产记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createCurrentSnapshot() },
                containerColor = AppColors.Accent
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "创建快照", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.snapshots.isEmpty()) {
            EmptyState(
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
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                // 年份筛选
                item {
                    YearFilter(
                        years = viewModel.getAvailableYears(),
                        selectedYear = uiState.selectedYear,
                        onYearSelected = { viewModel.filterByYear(it) }
                    )
                }

                // 当前选中快照详情
                uiState.selectedSnapshot?.let { snapshot ->
                    item {
                        SnapshotDetailCard(
                            snapshot = snapshot,
                            previousSnapshot = uiState.previousSnapshot,
                            currencyFormat = currencyFormat,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }

                    // 账户明细
                    item {
                        AccountsDetailCard(
                            accountsJson = snapshot.accountsJson,
                            viewModel = viewModel,
                            currencyFormat = currencyFormat,
                            modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                        )
                    }
                }

                // 历史记录列表
                item {
                    Text(
                        text = "历史记录",
                        style = AppTypography.TitleMedium,
                        modifier = Modifier.padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.PaddingS)
                    )
                }

                items(uiState.filteredSnapshots) { snapshot ->
                    SnapshotListItem(
                        snapshot = snapshot,
                        isSelected = snapshot.id == uiState.selectedSnapshot?.id,
                        currencyFormat = currencyFormat,
                        onClick = { viewModel.selectSnapshot(snapshot) },
                        modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearFilter(
    years: List<Int>,
    selectedYear: Int?,
    onYearSelected: (Int?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.PaddingS),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedYear == null,
                onClick = { onYearSelected(null) },
                label = { Text("全部") }
            )
        }
        items(years) { year ->
            FilterChip(
                selected = selectedYear == year,
                onClick = { onYearSelected(year) },
                label = { Text("${year}年") }
            )
        }
    }
}

@Composable
private fun SnapshotDetailCard(
    snapshot: MonthlySnapshotEntity,
    previousSnapshot: MonthlySnapshotEntity?,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.PaddingL)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = AppColors.Accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${snapshot.year}年${snapshot.month}月",
                        style = AppTypography.TitleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                        .format(Date(snapshot.snapshotDate)),
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // 净资产
            Text(
                text = "净资产",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormat.format(snapshot.netWorth),
                    style = AppTypography.TitleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                previousSnapshot?.let {
                    val change = snapshot.netWorth - it.netWorth
                    val changePercent = if (it.netWorth > 0) (change / it.netWorth * 100) else 0.0
                    ChangeIndicator(
                        change = change,
                        changePercent = changePercent,
                        currencyFormat = currencyFormat,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // 资产分布
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "现金资产",
                    value = currencyFormat.format(snapshot.cashAssets),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "投资资产",
                    value = currencyFormat.format(snapshot.investmentAssets),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "负债",
                    value = currencyFormat.format(snapshot.totalLiabilities),
                    valueColor = AppColors.Expense,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // 收支情况
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "本月收入",
                    value = currencyFormat.format(snapshot.monthlyIncome),
                    valueColor = AppColors.Income,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "本月支出",
                    value = currencyFormat.format(snapshot.monthlyExpense),
                    valueColor = AppColors.Expense,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "储蓄率",
                    value = String.format("%.1f%%", snapshot.savingsRate),
                    valueColor = if (snapshot.savingsRate >= 20) AppColors.Income else AppColors.TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }

            // 投资收益
            if (snapshot.investmentPrincipal > 0) {
                Spacer(modifier = Modifier.height(AppDimens.SpacingL))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "投资本金",
                        value = currencyFormat.format(snapshot.investmentPrincipal),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = "投资收益",
                        value = currencyFormat.format(snapshot.investmentReturn),
                        valueColor = if (snapshot.investmentReturn >= 0) AppColors.Income else AppColors.Expense,
                        modifier = Modifier.weight(1f)
                    )
                    val returnRate = if (snapshot.investmentPrincipal > 0) {
                        snapshot.investmentReturn / snapshot.investmentPrincipal * 100
                    } else 0.0
                    StatItem(
                        label = "收益率",
                        value = String.format("%.2f%%", returnRate),
                        valueColor = if (returnRate >= 0) AppColors.Income else AppColors.Expense,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountsDetailCard(
    accountsJson: String,
    viewModel: AssetHistoryViewModel,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    val accounts = viewModel.parseAccountSnapshots(accountsJson)
    if (accounts.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.PaddingL)
        ) {
            Text(
                text = "账户明细",
                style = AppTypography.TitleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            accounts.forEach { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = account.name,
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = currencyFormat.format(account.balance),
                        style = AppTypography.BodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapshotListItem(
    snapshot: MonthlySnapshotEntity,
    isSelected: Boolean,
    currencyFormat: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.AccentLight else AppColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.PaddingM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${snapshot.year}年${snapshot.month}月",
                    style = AppTypography.BodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "结余: ${currencyFormat.format(snapshot.monthlyBalance)}",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormat.format(snapshot.netWorth),
                    style = AppTypography.BodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "储蓄率 ${String.format("%.1f", snapshot.savingsRate)}%",
                    style = AppTypography.Caption,
                    color = if (snapshot.savingsRate >= 20) AppColors.Income else AppColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color = AppColors.TextPrimary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = AppTypography.Caption,
            color = AppColors.TextMuted
        )
        Text(
            text = value,
            style = AppTypography.BodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun ChangeIndicator(
    change: Double,
    changePercent: Double,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when {
        change > 0 -> Icons.Filled.TrendingUp to AppColors.Income
        change < 0 -> Icons.Filled.TrendingDown to AppColors.Expense
        else -> Icons.Default.TrendingFlat to AppColors.TextMuted
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = String.format("%+.1f%%", changePercent),
            style = AppTypography.Caption,
            color = color
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无历史记录",
                style = AppTypography.BodyLarge,
                color = AppColors.TextMuted
            )
            Text(
                text = "点击右下角按钮创建当月快照",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }
    }
}
