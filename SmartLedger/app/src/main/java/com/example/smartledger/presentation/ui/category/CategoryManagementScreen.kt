package com.example.smartledger.presentation.ui.category

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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

/**
 * 分类管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryUiModel?>(null) }

    val categories = when (selectedTab) {
        TransactionType.EXPENSE -> uiState.expenseCategories
        TransactionType.INCOME -> uiState.incomeCategories
    }

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "分类管理",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AppColors.Accent,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加分类"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
        ) {
            // 类型切换标签
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimens.PaddingL),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                FilterChip(
                    selected = selectedTab == TransactionType.EXPENSE,
                    onClick = { selectedTab = TransactionType.EXPENSE },
                    label = {
                        Text(
                            text = "支出分类",
                            style = AppTypography.LabelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.Accent,
                        selectedLabelColor = Color.White,
                        containerColor = AppColors.Card,
                        labelColor = AppColors.TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedTab == TransactionType.INCOME,
                    onClick = { selectedTab = TransactionType.INCOME },
                    label = {
                        Text(
                            text = "收入分类",
                            style = AppTypography.LabelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.Success,
                        selectedLabelColor = Color.White,
                        containerColor = AppColors.Card,
                        labelColor = AppColors.TextSecondary
                    )
                )
            }

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📁",
                            style = AppTypography.NumberLarge
                        )
                        Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                        Text(
                            text = "暂无分类",
                            style = AppTypography.TitleSmall,
                            color = AppColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                        Text(
                            text = "点击右下角按钮添加分类",
                            style = AppTypography.Caption,
                            color = AppColors.TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = AppDimens.PaddingL,
                        end = AppDimens.PaddingL,
                        bottom = 80.dp
                    )
                ) {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            onEdit = { editingCategory = category },
                            onDelete = { viewModel.deleteCategory(category.id) }
                        )
                    }
                }
            }
        }
    }

    // 添加分类对话框
    if (showAddDialog) {
        AddCategoryDialog(
            categoryType = selectedTab,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, icon, color ->
                viewModel.addCategory(name, icon, color, selectedTab)
                showAddDialog = false
            }
        )
    }

    // 编辑分类对话框
    editingCategory?.let { category ->
        EditCategoryDialog(
            category = category,
            onDismiss = { editingCategory = null },
            onConfirm = { name, icon, color ->
                viewModel.updateCategory(category.id, name, icon, color)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun CategoryItem(
    category: CategoryUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        AppColors.Primary
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (!category.isSystem) onEdit else ({})
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.icon,
                    style = AppTypography.TitleSmall
                )
            }

            Spacer(modifier = Modifier.width(AppDimens.SpacingM))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.name,
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextPrimary
                    )
                    if (category.isSystem) {
                        Spacer(modifier = Modifier.width(AppDimens.SpacingXS))
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "系统分类",
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.TextMuted
                        )
                    }
                }
                Text(
                    text = if (category.isSystem) "系统预设" else "自定义",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }

            if (!category.isSystem) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "编辑",
                        tint = AppColors.TextMuted
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "删除",
                        tint = AppColors.Accent
                    )
                }
            }
        }
    }
}
