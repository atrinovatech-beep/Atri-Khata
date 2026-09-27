package com.example.data.repository

import com.example.data.analytics.FinancialHealthEngine
import com.example.data.local.dao.AccountBalanceDao
import com.example.data.local.dao.AccountBalanceSummary
import com.example.data.local.dao.LedgerDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AccountBalanceEntity
import com.example.data.local.entity.LedgerEntry
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountTimelinePoint
import com.example.data.model.FinancialHealthAudit
import com.example.data.model.TimelineTimeRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Unified Financial summary model for UI dashboards and ViewModels.
 */
data class FinancialSummary(
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val totalEquity: Double = 0.0,
    val liquidCash: Double = 0.0,
    val accountsReceivable: Double = 0.0,
    val accountsPayable: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = totalSales - totalExpenses
)

/**
 * Clean architectural Repository abstracting Room DAOs for transactions,
 * general ledger, and account balances.
 *
 * Provides a consolidated, reactive API for ViewModels to interact with financial data.
 */
class AccountingRepository(
    private val transactionDao: TransactionDao,
    private val accountBalanceDao: AccountBalanceDao,
    private val ledgerDao: LedgerDao? = null
) {

    // =========================================================================
    // Transactions API
    // =========================================================================

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    val totalSales: Flow<Double> = transactionDao.getTotalSales().map { it ?: 0.0 }

    val totalPurchases: Flow<Double> = transactionDao.getTotalPurchases().map { it ?: 0.0 }

    val totalExpenses: Flow<Double> = transactionDao.getTotalExpenses().map { it ?: 0.0 }

    val totalPaymentIn: Flow<Double> = transactionDao.getTotalPaymentIn().map { it ?: 0.0 }

    val totalPaymentOut: Flow<Double> = transactionDao.getTotalPaymentOut().map { it ?: 0.0 }

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)

    fun getTransactionsPaged(limit: Int, offset: Int): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsPaged(limit, offset)

    fun getTransactionById(id: Long): Flow<TransactionEntity?> =
        transactionDao.getTransactionById(id)

    suspend fun getTransactionByIdSync(id: Long): TransactionEntity? =
        transactionDao.getTransactionByIdSync(id)

    fun getTransactionsForParty(partyId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForParty(partyId)

    fun getTransactionsByAccount(account: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByAccount(account)

    fun getTransactionsByDateRange(startDateMillis: Long, endDateMillis: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByDateRange(startDateMillis, endDateMillis)

    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByStatus(status: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByStatus(status)

    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(category)

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactions(query)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long> =
        transactionDao.insertTransactions(transactions)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long): Int =
        transactionDao.deleteTransactionById(id)

    suspend fun deleteTransactionsByParty(partyId: Long): Int =
        transactionDao.deleteTransactionsByParty(partyId)

    suspend fun getAllTransactionsSync(): List<TransactionEntity> =
        transactionDao.getAllTransactionsSync()

    suspend fun getTransactionsCount(): Int =
        transactionDao.getTransactionsCount()

    // =========================================================================
    // Account Balances & Chart of Accounts API
    // =========================================================================

    val allAccounts: Flow<List<AccountBalanceEntity>> = accountBalanceDao.getAllAccounts()

    val activeAccounts: Flow<List<AccountBalanceEntity>> = accountBalanceDao.getActiveAccounts()

    val accountSummaries: Flow<List<AccountBalanceSummary>> = accountBalanceDao.getAccountBalanceSummaries()

    val totalAssets: Flow<Double> = accountBalanceDao.getTotalAssetsBalance().map { it ?: 0.0 }

    val totalLiabilities: Flow<Double> = accountBalanceDao.getTotalLiabilitiesBalance().map { it ?: 0.0 }

    val totalEquity: Flow<Double> = accountBalanceDao.getTotalEquityBalance().map { it ?: 0.0 }

    val totalIncome: Flow<Double> = accountBalanceDao.getTotalIncomeBalance().map { it ?: 0.0 }

    val totalExpense: Flow<Double> = accountBalanceDao.getTotalExpenseBalance().map { it ?: 0.0 }

    val liquidCashBalance: Flow<Double> = accountBalanceDao.getLiquidCashBalance().map { it ?: 0.0 }

    val accountsReceivable: Flow<Double> = accountBalanceDao.getReceivablesBalance().map { it ?: 0.0 }

    val accountsPayable: Flow<Double> = accountBalanceDao.getPayablesBalance().map { it ?: 0.0 }

    fun getAccountsByType(type: String): Flow<List<AccountBalanceEntity>> =
        accountBalanceDao.getAccountsByType(type)

    fun getAccountByName(name: String): Flow<AccountBalanceEntity?> =
        accountBalanceDao.getAccountByName(name)

    suspend fun getAccountByNameSync(name: String): AccountBalanceEntity? =
        accountBalanceDao.getAccountByNameSync(name)

    fun getAccountByCode(code: String): Flow<AccountBalanceEntity?> =
        accountBalanceDao.getAccountByCode(code)

    suspend fun getAccountByCodeSync(code: String): AccountBalanceEntity? =
        accountBalanceDao.getAccountByCodeSync(code)

    fun getAccountBalance(name: String): Flow<Double> =
        accountBalanceDao.getAccountBalance(name).map { it ?: 0.0 }

    suspend fun getAccountBalanceSync(name: String): Double =
        accountBalanceDao.getAccountBalanceSync(name) ?: 0.0

    suspend fun insertAccount(account: AccountBalanceEntity): Long =
        accountBalanceDao.insertAccount(account)

    suspend fun insertAccounts(accounts: List<AccountBalanceEntity>): List<Long> =
        accountBalanceDao.insertAll(accounts)

    suspend fun updateAccount(account: AccountBalanceEntity) =
        accountBalanceDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountBalanceEntity) =
        accountBalanceDao.deleteAccount(account)

    suspend fun deleteAccountByName(name: String) =
        accountBalanceDao.deleteByName(name)

    suspend fun getAllAccountsSync(): List<AccountBalanceEntity> =
        accountBalanceDao.getAllAccountsSync()

    suspend fun getAccountCount(): Int =
        accountBalanceDao.getAccountCount()

    // =========================================================================
    // Combined Reactive Financial Overview
    // =========================================================================

    /**
     * Combined real-time financial health snapshot for ViewModels.
     */
    val financialSummary: Flow<FinancialSummary> = combine(
        listOf(
            totalAssets,
            totalLiabilities,
            totalEquity,
            liquidCashBalance,
            accountsReceivable,
            accountsPayable,
            totalSales,
            totalExpenses
        )
    ) { values ->
        val assets = values[0]
        val liabilities = values[1]
        val equity = values[2]
        val liquid = values[3]
        val ar = values[4]
        val ap = values[5]
        val sales = values[6]
        val expenses = values[7]
        FinancialSummary(
            totalAssets = assets,
            totalLiabilities = liabilities,
            totalEquity = equity,
            liquidCash = liquid,
            accountsReceivable = ar,
            accountsPayable = ap,
            totalSales = sales,
            totalExpenses = expenses,
            netProfit = sales - expenses
        )
    }

    // =========================================================================
    // Account Balances Over Time & Ledger Health Analytics
    // =========================================================================

    /**
     * Chronological account balances over time for Recharts visualization.
     */
    val balanceTimeline: Flow<List<AccountTimelinePoint>> = combine(
        ledgerDao?.getActiveEntries() ?: flowOf(emptyList()),
        accountBalanceDao.getAllAccounts(),
        transactionDao.getAllTransactions()
    ) { ledgerEntries, accounts, transactions ->
        FinancialHealthEngine.calculateTimelinePoints(
            ledgerEntries = ledgerEntries,
            accounts = accounts,
            transactions = transactions,
            timeRange = TimelineTimeRange.ALL_TIME
        )
    }

    /**
     * Financial health audit and assessment computed from the double-entry ledger.
     */
    val financialHealthAudit: Flow<FinancialHealthAudit> = combine(
        balanceTimeline,
        accountBalanceDao.getAllAccounts(),
        ledgerDao?.getActiveEntries() ?: flowOf(emptyList())
    ) { timeline, accounts, ledgerEntries ->
        FinancialHealthEngine.auditFinancialHealth(
            timeline = timeline,
            accounts = accounts,
            ledgerEntries = ledgerEntries
        )
    }

    fun getBalanceTimelineForRange(timeRange: TimelineTimeRange): Flow<List<AccountTimelinePoint>> =
        combine(
            ledgerDao?.getActiveEntries() ?: flowOf(emptyList()),
            accountBalanceDao.getAllAccounts(),
            transactionDao.getAllTransactions()
        ) { ledgerEntries, accounts, transactions ->
            FinancialHealthEngine.calculateTimelinePoints(
                ledgerEntries = ledgerEntries,
                accounts = accounts,
                transactions = transactions,
                timeRange = timeRange
            )
        }

    // =========================================================================
    // Synchronized Accounting Operations
    // =========================================================================

    /**
     * Records a transaction with optional automatic General Ledger entry posting
     * and immediate account balance synchronization.
     */
    suspend fun recordTransaction(
        transaction: TransactionEntity,
        autoPostLedger: Boolean = true
    ): Long {
        val insertedId = transactionDao.insertTransaction(transaction)

        if (autoPostLedger && ledgerDao != null) {
            postLedgerForTransaction(transaction.copy(id = insertedId))
            syncBalancesWithLedger()
        }

        return insertedId
    }

    /**
     * Synchronizes all account balances from double-entry General Ledger records.
     */
    suspend fun syncBalancesWithLedger() {
        if (ledgerDao == null) return
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

    /**
     * Posts standard double-entry General Ledger entries for a recorded transaction.
     */
    private suspend fun postLedgerForTransaction(tx: TransactionEntity) {
        val ledger = ledgerDao ?: return
        val type = tx.type.lowercase()
        val voucherNo = tx.invoiceNumber.ifBlank { "TX-${tx.id}" }
        val entries = mutableListOf<LedgerEntry>()

        when {
            type.contains("sale") -> {
                val cashAccount = if (tx.paymentMethod.contains("credit", ignoreCase = true)) "Accounts Receivable" else "Cash in Hand"
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = cashAccount,
                        accountType = "Asset",
                        partyType = "Customer",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Sales Invoice",
                        voucherNo = voucherNo,
                        debit = tx.amount,
                        credit = 0.0,
                        againstAccount = "Sales Account",
                        remarks = tx.notes.ifBlank { "Sales transaction #$voucherNo" }
                    )
                )
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = "Sales Account",
                        accountType = "Income",
                        partyType = "Customer",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Sales Invoice",
                        voucherNo = voucherNo,
                        debit = 0.0,
                        credit = tx.amount,
                        againstAccount = cashAccount,
                        remarks = tx.notes.ifBlank { "Revenue from #$voucherNo" }
                    )
                )
            }
            type.contains("purchase") -> {
                val paymentAccount = if (tx.paymentMethod.contains("credit", ignoreCase = true)) "Accounts Payable" else "Cash in Hand"
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = "Purchase Account",
                        accountType = "Expense",
                        partyType = "Supplier",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Purchase Invoice",
                        voucherNo = voucherNo,
                        debit = tx.amount,
                        credit = 0.0,
                        againstAccount = paymentAccount,
                        remarks = tx.notes.ifBlank { "Purchase transaction #$voucherNo" }
                    )
                )
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = paymentAccount,
                        accountType = if (paymentAccount == "Accounts Payable") "Liability" else "Asset",
                        partyType = "Supplier",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Purchase Invoice",
                        voucherNo = voucherNo,
                        debit = 0.0,
                        credit = tx.amount,
                        againstAccount = "Purchase Account",
                        remarks = tx.notes.ifBlank { "Paid to supplier for #$voucherNo" }
                    )
                )
            }
            type.contains("payment in") || type.contains("receipt") -> {
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = "Cash in Hand",
                        accountType = "Asset",
                        partyType = "Customer",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Payment In",
                        voucherNo = voucherNo,
                        debit = tx.amount,
                        credit = 0.0,
                        againstAccount = "Accounts Receivable",
                        remarks = tx.notes.ifBlank { "Customer payment #$voucherNo" }
                    )
                )
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = "Accounts Receivable",
                        accountType = "Asset",
                        partyType = "Customer",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Payment In",
                        voucherNo = voucherNo,
                        debit = 0.0,
                        credit = tx.amount,
                        againstAccount = "Cash in Hand",
                        remarks = tx.notes.ifBlank { "Settled receivable #$voucherNo" }
                    )
                )
            }
            type.contains("payment out") || type.contains("expense") -> {
                val expenseAccount = if (type.contains("expense")) "General & Administrative" else "Accounts Payable"
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = expenseAccount,
                        accountType = if (expenseAccount == "Accounts Payable") "Liability" else "Expense",
                        partyType = "Supplier",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Payment Out",
                        voucherNo = voucherNo,
                        debit = tx.amount,
                        credit = 0.0,
                        againstAccount = "Cash in Hand",
                        remarks = tx.notes.ifBlank { "Expense payment #$voucherNo" }
                    )
                )
                entries.add(
                    LedgerEntry(
                        postingDateMillis = tx.dateMillis,
                        postingDateBS = tx.dateBs,
                        postingDateAD = tx.dateAd,
                        account = "Cash in Hand",
                        accountType = "Asset",
                        partyType = "Supplier",
                        partyId = tx.partyId,
                        partyName = tx.partyName,
                        voucherType = "Payment Out",
                        voucherNo = voucherNo,
                        debit = 0.0,
                        credit = tx.amount,
                        againstAccount = expenseAccount,
                        remarks = tx.notes.ifBlank { "Paid from cash for #$voucherNo" }
                    )
                )
            }
        }

        if (entries.isNotEmpty()) {
            ledger.insertAll(entries)
        }
    }
}
