package com.example.smartledger.presentation.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    // TODO: 注入Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val today = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date())
            _uiState.update {
                it.copy(
                    dateText = today,
                    categories = getDefaultExpenseCategories(),
                    accountName = "微信支付"
                )
            }
        }
    }

    fun setTransactionType(type: Int) {
        val categories = when (type) {
            0 -> getDefaultExpenseCategories()
            1 -> getDefaultIncomeCategories()
            else -> getDefaultExpenseCategories()
        }
        _uiState.update {
            it.copy(
                transactionType = type,
                categories = categories,
                selectedCategoryId = null
            )
        }
    }

    fun selectCategory(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        updateCanSave()
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

            // TODO: 保存到数据库
            // transactionRepository.addTransaction(...)

            // 重置状态
            _uiState.update {
                it.copy(
                    amountText = "",
                    selectedCategoryId = null,
                    note = ""
                )
            }
        }
    }

    private fun updateCanSave() {
        _uiState.update { state ->
            val amount = state.amountText.toDoubleOrNull() ?: 0.0
            state.copy(canSave = amount > 0 && state.selectedCategoryId != null)
        }
    }

    private fun getDefaultExpenseCategories(): List<CategoryUiModel> {
        return listOf(
            CategoryUiModel(1, "餐饮美食", "\uD83C\uDF5C", "#FFF3E0"),
            CategoryUiModel(2, "交通出行", "\uD83D\uDE87", "#E3F2FD"),
            CategoryUiModel(3, "购物消费", "\uD83D\uDED2", "#FCE4EC"),
            CategoryUiModel(4, "娱乐休闲", "\uD83C\uDFAC", "#F3E5F5"),
            CategoryUiModel(5, "居住生活", "\uD83C\uDFE0", "#E8F5E9"),
            CategoryUiModel(6, "医疗健康", "\uD83D\uDC8A", "#FFF8E1"),
            CategoryUiModel(7, "教育学习", "\uD83D\uDCDA", "#E0F7FA"),
            CategoryUiModel(8, "人情往来", "\uD83C\uDF81", "#FFEBEE"),
            CategoryUiModel(9, "金融保险", "\uD83C\uDFE6", "#E8EAF6"),
            CategoryUiModel(10, "其他支出", "\uD83D\uDCDD", "#ECEFF1")
        )
    }

    private fun getDefaultIncomeCategories(): List<CategoryUiModel> {
        return listOf(
            CategoryUiModel(11, "工资薪酬", "\uD83D\uDCB0", "#E8F5E9"),
            CategoryUiModel(12, "奖金收入", "\uD83C\uDF96", "#FFF8E1"),
            CategoryUiModel(13, "投资收益", "\uD83D\uDCC8", "#E3F2FD"),
            CategoryUiModel(14, "兼职收入", "\uD83D\uDCBC", "#F3E5F5"),
            CategoryUiModel(15, "其他收入", "\uD83D\uDCB5", "#ECEFF1")
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
    val dateText: String = "",
    val accountName: String = "",
    val note: String = "",
    val canSave: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
