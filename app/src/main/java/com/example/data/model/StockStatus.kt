package com.example.data.model

import com.example.data.local.entity.InventoryItemEntity

/**
 * Reusable Centralized Stock Status helper
 * - IN_STOCK: stockQuantity > minStockAlert
 * - LOW_STOCK: stockQuantity > 0.0 && stockQuantity <= minStockAlert
 * - OUT_OF_STOCK: stockQuantity <= 0.0
 */
enum class StockStatus(val label: String) {
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock");

    companion object {
        fun fromItem(item: InventoryItemEntity): StockStatus {
            return when {
                item.stockQuantity <= 0.0 -> OUT_OF_STOCK
                item.stockQuantity <= item.minStockAlert -> LOW_STOCK
                else -> IN_STOCK
            }
        }

        fun fromQuantity(quantity: Double, minStockAlert: Double): StockStatus {
            return when {
                quantity <= 0.0 -> OUT_OF_STOCK
                quantity <= minStockAlert -> LOW_STOCK
                else -> IN_STOCK
            }
        }
    }
}

/**
 * Inventory Sorting options
 */
enum class InventorySortOrder(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    STOCK_LOW_HIGH("Stock: Low → High"),
    STOCK_HIGH_LOW("Stock: High → Low"),
    PRICE_LOW_HIGH("Price: Low → High"),
    PRICE_HIGH_LOW("Price: High → Low"),
    RECENTLY_ADDED("Recently Added")
}

/**
 * Dashboard Summary derived reactively from Room database
 */
data class InventoryDashboardSummary(
    val totalItems: Int = 0,
    val totalStockQuantity: Double = 0.0,
    val totalStockValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val categoriesCount: Int = 0
)
