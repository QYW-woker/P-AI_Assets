package com.example.smartledger.presentation.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 分类管理ViewModel
 */
@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 先检查是否有分类，如果没有则初始化默认分类
                var expenseCategories = categoryRepository.getCategoriesByType(TransactionType.EXPENSE).first()
                var incomeCategories = categoryRepository.getCategoriesByType(TransactionType.INCOME).first()

                if (expenseCategories.isEmpty() && incomeCategories.isEmpty()) {
                    // 初始化默认分类
                    categoryRepository.initDefaultCategories()
                    // 重新加载
                    expenseCategories = categoryRepository.getCategoriesByType(TransactionType.EXPENSE).first()
                    incomeCategories = categoryRepository.getCategoriesByType(TransactionType.INCOME).first()
                }

                _uiState.value = CategoryManagementUiState(
                    expenseCategories = expenseCategories.map { it.toUiModel() },
                    incomeCategories = incomeCategories.map { it.toUiModel() },
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

    fun addCategory(
        name: String,
        icon: String,
        color: String,
        type: TransactionType
    ) {
        viewModelScope.launch {
            val maxSortOrder = when (type) {
                TransactionType.EXPENSE -> _uiState.value.expenseCategories.maxOfOrNull { it.sortOrder } ?: 0
                TransactionType.INCOME -> _uiState.value.incomeCategories.maxOfOrNull { it.sortOrder } ?: 0
                TransactionType.TRANSFER -> 0 // 转账不需要分类
            }

            val category = CategoryEntity(
                name = name,
                icon = icon,
                color = color,
                type = type,
                sortOrder = maxSortOrder + 1,
                isSystem = false
            )
            categoryRepository.insertCategory(category)
            loadCategories()
        }
    }

    fun updateCategory(
        id: Long,
        name: String,
        icon: String,
        color: String
    ) {
        viewModelScope.launch {
            val existing = categoryRepository.getCategoryById(id)
            if (existing != null) {
                val updated = existing.copy(
                    name = name,
                    icon = icon,
                    color = color
                )
                categoryRepository.updateCategory(updated)
                loadCategories()
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(categoryId)
            if (category != null && !category.isSystem) {
                categoryRepository.deleteCategory(category)
                loadCategories()
            }
        }
    }

    fun refresh() {
        loadCategories()
    }

    private fun CategoryEntity.toUiModel() = CategoryUiModel(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        isSystem = isSystem,
        sortOrder = sortOrder
    )
}

/**
 * 分类管理UI状态
 */
data class CategoryManagementUiState(
    val expenseCategories: List<CategoryUiModel> = emptyList(),
    val incomeCategories: List<CategoryUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * 分类UI模型
 */
data class CategoryUiModel(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType,
    val isSystem: Boolean,
    val sortOrder: Int
)
