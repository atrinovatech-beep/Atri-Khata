package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Chart of Accounts and Account Balances master entity.
 * Represents a standard ledger account (Asset, Liability, Equity, Income, Expense)
 * with opening balance, aggregated debits, aggregated credits, and live current balance.
 */
@Entity(
    tableName = "account_balances",
    indices = [
        Index(value = ["accountName"], unique = true),
        Index(value = ["accountCode"]),
        Index(value = ["accountType"]),
        Index(value = ["isActive"])
    ]
)
data class AccountBalanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Account identification
    val accountCode: String, // e.g. "1010", "1020", "1110", "2010", "3010", "4010", "5010"
    val accountName: String, // e.g. "Cash in Hand", "Bank Account", "Accounts Receivable"
    val accountType: String, // "Asset", "Liability", "Equity", "Income", "Expense"
    val rootType: String = accountType,
    val parentAccount: String? = null,

    // Balances
    val currency: String = "Rs.",
    val openingBalance: Double = 0.0,
    val totalDebit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val currentBalance: Double = 0.0, // For Assets/Expenses: (Opening + Debit - Credit); For Liabilities/Equity/Income: (Opening + Credit - Debit)

    // Properties & Audit
    val isGroup: Boolean = false,
    val isActive: Boolean = true,
    val description: String = "",
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    /**
     * Determines whether normal balance is Debit or Credit.
     */
    val isDebitNormal: Boolean
        get() = accountType == "Asset" || accountType == "Expense"

    /**
     * Calculates net balance based on accounting normal rules.
     */
    fun calculateCurrentBalance(): Double {
        return if (isDebitNormal) {
            openingBalance + totalDebit - totalCredit
        } else {
            openingBalance + totalCredit - totalDebit
        }
    }
}
