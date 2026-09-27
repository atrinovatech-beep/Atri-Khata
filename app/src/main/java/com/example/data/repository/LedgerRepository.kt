package com.example.data.repository

import com.example.data.local.dao.AccountBalanceRow
import com.example.data.local.dao.LedgerDao
import com.example.data.local.entity.LedgerEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs

/**
 * Repository to manage Frappe-style double-entry accounting records in the Room database.
 * Enforces the core accounting equation: Total Debits == Total Credits for every voucher.
 */
class LedgerRepository(
    private val ledgerDao: LedgerDao
) {

    val allEntries: Flow<List<LedgerEntry>> = ledgerDao.getAllEntries()

    val activeEntries: Flow<List<LedgerEntry>> = ledgerDao.getActiveEntries()

    val totalDebits: Flow<Double> = ledgerDao.getTotalDebits().map { it ?: 0.0 }

    val totalCredits: Flow<Double> = ledgerDao.getTotalCredits().map { it ?: 0.0 }

    val trialBalance: Flow<List<AccountBalanceRow>> = ledgerDao.getTrialBalance()

    fun getEntriesForVoucher(voucherType: String, voucherNo: String): Flow<List<LedgerEntry>> =
        ledgerDao.getEntriesByVoucher(voucherType, voucherNo)

    fun getEntriesForAccount(account: String): Flow<List<LedgerEntry>> =
        ledgerDao.getEntriesByAccount(account)

    fun getEntriesForParty(partyId: Long): Flow<List<LedgerEntry>> =
        ledgerDao.getEntriesByParty(partyId)

    fun getRecentLedgerHistory(limit: Int = 50): Flow<List<LedgerEntry>> =
        ledgerDao.getRecentLedgerHistory(limit)

    fun getLedgerHistoryPaged(limit: Int, offset: Int): Flow<List<LedgerEntry>> =
        ledgerDao.getLedgerHistoryPaged(limit, offset)

    fun getAccountLedgerHistory(account: String, limit: Int = 100): Flow<List<LedgerEntry>> =
        ledgerDao.getAccountLedgerHistory(account, limit)

    fun getPartyLedgerHistory(partyId: Long, limit: Int = 100): Flow<List<LedgerEntry>> =
        ledgerDao.getPartyLedgerHistory(partyId, limit)

    fun getLedgerHistoryByVoucherType(voucherType: String): Flow<List<LedgerEntry>> =
        ledgerDao.getLedgerHistoryByVoucherType(voucherType)

    fun getLedgerHistoryBetweenDates(startMillis: Long, endMillis: Long): Flow<List<LedgerEntry>> =
        ledgerDao.getLedgerHistoryBetweenDates(startMillis, endMillis)

    fun getFiscalYearHistory(fiscalYear: String): Flow<List<LedgerEntry>> =
        ledgerDao.getEntriesForFiscalYear(fiscalYear)

    fun getAccountBalance(account: String): Flow<Double> =
        ledgerDao.getAccountBalance(account).map { it ?: 0.0 }

    fun getPartyBalance(partyId: Long): Flow<Double> =
        ledgerDao.getPartyBalance(partyId).map { it ?: 0.0 }

    /**
     * Record a balanced double-entry transaction.
     * Enforces that total debits equal total credits within 0.01 tolerance.
     */
    suspend fun recordDoubleEntry(entries: List<LedgerEntry>): Result<List<Long>> {
        if (entries.isEmpty()) {
            return Result.failure(IllegalArgumentException("Ledger entries list cannot be empty"))
        }

        val totalDebit = entries.sumOf { it.debit }
        val totalCredit = entries.sumOf { it.credit }

        if (abs(totalDebit - totalCredit) > 0.01) {
            return Result.failure(
                IllegalStateException(
                    "Double-entry unbalanced: Total Debits (Rs. $totalDebit) must equal Total Credits (Rs. $totalCredit)"
                )
            )
        }

        return try {
            val ids = ledgerDao.insertAll(entries)
            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Post Frappe-style GL entries for a Sales Invoice:
     * - Debit: Cash or Bank (for immediate payments)
     * - Debit: Accounts Receivable (for customer balance due)
     * - Credit: Sales Account (for grand total income)
     */
    suspend fun recordSalesInvoiceGL(
        invoiceNumber: String,
        partyId: Long?,
        partyName: String?,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        dateMillis: Long = System.currentTimeMillis(),
        dateBs: String = "",
        dateAd: String = "",
        fiscalYear: String = "",
        remarks: String = ""
    ): Result<List<Long>> {
        if (grandTotal <= 0.0) return Result.success(emptyList())

        val entries = mutableListOf<LedgerEntry>()
        val dueAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)
        val settledCash = paidAmount.coerceAtMost(grandTotal)

        // 1. Credit Sales Account (Income)
        entries.add(
            LedgerEntry(
                postingDateMillis = dateMillis,
                postingDateBS = dateBs,
                postingDateAD = dateAd,
                account = "Sales Account",
                accountType = "Income",
                partyType = "Customer",
                partyId = partyId,
                partyName = partyName,
                voucherType = "Sales Invoice",
                voucherNo = invoiceNumber,
                debit = 0.0,
                credit = grandTotal,
                againstAccount = if (settledCash > 0 && dueAmount > 0) "Cash & Debtors" else if (settledCash > 0) paymentMethod else "Accounts Receivable",
                fiscalYear = fiscalYear,
                remarks = remarks.ifBlank { "Sales Invoice #$invoiceNumber" }
            )
        )

        // 2. Debit Cash / Bank (Liquid Asset)
        if (settledCash > 0.0) {
            val cashAccount = if (paymentMethod.contains("bank", ignoreCase = true) || paymentMethod.contains("fonepay", ignoreCase = true) || paymentMethod.contains("cheque", ignoreCase = true)) {
                "Bank Account"
            } else {
                "Cash in Hand"
            }
            entries.add(
                LedgerEntry(
                    postingDateMillis = dateMillis,
                    postingDateBS = dateBs,
                    postingDateAD = dateAd,
                    account = cashAccount,
                    accountType = "Asset",
                    partyType = "Customer",
                    partyId = partyId,
                    partyName = partyName,
                    voucherType = "Sales Invoice",
                    voucherNo = invoiceNumber,
                    debit = settledCash,
                    credit = 0.0,
                    againstAccount = "Sales Account",
                    fiscalYear = fiscalYear,
                    remarks = "Received via $paymentMethod for #$invoiceNumber"
                )
            )
        }

        // 3. Debit Accounts Receivable (Debtors Asset)
        if (dueAmount > 0.0) {
            entries.add(
                LedgerEntry(
                    postingDateMillis = dateMillis,
                    postingDateBS = dateBs,
                    postingDateAD = dateAd,
                    account = "Accounts Receivable",
                    accountType = "Asset",
                    partyType = "Customer",
                    partyId = partyId,
                    partyName = partyName,
                    voucherType = "Sales Invoice",
                    voucherNo = invoiceNumber,
                    debit = dueAmount,
                    credit = 0.0,
                    againstAccount = "Sales Account",
                    fiscalYear = fiscalYear,
                    remarks = "Credit sale balance for #$invoiceNumber"
                )
            )
        }

        return recordDoubleEntry(entries)
    }

    /**
     * Post Frappe-style GL entries for a Purchase Invoice:
     * - Debit: Purchase Account / Inventory
     * - Credit: Cash or Bank (for immediate payments)
     * - Credit: Accounts Payable (for supplier balance due)
     */
    suspend fun recordPurchaseInvoiceGL(
        purchaseNumber: String,
        partyId: Long?,
        partyName: String?,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        dateMillis: Long = System.currentTimeMillis(),
        dateBs: String = "",
        dateAd: String = "",
        fiscalYear: String = "",
        remarks: String = ""
    ): Result<List<Long>> {
        if (grandTotal <= 0.0) return Result.success(emptyList())

        val entries = mutableListOf<LedgerEntry>()
        val dueAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)
        val settledCash = paidAmount.coerceAtMost(grandTotal)

        // 1. Debit Purchase Account (Expense / Asset)
        entries.add(
            LedgerEntry(
                postingDateMillis = dateMillis,
                postingDateBS = dateBs,
                postingDateAD = dateAd,
                account = "Purchase Account",
                accountType = "Expense",
                partyType = "Supplier",
                partyId = partyId,
                partyName = partyName,
                voucherType = "Purchase Invoice",
                voucherNo = purchaseNumber,
                debit = grandTotal,
                credit = 0.0,
                againstAccount = if (settledCash > 0 && dueAmount > 0) "Cash & Creditors" else if (settledCash > 0) paymentMethod else "Accounts Payable",
                fiscalYear = fiscalYear,
                remarks = remarks.ifBlank { "Purchase Invoice #$purchaseNumber" }
            )
        )

        // 2. Credit Cash / Bank
        if (settledCash > 0.0) {
            val cashAccount = if (paymentMethod.contains("bank", ignoreCase = true) || paymentMethod.contains("fonepay", ignoreCase = true) || paymentMethod.contains("cheque", ignoreCase = true)) {
                "Bank Account"
            } else {
                "Cash in Hand"
            }
            entries.add(
                LedgerEntry(
                    postingDateMillis = dateMillis,
                    postingDateBS = dateBs,
                    postingDateAD = dateAd,
                    account = cashAccount,
                    accountType = "Asset",
                    partyType = "Supplier",
                    partyId = partyId,
                    partyName = partyName,
                    voucherType = "Purchase Invoice",
                    voucherNo = purchaseNumber,
                    debit = 0.0,
                    credit = settledCash,
                    againstAccount = "Purchase Account",
                    fiscalYear = fiscalYear,
                    remarks = "Paid via $paymentMethod for #$purchaseNumber"
                )
            )
        }

        // 3. Credit Accounts Payable (Creditors Liability)
        if (dueAmount > 0.0) {
            entries.add(
                LedgerEntry(
                    postingDateMillis = dateMillis,
                    postingDateBS = dateBs,
                    postingDateAD = dateAd,
                    account = "Accounts Payable",
                    accountType = "Liability",
                    partyType = "Supplier",
                    partyId = partyId,
                    partyName = partyName,
                    voucherType = "Purchase Invoice",
                    voucherNo = purchaseNumber,
                    debit = 0.0,
                    credit = dueAmount,
                    againstAccount = "Purchase Account",
                    fiscalYear = fiscalYear,
                    remarks = "Credit purchase balance for #$purchaseNumber"
                )
            )
        }

        return recordDoubleEntry(entries)
    }

    /**
     * Post Frappe-style GL entries for Payment In (Customer Receipt):
     * - Debit: Cash or Bank
     * - Credit: Accounts Receivable
     */
    suspend fun recordPaymentInGL(
        receiptNumber: String,
        partyId: Long?,
        partyName: String?,
        amount: Double,
        paymentMethod: String,
        dateMillis: Long = System.currentTimeMillis(),
        dateBs: String = "",
        dateAd: String = "",
        fiscalYear: String = "",
        remarks: String = ""
    ): Result<List<Long>> {
        if (amount <= 0.0) return Result.success(emptyList())

        val cashAccount = if (paymentMethod.contains("bank", ignoreCase = true) || paymentMethod.contains("fonepay", ignoreCase = true) || paymentMethod.contains("cheque", ignoreCase = true)) {
            "Bank Account"
        } else {
            "Cash in Hand"
        }

        val entries = listOf(
            LedgerEntry(
                postingDateMillis = dateMillis,
                postingDateBS = dateBs,
                postingDateAD = dateAd,
                account = cashAccount,
                accountType = "Asset",
                partyType = "Customer",
                partyId = partyId,
                partyName = partyName,
                voucherType = "Payment In",
                voucherNo = receiptNumber,
                debit = amount,
                credit = 0.0,
                againstAccount = "Accounts Receivable",
                fiscalYear = fiscalYear,
                remarks = remarks.ifBlank { "Payment In #$receiptNumber" }
            ),
            LedgerEntry(
                postingDateMillis = dateMillis,
                postingDateBS = dateBs,
                postingDateAD = dateAd,
                account = "Accounts Receivable",
                accountType = "Asset",
                partyType = "Customer",
                partyId = partyId,
                partyName = partyName,
                voucherType = "Payment In",
                voucherNo = receiptNumber,
                debit = 0.0,
                credit = amount,
                againstAccount = cashAccount,
                fiscalYear = fiscalYear,
                remarks = remarks.ifBlank { "Settled from customer receipt #$receiptNumber" }
            )
        )

        return recordDoubleEntry(entries)
    }

    /**
     * Post Frappe-style GL entries for Payment Out (Supplier / Expense Payment):
     * - Debit: Accounts Payable (or Expense)
     * - Credit: Cash or Bank
     */
    suspend fun recordPaymentOutGL(
        voucherNumber: String,
        partyId: Long?,
        partyName: String?,
        amount: Double,
        paymentMethod: String,
        expenseAccount: String = "Accounts Payable",
        dateMillis: Long = System.currentTimeMillis(),
        dateBs: String = "",
        dateAd: String = "",
        fiscalYear: String = "",
        remarks: String = ""
    ): Result<List<Long>> {
        if (amount <= 0.0) return Result.success(emptyList())

        val cashAccount = if (paymentMethod.contains("bank", ignoreCase = true) || paymentMethod.contains("fonepay", ignoreCase = true) || paymentMethod.contains("cheque", ignoreCase = true)) {
            "Bank Account"
        } else {
            "Cash in Hand"
        }

        val entries = listOf(
            LedgerEntry(
                postingDateMillis = dateMillis,
                postingDateBS = dateBs,
                postingDateAD = dateAd,
                account = expenseAccount,
                accountType = if (expenseAccount == "Accounts Payable") "Liability" else "Expense",
                partyType = "Supplier",
                partyId = partyId,
                partyName = partyName,
                voucherType = "Payment Out",
                voucherNo = voucherNumber,
                debit = amount,
                credit = 0.0,
                againstAccount = cashAccount,
                fiscalYear = fiscalYear,
                remarks = remarks.ifBlank { "Payment Out #$voucherNumber" }
            ),
            LedgerEntry(
                postingDateMillis = dateMillis,
                postingDateBS = dateBs,
                postingDateAD = dateAd,
                account = cashAccount,
                accountType = "Asset",
                partyType = "Supplier",
                partyId = partyId,
                partyName = partyName,
                voucherType = "Payment Out",
                voucherNo = voucherNumber,
                debit = 0.0,
                credit = amount,
                againstAccount = expenseAccount,
                fiscalYear = fiscalYear,
                remarks = remarks.ifBlank { "Paid via $paymentMethod for #$voucherNumber" }
            )
        )

        return recordDoubleEntry(entries)
    }

    suspend fun cancelVoucher(voucherType: String, voucherNo: String) {
        ledgerDao.cancelVoucherEntries(voucherType, voucherNo)
    }

    suspend fun deleteVoucherEntries(voucherType: String, voucherNo: String) {
        ledgerDao.deleteEntriesByVoucher(voucherType, voucherNo)
    }

    suspend fun getAllEntriesSync(): List<LedgerEntry> = ledgerDao.getAllEntriesSync()

    suspend fun getEntriesCount(): Int = ledgerDao.getEntriesCount()

    suspend fun restoreLedgerEntries(entries: List<LedgerEntry>, replaceExisting: Boolean = false): Int {
        if (replaceExisting) {
            ledgerDao.deleteAllEntries()
        }
        val insertedIds = ledgerDao.insertAll(entries)
        return insertedIds.size
    }
}
