package com.example.smartledger.domain.repository

import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.AccountType
import kotlinx.coroutines.flow.Flow

/**
 * 账户数据仓库接口
 */
interface AccountRepository {

    /**
     * 获取所有活跃账户
     */
    fun getAllActiveAccounts(): Flow<List<AccountEntity>>

    /**
     * 获取资产账户（非投资类）
     */
    fun getAssetAccounts(): Flow<List<AccountEntity>>

    /**
     * 获取投资账户
     */
    fun getInvestmentAccounts(): Flow<List<AccountEntity>>

    /**
     * 根据类型获取账户
     */
    fun getAccountsByType(type: AccountType): Flow<List<AccountEntity>>

    /**
     * 获取总余额
     */
    fun getTotalBalance(): Flow<Double>

    /**
     * 获取资产总额（非投资）
     */
    fun getTotalAssetBalance(): Flow<Double>

    /**
     * 获取投资总额
     */
    fun getTotalInvestmentBalance(): Flow<Double>

    /**
     * 根据ID获取账户
     */
    suspend fun getAccountById(id: Long): AccountEntity?

    /**
     * 插入账户
     */
    suspend fun insertAccount(account: AccountEntity): Long

    /**
     * 更新账户
     */
    suspend fun updateAccount(account: AccountEntity)

    /**
     * 删除账户（软删除）
     */
    suspend fun deleteAccount(account: AccountEntity)

    /**
     * 更新账户余额（增量更新，在现有余额基础上增加）
     */
    suspend fun updateBalance(accountId: Long, amount: Double)

    /**
     * 设置账户余额（直接设置为新值，不累加）
     */
    suspend fun setBalance(accountId: Long, newBalance: Double)

    /**
     * 增加账户余额
     */
    suspend fun incrementBalance(accountId: Long, delta: Double)

    /**
     * 获取所有账户（用于备份）
     */
    suspend fun getAllAccounts(): List<AccountEntity>

    /**
     * 初始化默认账户
     */
    suspend fun initDefaultAccounts()
}
