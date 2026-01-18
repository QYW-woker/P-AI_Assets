package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.AccountDao
import com.example.smartledger.data.local.dao.MonthlySnapshotDao
import com.example.smartledger.data.local.entity.AccountSnapshot
import com.example.smartledger.data.local.entity.AccountType
import com.example.smartledger.data.local.entity.MonthlySnapshotEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.MonthlySnapshotRepository
import com.example.smartledger.domain.repository.TransactionRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 月度资产快照仓库实现
 */
@Singleton
class MonthlySnapshotRepositoryImpl @Inject constructor(
    private val snapshotDao: MonthlySnapshotDao,
    private val accountDao: AccountDao,
    private val transactionRepository: TransactionRepository
) : MonthlySnapshotRepository {

    private val gson = Gson()

    override fun getAll(): Flow<List<MonthlySnapshotEntity>> {
        return snapshotDao.getAll()
    }

    override fun getByYear(year: Int): Flow<List<MonthlySnapshotEntity>> {
        return snapshotDao.getByYear(year)
    }

    override suspend fun getByYearMonth(year: Int, month: Int): MonthlySnapshotEntity? {
        return snapshotDao.getByYearMonth(year, month)
    }

    override fun getRecent(limit: Int): Flow<List<MonthlySnapshotEntity>> {
        return snapshotDao.getRecent(limit)
    }

    override suspend fun getLatest(): MonthlySnapshotEntity? {
        return snapshotDao.getLatest()
    }

    override suspend fun getPreviousMonth(year: Int, month: Int): MonthlySnapshotEntity? {
        return snapshotDao.getPreviousMonth(year, month)
    }

    override fun getAssetTrend(): Flow<List<MonthlySnapshotEntity>> {
        return snapshotDao.getAssetTrend()
    }

    override suspend fun createCurrentMonthSnapshot(): MonthlySnapshotEntity {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        // 获取本月日期范围
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        // 获取所有账户
        val accounts = accountDao.getAllActiveAccounts().first()

        // 计算各类资产
        val cashTypes = listOf(AccountType.CASH, AccountType.BANK, AccountType.ALIPAY, AccountType.WECHAT)
        val investmentTypes = listOf(AccountType.INVESTMENT_STOCK, AccountType.INVESTMENT_FUND, AccountType.INVESTMENT_DEPOSIT)
        val liabilityTypes = listOf(AccountType.CREDIT_CARD)

        val cashAccounts = accounts.filter { it.type in cashTypes && it.isIncludeInTotal }
        val investmentAccounts = accounts.filter { it.type in investmentTypes && it.isIncludeInTotal }
        val liabilityAccounts = accounts.filter { it.type in liabilityTypes }

        val cashAssets = cashAccounts.sumOf { it.balance }
        val investmentAssets = investmentAccounts.sumOf { it.balance }
        val investmentPrincipal = investmentAccounts.sumOf { it.initialBalance }
        val investmentReturn = investmentAssets - investmentPrincipal
        val totalLiabilities = liabilityAccounts.sumOf { kotlin.math.abs(it.balance) }

        val totalAssets = accounts.filter { it.isIncludeInTotal && it.type !in liabilityTypes }
            .sumOf { it.balance }
        val netWorth = totalAssets - totalLiabilities

        // 获取本月收支
        val monthlyIncome = transactionRepository.getTotalByDateRange(TransactionType.INCOME, monthStart, monthEnd)
        val monthlyExpense = transactionRepository.getTotalByDateRange(TransactionType.EXPENSE, monthStart, monthEnd)
        val monthlyBalance = monthlyIncome - monthlyExpense
        val savingsRate = if (monthlyIncome > 0) (monthlyBalance / monthlyIncome * 100) else 0.0

        // 生成账户快照JSON
        val accountSnapshots = accounts.map { account ->
            AccountSnapshot(
                accountId = account.id,
                name = account.name,
                type = account.type.name,
                balance = account.balance,
                initialBalance = account.initialBalance
            )
        }
        val accountsJson = gson.toJson(accountSnapshots)

        val snapshot = MonthlySnapshotEntity(
            year = year,
            month = month,
            snapshotDate = System.currentTimeMillis(),
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = netWorth,
            cashAssets = cashAssets,
            investmentAssets = investmentAssets,
            investmentPrincipal = investmentPrincipal,
            investmentReturn = investmentReturn,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense,
            monthlyBalance = monthlyBalance,
            savingsRate = savingsRate,
            accountsJson = accountsJson
        )

        // 检查是否已存在，存在则更新
        val existingSnapshot = snapshotDao.getByYearMonth(year, month)
        return if (existingSnapshot != null) {
            val updated = snapshot.copy(id = existingSnapshot.id)
            snapshotDao.update(updated)
            updated
        } else {
            val id = snapshotDao.insert(snapshot)
            snapshot.copy(id = id)
        }
    }

    override suspend fun save(snapshot: MonthlySnapshotEntity): Long {
        return snapshotDao.insert(snapshot)
    }

    override suspend fun delete(snapshot: MonthlySnapshotEntity) {
        snapshotDao.delete(snapshot)
    }

    override suspend fun exists(year: Int, month: Int): Boolean {
        return snapshotDao.exists(year, month)
    }

    override suspend fun getAllForBackup(): List<MonthlySnapshotEntity> {
        return snapshotDao.getAllForBackup()
    }

    override suspend fun deleteAll() {
        snapshotDao.clearAll()
    }
}
