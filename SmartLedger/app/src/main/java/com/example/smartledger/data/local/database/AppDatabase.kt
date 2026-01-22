package com.example.smartledger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smartledger.data.local.dao.AccountDao
import com.example.smartledger.data.local.dao.BudgetDao
import com.example.smartledger.data.local.dao.CategoryDao
import com.example.smartledger.data.local.dao.GoalDao
import com.example.smartledger.data.local.dao.GoalTransactionDao
import com.example.smartledger.data.local.dao.InvestmentHoldingDao
import com.example.smartledger.data.local.dao.MonthlySnapshotDao
import com.example.smartledger.data.local.dao.RecurringTransactionDao
import com.example.smartledger.data.local.dao.TransactionDao
import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.BudgetEntity
import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.data.local.entity.GoalTransactionEntity
import com.example.smartledger.data.local.entity.InvestmentHoldingEntity
import com.example.smartledger.data.local.entity.MonthlySnapshotEntity
import com.example.smartledger.data.local.entity.RecurringTransactionEntity
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
        GoalEntity::class,
        GoalTransactionEntity::class,
        RecurringTransactionEntity::class,
        MonthlySnapshotEntity::class,
        InvestmentHoldingEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun goalTransactionDao(): GoalTransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun monthlySnapshotDao(): MonthlySnapshotDao
    abstract fun investmentHoldingDao(): InvestmentHoldingDao

    companion object {
        const val DATABASE_NAME = "smart_ledger.db"
    }
}
