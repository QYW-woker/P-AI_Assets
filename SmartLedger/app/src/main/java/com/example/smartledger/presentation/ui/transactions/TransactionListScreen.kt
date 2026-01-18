package com.example.smartledger.presentation.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
 * 交易列表页面 - 增强版，支持多维度筛选
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
    val filterOptions by viewModel.filterOptions.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "账目明细",
                onBackClick = onNavigateBack,
                actions = {
                    // 筛选按钮
                    IconButton(onClick = { showFilterSheet = true }) {
                        Box {
                            Text(
                                text = "⚙️",
                                style = AppTypography.BodyMedium
                            )
                            if (uiState.isFiltered) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Accent)
                                )
                            }
                        }
                    }
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
            // 时间周期选择器
            TimePeriodSelector(
                selectedPeriod = uiState.timePeriod,
                onPeriodSelected = { period ->
                    if (period == TimePeriod.CUSTOM) {
                        showDateRangePicker = true
                    } else {
                        viewModel.setTimePeriod(period)
                    }
                }
            )

            // 月份选择器和汇总（仅月份模式显示月份导航）
            MonthSummaryHeader(
                monthTitle = uiState.monthTitle,
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                balance = uiState.balance,
                showNavigation = uiState.timePeriod == TimePeriod.MONTH,
                onPreviousMonth = { viewModel.goToPreviousMonth() },
                onNextMonth = { viewModel.goToNextMonth() }
            )

            // 类型筛选
            FilterRow(
                selectedType = uiState.filterType,
                onTypeSelected = { viewModel.filterByType(it) }
            )

            // 筛选状态指示和统计
            if (uiState.isFiltered) {
                FilterStatusBar(
                    filterStats = uiState.filterStats,
                    onClearFilters = { viewModel.clearAllFilters() }
                )
            }

            // 交易列表
            if (uiState.transactionGroups.isEmpty()) {
                EmptyTransactionsState(isFiltered = uiState.isFiltered)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    uiState.transactionGroups.forEach { group ->
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

    // 高级筛选底部弹窗
    if (showFilterSheet) {
        AdvancedFilterSheet(
            filterOptions = filterOptions,
            currentCategoryIds = uiState.filterCategoryIds,
            currentAccountIds = uiState.filterAccountIds,
            currentMinAmount = uiState.minAmount,
            currentMaxAmount = uiState.maxAmount,
            currentKeyword = uiState.searchKeyword,
            onDismiss = { showFilterSheet = false },
            onApplyFilters = { categoryIds, accountIds, minAmount, maxAmount, keyword ->
                viewModel.filterByCategories(categoryIds)
                viewModel.filterByAccounts(accountIds)
                viewModel.filterByAmountRange(minAmount, maxAmount)
                viewModel.filterByKeyword(keyword)
                showFilterSheet = false
            }
        )
    }

    // 自定义日期范围对话框
    if (showDateRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showDateRangePicker = false },
            onConfirm = { startDate, endDate ->
                viewModel.setCustomDateRange(startDate, endDate)
                showDateRangePicker = false
            }
        )
    }
}

/**
 * 时间周期选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    val periods = listOf(
        TimePeriod.WEEK to "本周",
        TimePeriod.MONTH to "本月",
        TimePeriod.QUARTER to "本季",
        TimePeriod.YEAR to "本年",
        TimePeriod.ALL to "全部",
        TimePeriod.CUSTOM to "自定义"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
    ) {
        periods.forEach { (period, label) ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(label, style = AppTypography.LabelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.Card,
                    labelColor = AppColors.TextSecondary
                )
            )
        }
    }
}

/**
 * 筛选状态栏
 */
@Composable
private fun FilterStatusBar(
    filterStats: FilterStatistics,
    onClearFilters: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.SpacingS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "筛选结果: ${filterStats.totalCount}笔交易",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
                ) {
                    Text(
                        text = "收入 ¥${String.format("%.0f", filterStats.totalIncome)}",
                        style = AppTypography.Caption,
                        color = AppColors.Success
                    )
                    Text(
                        text = "支出 ¥${String.format("%.0f", filterStats.totalExpense)}",
                        style = AppTypography.Caption,
                        color = AppColors.Accent
                    )
                    Text(
                        text = "占比 ${String.format("%.1f", filterStats.percentOfTotal)}%",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }
            }

            TextButton(onClick = onClearFilters) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "清除筛选",
                    tint = AppColors.Accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "清除",
                    style = AppTypography.LabelSmall,
                    color = AppColors.Accent
                )
            }
        }
    }
}

