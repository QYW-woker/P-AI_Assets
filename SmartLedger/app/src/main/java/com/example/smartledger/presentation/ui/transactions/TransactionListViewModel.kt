package com.example.smartledger.presentation.ui.transactions

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
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    // 筛选选项数据
    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())

    private var categoryMap: Map<Long, CategoryInfo> = emptyMap()
    private var accountMap: Map<Long, AccountInfo> = emptyMap()
    private var allTransactions: List<TransactionEntity> = emptyList()

    init {
        loadCategories()
        loadAccounts()
        loadCurrentMonthTransactions()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = categoryRepository.getAllActiveCategories().first()
            categoryMap = categories.associate {
                it.id to CategoryInfo(it.id, it.name, it.icon, it.color, it.type)
            }
            // 更新筛选选项
            _filterOptions.value = _filterOptions.value.copy(
                availableCategories = categories.map {
                    FilterCategory(it.id, it.name, it.icon, it.type)
                }
            )
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val accounts = accountRepository.getAllActiveAccounts().first()
            accountMap = accounts.associate {
                it.id to AccountInfo(it.id, it.name, it.icon, it.color)
            }
            // 更新筛选选项
            _filterOptions.value = _filterOptions.value.copy(
                availableAccounts = accounts.map {
                    FilterAccount(it.id, it.name, it.icon)
                }
            )
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
            selectedMonth = month,
            timePeriod = TimePeriod.MONTH
        )

        loadTransactions(startDate, endDate)
    }

    /**
     * 设置时间筛选周期
     */
    fun setTimePeriod(period: TimePeriod) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        when (period) {
            TimePeriod.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimePeriod.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimePeriod.QUARTER -> {
                val currentMonth = calendar.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                calendar.set(Calendar.MONTH, quarterStartMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimePeriod.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimePeriod.ALL -> {
                calendar.set(2020, Calendar.JANUARY, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimePeriod.CUSTOM -> {
                // 自定义时间范围使用 setCustomDateRange 方法
                return
            }
        }

        val startDate = calendar.timeInMillis
        _uiState.value = _uiState.value.copy(
            timePeriod = period,
            customStartDate = startDate,
            customEndDate = endDate
        )
        loadTransactions(startDate, endDate)
    }

    /**
     * 设置自定义日期范围
     */
    fun setCustomDateRange(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(
            timePeriod = TimePeriod.CUSTOM,
            customStartDate = startDate,
            customEndDate = endDate
        )
        loadTransactions(startDate, endDate)
    }

    private fun loadTransactions(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                allTransactions = transactionRepository.getTransactionsByDateRange(startDate, endDate)

                applyFilters()

                _uiState.value = _uiState.value.copy(
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

    /**
     * 应用所有筛选条件
     */
    private fun applyFilters() {
        val state = _uiState.value

        var filtered = allTransactions

        // 类型筛选
        if (state.filterType != null) {
            filtered = filtered.filter { it.type == state.filterType }
        }

        // 分类筛选
        if (state.filterCategoryIds.isNotEmpty()) {
            filtered = filtered.filter { it.categoryId in state.filterCategoryIds }
        }

        // 账户筛选
        if (state.filterAccountIds.isNotEmpty()) {
            filtered = filtered.filter { it.accountId in state.filterAccountIds }
        }

        // 金额范围筛选
        if (state.minAmount != null) {
            filtered = filtered.filter { it.amount >= state.minAmount }
        }
        if (state.maxAmount != null) {
            filtered = filtered.filter { it.amount <= state.maxAmount }
        }

        // 关键词筛选
        if (state.searchKeyword.isNotBlank()) {
            filtered = filtered.filter {
                it.note.contains(state.searchKeyword, ignoreCase = true) ||
                it.tags.contains(state.searchKeyword, ignoreCase = true) ||
                categoryMap[it.categoryId]?.name?.contains(state.searchKeyword, ignoreCase = true) == true
            }
        }

        // 按日期分组
        val groupedTransactions = filtered
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

        val totalIncome = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        // 计算筛选统计
        val filterStats = FilterStatistics(
            totalCount = filtered.size,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            percentOfTotal = if (allTransactions.isNotEmpty()) {
                (filtered.size.toFloat() / allTransactions.size * 100)
            } else 0f,
            avgAmount = if (filtered.isNotEmpty()) {
                filtered.sumOf { it.amount } / filtered.size
            } else 0.0
        )

        _uiState.value = _uiState.value.copy(
            transactionGroups = groupedTransactions,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            filterStats = filterStats,
            isFiltered = state.filterType != null ||
                        state.filterCategoryIds.isNotEmpty() ||
                        state.filterAccountIds.isNotEmpty() ||
                        state.minAmount != null ||
                        state.maxAmount != null ||
                        state.searchKeyword.isNotBlank()
        )
    }

    fun filterByType(type: TransactionType?) {
        _uiState.value = _uiState.value.copy(filterType = type)
        applyFilters()
    }

    fun filterByCategories(categoryIds: Set<Long>) {
        _uiState.value = _uiState.value.copy(filterCategoryIds = categoryIds)
        applyFilters()
    }

    fun filterByAccounts(accountIds: Set<Long>) {
        _uiState.value = _uiState.value.copy(filterAccountIds = accountIds)
        applyFilters()
    }

    fun filterByAmountRange(minAmount: Double?, maxAmount: Double?) {
        _uiState.value = _uiState.value.copy(
            minAmount = minAmount,
            maxAmount = maxAmount
        )
        applyFilters()
    }

    fun filterByKeyword(keyword: String) {
        _uiState.value = _uiState.value.copy(searchKeyword = keyword)
        applyFilters()
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            filterType = null,
            filterCategoryIds = emptySet(),
            filterAccountIds = emptySet(),
            minAmount = null,
            maxAmount = null,
            searchKeyword = ""
        )
        applyFilters()
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(transactionId)
            if (transaction != null) {
                transactionRepository.deleteTransaction(transaction)
                // 重新加载当前数据
                val state = _uiState.value
                if (state.timePeriod == TimePeriod.MONTH) {
                    loadTransactionsByMonth(state.selectedYear, state.selectedMonth)
                } else {
                    setTimePeriod(state.timePeriod)
                }
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

    // ============ 批量选择功能 ============

    /**
     * 切换选择模式
     */
    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = !_uiState.value.isSelectionMode,
            selectedTransactionIds = emptySet()
        )
    }

    /**
     * 退出选择模式
     */
    fun exitSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = false,
            selectedTransactionIds = emptySet()
        )
    }

    /**
     * 切换单个交易的选中状态
     */
    fun toggleTransactionSelection(transactionId: Long) {
        val currentSelected = _uiState.value.selectedTransactionIds
        _uiState.value = _uiState.value.copy(
            selectedTransactionIds = if (transactionId in currentSelected) {
                currentSelected - transactionId
            } else {
                currentSelected + transactionId
            }
        )
    }

    /**
     * 全选当前筛选结果
     */
    fun selectAll() {
        val allIds = _uiState.value.transactionGroups
            .flatMap { it.transactions }
            .map { it.id }
            .toSet()
        _uiState.value = _uiState.value.copy(selectedTransactionIds = allIds)
    }

    /**
     * 清除选择
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedTransactionIds = emptySet())
    }

    /**
     * 批量删除选中的交易
     */
    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedTransactionIds
            if (selectedIds.isEmpty()) return@launch

            try {
                selectedIds.forEach { id ->
                    val transaction = transactionRepository.getTransactionById(id)
                    if (transaction != null) {
                        transactionRepository.deleteTransaction(transaction)
                    }
                }

                // 退出选择模式并重新加载数据
                _uiState.value = _uiState.value.copy(
                    isSelectionMode = false,
                    selectedTransactionIds = emptySet()
                )

                // 重新加载数据
                val state = _uiState.value
                if (state.timePeriod == TimePeriod.MONTH) {
                    loadTransactionsByMonth(state.selectedYear, state.selectedMonth)
                } else {
                    setTimePeriod(state.timePeriod)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "批量删除失败: ${e.message}"
                )
            }
        }
    }

    // ============ 复制功能 ============

    /**
     * 复制交易（创建一个相同内容的新交易，日期为当前时间）
     */
    fun copyTransaction(transactionId: Long) {
        viewModelScope.launch {
            try {
                val originalTransaction = transactionRepository.getTransactionById(transactionId)
                if (originalTransaction != null) {
                    // 创建副本，使用当前时间
                    val copiedTransaction = originalTransaction.copy(
                        id = 0, // 新ID
                        date = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    transactionRepository.insertTransaction(copiedTransaction)

                    // 显示成功提示
                    _uiState.value = _uiState.value.copy(showCopySuccess = true)

                    // 重新加载当前数据
                    val state = _uiState.value
                    if (state.timePeriod == TimePeriod.MONTH) {
                        loadTransactionsByMonth(state.selectedYear, state.selectedMonth)
                    } else {
                        setTimePeriod(state.timePeriod)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "复制失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 隐藏复制成功提示
     */
    fun dismissCopySuccess() {
        _uiState.value = _uiState.value.copy(showCopySuccess = false)
    }

    private fun TransactionEntity.toTransactionItem(): TransactionItem {
        val categoryInfo = categoryMap[categoryId]
        val accountInfo = accountMap[accountId]
        return TransactionItem(
            id = id,
            amount = amount,
            type = type,
            categoryId = categoryId,
            categoryName = categoryInfo?.name ?: "未分类",
            categoryIcon = categoryInfo?.icon ?: "📦",
            categoryColor = categoryInfo?.color ?: "#ECEFF1",
            accountId = accountId,
            accountName = accountInfo?.name ?: "未知账户",
            note = note,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(date)),
            date = date
        )
    }

    private data class CategoryInfo(
        val id: Long,
        val name: String,
        val icon: String,
        val color: String,
        val type: TransactionType
    )

    private data class AccountInfo(
        val id: Long,
        val name: String,
        val icon: String,
        val color: String
    )
}

/**
 * 时间周期枚举
 */
enum class TimePeriod {
    WEEK,      // 本周
    MONTH,     // 本月
    QUARTER,   // 本季度
    YEAR,      // 本年
    ALL,       // 全部
    CUSTOM     // 自定义
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
    val timePeriod: TimePeriod = TimePeriod.MONTH,
    val customStartDate: Long = 0,
    val customEndDate: Long = 0,
    // 筛选条件
    val filterType: TransactionType? = null,
    val filterCategoryIds: Set<Long> = emptySet(),
    val filterAccountIds: Set<Long> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val searchKeyword: String = "",
    // 筛选状态
    val isFiltered: Boolean = false,
    val filterStats: FilterStatistics = FilterStatistics(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // 批量选择状态
    val isSelectionMode: Boolean = false,
    val selectedTransactionIds: Set<Long> = emptySet(),
    // 复制成功提示
    val showCopySuccess: Boolean = false
)

/**
 * 筛选统计
 */
data class FilterStatistics(
    val totalCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val percentOfTotal: Float = 0f,
    val avgAmount: Double = 0.0
)

/**
 * 筛选选项
 */
data class FilterOptions(
    val availableCategories: List<FilterCategory> = emptyList(),
    val availableAccounts: List<FilterAccount> = emptyList()
)

data class FilterCategory(
    val id: Long,
    val name: String,
    val icon: String,
    val type: TransactionType
)

data class FilterAccount(
    val id: Long,
    val name: String,
    val icon: String
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
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val accountId: Long,
    val accountName: String,
    val note: String,
    val time: String,
    val date: Long = 0
)
