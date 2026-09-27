package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String = "",
    val category: String = "General",
    val stockQuantity: Double = 0.0,
    val unit: String = "pcs",
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val minStockAlert: Double = 5.0,
    val createdAt: Long = System.currentTimeMillis()
)
