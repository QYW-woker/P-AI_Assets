package com.example.smartledger.presentation.ui.search

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 搜索页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<TransactionType?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        query = searchText,
                        onQueryChange = {
                            searchText = it
                            viewModel.search(it)
                        },
                        onClear = {
                            searchText = ""
                            viewModel.clearFilters()
                        },
                        focusRequester = focusRequester
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "筛选",
                            tint = if (uiState.filterApplied) AppColors.Accent else AppColors.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 筛选选项
            if (showFilters) {
                FilterSection(
                    selectedType = selectedType,
                    onTypeSelected = { type ->
                        selectedType = if (selectedType == type) null else type
                        viewModel.applyFilters(transactionType = selectedType)
                    },
                    onClearFilters = {
                        selectedType = null
                        viewModel.clearFilters()
                    }
                )
            }

            // 搜索结果
            if (uiState.results.isEmpty() && searchText.isNotBlank()) {
                EmptySearchState(query = searchText)
            } else if (uiState.results.isEmpty() && searchText.isBlank()) {
                SearchPromptState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(AppDimens.PaddingL)
                ) {
                    item {
                        Text(
                            text = "找到 ${uiState.results.size} 条记录",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                    }

                    items(uiState.results) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onNavigateToTransactionDetail(result.id) }
                        )
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
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Card)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = AppColors.TextMuted,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                color = AppColors.TextPrimary,
                fontSize = AppTypography.BodyMedium.fontSize
            ),
            singleLine = true,
            cursorBrush = SolidColor(AppColors.Accent),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "搜索交易记录...",
                            style = AppTypography.BodyMedium,
                            color = AppColors.TextMuted
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "清除",
                    tint = AppColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.PaddingL, vertical = AppDimens.PaddingM)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "筛选条件",
                style = AppTypography.LabelMedium,
                color = AppColors.TextSecondary
            )

            Text(
                text = "清除",
                style = AppTypography.LabelSmall,
                color = AppColors.Accent,
                modifier = Modifier.clickable(onClick = onClearFilters)
            )
        }

        Spacer(modifier = Modifier.height(AppDimens.SpacingS))

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
        ) {
            FilterChip(
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { onTypeSelected(TransactionType.EXPENSE) },
                label = {
                    Text("支出", style = AppTypography.LabelSmall)
                },
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
                label = {
                    Text("收入", style = AppTypography.LabelSmall)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Success,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.Card,
                    labelColor = AppColors.TextSecondary
                )
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SearchResultItem,
    onClick: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(result.categoryColor))
    } catch (e: Exception) {
        AppColors.Primary
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.categoryIcon,
                    style = AppTypography.BodyLarge
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.categoryName,
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextPrimary
                )
                if (result.note.isNotBlank()) {
                    Text(
                        text = result.note,
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted,
                        maxLines = 1
                    )
                }
                Text(
                    text = result.date,
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }

            Text(
                text = "${if (result.type == TransactionType.EXPENSE) "-" else "+"}¥${String.format("%.2f", result.amount)}",
                style = AppTypography.NumberSmall,
                color = if (result.type == TransactionType.EXPENSE) AppColors.Accent else AppColors.Success
            )
        }
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔍",
                style = AppTypography.NumberLarge
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            Text(
                text = "未找到 \"$query\" 相关记录",
                style = AppTypography.BodyMedium,
                color = AppColors.TextMuted
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = "尝试其他关键词",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }
    }
}

@Composable
private fun SearchPromptState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔍",
                style = AppTypography.NumberLarge
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            Text(
                text = "搜索交易记录",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = "输入关键词搜索备注、分类或金额",
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }
    }
}
