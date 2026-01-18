package com.example.smartledger.presentation.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.RecurringFrequency
import com.example.smartledger.data.local.entity.RecurringTransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.RecurringTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 固定收支ViewModel
 */
@HiltViewModel
class RecurringTransactionViewModel @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringTransactionUiState())
    val uiState: StateFlow<RecurringTransactionUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(RecurringFormState())
    val formState: StateFlow<RecurringFormState> = _formState.asStateFlow()

    init {
        loadRecurringTransactions()
        loadFormOptions()
    }

    private fun loadRecurringTransactions() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                recurringRepository.getAllActive().collect { list ->
                    val totalFixedIncome = list
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    val totalFixedExpense = list
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recurringTransactions = list,
                        totalFixedIncome = totalFixedIncome,
                        totalFixedExpense = totalFixedExpense
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadFormOptions() {
        viewModelScope.launch {
            val categories = categoryRepository.getAllActiveCategories().first()
            val accounts = accountRepository.getAllActiveAccounts().first()

            _formState.value = _formState.value.copy(
                availableCategories = categories.map {
                    CategoryOption(it.id, it.name, it.icon, it.type)
                },
                availableAccounts = accounts.map {
                    AccountOption(it.id, it.name, it.icon)
                }
            )
        }
    }

    fun showAddDialog() {
        _formState.value = RecurringFormState(
            isEditing = false,
            availableCategories = _formState.value.availableCategories,
            availableAccounts = _formState.value.availableAccounts
        )
        _uiState.value = _uiState.value.copy(showDialog = true)
    }

    fun showEditDialog(recurring: RecurringTransactionEntity) {
        _formState.value = RecurringFormState(
            isEditing = true,
            editingId = recurring.id,
            name = recurring.name,
            amount = recurring.amount.toString(),
            type = recurring.type,
            categoryId = recurring.categoryId,
            accountId = recurring.accountId,
            frequency = recurring.frequency,
            dayOfPeriod = recurring.dayOfPeriod,
            note = recurring.note,
            autoExecute = recurring.autoExecute,
            availableCategories = _formState.value.availableCategories,
            availableAccounts = _formState.value.availableAccounts
        )
        _uiState.value = _uiState.value.copy(showDialog = true)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }

    fun updateFormName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateFormAmount(amount: String) {
        _formState.value = _formState.value.copy(amount = amount)
    }

    fun updateFormType(type: TransactionType) {
        _formState.value = _formState.value.copy(type = type, categoryId = null)
    }

    fun updateFormCategory(categoryId: Long) {
        _formState.value = _formState.value.copy(categoryId = categoryId)
    }

    fun updateFormAccount(accountId: Long) {
        _formState.value = _formState.value.copy(accountId = accountId)
    }

    fun updateFormFrequency(frequency: RecurringFrequency) {
        _formState.value = _formState.value.copy(frequency = frequency)
    }

    fun updateFormDayOfPeriod(day: Int) {
        _formState.value = _formState.value.copy(dayOfPeriod = day)
    }

    fun updateFormNote(note: String) {
        _formState.value = _formState.value.copy(note = note)
    }

    fun updateFormAutoExecute(autoExecute: Boolean) {
        _formState.value = _formState.value.copy(autoExecute = autoExecute)
    }

    fun saveRecurring() {
        val form = _formState.value
        val amount = form.amount.toDoubleOrNull() ?: return
        val categoryId = form.categoryId ?: return
        val accountId = form.accountId ?: return

        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // 计算首次执行日期
                when (form.frequency) {
                    RecurringFrequency.DAILY -> {
                        // 明天
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    RecurringFrequency.WEEKLY -> {
                        // 下一个指定的星期几
                        val targetDayOfWeek = form.dayOfPeriod
                        while (calendar.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                            calendar.add(Calendar.WEEK_OF_YEAR, 1)
                        }
                    }
                    RecurringFrequency.MONTHLY -> {
                        // 下一个指定日期
                        val targetDay = form.dayOfPeriod.coerceAtMost(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                        calendar.set(Calendar.DAY_OF_MONTH, targetDay)
                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                            calendar.add(Calendar.MONTH, 1)
                            val newTargetDay = form.dayOfPeriod.coerceAtMost(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                            calendar.set(Calendar.DAY_OF_MONTH, newTargetDay)
                        }
                    }
                    RecurringFrequency.YEARLY -> {
                        // 下一年的指定日期
                        calendar.set(Calendar.MONTH, 0)
                        calendar.set(Calendar.DAY_OF_MONTH, form.dayOfPeriod)
                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                            calendar.add(Calendar.YEAR, 1)
                        }
                    }
                }

                val entity = RecurringTransactionEntity(
                    id = form.editingId ?: 0,
                    name = form.name,
                    amount = amount,
                    type = form.type,
                    categoryId = categoryId,
                    accountId = accountId,
                    frequency = form.frequency,
                    dayOfPeriod = form.dayOfPeriod,
                    note = form.note,
                    autoExecute = form.autoExecute,
                    startDate = System.currentTimeMillis(),
                    nextExecutionDate = calendar.timeInMillis
                )

                if (form.isEditing && form.editingId != null) {
                    recurringRepository.update(entity)
                } else {
                    recurringRepository.create(entity)
                }

                _uiState.value = _uiState.value.copy(showDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun toggleActive(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringRepository.setActive(recurring.id, !recurring.isActive)
        }
    }

    fun deleteRecurring(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            recurringRepository.delete(recurring)
        }
    }

    fun processAllDue() {
        viewModelScope.launch {
            try {
                val count = recurringRepository.processAllDue()
                _uiState.value = _uiState.value.copy(
                    successMessage = if (count > 0) "已自动记录 $count 笔交易" else "暂无到期的固定收支"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }
}

/**
 * UI状态
 */
data class RecurringTransactionUiState(
    val isLoading: Boolean = true,
    val recurringTransactions: List<RecurringTransactionEntity> = emptyList(),
    val totalFixedIncome: Double = 0.0,
    val totalFixedExpense: Double = 0.0,
    val showDialog: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

/**
 * 表单状态
 */
data class RecurringFormState(
    val isEditing: Boolean = false,
    val editingId: Long? = null,
    val name: String = "",
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfPeriod: Int = 1,
    val note: String = "",
    val autoExecute: Boolean = true,
    val availableCategories: List<CategoryOption> = emptyList(),
    val availableAccounts: List<AccountOption> = emptyList()
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                amount.toDoubleOrNull()!! > 0 &&
                categoryId != null &&
                accountId != null
}

data class CategoryOption(
    val id: Long,
    val name: String,
    val icon: String,
    val type: TransactionType
)

data class AccountOption(
    val id: Long,
    val name: String,
    val icon: String
)
