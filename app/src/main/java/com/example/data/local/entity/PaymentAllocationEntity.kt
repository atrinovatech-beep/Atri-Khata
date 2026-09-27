package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.NepaliDateUtils

@Entity(
    tableName = "payment_allocations",
    foreignKeys = [
        ForeignKey(
            entity = SalesInvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("invoiceId"),
        Index("partyId"),
        Index("paymentDateMillis")
    ]
)
data class PaymentAllocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val partyId: Long? = null,
    val paymentAmount: Double,
    val paymentDateMillis: Long = System.currentTimeMillis(),
    val paymentDateBS: String = "",
    val paymentDateAD: String = "",
    val paymentMethod: String = "Cash", // "Cash", "Bank Transfer", "UPI / Online", "Cheque"
    val paymentAccountId: String? = null,
    val transactionReference: String? = null, // e.g. Cheque No / Fonepay UTR
    val remarks: String? = null,
    val recordedBy: String = "Admin",
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayBsDate: String
        get() = if (paymentDateBS.isNotBlank()) paymentDateBS else NepaliDateUtils.formatBsDate(paymentDateMillis)

    val displayAdDate: String
        get() = if (paymentDateAD.isNotBlank()) paymentDateAD else NepaliDateUtils.formatAdDate(paymentDateMillis)
}
