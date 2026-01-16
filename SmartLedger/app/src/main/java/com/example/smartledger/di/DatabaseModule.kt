package com.example.smartledger.di

import android.content.Context
import androidx.room.Room
import com.example.smartledger.data.local.database.AppDatabase
import com.example.smartledger.data.local.dao.TransactionDao
import com.example.smartledger.data.local.dao.CategoryDao
import com.example.smartledger.data.local.dao.AccountDao
import com.example.smartledger.data.local.dao.BudgetDao
import com.example.smartledger.data.local.dao.GoalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库相关的依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供Room数据库实例
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_ledger.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * 提供TransactionDao
     */
    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    /**
     * 提供CategoryDao
     */
    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    /**
     * 提供AccountDao
     */
    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    /**
     * 提供BudgetDao
     */
    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

    /**
     * 提供GoalDao
     */
    @Provides
    @Singleton
    fun provideGoalDao(database: AppDatabase): GoalDao {
        return database.goalDao()
    }
}
