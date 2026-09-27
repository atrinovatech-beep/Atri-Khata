package com.example

import com.example.data.analytics.FinancialHealthEngine
import com.example.data.local.entity.AccountBalanceEntity
import com.example.data.local.entity.LedgerEntry
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.TimelineTimeRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialHealthEngineTest {

    @Test
    fun calculateTimelinePoints_withLedgerEntries_calculatesCorrectRunningBalances() {
        val accounts = listOf(
            AccountBalanceEntity(accountCode = "1010", accountName = "Cash in Hand", accountType = "Asset"),
            AccountBalanceEntity(accountCode = "1020", accountName = "Bank Account", accountType = "Asset"),
            AccountBalanceEntity(accountCode = "1110", accountName = "Accounts Receivable", accountType = "Asset"),
            AccountBalanceEntity(accountCode = "2010", accountName = "Accounts Payable", accountType = "Liability"),
            AccountBalanceEntity(accountCode = "4010", accountName = "Sales Account", accountType = "Income")
        )

        val entries = listOf(
            LedgerEntry(
                postingDateMillis = 1700000000000L,
                postingDateBS = "2080/08/01",
                postingDateAD = "2023-11-15",
                account = "Cash in Hand",
                accountType = "Asset",
                voucherType = "Sales Invoice",
                voucherNo = "INV-001",
                debit = 50000.0,
                credit = 0.0
            ),
            LedgerEntry(
                postingDateMillis = 1700000000000L,
                postingDateBS = "2080/08/01",
                postingDateAD = "2023-11-15",
                account = "Sales Account",
                accountType = "Income",
                voucherType = "Sales Invoice",
                voucherNo = "INV-001",
                debit = 0.0,
                credit = 50000.0
            )
        )

        val timeline = FinancialHealthEngine.calculateTimelinePoints(
            ledgerEntries = entries,
            accounts = accounts,
            transactions = emptyList(),
            timeRange = TimelineTimeRange.ALL_TIME
        )

        assertTrue("Timeline should contain points", timeline.isNotEmpty())
        val latest = timeline.last()
        assertEquals(50000.0, latest.liquidCash, 0.01)
        assertEquals(50000.0, latest.totalAssets, 0.01)
        assertEquals(0.0, latest.totalLiabilities, 0.01)
        assertEquals(50000.0, latest.netWorth, 0.01)
    }

    @Test
    fun auditFinancialHealth_withBalancedLedger_verifiesBalanceAndRatios() {
        val accounts = listOf(
            AccountBalanceEntity(accountCode = "1010", accountName = "Cash in Hand", accountType = "Asset", currentBalance = 75000.0),
            AccountBalanceEntity(accountCode = "2010", accountName = "Accounts Payable", accountType = "Liability", currentBalance = 25000.0)
        )

        val entries = listOf(
            LedgerEntry(account = "Cash in Hand", accountType = "Asset", voucherType = "V1", voucherNo = "1", debit = 75000.0, credit = 0.0),
            LedgerEntry(account = "Capital", accountType = "Equity", voucherType = "V1", voucherNo = "1", debit = 0.0, credit = 75000.0)
        )

        val timeline = FinancialHealthEngine.calculateTimelinePoints(
            ledgerEntries = entries,
            accounts = accounts,
            transactions = emptyList()
        )

        val audit = FinancialHealthEngine.auditFinancialHealth(
            timeline = timeline,
            accounts = accounts,
            ledgerEntries = entries
        )

        assertNotNull(audit)
        assertTrue("Ledger should be balanced", audit.isLedgerBalanced)
        assertEquals(0.0, audit.balanceDifference, 0.001)
        assertTrue("Health score should be high for positive liquidity", audit.overallScore >= 70)
    }
}
