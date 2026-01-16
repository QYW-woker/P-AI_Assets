package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.CategoryDao
import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(type)
    }

    override fun getAllActiveCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllActiveCategories()
    }

    override fun getSubCategories(parentId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getSubCategories(parentId)
    }

    override suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }

    override suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.softDelete(category.id)
    }

    override suspend fun getAllCategories(): List<CategoryEntity> {
        return categoryDao.getAllCategoriesForBackup()
    }

    override suspend fun initDefaultCategories() {
        // 检查是否已有分类
        val existingCount = categoryDao.getCategoryCountByType(TransactionType.EXPENSE)
        if (existingCount > 0) return

        // 支出分类
        val expenseCategories = listOf(
            CategoryEntity(name = "餐饮", icon = "🍜", color = "#FF6B6B", type = TransactionType.EXPENSE, sortOrder = 1, isSystem = true),
            CategoryEntity(name = "交通", icon = "🚗", color = "#4ECDC4", type = TransactionType.EXPENSE, sortOrder = 2, isSystem = true),
            CategoryEntity(name = "购物", icon = "🛒", color = "#45B7D1", type = TransactionType.EXPENSE, sortOrder = 3, isSystem = true),
            CategoryEntity(name = "居住", icon = "🏠", color = "#96CEB4", type = TransactionType.EXPENSE, sortOrder = 4, isSystem = true),
            CategoryEntity(name = "娱乐", icon = "🎮", color = "#FFEAA7", type = TransactionType.EXPENSE, sortOrder = 5, isSystem = true),
            CategoryEntity(name = "医疗", icon = "💊", color = "#DDA0DD", type = TransactionType.EXPENSE, sortOrder = 6, isSystem = true),
            CategoryEntity(name = "教育", icon = "📚", color = "#98D8C8", type = TransactionType.EXPENSE, sortOrder = 7, isSystem = true),
            CategoryEntity(name = "人情", icon = "🎁", color = "#F7DC6F", type = TransactionType.EXPENSE, sortOrder = 8, isSystem = true),
            CategoryEntity(name = "通讯", icon = "📱", color = "#BB8FCE", type = TransactionType.EXPENSE, sortOrder = 9, isSystem = true),
            CategoryEntity(name = "其他", icon = "📦", color = "#AEB6BF", type = TransactionType.EXPENSE, sortOrder = 10, isSystem = true)
        )

        // 收入分类
        val incomeCategories = listOf(
            CategoryEntity(name = "工资", icon = "💰", color = "#2ECC71", type = TransactionType.INCOME, sortOrder = 1, isSystem = true),
            CategoryEntity(name = "奖金", icon = "🏆", color = "#F39C12", type = TransactionType.INCOME, sortOrder = 2, isSystem = true),
            CategoryEntity(name = "副业", icon = "💼", color = "#3498DB", type = TransactionType.INCOME, sortOrder = 3, isSystem = true),
            CategoryEntity(name = "投资收益", icon = "📈", color = "#9B59B6", type = TransactionType.INCOME, sortOrder = 4, isSystem = true),
            CategoryEntity(name = "礼金", icon = "🧧", color = "#E74C3C", type = TransactionType.INCOME, sortOrder = 5, isSystem = true),
            CategoryEntity(name = "其他", icon = "💵", color = "#1ABC9C", type = TransactionType.INCOME, sortOrder = 6, isSystem = true)
        )

        expenseCategories.forEach { categoryDao.insert(it) }
        incomeCategories.forEach { categoryDao.insert(it) }
    }
}
