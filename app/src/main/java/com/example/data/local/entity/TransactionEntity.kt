package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.NepaliDateUtils

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partyId: Long = 0L,
    val partyName: String,
    val type: String, // "Sales Invoice", "Purchase", "Payment In", "Payment Out", "Expense"
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val dateBs: String = "", // e.g. "2082/06/21"
    val dateAd: String = "", // e.g. "17 Sep 2026"
    val paymentMethod: String = "Cash", // "Cash", "Bank Transfer", "UPI / Online", "Credit"
    val invoiceNumber: String = "",
    val notes: String = "",
    val status: String = "Completed",
    val salesInvoiceId: Long? = null
) {
    val displayBsDate: String
        get() = if (dateBs.isNotBlank()) dateBs else NepaliDateUtils.formatBsDate(dateMillis)

    val displayAdDate: String
        get() = if (dateAd.isNotBlank()) dateAd else NepaliDateUtils.formatAdDate(dateMillis)
}
