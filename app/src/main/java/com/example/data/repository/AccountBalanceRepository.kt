package com.example.data.repository

import com.example.data.local.dao.AccountBalanceDao
import com.example.data.local.dao.LedgerDao
import com.example.data.local.entity.AccountBalanceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountBalanceRepository(
    private val accountBalanceDao: AccountBalanceDao,
    private val ledgerDao: LedgerDao
) {

    val allAccounts: Flow<List<AccountBalanceEntity>> = accountBalanceDao.getAllAccounts()

    val activeAccounts: Flow<List<AccountBalanceEntity>> = accountBalanceDao.getActiveAccounts()

    val totalAssets: Flow<Double> = accountBalanceDao.getTotalAssetsBalance().map { it ?: 0.0 }

    val totalLiabilities: Flow<Double> = accountBalanceDao.getTotalLiabilitiesBalance().map { it ?: 0.0 }

    val totalEquity: Flow<Double> = accountBalanceDao.getTotalEquityBalance().map { it ?: 0.0 }

    val totalIncome: Flow<Double> = accountBalanceDao.getTotalIncomeBalance().map { it ?: 0.0 }

    val totalExpense: Flow<Double> = accountBalanceDao.getTotalExpenseBalance().map { it ?: 0.0 }

    val liquidCashBalance: Flow<Double> = accountBalanceDao.getLiquidCashBalance().map { it ?: 0.0 }

    val accountsReceivable: Flow<Double> = accountBalanceDao.getReceivablesBalance().map { it ?: 0.0 }

    val accountsPayable: Flow<Double> = accountBalanceDao.getPayablesBalance().map { it ?: 0.0 }

    val accountSummaries: Flow<List<com.example.data.local.dao.AccountBalanceSummary>> =
        accountBalanceDao.getAccountBalanceSummaries()

    fun getAccountsByType(type: String): Flow<List<AccountBalanceEntity>> =
        accountBalanceDao.getAccountsByType(type)

    fun getAccountByName(name: String): Flow<AccountBalanceEntity?> =
        accountBalanceDao.getAccountByName(name)

    fun getAccountByCode(code: String): Flow<AccountBalanceEntity?> =
        accountBalanceDao.getAccountByCode(code)

    fun getAccountBalance(name: String): Flow<Double> =
        accountBalanceDao.getAccountBalance(name).map { it ?: 0.0 }

    suspend fun getAccountBalanceSync(name: String): Double =
        accountBalanceDao.getAccountBalanceSync(name) ?: 0.0

    suspend fun insertAccount(account: AccountBalanceEntity): Long =
        accountBalanceDao.insertAccount(account)

    suspend fun updateAccount(account: AccountBalanceEntity) =
        accountBalanceDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountBalanceEntity) =
        accountBalanceDao.deleteAccount(account)

    /**
     * Recalculates and synchronizes all account balances from double-entry General Ledger entries.
     */
    suspend fun syncBalancesWithLedger() {
        val allEntries = ledgerDao.getAllEntriesSync().filter { !it.isCancelled }
        val accounts = accountBalanceDao.getAllAccountsSync()

        val debitSums = mutableMapOf<String, Double>()
        val creditSums = mutableMapOf<String, Double>()

        for (entry in allEntries) {
            debitSums[entry.account] = (debitSums[entry.account] ?: 0.0) + entry.debit
            creditSums[entry.account] = (creditSums[entry.account] ?: 0.0) + entry.credit
        }

        for (acc in accounts) {
            val totalDr = debitSums[acc.accountName] ?: 0.0
            val totalCr = creditSums[acc.accountName] ?: 0.0
            val isDebitNormal = acc.accountType == "Asset" || acc.accountType == "Expense"
            val calculatedBalance = if (isDebitNormal) {
                acc.openingBalance + totalDr - totalCr
            } else {
                acc.openingBalance + totalCr - totalDr
            }

            accountBalanceDao.updateBalances(
                accountName = acc.accountName,
                currentBalance = calculatedBalance,
                totalDebit = totalDr,
                totalCredit = totalCr,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    companion object {
        val DEFAULT_CHART_OF_ACCOUNTS = listOf(
            // Assets (1000 - 1999)
            AccountBalanceEntity(accountCode = "1010", accountName = "Cash in Hand", accountType = "Asset", description = "Physical currency in cash drawer"),
            AccountBalanceEntity(accountCode = "1020", accountName = "Bank Account", accountType = "Asset", description = "Primary operating bank account"),
            AccountBalanceEntity(accountCode = "1110", accountName = "Accounts Receivable", accountType = "Asset", description = "Money owed by customers for credit sales"),
            AccountBalanceEntity(accountCode = "1210", accountName = "Inventory Asset", accountType = "Asset", description = "Value of goods and stock held for sale"),

            // Liabilities (2000 - 2999)
            AccountBalanceEntity(accountCode = "2010", accountName = "Accounts Payable", accountType = "Liability", description = "Money owed to suppliers for credit purchases"),
            AccountBalanceEntity(accountCode = "2110", accountName = "VAT / Tax Payable", accountType = "Liability", description = "Collected 13% VAT payable to IRD"),

            // Equity (3000 - 3999)
            AccountBalanceEntity(accountCode = "3010", accountName = "Owner's Capital", accountType = "Equity", description = "Owner's investment in the business"),
            AccountBalanceEntity(accountCode = "3020", accountName = "Retained Earnings", accountType = "Equity", description = "Cumulative net earnings retained in business"),

            // Income (4000 - 4999)
            AccountBalanceEntity(accountCode = "4010", accountName = "Sales Account", accountType = "Income", description = "Revenue from goods sold"),
            AccountBalanceEntity(accountCode = "4020", accountName = "Service Income", accountType = "Income", description = "Revenue from services rendered"),

            // Expenses (5000 - 5999)
            AccountBalanceEntity(accountCode = "5010", accountName = "Purchase Account", accountType = "Expense", description = "Cost of inventory purchased"),
            AccountBalanceEntity(accountCode = "5020", accountName = "Salaries & Wages", accountType = "Expense", description = "Staff payroll expenses"),
            AccountBalanceEntity(accountCode = "5030", accountName = "Rent Expense", accountType = "Expense", description = "Premises rent"),
            AccountBalanceEntity(accountCode = "5040", accountName = "Utility Expense", accountType = "Expense", description = "Electricity, water, internet charges"),
            AccountBalanceEntity(accountCode = "5050", accountName = "General & Administrative", accountType = "Expense", description = "Office supplies and administrative costs")
        )

        suspend fun seedDefaultAccounts(accountBalanceDao: AccountBalanceDao) {
            if (accountBalanceDao.getAccountCount() == 0) {
                accountBalanceDao.insertAll(DEFAULT_CHART_OF_ACCOUNTS)
            }
        }
    }
}
