package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountBalanceEntity
import kotlinx.coroutines.flow.Flow

data class AccountBalanceSummary(
    val accountName: String,
    val accountCode: String,
    val accountType: String,
    val currentBalance: Double,
    val totalDebit: Double,
    val totalCredit: Double,
    val currency: String
)

@Dao
interface AccountBalanceDao {

    @Query("SELECT * FROM account_balances ORDER BY accountCode ASC, accountName ASC")
    fun getAllAccounts(): Flow<List<AccountBalanceEntity>>

    @Query("SELECT * FROM account_balances WHERE isActive = 1 ORDER BY accountCode ASC, accountName ASC")
    fun getActiveAccounts(): Flow<List<AccountBalanceEntity>>

    @Query("SELECT * FROM account_balances WHERE accountType = :type AND isActive = 1 ORDER BY accountCode ASC, accountName ASC")
    fun getAccountsByType(type: String): Flow<List<AccountBalanceEntity>>

    @Query("SELECT * FROM account_balances WHERE accountName = :name LIMIT 1")
    fun getAccountByName(name: String): Flow<AccountBalanceEntity?>

    @Query("SELECT * FROM account_balances WHERE accountName = :name LIMIT 1")
    suspend fun getAccountByNameSync(name: String): AccountBalanceEntity?

    @Query("SELECT * FROM account_balances WHERE accountCode = :code LIMIT 1")
    fun getAccountByCode(code: String): Flow<AccountBalanceEntity?>

    @Query("SELECT * FROM account_balances WHERE accountCode = :code LIMIT 1")
    suspend fun getAccountByCodeSync(code: String): AccountBalanceEntity?

    @Query("SELECT currentBalance FROM account_balances WHERE accountName = :name LIMIT 1")
    fun getAccountBalance(name: String): Flow<Double?>

    @Query("SELECT currentBalance FROM account_balances WHERE accountName = :name LIMIT 1")
    suspend fun getAccountBalanceSync(name: String): Double?

    @Query("SELECT SUM(currentBalance) FROM account_balances WHERE accountType = :type AND isActive = 1")
    fun getTotalBalanceByType(type: String): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM account_balances WHERE accountType = 'Asset' AND isActive = 1")
    fun getTotalAssetsBalance(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM account_balances WHERE accountType = 'Liability' AND isActive = 1")
    fun getTotalLiabilitiesBalance(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM account_balances WHERE accountType = 'Equity' AND isActive = 1")
    fun getTotalEquityBalance(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM account_balances WHERE accountType = 'Income' AND isActive = 1")
    fun getTotalIncomeBalance(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM account_balances WHERE accountType = 'Expense' AND isActive = 1")
    fun getTotalExpenseBalance(): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM account_balances WHERE (accountName = 'Cash in Hand' OR accountName = 'Bank Account') AND isActive = 1")
    fun getLiquidCashBalance(): Flow<Double?>

    @Query("SELECT currentBalance FROM account_balances WHERE accountName = 'Accounts Receivable' AND isActive = 1 LIMIT 1")
    fun getReceivablesBalance(): Flow<Double?>

    @Query("SELECT currentBalance FROM account_balances WHERE accountName = 'Accounts Payable' AND isActive = 1 LIMIT 1")
    fun getPayablesBalance(): Flow<Double?>

    @Query("""
        SELECT 
            accountName, 
            accountCode, 
            accountType, 
            currentBalance, 
            totalDebit, 
            totalCredit, 
            currency 
        FROM account_balances 
        WHERE isActive = 1 
        ORDER BY accountCode ASC
    """)
    fun getAccountBalanceSummaries(): Flow<List<AccountBalanceSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountBalanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountBalanceEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountBalanceEntity)

    @Query("""
        UPDATE account_balances 
        SET currentBalance = :currentBalance,
            totalDebit = :totalDebit,
            totalCredit = :totalCredit,
            lastUpdatedMillis = :updatedAt
        WHERE accountName = :accountName
    """)
    suspend fun updateBalances(
        accountName: String,
        currentBalance: Double,
        totalDebit: Double,
        totalCredit: Double,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteAccount(account: AccountBalanceEntity)

    @Query("DELETE FROM account_balances WHERE accountName = :accountName")
    suspend fun deleteByName(accountName: String)

    @Query("DELETE FROM account_balances")
    suspend fun deleteAllAccounts()

    @Query("SELECT * FROM account_balances ORDER BY accountCode ASC, accountName ASC")
    suspend fun getAllAccountsSync(): List<AccountBalanceEntity>

    @Query("SELECT COUNT(*) FROM account_balances")
    suspend fun getAccountCount(): Int
}
