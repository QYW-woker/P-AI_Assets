package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.HoldingType
import com.example.smartledger.data.local.entity.InvestmentHoldingEntity
import kotlinx.coroutines.flow.Flow

/**
 * 投资持仓仓库接口
 */
interface InvestmentHoldingRepository {

    /**
     * 获取所有活跃持仓
     */
    fun getAllActive(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取所有持仓（包括已清仓）
     */
    fun getAll(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取指定账户的持仓
     */
    fun getByAccount(accountId: Long): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取指定类型的持仓
     */
    fun getByType(type: HoldingType): Flow<List<InvestmentHoldingEntity>>

    /**
     * 根据ID获取持仓
     */
    suspend fun getById(id: Long): InvestmentHoldingEntity?

    /**
     * 根据代码查找持仓
     */
    suspend fun getByCode(code: String, accountId: Long): InvestmentHoldingEntity?

    /**
     * 获取持仓汇总
     */
    suspend fun getSummary(): InvestmentSummary

    /**
     * 获取盈利的持仓
     */
    fun getProfitableHoldings(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 获取亏损的持仓
     */
    fun getLossHoldings(): Flow<List<InvestmentHoldingEntity>>

    /**
     * 创建持仓
     */
    suspend fun create(holding: InvestmentHoldingEntity): Long

    /**
     * 更新持仓
     */
    suspend fun update(holding: InvestmentHoldingEntity)

    /**
     * 删除持仓
     */
    suspend fun delete(holding: InvestmentHoldingEntity)

    /**
     * 更新持仓价格
     */
    suspend fun updatePrice(id: Long, currentPrice: Double)

    /**
     * 标记为已清仓
     */
    suspend fun markAsSold(id: Long)

    /**
     * 买入/加仓
     */
    suspend fun buy(
        accountId: Long,
        name: String,
        code: String,
        holdingType: HoldingType,
        quantity: Double,
        price: Double,
        note: String = ""
    ): Long

    /**
     * 卖出/减仓
     */
    suspend fun sell(id: Long, quantity: Double, price: Double)

    /**
     * 获取所有数据用于备份
     */
    suspend fun getAllForBackup(): List<InvestmentHoldingEntity>

    /**
     * 清空所有数据
     */
    suspend fun deleteAll()
}

/**
 * 投资汇总数据
 */
data class InvestmentSummary(
    val totalPrincipal: Double,
    val totalMarketValue: Double,
    val totalProfitLoss: Double,
    val returnRate: Double,
    val holdingCount: Int,
    val profitableCount: Int,
    val lossCount: Int
)
