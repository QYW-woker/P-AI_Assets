package com.example.smartledger.presentation.ui.transactions

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
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

// iOS风格颜色
private val iOSBackground = Color(0xFFF2F2F7)
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSRed = Color(0xFFFF3B30)

/**
 * 交易详情页面 - iOS卡通风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditAmountDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        IOSAlertDialog(
            icon = "🗑️",
            title = "确认删除",
            message = "删除后无法恢复，确定要删除这条记录吗？",
            confirmText = "删除",
            confirmColor = iOSRed,
            onConfirm = {
                viewModel.deleteTransaction()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // 编辑金额对话框
    if (showEditAmountDialog && uiState.transaction != null) {
        EditAmountDialog(
            currentAmount = uiState.transaction!!.amount,
            onConfirm = { newAmount ->
                viewModel.updateAmount(newAmount)
                showEditAmountDialog = false
            },
            onDismiss = { showEditAmountDialog = false }
        )
    }

    // 编辑备注对话框
    if (showEditNoteDialog && uiState.transaction != null) {
        EditNoteDialog(
            currentNote = uiState.transaction!!.note,
            onConfirm = { newNote ->
                viewModel.updateNote(newNote)
                showEditNoteDialog = false
            },
            onDismiss = { showEditNoteDialog = false }
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
        } else if (uiState.transaction == null) {
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
                        text = "记录不存在",
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
                        title = "📝 交易详情",
                        onBack = onNavigateBack,
                        onDelete = { showDeleteDialog = true }
                    )
                }

                // 金额卡片
                item {
                    AmountCard(
                        detail = uiState.transaction!!,
                        onEditAmount = { showEditAmountDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 交易信息
                item {
                    TransactionInfoCard(
                        detail = uiState.transaction!!,
                        onEditNote = { showEditNoteDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 时间线
                item {
                    TimelineCard(
                        detail = uiState.transaction!!,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 快捷操作
                item {
                    QuickActionsCard(
                        onDuplicate = { viewModel.duplicateTransaction() },
                        onShare = { /* TODO: 分享功能 */ },
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
 * 金额卡片
 */
@Composable
private fun AmountCard(
    detail: TransactionDetailUiModel,
    onEditAmount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (detail.isExpense) {
        listOf(Color(0xFFFF9500), Color(0xFFFF6B6B))
    } else {
        listOf(Color(0xFF34C759), Color(0xFF30D158))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors = gradientColors))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = detail.categoryIcon, fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = detail.categoryName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 金额（可编辑）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onEditAmount)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (detail.isExpense) "-" else "+"}¥${String.format("%.2f", detail.amount)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "✏️", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (detail.isExpense) "支出" else "收入",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 交易信息卡片
 */
@Composable
private fun TransactionInfoCard(
    detail: TransactionDetailUiModel,
    onEditNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())

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
                text = "ℹ️ 交易信息",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(icon = "🏷️", label = "分类", value = detail.categoryName)
            InfoRow(icon = "📅", label = "时间", value = dateFormat.format(Date(detail.date)))
            InfoRow(icon = "🏦", label = "账户", value = detail.accountName)

            if (detail.tags.isNotEmpty()) {
                InfoRow(icon = "🔖", label = "标签", value = detail.tags)
            }

            // 备注（可编辑）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F2F7))
                    .clickable(onClick = onEditNote)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✏️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "备注",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                        Text(
                            text = detail.note.ifEmpty { "添加备注..." },
                            fontSize = 14.sp,
                            color = if (detail.note.isEmpty()) Color(0xFFC7C7CC) else Color(0xFF1C1C1E)
                        )
                    }
                }
                Text(text = "→", fontSize = 14.sp, color = Color(0xFFC7C7CC))
            }
        }
    }
}

@Composable
private fun InfoRow(
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
 * 时间线卡片
 */
@Composable
private fun TimelineCard(
    detail: TransactionDetailUiModel,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

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
                text = "⏱️ 时间线",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 创建时间
            TimelineItem(
                icon = "🆕",
                title = "创建记录",
                time = dateFormat.format(Date(detail.date)),
                isLast = detail.updatedAt == detail.date
            )

            // 最后更新（如果有）
            if (detail.updatedAt != detail.date) {
                TimelineItem(
                    icon = "✏️",
                    title = "最后修改",
                    time = dateFormat.format(Date(detail.updatedAt)),
                    isLast = true
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    icon: String,
    title: String,
    time: String,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iOSAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 14.sp)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(Color(0xFFE5E5EA))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color(0xFF8E8E93)
            )
        }
    }
}

/**
 * 快捷操作卡片
 */
@Composable
private fun QuickActionsCard(
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
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
                text = "⚡ 快捷操作",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = "📋",
                    label = "复制",
                    onClick = onDuplicate
                )
                ActionButton(
                    icon = "📤",
                    label = "分享",
                    onClick = onShare
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iOSAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF8E8E93)
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
 * 编辑金额对话框
 */
@Composable
private fun EditAmountDialog(
    currentAmount: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf(currentAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "💰 修改金额",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("金额") },
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
                    amountText.toDoubleOrNull()?.let { onConfirm(it) }
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
 * 编辑备注对话框
 */
@Composable
private fun EditNoteDialog(
    currentNote: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(currentNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "✏️ 修改备注",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
        },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("备注") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iOSAccent,
                    unfocusedBorderColor = Color(0xFFE5E5EA)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(noteText) }) {
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
    val date: Long,
    val updatedAt: Long = date
)

/**
 * 交易详情ViewModel
 */
@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
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
                val account = accountRepository.getAccountById(transaction.accountId)

                _uiState.value = TransactionDetailUiState(
                    transaction = TransactionDetailUiModel(
                        id = transaction.id,
                        categoryName = category?.name ?: "未分类",
                        categoryIcon = category?.icon ?: "📦",
                        categoryColor = category?.color ?: "#CCCCCC",
                        accountName = account?.name ?: "默认账户",
                        amount = transaction.amount,
                        note = transaction.note,
                        tags = transaction.tags,
                        isExpense = transaction.type == TransactionType.EXPENSE,
                        date = transaction.date,
                        updatedAt = transaction.updatedAt
                    ),
                    isLoading = false
                )
            } else {
                _uiState.value = TransactionDetailUiState(isLoading = false)
            }
        }
    }

    fun updateAmount(newAmount: Double) {
        viewModelScope.launch {
            currentTransaction?.let { tx ->
                val updated = tx.copy(
                    amount = newAmount,
                    updatedAt = System.currentTimeMillis()
                )
                transactionRepository.updateTransaction(updated)
                currentTransaction = updated
                loadTransaction(tx.id)
            }
        }
    }

    fun updateNote(newNote: String) {
        viewModelScope.launch {
            currentTransaction?.let { tx ->
                val updated = tx.copy(
                    note = newNote,
                    updatedAt = System.currentTimeMillis()
                )
                transactionRepository.updateTransaction(updated)
                currentTransaction = updated
                loadTransaction(tx.id)
            }
        }
    }

    fun duplicateTransaction() {
        viewModelScope.launch {
            currentTransaction?.let { tx ->
                val duplicate = tx.copy(
                    id = 0,
                    date = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                transactionRepository.insertTransaction(duplicate)
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
