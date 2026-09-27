package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Frappe / ERPNext-style General Ledger (GL) Entry.
 * Represents a single debit or credit entry in a double-entry accounting ledger.
 *
 * Rule: For every financial transaction (voucher), the sum of all debits must equal
 * the sum of all credits across its ledger entries:
 *   Sum(debit) == Sum(credit)
 */
@Entity(
    tableName = "ledger_entries",
    indices = [
        Index(value = ["voucherNo"]),
        Index(value = ["voucherType"]),
        Index(value = ["account"]),
        Index(value = ["partyId"]),
        Index(value = ["postingDateMillis"]),
        Index(value = ["fiscalYear"]),
        Index(value = ["isCancelled"])
    ]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Posting date information
    val postingDateMillis: Long = System.currentTimeMillis(),
    val postingDateBS: String = "",
    val postingDateAD: String = "",

    // Chart of Accounts leg
    val account: String, // e.g. "Cash", "Bank - Nabil", "Accounts Receivable", "Sales", "Accounts Payable"
    val accountType: String = "Asset", // "Asset", "Liability", "Equity", "Income", "Expense"

    // Sub-ledger / Party link
    val partyType: String? = null, // "Customer", "Supplier", "Staff", null
    val partyId: Long? = null,
    val partyName: String? = null,

    // Voucher / Source document link
    val voucherType: String, // "Sales Invoice", "Purchase Invoice", "Payment In", "Payment Out", "Journal Entry"
    val voucherNo: String, // Invoice or receipt reference

    // Financial leg (debit and credit)
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val netAmount: Double = debit - credit, // Positive for Dr, Negative for Cr

    // Contra / Against Account
    val againstAccount: String = "", // e.g., "Cash", "Sales", "Debtors"
    val currency: String = "Rs.",
    val fiscalYear: String = "",
    val remarks: String = "",

    // Audit and lifecycle
    val isCancelled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
