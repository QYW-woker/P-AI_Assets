package com.example.smartledger.presentation.ui.transactions

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography
import com.example.smartledger.utils.toColor

/**
 * 交易列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "账目明细",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "搜索",
                            tint = AppColors.TextPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 月份选择器和汇总
            MonthSummaryHeader(
                monthTitle = uiState.monthTitle,
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                balance = uiState.balance,
                onPreviousMonth = { viewModel.goToPreviousMonth() },
                onNextMonth = { viewModel.goToNextMonth() }
            )

            // 类型筛选
            FilterRow(
                selectedType = uiState.filterType,
                onTypeSelected = { viewModel.filterByType(it) }
            )

            // 交易列表
            if (uiState.transactionGroups.isEmpty()) {
                EmptyTransactionsState()
            } else {
                val filteredGroups = if (uiState.filterType != null) {
                    uiState.transactionGroups.mapNotNull { group ->
                        val filteredTransactions = group.transactions.filter {
                            it.type == uiState.filterType
                        }
                        if (filteredTransactions.isNotEmpty()) {
                            group.copy(
                                transactions = filteredTransactions,
                                dayTotal = filteredTransactions.sumOf {
                                    if (it.type == TransactionType.EXPENSE) -it.amount else it.amount
                                }
                            )
                        } else null
                    }
                } else {
                    uiState.transactionGroups
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    filteredGroups.forEach { group ->
                        item {
                            DateHeader(
                                date = group.date,
                                dayTotal = group.dayTotal
                            )
                        }

                        items(group.transactions) { transaction ->
                            TransactionItemCard(
                                transaction = transaction,
                                onClick = { onNavigateToTransactionDetail(transaction.id) },
                                onDelete = { viewModel.deleteTransaction(transaction.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSummaryHeader(
    monthTitle: String,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimens.PaddingL)
    ) {
        Column {
            // 月份导航
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "上个月",
                        tint = AppColors.TextSecondary
                    )
                }

                Text(
                    text = monthTitle,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下个月",
                        tint = AppColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // 汇总数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "收入",
                    amount = totalIncome,
                    color = AppColors.Success
                )
                SummaryItem(
                    label = "支出",
                    amount = totalExpense,
                    color = AppColors.Accent
                )
                SummaryItem(
                    label = "结余",
                    amount = balance,
                    color = if (balance >= 0) AppColors.Info else AppColors.Accent
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
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
            text = "¥${String.format("%.2f", amount)}",
            style = AppTypography.NumberSmall,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("全部", style = AppTypography.LabelSmall) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AppColors.Primary,
                selectedLabelColor = Color.White,
                containerColor = AppColors.Card,
                labelColor = AppColors.TextSecondary
            )
        )

        FilterChip(
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            label = { Text("支出", style = AppTypography.LabelSmall) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AppColors.Accent,
                selectedLabelColor = Color.White,
                containerColor = AppColors.Card,
                labelColor = AppColors.TextSecondary
            )
        )

        FilterChip(
            selected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            label = { Text("收入", style = AppTypography.LabelSmall) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AppColors.Success,
                selectedLabelColor = Color.White,
                containerColor = AppColors.Card,
                labelColor = AppColors.TextSecondary
            )
        )
    }
}

@Composable
private fun DateHeader(
    date: String,
    dayTotal: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppDimens.PaddingL,
                vertical = AppDimens.SpacingS
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = date,
            style = AppTypography.LabelMedium,
            color = AppColors.TextSecondary
        )
        Text(
            text = "${if (dayTotal >= 0) "+" else ""}¥${String.format("%.2f", dayTotal)}",
            style = AppTypography.LabelMedium,
            color = if (dayTotal >= 0) AppColors.Success else AppColors.Accent
        )
    }
}

@Composable
private fun TransactionItemCard(
    transaction: TransactionItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = try {
        transaction.categoryColor.toColor()
    } catch (e: Exception) {
        AppColors.Primary
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.categoryIcon,
                    style = AppTypography.BodyMedium
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName,
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextPrimary
                )
                Row {
                    Text(
                        text = transaction.time,
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                    if (transaction.note.isNotBlank()) {
                        Text(
                            text = " · ${transaction.note}",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}¥${String.format("%.2f", transaction.amount)}",
                style = AppTypography.NumberSmall,
                color = if (transaction.type == TransactionType.EXPENSE) AppColors.Accent else AppColors.Success
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = AppColors.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📋",
                style = AppTypography.NumberLarge
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            Text(
                text = "本月暂无记录",
                style = AppTypography.TitleSmall,
                color = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = "开始记录您的第一笔账目吧",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }
    }
}
