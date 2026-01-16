package com.example.smartledger.presentation.ui.search

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
 * 搜索页面ViewModel
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var allTransactions: List<TransactionEntity> = emptyList()
    private var categoryMap: Map<Long, CategoryInfo> = emptyMap()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 加载所有分类信息
                val categories = categoryRepository.getAllActiveCategories().first()
                categoryMap = categories.associate {
                    it.id to CategoryInfo(it.name, it.icon, it.color)
                }

                // 加载最近3个月的交易
                val calendar = Calendar.getInstance()
                val endDate = calendar.timeInMillis
                calendar.add(Calendar.MONTH, -3)
                val startDate = calendar.timeInMillis

                allTransactions = transactionRepository.getTransactionsByDateRange(startDate, endDate).first()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                isLoading = true
            )

            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    results = emptyList(),
                    isLoading = false
                )
                return@launch
            }

            val filteredTransactions = allTransactions.filter { transaction ->
                val categoryInfo = categoryMap[transaction.categoryId]
                val categoryName = categoryInfo?.name ?: ""

                transaction.note.contains(query, ignoreCase = true) ||
                transaction.tags.contains(query, ignoreCase = true) ||
                categoryName.contains(query, ignoreCase = true) ||
                String.format("%.2f", transaction.amount).contains(query)
            }

            val results = filteredTransactions.map { it.toSearchResult() }

            _uiState.value = _uiState.value.copy(
                results = results,
                isLoading = false
            )
        }
    }

    fun applyFilters(
        transactionType: TransactionType? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        categoryIds: List<Long>? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            var filteredTransactions = allTransactions

            // 按类型筛选
            transactionType?.let { type ->
                filteredTransactions = filteredTransactions.filter { it.type == type }
            }

            // 按金额范围筛选
            minAmount?.let { min ->
                filteredTransactions = filteredTransactions.filter { it.amount >= min }
            }
            maxAmount?.let { max ->
                filteredTransactions = filteredTransactions.filter { it.amount <= max }
            }

            // 按日期范围筛选
            startDate?.let { start ->
                filteredTransactions = filteredTransactions.filter { it.date >= start }
            }
            endDate?.let { end ->
                filteredTransactions = filteredTransactions.filter { it.date <= end }
            }

            // 按分类筛选
            categoryIds?.let { ids ->
                if (ids.isNotEmpty()) {
                    filteredTransactions = filteredTransactions.filter { it.categoryId in ids }
                }
            }

            // 按搜索关键词筛选
            val query = _uiState.value.searchQuery
            if (query.isNotBlank()) {
                filteredTransactions = filteredTransactions.filter { transaction ->
                    val categoryInfo = categoryMap[transaction.categoryId]
                    val categoryName = categoryInfo?.name ?: ""

                    transaction.note.contains(query, ignoreCase = true) ||
                    transaction.tags.contains(query, ignoreCase = true) ||
                    categoryName.contains(query, ignoreCase = true)
                }
            }

            val results = filteredTransactions.map { it.toSearchResult() }

            _uiState.value = _uiState.value.copy(
                results = results,
                isLoading = false,
                filterApplied = transactionType != null || minAmount != null ||
                               maxAmount != null || startDate != null ||
                               endDate != null || !categoryIds.isNullOrEmpty()
            )
        }
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            results = emptyList(),
            filterApplied = false
        )
    }

    private fun TransactionEntity.toSearchResult(): SearchResultItem {
        val categoryInfo = categoryMap[categoryId]
        return SearchResultItem(
            id = id,
            amount = amount,
            type = type,
            categoryName = categoryInfo?.name ?: "未分类",
            categoryIcon = categoryInfo?.icon ?: "📦",
            categoryColor = categoryInfo?.color ?: "#ECEFF1",
            note = note,
            date = dateFormat.format(Date(date)),
            tags = tags
        )
    }

    private data class CategoryInfo(
        val name: String,
        val icon: String,
        val color: String
    )
}

/**
 * 搜索页面UI状态
 */
data class SearchUiState(
    val searchQuery: String = "",
    val results: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val filterApplied: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 搜索结果项
 */
data class SearchResultItem(
    val id: Long,
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val note: String,
    val date: String,
    val tags: String
)
