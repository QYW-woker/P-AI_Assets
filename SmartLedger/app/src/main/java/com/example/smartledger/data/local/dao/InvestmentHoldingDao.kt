package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.HoldingType
import com.example.smartledger.data.local.entity.InvestmentHoldingEntity
import kotlinx.coroutines.flow.Flow

/**
 * 投资持仓数据访问对象
 */
@Dao
interface InvestmentHoldingDao {

    /**
     * 获取所有活跃持仓
     */
    @Query("SELECT * FROM investment_holdings WHERE isActive = 1 ORDER BY marketValue DESC")
    fun getAllActive(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取所有持仓（包括已清仓）
     */
    @Query("SELECT * FROM investment_holdings ORDER BY marketValue DESC")
    fun getAll(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取指定账户的持仓
     */
    @Query("SELECT * FROM investment_holdings WHERE accountId = :accountId AND isActive = 1 ORDER BY marketValue DESC")
    fun getByAccount(accountId: Long): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取指定类型的持仓
     */
    @Query("SELECT * FROM investment_holdings WHERE holdingType = :type AND isActive = 1 ORDER BY marketValue DESC")
    fun getByType(type: HoldingType): Flow<List<InvestmentHoldingEntity>>

    /**
     * 根据ID获取持仓
     */
    @Query("SELECT * FROM investment_holdings WHERE id = :id")
    suspend fun getById(id: Long): InvestmentHoldingEntity?

    /**
     * 根据代码查找持仓
     */
    @Query("SELECT * FROM investment_holdings WHERE code = :code AND accountId = :accountId AND isActive = 1 LIMIT 1")
    suspend fun getByCode(code: String, accountId: Long): InvestmentHoldingEntity?

    /**
     * 获取持仓汇总统计
     */
    @Query("SELECT SUM(principal) FROM investment_holdings WHERE isActive = 1")
    suspend fun getTotalPrincipal(): Double?

    @Query("SELECT SUM(marketValue) FROM investment_holdings WHERE isActive = 1")
    suspend fun getTotalMarketValue(): Double?

    @Query("SELECT SUM(profitLoss) FROM investment_holdings WHERE isActive = 1")
    suspend fun getTotalProfitLoss(): Double?

    /**
     * 获取按类型汇总的持仓
     */
    @Query("""
        SELECT holdingType, SUM(marketValue) as total
        FROM investment_holdings
        WHERE isActive = 1
        GROUP BY holdingType
    """)
    suspend fun getMarketValueByType(): List<HoldingTypeSummary>

    /**
     * 获取盈利的持仓
     */
    @Query("SELECT * FROM investment_holdings WHERE isActive = 1 AND profitLoss > 0 ORDER BY profitLoss DESC")
    fun getProfitableHoldings(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取亏损的持仓
     */
    @Query("SELECT * FROM investment_holdings WHERE isActive = 1 AND profitLoss < 0 ORDER BY profitLoss ASC")
    fun getLossHoldings(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 插入持仓
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holding: InvestmentHoldingEntity): Long

    /**
     * 更新持仓
     */
    @Update
    suspend fun update(holding: InvestmentHoldingEntity)

    /**
     * 删除持仓
     */
    @Delete
    suspend fun delete(holding: InvestmentHoldingEntity)

    /**
     * 更新当前价格并重新计算收益
     */
    @Query("""
        UPDATE investment_holdings
        SET currentPrice = :currentPrice,
            marketValue = quantity * :currentPrice,
            profitLoss = (quantity * :currentPrice) - principal,
            returnRate = CASE WHEN principal > 0 THEN ((quantity * :currentPrice) - principal) / principal * 100 ELSE 0 END,
            lastUpdateDate = :updateDate,
            updatedAt = :updateDate
        WHERE id = :id
    """)
    suspend fun updatePrice(id: Long, currentPrice: Double, updateDate: Long = System.currentTimeMillis())

    /**
     * 标记为已清仓
     */
    @Query("UPDATE investment_holdings SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun markAsSold(id: Long, now: Long = System.currentTimeMillis())

    /**
     * 获取所有数据用于备份
     */
    @Query("SELECT * FROM investment_holdings")
    suspend fun getAllForBackup(): List<InvestmentHoldingEntity>

    /**
     * 清空所有数据
     */
    @Query("DELETE FROM investment_holdings")
    suspend fun clearAll()
}

/**
 * 按类型汇总的数据类
 */
data class HoldingTypeSummary(
    val holdingType: HoldingType,
    val total: Double
)
