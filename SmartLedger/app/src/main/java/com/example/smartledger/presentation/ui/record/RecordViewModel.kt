package com.example.smartledger.presentation.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 记账页面ViewModel
 */
@HiltViewModel
class RecordViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var selectedDate: Long = System.currentTimeMillis()
    private var selectedAccountId: Long = 1L

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date())

                // 加载分类，如果没有则初始化
                var expenseCategories = categoryRepository.getCategoriesByType(TransactionType.EXPENSE).first()
                if (expenseCategories.isEmpty()) {
                    categoryRepository.initDefaultCategories()
                    expenseCategories = categoryRepository.getCategoriesByType(TransactionType.EXPENSE).first()
                }

                val categoryUiModels = expenseCategories.map { category ->
                    CategoryUiModel(
                        id = category.id,
                        name = category.name,
                        icon = category.icon,
                        color = category.color
                    )
                }

                // 加载默认账户
                val accounts = accountRepository.getAllActiveAccounts().first()
                val defaultAccount = accounts.firstOrNull()

                _uiState.update {
                    it.copy(
                        dateText = today,
                        categories = categoryUiModels,
                        accountName = defaultAccount?.name ?: "现金",
                        accounts = accounts.map { acc ->
                            AccountUiModel(acc.id, acc.name, acc.icon)
                        }
                    )
                }

                if (defaultAccount != null) {
                    selectedAccountId = defaultAccount.id
                }
            } catch (e: Exception) {
                // 使用默认数据
                val today = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date())
                _uiState.update {
                    it.copy(
                        dateText = today,
                        categories = getDefaultExpenseCategories(),
                        accountName = "现金"
                    )
                }
            }
        }
    }

    fun setTransactionType(type: Int) {
        viewModelScope.launch {
            val transactionType = when (type) {
                0 -> TransactionType.EXPENSE
                1 -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }

            var categories = categoryRepository.getCategoriesByType(transactionType).first()
            if (categories.isEmpty()) {
                categoryRepository.initDefaultCategories()
                categories = categoryRepository.getCategoriesByType(transactionType).first()
            }

            val categoryUiModels = categories.map { category ->
                CategoryUiModel(
                    id = category.id,
                    name = category.name,
                    icon = category.icon,
                    color = category.color
                )
            }

            _uiState.update {
                it.copy(
                    transactionType = type,
                    categories = categoryUiModels,
                    selectedCategoryId = null
                )
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        updateCanSave()
    }

    fun selectAccount(accountId: Long) {
        viewModelScope.launch {
            selectedAccountId = accountId
            val account = accountRepository.getAccountById(accountId)
            _uiState.update { it.copy(accountName = account?.name ?: "现金") }
        }
    }

    fun setDate(timestamp: Long) {
        selectedDate = timestamp
        val dateText = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date(timestamp))
        _uiState.update { it.copy(dateText = dateText) }
    }

    fun appendNumber(number: String) {
        _uiState.update { state ->
            val currentAmount = state.amountText
            // 限制长度和小数位数
            if (currentAmount.length >= 10) return@update state
            if (currentAmount.contains(".") && currentAmount.substringAfter(".").length >= 2) {
                return@update state
            }
            // 避免前导零
            val newAmount = if (currentAmount == "0" && number != ".") {
                number
            } else {
                currentAmount + number
            }
            state.copy(amountText = newAmount)
        }
        updateCanSave()
    }

    fun appendDot() {
        _uiState.update { state ->
            if (state.amountText.contains(".")) return@update state
            val newAmount = if (state.amountText.isEmpty()) "0." else state.amountText + "."
            state.copy(amountText = newAmount)
        }
    }

    fun backspace() {
        _uiState.update { state ->
            val newAmount = state.amountText.dropLast(1)
            state.copy(amountText = newAmount)
        }
        updateCanSave()
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            val amount = state.amountText.toDoubleOrNull() ?: return@launch
            val categoryId = state.selectedCategoryId ?: return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val transactionType = when (state.transactionType) {
                    0 -> TransactionType.EXPENSE
                    1 -> TransactionType.INCOME
                    else -> TransactionType.TRANSFER
                }

                val transaction = TransactionEntity(
                    type = transactionType,
                    amount = amount,
                    categoryId = categoryId,
                    accountId = selectedAccountId,
                    note = state.note,
                    date = selectedDate,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                transactionRepository.insertTransaction(transaction)

                // 更新账户余额
                if (transactionType == TransactionType.EXPENSE) {
                    accountRepository.incrementBalance(selectedAccountId, -amount)
                } else if (transactionType == TransactionType.INCOME) {
                    accountRepository.incrementBalance(selectedAccountId, amount)
                }

                // 重置状态
                _uiState.update {
                    it.copy(
                        amountText = "",
                        selectedCategoryId = null,
                        note = "",
                        isLoading = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun updateCanSave() {
        _uiState.update { state ->
            val amount = state.amountText.toDoubleOrNull() ?: 0.0
            state.copy(canSave = amount > 0 && state.selectedCategoryId != null)
        }
    }

    private fun getDefaultExpenseCategories(): List<CategoryUiModel> {
        return listOf(
            CategoryUiModel(1, "餐饮美食", "🍜", "#FFF3E0"),
            CategoryUiModel(2, "交通出行", "🚗", "#E3F2FD"),
            CategoryUiModel(3, "购物消费", "🛒", "#FCE4EC"),
            CategoryUiModel(4, "娱乐休闲", "🎮", "#F3E5F5"),
            CategoryUiModel(5, "居住生活", "🏠", "#E8F5E9"),
            CategoryUiModel(6, "医疗健康", "💊", "#FFF8E1"),
            CategoryUiModel(7, "教育学习", "📚", "#E0F7FA"),
            CategoryUiModel(8, "人情往来", "🎁", "#FFEBEE"),
            CategoryUiModel(9, "通讯网络", "📱", "#E8EAF6"),
            CategoryUiModel(10, "其他支出", "📦", "#ECEFF1")
        )
    }

    private fun getDefaultIncomeCategories(): List<CategoryUiModel> {
        return listOf(
            CategoryUiModel(11, "工资薪酬", "💰", "#E8F5E9"),
            CategoryUiModel(12, "奖金收入", "🏆", "#FFF8E1"),
            CategoryUiModel(13, "投资收益", "📈", "#E3F2FD"),
            CategoryUiModel(14, "兼职收入", "💼", "#F3E5F5"),
            CategoryUiModel(15, "其他收入", "💵", "#ECEFF1")
        )
    }
}

/**
 * 记账页面UI状态
 */
data class RecordUiState(
    val transactionType: Int = 0, // 0=支出, 1=收入, 2=转账
    val amountText: String = "",
    val selectedCategoryId: Long? = null,
    val categories: List<CategoryUiModel> = emptyList(),
    val accounts: List<AccountUiModel> = emptyList(),
    val dateText: String = "",
    val accountName: String = "",
    val note: String = "",
    val canSave: Boolean = false,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class AccountUiModel(
    val id: Long,
    val name: String,
    val icon: String
)
