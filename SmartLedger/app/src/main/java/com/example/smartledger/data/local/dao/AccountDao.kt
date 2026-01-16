package com.example.smartledger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.AccountType
import kotlinx.coroutines.flow.Flow

/**
 * 账户DAO
 */
@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY sortOrder")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY sortOrder")
    fun getAllAccountsIncludingInactive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE type IN (:types) AND isActive = 1 ORDER BY sortOrder")
    fun getAccountsByTypes(types: List<AccountType>): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE type NOT IN ('INVESTMENT_STOCK', 'INVESTMENT_FUND', 'INVESTMENT_DEPOSIT') AND isActive = 1 ORDER BY sortOrder")
    fun getAssetAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE type IN ('INVESTMENT_STOCK', 'INVESTMENT_FUND', 'INVESTMENT_DEPOSIT') AND isActive = 1 ORDER BY sortOrder")
    fun getInvestmentAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT SUM(balance) FROM accounts WHERE isIncludeInTotal = 1 AND isActive = 1")
    fun getTotalBalance(): Flow<Double?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: Double)

    @Query("UPDATE accounts SET balance = :balance WHERE id = :accountId")
    suspend fun setBalance(accountId: Long, balance: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE accounts SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT COUNT(*) FROM accounts WHERE isActive = 1")
    suspend fun getAccountCount(): Int
}
