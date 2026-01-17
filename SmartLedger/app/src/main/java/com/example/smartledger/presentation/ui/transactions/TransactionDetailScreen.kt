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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography
import com.example.smartledger.utils.toColor
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
 * 交易详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除", style = AppTypography.TitleMedium) },
            text = { Text("确定要删除这条记录吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction()
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

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "交易详情",
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
            uiState.transaction == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("交易记录不存在", color = AppColors.TextMuted)
                }
            }
            else -> {
                TransactionDetailContent(
                    detail = uiState.transaction!!,
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
private fun TransactionDetailContent(
    detail: TransactionDetailUiModel,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())

    Column(modifier = modifier) {
        // 金额卡片
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(detail.categoryColor.toColor()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = detail.categoryIcon,
                        style = AppTypography.TitleLarge
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                Text(
                    text = detail.categoryName,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Text(
                    text = "${if (detail.isExpense) "-" else "+"}¥${String.format("%.2f", detail.amount)}",
                    style = AppTypography.NumberLarge,
                    color = if (detail.isExpense) AppColors.Accent else AppColors.Success
                )
            }
        }

        Spacer(modifier = Modifier.height(AppDimens.SpacingL))

        // 详情信息
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                DetailRow(label = "类型", value = if (detail.isExpense) "支出" else "收入")
                DetailRow(label = "时间", value = dateFormat.format(Date(detail.date)))
                DetailRow(label = "账户", value = detail.accountName)
                if (detail.note.isNotEmpty()) {
                    DetailRow(label = "备注", value = detail.note)
                }
                if (detail.tags.isNotEmpty()) {
                    DetailRow(label = "标签", value = detail.tags)
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

/**
 * 交易详情UI模型
 */
data class TransactionDetailUiModel(
    val id: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val accountName: String,
    val amount: Double,
    val note: String,
    val tags: String,
    val isExpense: Boolean,
    val date: Long
)

/**
 * 交易详情ViewModel
 */
@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private var currentTransaction: TransactionEntity? = null

    fun loadTransaction(transactionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val transaction = transactionRepository.getTransactionById(transactionId)
            if (transaction != null) {
                currentTransaction = transaction
                val category = categoryRepository.getCategoryById(transaction.categoryId)

                _uiState.value = TransactionDetailUiState(
                    transaction = TransactionDetailUiModel(
                        id = transaction.id,
                        categoryName = category?.name ?: "未分类",
                        categoryIcon = category?.icon ?: "📦",
                        categoryColor = category?.color ?: "#CCCCCC",
                        accountName = "默认账户", // TODO: 从AccountRepository获取
                        amount = transaction.amount,
                        note = transaction.note,
                        tags = transaction.tags,
                        isExpense = transaction.type == TransactionType.EXPENSE,
                        date = transaction.date
                    ),
                    isLoading = false
                )
            } else {
                _uiState.value = TransactionDetailUiState(isLoading = false)
            }
        }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            currentTransaction?.let {
                transactionRepository.deleteTransaction(it)
            }
        }
    }
}

/**
 * 交易详情UI状态
 */
data class TransactionDetailUiState(
    val transaction: TransactionDetailUiModel? = null,
    val isLoading: Boolean = true
)
