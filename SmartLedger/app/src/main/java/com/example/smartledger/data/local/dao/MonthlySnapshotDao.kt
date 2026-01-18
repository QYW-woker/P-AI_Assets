package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.MonthlySnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * 月度资产快照数据访问对象
 */
@Dao
interface MonthlySnapshotDao {

    /**
     * 获取所有快照（按时间降序）
     */
    @Query("SELECT * FROM monthly_snapshots ORDER BY year DESC, month DESC")
    fun getAll(): Flow<List<MonthlySnapshotEntity>>

    /**
     * 获取指定年份的所有快照
     */
    @Query("SELECT * FROM monthly_snapshots WHERE year = :year ORDER BY month DESC")
    fun getByYear(year: Int): Flow<List<MonthlySnapshotEntity>>

    /**
     * 获取指定月份的快照
     */
    @Query("SELECT * FROM monthly_snapshots WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getByYearMonth(year: Int, month: Int): MonthlySnapshotEntity?

    /**
     * 获取最近N个月的快照
     */
    @Query("SELECT * FROM monthly_snapshots ORDER BY year DESC, month DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<MonthlySnapshotEntity>>

    /**
     * 获取最新的快照
     */
    @Query("SELECT * FROM monthly_snapshots ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getLatest(): MonthlySnapshotEntity?

    /**
     * 获取上个月的快照
     */
    @Query("""
        SELECT * FROM monthly_snapshots
        WHERE (year = :year AND month < :month) OR (year < :year)
        ORDER BY year DESC, month DESC
        LIMIT 1
    """)
    suspend fun getPreviousMonth(year: Int, month: Int): MonthlySnapshotEntity?

    /**
     * 获取资产变化趋势（最近12个月）
     */
    @Query("SELECT * FROM monthly_snapshots ORDER BY year DESC, month DESC LIMIT 12")
    fun getAssetTrend(): Flow<List<MonthlySnapshotEntity>>

    /**
     * 插入快照
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: MonthlySnapshotEntity): Long

    /**
     * 更新快照
     */
    @Update
    suspend fun update(snapshot: MonthlySnapshotEntity)

    /**
     * 删除快照
     */
    @Delete
    suspend fun delete(snapshot: MonthlySnapshotEntity)

    /**
     * 删除指定月份的快照
     */
    @Query("DELETE FROM monthly_snapshots WHERE year = :year AND month = :month")
    suspend fun deleteByYearMonth(year: Int, month: Int)

    /**
     * 检查指定月份是否已有快照
     */
    @Query("SELECT EXISTS(SELECT 1 FROM monthly_snapshots WHERE year = :year AND month = :month)")
    suspend fun exists(year: Int, month: Int): Boolean

    /**
     * 获取所有数据用于备份
     */
    @Query("SELECT * FROM monthly_snapshots ORDER BY year, month")
    suspend fun getAllForBackup(): List<MonthlySnapshotEntity>

    /**
     * 清空所有数据
     */
    @Query("DELETE FROM monthly_snapshots")
    suspend fun clearAll()
}
