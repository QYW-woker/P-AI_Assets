package com.example.smartledger.presentation.ui.accounts

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 账户详情页面
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

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除", style = AppTypography.TitleMedium) },
            text = { Text("确定要删除这个账户吗？删除后相关的交易记录不会被删除。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount()
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
                title = "账户详情",
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
            uiState.account == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("账户不存在", color = AppColors.TextMuted)
                }
            }
            else -> {
                AccountDetailContent(
                    account = uiState.account!!,
                    recentTransactions = uiState.recentTransactions,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.Background)
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun AccountDetailContent(
    account: AccountDetailUiModel,
    recentTransactions: List<AccountTransactionUiModel>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(AppDimens.PaddingL)
    ) {
        // 账户信息卡片
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(account.color.toColor()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.icon,
                            style = AppTypography.TitleLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                    Text(
                        text = account.name,
                        style = AppTypography.TitleMedium,
                        color = AppColors.TextPrimary
                    )

                    Text(
                        text = account.typeName,
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )

                    Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                    Text(
                        text = "¥${formatAmount(account.balance)}",
                        style = AppTypography.NumberLarge,
                        color = if (account.balance >= 0) AppColors.Success else AppColors.Accent
                    )

                    Text(
                        text = "当前余额",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }
            }
        }

        // 本月统计
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "本月统计",
                        style = AppTypography.TitleSmall,
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "收入",
                                style = AppTypography.Caption,
                                color = AppColors.TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¥${formatAmount(account.monthlyIncome)}",
                                style = AppTypography.NumberSmall,
                                color = AppColors.Success
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "支出",
                                style = AppTypography.Caption,
                                color = AppColors.TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¥${formatAmount(account.monthlyExpense)}",
                                style = AppTypography.NumberSmall,
                                color = AppColors.Accent
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "净收入",
                                style = AppTypography.Caption,
                                color = AppColors.TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val netIncome = account.monthlyIncome - account.monthlyExpense
                            Text(
                                text = "¥${formatAmount(netIncome)}",
                                style = AppTypography.NumberSmall,
                                color = if (netIncome >= 0) AppColors.Success else AppColors.Accent
                            )
                        }
                    }
                }
            }
        }

        // 最近交易
        if (recentTransactions.isNotEmpty()) {
            item {
                Text(
                    text = "最近交易",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )
            }

            items(recentTransactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }

        item {
            Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
        }
    }
}

@Composable
private fun TransactionItem(transaction: AccountTransactionUiModel) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.categoryName,
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
            }

            Text(
                text = "${if (transaction.isExpense) "-" else "+"}¥${formatAmount(transaction.amount)}",
                style = AppTypography.NumberSmall,
                color = if (transaction.isExpense) AppColors.Accent else AppColors.Success
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 10000) {
        String.format("%.2f万", amount / 10000)
    } else {
        String.format("%.2f", amount)
    }
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
                // 获取本月时间范围
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                val monthEnd = calendar.timeInMillis

                // 获取最近交易
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
                        categoryName = "交易", // TODO: 从CategoryRepository获取分类名
                        amount = tx.amount,
                        isExpense = tx.type == TransactionType.EXPENSE,
                        date = tx.date
                    )
                }

                val typeName = when (account.type.name) {
                    "CASH" -> "现金"
                    "DEBIT_CARD" -> "储蓄卡"
                    "CREDIT_CARD" -> "信用卡"
                    "ALIPAY" -> "支付宝"
                    "WECHAT" -> "微信"
                    "INVESTMENT" -> "投资账户"
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
                    isLoading = false
                )
            } else {
                _uiState.value = AccountDetailUiState(isLoading = false)
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
    val isLoading: Boolean = true
)
