package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 分类数据仓库接口
 */
interface CategoryRepository {

    /**
     * 获取指定类型的分类
     */
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    /**
     * 获取所有活跃分类
     */
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>

    /**
     * 获取子分类
     */
    fun getSubCategories(parentId: Long): Flow<List<CategoryEntity>>

    /**
     * 根据ID获取分类
     */
    suspend fun getCategoryById(id: Long): CategoryEntity?

    /**
     * 插入分类
     */
    suspend fun insertCategory(category: CategoryEntity): Long

    /**
     * 更新分类
     */
    suspend fun updateCategory(category: CategoryEntity)

    /**
     * 删除分类（软删除）
     */
    suspend fun deleteCategory(category: CategoryEntity)

    /**
     * 获取所有分类（用于备份）
     */
    suspend fun getAllCategories(): List<CategoryEntity>

    /**
     * 初始化默认分类
     */
    suspend fun initDefaultCategories()

    /**
     * 清除自定义分类（保留系统默认分类）
     */
    suspend fun clearCustomCategories()
}
