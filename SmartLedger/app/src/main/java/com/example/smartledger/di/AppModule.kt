package com.example.smartledger.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DataStore扩展
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 应用级别的依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供DataStore实例
     */
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    /**
     * 提供应用上下文
     */
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context {
        return context
    }
}
