package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.NepaliDateUtils

@Entity(
    tableName = "invoice_activities",
    foreignKeys = [
        ForeignKey(
            entity = SalesInvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("invoiceId"),
        Index("timestampMillis")
    ]
)
data class InvoiceActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val action: String, // "Created", "Payment Recorded", "Edited", "Printed", "Shared PDF", "Status Changed", "Cancelled"
    val description: String,
    val performedBy: String = "Admin",
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val displayTime: String
        get() = NepaliDateUtils.formatAdDate(timestampMillis)
}
