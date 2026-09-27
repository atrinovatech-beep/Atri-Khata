package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.NepaliDateUtils

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["partyId"]),
        Index(value = ["dateMillis"]),
        Index(value = ["type"]),
        Index(value = ["invoiceNumber"]),
        Index(value = ["salesInvoiceId"]),
        Index(value = ["account"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partyId: Long = 0L,
    val partyName: String,
    val type: String, // "Sales Invoice", "Purchase", "Payment In", "Payment Out", "Expense", "Journal Entry", "Contra"
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val dateBs: String = "", // e.g. "2082/06/21"
    val dateAd: String = "", // e.g. "17 Sep 2026"
    val paymentMethod: String = "Cash", // "Cash", "Bank Transfer", "UPI / Online", "Credit"
    val invoiceNumber: String = "",
    val notes: String = "",
    val status: String = "Completed",
    val salesInvoiceId: Long? = null,
    val account: String = "Cash in Hand", // Linked chart of accounts leg
    val category: String = "General",
    val referenceNumber: String = "",
    val taxAmount: Double = 0.0
) {
    val displayBsDate: String
        get() = if (dateBs.isNotBlank()) dateBs else NepaliDateUtils.formatBsDate(dateMillis)

    val displayAdDate: String
        get() = if (dateAd.isNotBlank()) dateAd else NepaliDateUtils.formatAdDate(dateMillis)
}
