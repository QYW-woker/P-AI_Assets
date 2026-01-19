package com.example.smartledger.presentation.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.AccountType
import com.example.smartledger.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 账户管理ViewModel
 */
@HiltViewModel
class AccountManagementViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountManagementUiState())
    val uiState: StateFlow<AccountManagementUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val accounts = accountRepository.getAllActiveAccounts().first()

                // 按类型分组
                val assetAccounts = accounts.filter {
                    it.type in listOf(
                        AccountType.CASH,
                        AccountType.BANK,
                        AccountType.ALIPAY,
                        AccountType.WECHAT,
                        AccountType.CREDIT_CARD
                    )
                }.map { it.toUiModel() }

                val investmentAccounts = accounts.filter {
                    it.type in listOf(
                        AccountType.INVESTMENT_STOCK,
                        AccountType.INVESTMENT_FUND,
                        AccountType.INVESTMENT_DEPOSIT
                    )
                }.map { it.toUiModel() }

                val totalAssets = assetAccounts.sumOf { it.balance }
                val totalInvestments = investmentAccounts.sumOf { it.balance }

                _uiState.value = AccountManagementUiState(
                    assetAccounts = assetAccounts,
                    investmentAccounts = investmentAccounts,
                    totalAssets = totalAssets,
                    totalInvestments = totalInvestments,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun addAccount(
        name: String,
        type: AccountType,
        icon: String,
        color: String,
        balance: Double,
        note: String
    ) {
        viewModelScope.launch {
            val account = AccountEntity(
                name = name,
                type = type,
                icon = icon,
                color = color,
                balance = balance,
                initialBalance = balance,
                note = note
            )
            accountRepository.insertAccount(account)
            loadAccounts()
        }
    }

    fun updateAccount(
        id: Long,
        name: String,
        icon: String,
        color: String,
        note: String
    ) {
        viewModelScope.launch {
            val existing = accountRepository.getAccountById(id)
            if (existing != null) {
                val updated = existing.copy(
                    name = name,
                    icon = icon,
                    color = color,
                    note = note
                )
                accountRepository.updateAccount(updated)
                loadAccounts()
            }
        }
    }

    fun updateBalance(accountId: Long, newBalance: Double) {
        viewModelScope.launch {
            // 使用 setBalance 直接设置新余额，而不是累加
            accountRepository.setBalance(accountId, newBalance)
            loadAccounts()
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            val account = accountRepository.getAccountById(accountId)
            if (account != null) {
                accountRepository.deleteAccount(account)
                loadAccounts()
            }
        }
    }

    fun refresh() {
        loadAccounts()
    }

    private fun AccountEntity.toUiModel() = AccountUiModel(
        id = id,
        name = name,
        type = type,
        icon = icon,
        color = color,
        balance = balance,
        note = note,
        typeName = type.toDisplayName()
    )

    private fun AccountType.toDisplayName(): String = when (this) {
        AccountType.CASH -> "现金"
        AccountType.BANK -> "银行卡"
        AccountType.ALIPAY -> "支付宝"
        AccountType.WECHAT -> "微信"
        AccountType.CREDIT_CARD -> "信用卡"
        AccountType.INVESTMENT_STOCK -> "股票"
        AccountType.INVESTMENT_FUND -> "基金"
        AccountType.INVESTMENT_DEPOSIT -> "定期存款"
    }
}

/**
 * 账户管理UI状态
 */
data class AccountManagementUiState(
    val assetAccounts: List<AccountUiModel> = emptyList(),
    val investmentAccounts: List<AccountUiModel> = emptyList(),
    val totalAssets: Double = 0.0,
    val totalInvestments: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * 账户UI模型
 */
data class AccountUiModel(
    val id: Long,
    val name: String,
    val type: AccountType,
    val icon: String,
    val color: String,
    val balance: Double,
    val note: String,
    val typeName: String
)
