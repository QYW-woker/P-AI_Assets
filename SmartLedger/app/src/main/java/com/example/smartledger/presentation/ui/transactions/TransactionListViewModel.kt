package com.example.smartledger.presentation.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
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
 * 交易列表ViewModel
 */
@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())

    private var categoryMap: Map<Long, CategoryInfo> = emptyMap()

    init {
        loadCategories()
        loadCurrentMonthTransactions()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = categoryRepository.getAllActiveCategories().first()
            categoryMap = categories.associate {
                it.id to CategoryInfo(it.name, it.icon, it.color)
            }
        }
    }

    fun loadCurrentMonthTransactions() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.timeInMillis

        loadTransactions(startDate, endDate)
    }

    fun loadTransactionsByMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.timeInMillis

        _uiState.value = _uiState.value.copy(
            selectedYear = year,
            selectedMonth = month
        )

        loadTransactions(startDate, endDate)
    }

    private fun loadTransactions(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val transactions = transactionRepository.getTransactionsByDateRange(startDate, endDate).first()

                // 按日期分组
                val groupedTransactions = transactions
                    .sortedByDescending { it.date }
                    .groupBy { dateFormat.format(Date(it.date)) }
                    .map { (date, items) ->
                        TransactionGroup(
                            date = date,
                            dayTotal = items.sumOf {
                                if (it.type == TransactionType.EXPENSE) -it.amount else it.amount
                            },
                            transactions = items.map { it.toTransactionItem() }
                        )
                    }

                val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                _uiState.value = _uiState.value.copy(
                    transactionGroups = groupedTransactions,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = totalIncome - totalExpense,
                    isLoading = false,
                    monthTitle = monthFormat.format(Date(startDate))
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun filterByType(type: TransactionType?) {
        _uiState.value = _uiState.value.copy(filterType = type)
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(transactionId)
            if (transaction != null) {
                transactionRepository.deleteTransaction(transaction)
                // 重新加载当前月份数据
                val state = _uiState.value
                loadTransactionsByMonth(state.selectedYear, state.selectedMonth)
            }
        }
    }

    fun goToPreviousMonth() {
        val state = _uiState.value
        var newYear = state.selectedYear
        var newMonth = state.selectedMonth - 1
        if (newMonth < 0) {
            newMonth = 11
            newYear--
        }
        loadTransactionsByMonth(newYear, newMonth)
    }

    fun goToNextMonth() {
        val state = _uiState.value
        var newYear = state.selectedYear
        var newMonth = state.selectedMonth + 1
        if (newMonth > 11) {
            newMonth = 0
            newYear++
        }
        loadTransactionsByMonth(newYear, newMonth)
    }

    private fun TransactionEntity.toTransactionItem(): TransactionItem {
        val categoryInfo = categoryMap[categoryId]
        return TransactionItem(
            id = id,
            amount = amount,
            type = type,
            categoryName = categoryInfo?.name ?: "未分类",
            categoryIcon = categoryInfo?.icon ?: "📦",
            categoryColor = categoryInfo?.color ?: "#ECEFF1",
            note = note,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(date))
        )
    }

    private data class CategoryInfo(
        val name: String,
        val icon: String,
        val color: String
    )
}

/**
 * 交易列表UI状态
 */
data class TransactionListUiState(
    val transactionGroups: List<TransactionGroup> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val monthTitle: String = "",
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val filterType: TransactionType? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * 交易分组（按日期）
 */
data class TransactionGroup(
    val date: String,
    val dayTotal: Double,
    val transactions: List<TransactionItem>
)

/**
 * 交易项
 */
data class TransactionItem(
    val id: Long,
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val note: String,
    val time: String
)
