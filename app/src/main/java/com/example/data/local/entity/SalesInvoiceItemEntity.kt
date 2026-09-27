package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = SalesInvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("invoiceId"),
        Index("productId")
    ]
)
data class SalesInvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val productId: Long? = null,
    val productNameSnapshot: String, // Stored snapshot so invoice is immutable to later catalog edits
    val productCodeSnapshot: String? = null, // SKU/barcode snapshot
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val rate: Double = 0.0,
    val discount: Double = 0.0,
    val discountPercent: Double = 0.0,
    val vatRate: Double = 0.0, // e.g. 13.0
    val vatAmount: Double = 0.0,
    val lineTotal: Double = 0.0,
    val batchNumber: String? = null,
    val expiryDate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
