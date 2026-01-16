package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * 分类统计结果
 */
data class CategorySummaryResult(
    val categoryId: Long,
    val totalAmount: Double,
    val count: Int
)

/**
 * 每日汇总结果
 */
data class DailyTotalResult(
    val dayTimestamp: Long,
    val totalAmount: Double
)

/**
 * 交易记录DAO
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long, start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long, start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :start AND :end")
    suspend fun getTotalByDateRange(type: TransactionType, start: Long, end: Long): Double?

    @Query("SELECT categoryId, SUM(amount) as totalAmount, COUNT(*) as count FROM transactions WHERE type = :type AND date BETWEEN :start AND :end GROUP BY categoryId ORDER BY totalAmount DESC")
    suspend fun getCategorySummary(type: TransactionType, start: Long, end: Long): List<CategorySummaryResult>

    @Query("""
        SELECT (date / 86400000) * 86400000 as dayTimestamp, SUM(amount) as totalAmount
        FROM transactions
        WHERE type = :type AND date BETWEEN :start AND :end
        GROUP BY (date / 86400000)
        ORDER BY dayTimestamp ASC
    """)
    suspend fun getDailyTotals(type: TransactionType, start: Long, end: Long): List<DailyTotalResult>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :start AND :end")
    suspend fun getCountByDateRange(start: Long, end: Long): Int

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsForBackup(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Delete
    suspend fun deleteAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