/**
 * 高级筛选底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedFilterSheet(
    filterOptions: FilterOptions,
    currentCategoryIds: Set<Long>,
    currentAccountIds: Set<Long>,
    currentMinAmount: Double?,
    currentMaxAmount: Double?,
    currentKeyword: String,
    onDismiss: () -> Unit,
    onApplyFilters: (Set<Long>, Set<Long>, Double?, Double?, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategoryIds by remember { mutableStateOf(currentCategoryIds) }
    var selectedAccountIds by remember { mutableStateOf(currentAccountIds) }
    var minAmountText by remember { mutableStateOf(currentMinAmount?.toString() ?: "") }
    var maxAmountText by remember { mutableStateOf(currentMaxAmount?.toString() ?: "") }
    var keyword by remember { mutableStateOf(currentKeyword) }
    var activeTab by remember { mutableStateOf(0) }

    val tabs = listOf("分类", "账户", "金额", "关键词")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.PaddingL)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "高级筛选",
                    style = AppTypography.TitleMedium,
                    color = AppColors.TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "关闭",
                        tint = AppColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // Tab切换
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = AppColors.Primary,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[activeTab])
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(AppColors.Primary)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                style = AppTypography.LabelMedium,
                                color = if (activeTab == index) AppColors.Primary else AppColors.TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // Tab内容
            when (activeTab) {
                0 -> CategoryFilterContent(
                    categories = filterOptions.availableCategories,
                    selectedIds = selectedCategoryIds,
                    onSelectionChange = { selectedCategoryIds = it }
                )
                1 -> AccountFilterContent(
                    accounts = filterOptions.availableAccounts,
                    selectedIds = selectedAccountIds,
                    onSelectionChange = { selectedAccountIds = it }
                )
                2 -> AmountFilterContent(
                    minAmount = minAmountText,
                    maxAmount = maxAmountText,
                    onMinAmountChange = { minAmountText = it },
                    onMaxAmountChange = { maxAmountText = it }
                )
                3 -> KeywordFilterContent(
                    keyword = keyword,
                    onKeywordChange = { keyword = it }
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingXL))

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedCategoryIds = emptySet()
                        selectedAccountIds = emptySet()
                        minAmountText = ""
                        maxAmountText = ""
                        keyword = ""
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.TextSecondary
                    )
                ) {
                    Text("重置", style = AppTypography.LabelMedium)
                }

                Button(
                    onClick = {
                        val minAmount = minAmountText.toDoubleOrNull()
                        val maxAmount = maxAmountText.toDoubleOrNull()
                        onApplyFilters(selectedCategoryIds, selectedAccountIds, minAmount, maxAmount, keyword)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    )
                ) {
                    Text("应用", style = AppTypography.LabelMedium)
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingXL))
        }
    }
}

/**
 * 分类筛选内容
 */
