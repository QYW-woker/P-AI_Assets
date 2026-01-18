package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.MonthlySnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * 月度资产快照仓库接口
 */
interface MonthlySnapshotRepository {

    /**
     * 获取所有快照
     */
    fun getAll(): Flow<List<MonthlySnapshotEntity>>

    /**
     * 获取指定年份的快照
     */
    fun getByYear(year: Int): Flow<List<MonthlySnapshotEntity>>

    /**
     * 获取指定月份的快照
     */
    suspend fun getByYearMonth(year: Int, month: Int): MonthlySnapshotEntity?

    /**
     * 获取最近N个月的快照
     */
    fun getRecent(limit: Int = 12): Flow<List<MonthlySnapshotEntity>>

    /**
     * 获取最新的快照
     */
    suspend fun getLatest(): MonthlySnapshotEntity?

    /**
     * 获取上个月的快照
     */
    suspend fun getPreviousMonth(year: Int, month: Int): MonthlySnapshotEntity?

    /**
     * 获取资产变化趋势
     */
    fun getAssetTrend(): Flow<List<MonthlySnapshotEntity>>

    /**
     * 创建当月快照
     * 自动收集当前所有账户余额和统计数据
     */
    suspend fun createCurrentMonthSnapshot(): MonthlySnapshotEntity

    /**
     * 插入或更新快照
     */
    suspend fun save(snapshot: MonthlySnapshotEntity): Long

    /**
     * 删除快照
     */
    suspend fun delete(snapshot: MonthlySnapshotEntity)

    /**
     * 检查指定月份是否已有快照
     */
    suspend fun exists(year: Int, month: Int): Boolean

    /**
     * 获取所有数据用于备份
     */
    suspend fun getAllForBackup(): List<MonthlySnapshotEntity>

    /**
     * 清空所有数据
     */
    suspend fun deleteAll()
}
