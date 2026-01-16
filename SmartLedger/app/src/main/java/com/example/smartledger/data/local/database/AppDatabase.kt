package com.example.smartledger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smartledger.data.local.dao.AccountDao
import com.example.smartledger.data.local.dao.BudgetDao
import com.example.smartledger.data.local.dao.CategoryDao
import com.example.smartledger.data.local.dao.GoalDao
import com.example.smartledger.data.local.dao.TransactionDao
import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.BudgetEntity
import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.data.local.entity.TransactionEntity

/**
 * 应用数据库
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao

    companion object {
        const val DATABASE_NAME = "smart_ledger.db"
    }
}