@Composable
private fun CategoryFilterContent(
    categories: List<FilterCategory>,
    selectedIds: Set<Long>,
    onSelectionChange: (Set<Long>) -> Unit
) {
    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }
    val incomeCategories = categories.filter { it.type == TransactionType.INCOME }

    Column(
        modifier = Modifier.height(300.dp)
    ) {
        LazyColumn {
            if (expenseCategories.isNotEmpty()) {
                item {
                    Text(
                        text = "支出分类",
                        style = AppTypography.LabelMedium,
                        color = AppColors.Accent,
                        modifier = Modifier.padding(vertical = AppDimens.SpacingS)
                    )
                }
                items(expenseCategories) { category ->
                    CategoryCheckboxItem(
                        category = category,
                        isSelected = category.id in selectedIds,
                        onToggle = {
                            onSelectionChange(
                                if (category.id in selectedIds) {
                                    selectedIds - category.id
                                } else {
                                    selectedIds + category.id
                                }
                            )
                        }
                    )
                }
            }

            if (incomeCategories.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                    Text(
                        text = "收入分类",
                        style = AppTypography.LabelMedium,
                        color = AppColors.Success,
                        modifier = Modifier.padding(vertical = AppDimens.SpacingS)
                    )
                }
                items(incomeCategories) { category ->
                    CategoryCheckboxItem(
                        category = category,
                        isSelected = category.id in selectedIds,
                        onToggle = {
                            onSelectionChange(
                                if (category.id in selectedIds) {
                                    selectedIds - category.id
                                } else {
                                    selectedIds + category.id
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCheckboxItem(
    category: FilterCategory,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = AppDimens.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.Primary
            )
        )
        Text(
            text = category.icon,
            style = AppTypography.BodyMedium,
            modifier = Modifier.padding(horizontal = AppDimens.SpacingS)
        )
        Text(
            text = category.name,
            style = AppTypography.BodyMedium,
            color = AppColors.TextPrimary
        )
    }
}

/**
 * 账户筛选内容
 */
@Composable
private fun AccountFilterContent(
    accounts: List<FilterAccount>,
    selectedIds: Set<Long>,
    onSelectionChange: (Set<Long>) -> Unit
) {
    Column(
        modifier = Modifier.height(300.dp)
    ) {
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无账户",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextMuted
                )
            }
        } else {
            LazyColumn {
                items(accounts) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectionChange(
                                    if (account.id in selectedIds) {
                                        selectedIds - account.id
                                    } else {
                                        selectedIds + account.id
                                    }
                                )
                            }
                            .padding(vertical = AppDimens.SpacingS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = account.id in selectedIds,
                            onCheckedChange = {
                                onSelectionChange(
                                    if (account.id in selectedIds) {
                                        selectedIds - account.id
                                    } else {
                                        selectedIds + account.id
                                    }
                                )
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppColors.Primary
                            )
                        )
                        Text(
                            text = account.icon,
                            style = AppTypography.BodyMedium,
                            modifier = Modifier.padding(horizontal = AppDimens.SpacingS)
                        )
                        Text(
                            text = account.name,
                            style = AppTypography.BodyMedium,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 金额筛选内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountFilterContent(
    minAmount: String,
    maxAmount: String,
    onMinAmountChange: (String) -> Unit,
    onMaxAmountChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.height(300.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
    ) {
        Text(
            text = "设置金额范围筛选交易",
            style = AppTypography.BodyMedium,
            color = AppColors.TextSecondary
        )

        OutlinedTextField(
            value = minAmount,
            onValueChange = { value ->
                if (value.isEmpty() || value.toDoubleOrNull() != null) {
                    onMinAmountChange(value)
                }
            },
            label = { Text("最小金额") },
            placeholder = { Text("不限") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Border
            )
        )

        OutlinedTextField(
            value = maxAmount,
            onValueChange = { value ->
                if (value.isEmpty() || value.toDoubleOrNull() != null) {
                    onMaxAmountChange(value)
                }
            },
            label = { Text("最大金额") },
            placeholder = { Text("不限") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Border
            )
        )

        // 快速选择
        Text(
            text = "快速选择",
            style = AppTypography.LabelMedium,
            color = AppColors.TextSecondary
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
        ) {
            listOf(
                "0-100" to Pair("0", "100"),
                "100-500" to Pair("100", "500"),
                "500-1000" to Pair("500", "1000"),
                "1000+" to Pair("1000", "")
            ).forEach { (label, range) ->
                FilterChip(
                    selected = minAmount == range.first && maxAmount == range.second,
                    onClick = {
                        onMinAmountChange(range.first)
                        onMaxAmountChange(range.second)
                    },
                    label = { Text(label, style = AppTypography.Caption) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.Primary,
                        selectedLabelColor = Color.White,
                        containerColor = AppColors.Background,
                        labelColor = AppColors.TextSecondary
                    )
                )
            }
        }
    }
}

/**
 * 关键词筛选内容
 */
@Composable
private fun KeywordFilterContent(
    keyword: String,
    onKeywordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.height(300.dp),
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL)
    ) {
        Text(
            text = "搜索交易备注、标签或分类名称",
            style = AppTypography.BodyMedium,
            color = AppColors.TextSecondary
        )

        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            label = { Text("关键词") },
            placeholder = { Text("输入搜索关键词") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = AppColors.TextMuted
                )
            },
            trailingIcon = {
                if (keyword.isNotEmpty()) {
                    IconButton(onClick = { onKeywordChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "清除",
                            tint = AppColors.TextMuted
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Border
            )
        )
    }
}

/**
 * 日期范围选择对话框
 */
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "自定义日期范围",
                style = AppTypography.TitleMedium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                Text(
                    text = "请输入日期 (格式: YYYY-MM-DD)",
                    style = AppTypography.Caption,
                    color = AppColors.TextSecondary
                )

                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("开始日期") },
                    placeholder = { Text("2024-01-01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = { Text("结束日期") },
                    placeholder = { Text("2024-12-31") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val startDate = dateFormat.parse(startDateText)?.time ?: return@TextButton
                        val endDate = dateFormat.parse(endDateText)?.time ?: return@TextButton
                        onConfirm(startDate, endDate + 24 * 60 * 60 * 1000) // Add one day to include end date
                    } catch (e: Exception) {
                        // Invalid date format
                    }
                }
            ) {
                Text("确定", color = AppColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextSecondary)
            }
        }
    )
}

@Composable
private fun MonthSummaryHeader(
    monthTitle: String,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    showNavigation: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.SpacingS)
    ) {
        Column {
            // 月份导航
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showNavigation) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上个月",
                            tint = AppColors.TextSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Text(
                    text = monthTitle,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )

                if (showNavigation) {
                    IconButton(onClick = onNextMonth) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下个月",
                            tint = AppColors.TextSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
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
            .padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.SpacingS),
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
                    if (transaction.accountName.isNotBlank()) {
                        Text(
                            text = " · ${transaction.accountName}",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                    }
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
private fun EmptyTransactionsState(isFiltered: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isFiltered) "🔍" else "📋",
                style = AppTypography.NumberLarge
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            Text(
                text = if (isFiltered) "没有符合条件的记录" else "本月暂无记录",
                style = AppTypography.TitleSmall,
                color = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = if (isFiltered) "尝试调整筛选条件" else "开始记录您的第一笔账目吧",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }
    }
}
