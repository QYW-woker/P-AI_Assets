package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.InvestmentHoldingDao
import com.example.smartledger.data.local.entity.HoldingType
import com.example.smartledger.data.local.entity.InvestmentHoldingEntity
import com.example.smartledger.domain.repository.InvestmentHoldingRepository
import com.example.smartledger.domain.repository.InvestmentSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 投资持仓仓库实现
 */
@Singleton
class InvestmentHoldingRepositoryImpl @Inject constructor(
    private val holdingDao: InvestmentHoldingDao
) : InvestmentHoldingRepository {

    override fun getAllActive(): Flow<List<InvestmentHoldingEntity>> {
        return holdingDao.getAllActive()
    }

    override fun getAll(): Flow<List<InvestmentHoldingEntity>> {
        return holdingDao.getAll()
    }

    override fun getByAccount(accountId: Long): Flow<List<InvestmentHoldingEntity>> {
        return holdingDao.getByAccount(accountId)
    }

    override fun getByType(type: HoldingType): Flow<List<InvestmentHoldingEntity>> {
        return holdingDao.getByType(type)
    }

    override suspend fun getById(id: Long): InvestmentHoldingEntity? {
        return holdingDao.getById(id)
    }

    override suspend fun getByCode(code: String, accountId: Long): InvestmentHoldingEntity? {
        return holdingDao.getByCode(code, accountId)
    }

    override suspend fun getSummary(): InvestmentSummary {
        val holdings = holdingDao.getAllActive().first()
        val totalPrincipal = holdings.sumOf { it.principal }
        val totalMarketValue = holdings.sumOf { it.marketValue }
        val totalProfitLoss = holdings.sumOf { it.profitLoss }
        val returnRate = if (totalPrincipal > 0) (totalProfitLoss / totalPrincipal * 100) else 0.0
        val profitableCount = holdings.count { it.profitLoss > 0 }
        val lossCount = holdings.count { it.profitLoss < 0 }

        return InvestmentSummary(
            totalPrincipal = totalPrincipal,
            totalMarketValue = totalMarketValue,
            totalProfitLoss = totalProfitLoss,
            returnRate = returnRate,
            holdingCount = holdings.size,
            profitableCount = profitableCount,
            lossCount = lossCount
        )
    }

    override fun getProfitableHoldings(): Flow<List<InvestmentHoldingEntity>> {
        return holdingDao.getProfitableHoldings()
    }

    override fun getLossHoldings(): Flow<List<InvestmentHoldingEntity>> {
        return holdingDao.getLossHoldings()
    }

    override suspend fun create(holding: InvestmentHoldingEntity): Long {
        return holdingDao.insert(holding)
    }

    override suspend fun update(holding: InvestmentHoldingEntity) {
        holdingDao.update(holding.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(holding: InvestmentHoldingEntity) {
        holdingDao.delete(holding)
    }

    override suspend fun updatePrice(id: Long, currentPrice: Double) {
        holdingDao.updatePrice(id, currentPrice)
    }

    override suspend fun markAsSold(id: Long) {
        holdingDao.markAsSold(id)
    }

    override suspend fun buy(
        accountId: Long,
        name: String,
        code: String,
        holdingType: HoldingType,
        quantity: Double,
        price: Double,
        note: String
    ): Long {
        // 检查是否已有该持仓
        val existing = if (code.isNotEmpty()) {
            holdingDao.getByCode(code, accountId)
        } else null

        return if (existing != null) {
            // 加仓：更新数量和成本
            val newQuantity = existing.quantity + quantity
            val newPrincipal = existing.principal + (quantity * price)
            val newCostPrice = newPrincipal / newQuantity
            val newMarketValue = newQuantity * existing.currentPrice
            val newProfitLoss = newMarketValue - newPrincipal
            val newReturnRate = if (newPrincipal > 0) (newProfitLoss / newPrincipal * 100) else 0.0

            val updated = existing.copy(
                quantity = newQuantity,
                costPrice = newCostPrice,
                principal = newPrincipal,
                marketValue = newMarketValue,
                profitLoss = newProfitLoss,
                returnRate = newReturnRate,
                updatedAt = System.currentTimeMillis()
            )
            holdingDao.update(updated)
            existing.id
        } else {
            // 新建持仓
            val principal = quantity * price
            val holding = InvestmentHoldingEntity(
                accountId = accountId,
                name = name,
                code = code,
                holdingType = holdingType,
                quantity = quantity,
                costPrice = price,
                currentPrice = price,
                principal = principal,
                marketValue = principal,
                profitLoss = 0.0,
                returnRate = 0.0,
                firstBuyDate = System.currentTimeMillis(),
                note = note
            )
            holdingDao.insert(holding)
        }
    }

    override suspend fun sell(id: Long, quantity: Double, price: Double) {
        val holding = holdingDao.getById(id) ?: return

        val newQuantity = holding.quantity - quantity
        if (newQuantity <= 0) {
            // 全部卖出，标记为已清仓
            holdingDao.markAsSold(id)
        } else {
            // 部分卖出，更新持仓
            val soldPrincipal = quantity * holding.costPrice
            val newPrincipal = holding.principal - soldPrincipal
            val newMarketValue = newQuantity * holding.currentPrice
            val newProfitLoss = newMarketValue - newPrincipal
            val newReturnRate = if (newPrincipal > 0) (newProfitLoss / newPrincipal * 100) else 0.0

            val updated = holding.copy(
                quantity = newQuantity,
                principal = newPrincipal,
                marketValue = newMarketValue,
                profitLoss = newProfitLoss,
                returnRate = newReturnRate,
                updatedAt = System.currentTimeMillis()
            )
            holdingDao.update(updated)
        }
    }

    override suspend fun getAllForBackup(): List<InvestmentHoldingEntity> {
        return holdingDao.getAllForBackup()
    }

    override suspend fun deleteAll() {
        holdingDao.clearAll()
    }
}
