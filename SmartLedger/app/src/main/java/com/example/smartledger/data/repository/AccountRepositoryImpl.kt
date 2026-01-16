package com.example.smartledger.data.repository

import com.example.smartledger.data.local.dao.AccountDao
import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.AccountType
import com.example.smartledger.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllActiveAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getAllActiveAccounts()
    }

    override fun getAssetAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getAssetAccounts()
    }

    override fun getInvestmentAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getInvestmentAccounts()
    }

    override fun getAccountsByType(type: AccountType): Flow<List<AccountEntity>> {
        return accountDao.getAccountsByType(type)
    }

    override fun getTotalBalance(): Flow<Double> {
        return accountDao.getTotalBalance().map { it ?: 0.0 }
    }

    override fun getTotalAssetBalance(): Flow<Double> {
        return getAssetAccounts().map { accounts ->
            accounts.filter { it.isIncludeInTotal }.sumOf { it.balance }
        }
    }

    override fun getTotalInvestmentBalance(): Flow<Double> {
        return getInvestmentAccounts().map { accounts ->
            accounts.sumOf { it.balance }
        }
    }

    override suspend fun getAccountById(id: Long): AccountEntity? {
        return accountDao.getAccountById(id)
    }

    override suspend fun insertAccount(account: AccountEntity): Long {
        return accountDao.insert(account)
    }

    override suspend fun updateAccount(account: AccountEntity) {
        accountDao.update(account)
    }

    override suspend fun deleteAccount(account: AccountEntity) {
        accountDao.softDelete(account.id)
    }

    override suspend fun updateBalance(accountId: Long, amount: Double) {
        accountDao.updateBalance(accountId, amount)
    }

    override suspend fun incrementBalance(accountId: Long, delta: Double) {
        accountDao.incrementBalance(accountId, delta)
    }

    override suspend fun getAllAccounts(): List<AccountEntity> {
        return accountDao.getAllAccountsForBackup()
    }

    override suspend fun initDefaultAccounts() {
        // 检查是否已有账户
        val existingAccounts = accountDao.getAccountCountByType(AccountType.CASH)
        if (existingAccounts > 0) return

        val defaultAccounts = listOf(
            AccountEntity(
                name = "现金",
                type = AccountType.CASH,
                icon = "💵",
                color = "#4CAF50",
                balance = 0.0,
                initialBalance = 0.0,
                sortOrder = 1
            ),
            AccountEntity(
                name = "银行卡",
                type = AccountType.BANK,
                icon = "🏦",
                color = "#2196F3",
                balance = 0.0,
                initialBalance = 0.0,
                sortOrder = 2
            ),
            AccountEntity(
                name = "支付宝",
                type = AccountType.ALIPAY,
                icon = "📱",
                color = "#1E88E5",
                balance = 0.0,
                initialBalance = 0.0,
                sortOrder = 3
            ),
            AccountEntity(
                name = "微信",
                type = AccountType.WECHAT,
                icon = "💬",
                color = "#4CAF50",
                balance = 0.0,
                initialBalance = 0.0,
                sortOrder = 4
            )
        )

        defaultAccounts.forEach { accountDao.insert(it) }
    }
}
