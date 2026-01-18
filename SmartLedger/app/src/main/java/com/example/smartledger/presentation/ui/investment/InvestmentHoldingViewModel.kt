package com.example.smartledger.presentation.ui.investment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.AccountType
import com.example.smartledger.data.local.entity.HoldingType
import com.example.smartledger.data.local.entity.InvestmentHoldingEntity
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.InvestmentHoldingRepository
import com.example.smartledger.domain.repository.InvestmentSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentHoldingViewModel @Inject constructor(
    private val holdingRepository: InvestmentHoldingRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentHoldingUiState())
    val uiState: StateFlow<InvestmentHoldingUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(HoldingFormState())
    val formState: StateFlow<HoldingFormState> = _formState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 获取投资账户
            accountRepository.getAllActiveAccounts().collect { accounts ->
                val investmentAccounts = accounts.filter {
                    it.type in listOf(
                        AccountType.INVESTMENT_STOCK,
                        AccountType.INVESTMENT_FUND,
                        AccountType.INVESTMENT_DEPOSIT
                    )
                }
                _uiState.value = _uiState.value.copy(
                    investmentAccounts = investmentAccounts
                )
            }
        }

        viewModelScope.launch {
            holdingRepository.getAllActive().collect { holdings ->
                val summary = holdingRepository.getSummary()
                _uiState.value = _uiState.value.copy(
                    holdings = holdings,
                    summary = summary,
                    isLoading = false
                )
            }
        }
    }

    fun filterByType(type: HoldingType?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun filterByAccount(accountId: Long?) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun showAddDialog() {
        _formState.value = HoldingFormState()
        _uiState.value = _uiState.value.copy(showDialog = true, editingHolding = null)
    }

    fun showEditDialog(holding: InvestmentHoldingEntity) {
        _formState.value = HoldingFormState(
            accountId = holding.accountId,
            name = holding.name,
            code = holding.code,
            holdingType = holding.holdingType,
            quantity = holding.quantity.toString(),
            costPrice = holding.costPrice.toString(),
            currentPrice = holding.currentPrice.toString(),
            note = holding.note
        )
        _uiState.value = _uiState.value.copy(showDialog = true, editingHolding = holding)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false, editingHolding = null)
    }

    fun updateFormState(newState: HoldingFormState) {
        _formState.value = newState
    }

    fun saveHolding() {
        val form = _formState.value
        if (!form.isValid) return

        viewModelScope.launch {
            val editing = _uiState.value.editingHolding
            val quantity = form.quantity.toDoubleOrNull() ?: 0.0
            val costPrice = form.costPrice.toDoubleOrNull() ?: 0.0
            val currentPrice = form.currentPrice.toDoubleOrNull() ?: costPrice
            val principal = quantity * costPrice
            val marketValue = quantity * currentPrice
            val profitLoss = marketValue - principal
            val returnRate = if (principal > 0) (profitLoss / principal * 100) else 0.0

            if (editing != null) {
                // 更新
                val updated = editing.copy(
                    accountId = form.accountId,
                    name = form.name,
                    code = form.code,
                    holdingType = form.holdingType,
                    quantity = quantity,
                    costPrice = costPrice,
                    currentPrice = currentPrice,
                    principal = principal,
                    marketValue = marketValue,
                    profitLoss = profitLoss,
                    returnRate = returnRate,
                    note = form.note,
                    updatedAt = System.currentTimeMillis()
                )
                holdingRepository.update(updated)
            } else {
                // 新建
                val holding = InvestmentHoldingEntity(
                    accountId = form.accountId,
                    name = form.name,
                    code = form.code,
                    holdingType = form.holdingType,
                    quantity = quantity,
                    costPrice = costPrice,
                    currentPrice = currentPrice,
                    principal = principal,
                    marketValue = marketValue,
                    profitLoss = profitLoss,
                    returnRate = returnRate,
                    firstBuyDate = System.currentTimeMillis(),
                    note = form.note
                )
                holdingRepository.create(holding)
            }
            dismissDialog()
        }
    }

    fun updatePrice(holding: InvestmentHoldingEntity, newPrice: Double) {
        viewModelScope.launch {
            holdingRepository.updatePrice(holding.id, newPrice)
        }
    }

    fun deleteHolding(holding: InvestmentHoldingEntity) {
        viewModelScope.launch {
            holdingRepository.delete(holding)
        }
    }

    fun markAsSold(holding: InvestmentHoldingEntity) {
        viewModelScope.launch {
            holdingRepository.markAsSold(holding.id)
        }
    }
}

data class InvestmentHoldingUiState(
    val holdings: List<InvestmentHoldingEntity> = emptyList(),
    val summary: InvestmentSummary = InvestmentSummary(0.0, 0.0, 0.0, 0.0, 0, 0, 0),
    val investmentAccounts: List<AccountEntity> = emptyList(),
    val selectedType: HoldingType? = null,
    val selectedAccountId: Long? = null,
    val showDialog: Boolean = false,
    val editingHolding: InvestmentHoldingEntity? = null,
    val isLoading: Boolean = true
) {
    val filteredHoldings: List<InvestmentHoldingEntity>
        get() {
            var result = holdings
            selectedType?.let { type ->
                result = result.filter { it.holdingType == type }
            }
            selectedAccountId?.let { accountId ->
                result = result.filter { it.accountId == accountId }
            }
            return result
        }
}

data class HoldingFormState(
    val accountId: Long = 0L,
    val name: String = "",
    val code: String = "",
    val holdingType: HoldingType = HoldingType.STOCK,
    val quantity: String = "",
    val costPrice: String = "",
    val currentPrice: String = "",
    val note: String = ""
) {
    val isValid: Boolean
        get() = accountId > 0 &&
                name.isNotBlank() &&
                quantity.toDoubleOrNull() != null &&
                quantity.toDoubleOrNull()!! > 0 &&
                costPrice.toDoubleOrNull() != null &&
                costPrice.toDoubleOrNull()!! > 0
}
