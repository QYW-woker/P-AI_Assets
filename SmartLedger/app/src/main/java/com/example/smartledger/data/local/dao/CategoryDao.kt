package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 分类DAO
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE type = :type AND isActive = 1 ORDER BY sortOrder")
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY type, sortOrder")
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY type, sortOrder")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE parentId = :parentId AND isActive = 1 ORDER BY sortOrder")
    fun getSubCategories(parentId: Long): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE isSystem = 0")
    suspend fun deleteAllCustomCategories()

    @Query("SELECT COUNT(*) FROM categories WHERE type = :type AND isActive = 1")
    suspend fun getCategoryCountByType(type: TransactionType): Int

    @Query("SELECT * FROM categories ORDER BY type, sortOrder")
    suspend fun getAllCategoriesForBackup(): List<CategoryEntity>

    @Query("DELETE FROM categories")
    suspend fun clearAll()
}
