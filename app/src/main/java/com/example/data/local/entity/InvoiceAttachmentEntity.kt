package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_attachments",
    foreignKeys = [
        ForeignKey(
            entity = SalesInvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("invoiceId")
    ]
)
data class InvoiceAttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long = 0L,
    val fileName: String,
    val fileType: String, // "IMAGE" or "PDF"
    val mimeType: String,
    val uriString: String,
    val fileSize: Long = 0L,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
