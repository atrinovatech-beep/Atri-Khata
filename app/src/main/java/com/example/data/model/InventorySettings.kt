package com.example.data.model

import org.json.JSONObject

/**
 * Atri Khata 2 - Business Inventory Presets
 */
enum class BusinessInventoryPreset(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultUnit: String
) {
    RETAIL(
        id = "retail",
        displayName = "Retail & General Store",
        description = "FMCG, garments, electronics, retail shop",
        defaultUnit = "Pcs"
    ),
    PHARMACY(
        id = "pharmacy",
        displayName = "Pharmacy & Healthcare",
        description = "Medicines, batch tracking, expiry monitoring",
        defaultUnit = "Box"
    ),
    WHOLESALE(
        id = "wholesale",
        displayName = "Wholesale & Distribution",
        description = "Bulk cartons, B2B trade, high volume stock",
        defaultUnit = "Box"
    ),
    SERVICE(
        id = "service",
        displayName = "Service Business",
        description = "Consultancy, salon, repair, service-only billing",
        defaultUnit = "Pcs"
    ),
    PERSONAL(
        id = "personal",
        displayName = "Personal Khata",
        description = "Personal budgeting, non-inventory ledger",
        defaultUnit = "Pcs"
    );

    companion object {
        fun fromBusinessType(businessType: String): BusinessInventoryPreset {
            val lower = businessType.lowercase()
            return when {
                lower.contains("pharmacy") || lower.contains("medical") || lower.contains("health") -> PHARMACY
                lower.contains("wholesale") || lower.contains("distribut") -> WHOLESALE
                lower.contains("service") || lower.contains("consult") -> SERVICE
                lower.contains("personal") -> PERSONAL
                else -> RETAIL
            }
        }

        fun fromId(id: String): BusinessInventoryPreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: RETAIL
        }
    }
}

/**
 * Supported & Future-ready Stock Valuation Methods
 */
enum class StockValuationMethod(
    val displayName: String,
    val formulaDescription: String,
    val isAvailable: Boolean
) {
    PURCHASE_PRICE(
        displayName = "Purchase Price",
        formulaDescription = "Stock Value = Quantity × Purchase Price (Active)",
        isAvailable = true
    ),
    WEIGHTED_AVERAGE(
        displayName = "Weighted Average Cost",
        formulaDescription = "Average cost across procurement lots (Future-ready)",
        isAvailable = false
    ),
    FIFO(
        displayName = "FIFO (First In First Out)",
        formulaDescription = "Values based on earliest available stock lot (Future-ready)",
        isAvailable = false
    );

    companion object {
        fun fromName(name: String): StockValuationMethod {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PURCHASE_PRICE
        }
    }
}

/**
 * Expired item sale handling
 */
enum class ExpiredItemAction(
    val displayName: String,
    val description: String
) {
    BLOCK_SALE("Block Sale", "Disallow billing expired products"),
    SHOW_WARNING("Show Warning", "Display warning alert but allow billing"),
    ALLOW_CONFIRMATION("Allow with Confirmation", "Prompt cashier for authorization");

    companion object {
        fun fromName(name: String): ExpiredItemAction {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: BLOCK_SALE
        }
    }
}

/**
 * Default sale price selection type
 */
enum class DefaultSalePriceType(val displayName: String) {
    SALE_PRICE("Standard Sale Price"),
    MRP("MRP (Maximum Retail Price)"),
    CUSTOM_PRICE("Custom Price");

    companion object {
        fun fromName(name: String): DefaultSalePriceType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SALE_PRICE
        }
    }
}

/**
 * Granular permissions model for inventory management
 */
data class InventoryPermissions(
    val viewInventory: Boolean = true,
    val addItem: Boolean = true,
    val editItem: Boolean = true,
    val deleteItem: Boolean = true,
    val viewPurchasePrice: Boolean = true,
    val viewStockValue: Boolean = true,
    val adjustStock: Boolean = true,
    val viewStockLedger: Boolean = true,
    val transferStock: Boolean = false
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("viewInventory", viewInventory)
        obj.put("addItem", addItem)
        obj.put("editItem", editItem)
        obj.put("deleteItem", deleteItem)
        obj.put("viewPurchasePrice", viewPurchasePrice)
        obj.put("viewStockValue", viewStockValue)
        obj.put("adjustStock", adjustStock)
        obj.put("viewStockLedger", viewStockLedger)
        obj.put("transferStock", transferStock)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject?): InventoryPermissions {
            if (obj == null) return InventoryPermissions()
            return InventoryPermissions(
                viewInventory = obj.optBoolean("viewInventory", true),
                addItem = obj.optBoolean("addItem", true),
                editItem = obj.optBoolean("editItem", true),
                deleteItem = obj.optBoolean("deleteItem", true),
                viewPurchasePrice = obj.optBoolean("viewPurchasePrice", true),
                viewStockValue = obj.optBoolean("viewStockValue", true),
                adjustStock = obj.optBoolean("adjustStock", true),
                viewStockLedger = obj.optBoolean("viewStockLedger", true),
                transferStock = obj.optBoolean("transferStock", false)
            )
        }
    }
}

/**
 * Complete, persistent, future-ready Inventory Configuration State
 */
data class InventorySettings(
    // Business Preset
    val businessPreset: BusinessInventoryPreset = BusinessInventoryPreset.RETAIL,

    // 1. General Inventory
    val inventoryModuleEnabled: Boolean = true,
    val trackStockAutomatically: Boolean = true,
    val showStockValue: Boolean = true,
    val showStockStatus: Boolean = true,
    val enableCategories: Boolean = true,
    val enableSku: Boolean = true,
    val enableBarcode: Boolean = true,

    // 2. Negative Stock
    val allowNegativeStock: Boolean = false,

    // 3. Item / Product Settings
    val itemNameEnabled: Boolean = true, // Fixed ON
    val skuEnabled: Boolean = true,
    val categoryEnabled: Boolean = true,
    val brandEnabled: Boolean = false,
    val unitEnabled: Boolean = true,
    val descriptionEnabled: Boolean = false,
    val purchasePriceEnabled: Boolean = true,
    val salePriceEnabled: Boolean = true,
    val vatEnabled: Boolean = true,
    val barcodeEnabled: Boolean = true,
    val itemImageEnabled: Boolean = false,

    // 4. Item Price Settings
    val defaultSalePriceType: DefaultSalePriceType = DefaultSalePriceType.SALE_PRICE,

    // 5. Stock Control
    val openingStockEnabled: Boolean = true,
    val stockAdjustmentEnabled: Boolean = true,
    val stockInEnabled: Boolean = true,
    val stockOutEnabled: Boolean = true,
    val stockLedgerEnabled: Boolean = true,
    val stockTransferEnabled: Boolean = false,

    // 6. Manual Stock Adjustment
    val allowManualStockAdjustment: Boolean = true,
    val requireAdjustmentReason: Boolean = true,
    val requireApprovalForAdjustment: Boolean = false,

    // 7 & 8. Low Stock & Alerts
    val lowStockAlertEnabled: Boolean = true,
    val outOfStockAlertEnabled: Boolean = true,
    val defaultLowStockThreshold: Int = 10,
    val defaultMeasurementUnit: String = "Pcs",

    // 9 & 10. Batch & Expiry
    val batchTrackingEnabled: Boolean = false,
    val expiryTrackingEnabled: Boolean = false,
    val requireBatchOnPurchase: Boolean = false,
    val requireBatchOnSale: Boolean = false,
    val allowMultipleBatches: Boolean = true,
    val expiryAlertEnabled: Boolean = false,
    val nearExpiryAlertEnabled: Boolean = false,
    val nearExpiryWarningDays: Int = 30,
    val criticalExpiryWarningDays: Int = 7,

    // 11. Expired Item Sale Action
    val expiredItemSaleAction: ExpiredItemAction = ExpiredItemAction.BLOCK_SALE,

    // 12. Stock Valuation
    val stockValuationMethod: StockValuationMethod = StockValuationMethod.PURCHASE_PRICE,

    // 13. Sales Integration
    val decreaseStockOnSale: Boolean = true,
    val allowSaleWithoutStock: Boolean = false,

    // 14. Purchase Integration
    val increaseStockOnPurchase: Boolean = true,

    // 15. Permissions
    val permissions: InventoryPermissions = InventoryPermissions()
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("businessPreset", businessPreset.id)
        obj.put("inventoryModuleEnabled", inventoryModuleEnabled)
        obj.put("trackStockAutomatically", trackStockAutomatically)
        obj.put("showStockValue", showStockValue)
        obj.put("showStockStatus", showStockStatus)
        obj.put("enableCategories", enableCategories)
        obj.put("enableSku", enableSku)
        obj.put("enableBarcode", enableBarcode)

        obj.put("allowNegativeStock", allowNegativeStock)

        obj.put("itemNameEnabled", itemNameEnabled)
        obj.put("skuEnabled", skuEnabled)
        obj.put("categoryEnabled", categoryEnabled)
        obj.put("brandEnabled", brandEnabled)
        obj.put("unitEnabled", unitEnabled)
        obj.put("descriptionEnabled", descriptionEnabled)
        obj.put("purchasePriceEnabled", purchasePriceEnabled)
        obj.put("salePriceEnabled", salePriceEnabled)
        obj.put("vatEnabled", vatEnabled)
        obj.put("barcodeEnabled", barcodeEnabled)
        obj.put("itemImageEnabled", itemImageEnabled)

        obj.put("defaultSalePriceType", defaultSalePriceType.name)

        obj.put("openingStockEnabled", openingStockEnabled)
        obj.put("stockAdjustmentEnabled", stockAdjustmentEnabled)
        obj.put("stockInEnabled", stockInEnabled)
        obj.put("stockOutEnabled", stockOutEnabled)
        obj.put("stockLedgerEnabled", stockLedgerEnabled)
        obj.put("stockTransferEnabled", stockTransferEnabled)

        obj.put("allowManualStockAdjustment", allowManualStockAdjustment)
        obj.put("requireAdjustmentReason", requireAdjustmentReason)
        obj.put("requireApprovalForAdjustment", requireApprovalForAdjustment)

        obj.put("lowStockAlertEnabled", lowStockAlertEnabled)
        obj.put("outOfStockAlertEnabled", outOfStockAlertEnabled)
        obj.put("defaultLowStockThreshold", defaultLowStockThreshold)
        obj.put("defaultMeasurementUnit", defaultMeasurementUnit)

        obj.put("batchTrackingEnabled", batchTrackingEnabled)
        obj.put("expiryTrackingEnabled", expiryTrackingEnabled)
        obj.put("requireBatchOnPurchase", requireBatchOnPurchase)
        obj.put("requireBatchOnSale", requireBatchOnSale)
        obj.put("allowMultipleBatches", allowMultipleBatches)
        obj.put("expiryAlertEnabled", expiryAlertEnabled)
        obj.put("nearExpiryAlertEnabled", nearExpiryAlertEnabled)
        obj.put("nearExpiryWarningDays", nearExpiryWarningDays)
        obj.put("criticalExpiryWarningDays", criticalExpiryWarningDays)

        obj.put("expiredItemSaleAction", expiredItemSaleAction.name)
        obj.put("stockValuationMethod", stockValuationMethod.name)

        obj.put("decreaseStockOnSale", decreaseStockOnSale)
        obj.put("allowSaleWithoutStock", allowSaleWithoutStock)
        obj.put("increaseStockOnPurchase", increaseStockOnPurchase)

        obj.put("permissions", permissions.toJson())

        return obj.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String?): InventorySettings? {
            if (jsonStr.isNullOrBlank()) return null
            return try {
                val obj = JSONObject(jsonStr)
                InventorySettings(
                    businessPreset = BusinessInventoryPreset.fromId(obj.optString("businessPreset", "retail")),
                    inventoryModuleEnabled = obj.optBoolean("inventoryModuleEnabled", true),
                    trackStockAutomatically = obj.optBoolean("trackStockAutomatically", true),
                    showStockValue = obj.optBoolean("showStockValue", true),
                    showStockStatus = obj.optBoolean("showStockStatus", true),
                    enableCategories = obj.optBoolean("enableCategories", true),
                    enableSku = obj.optBoolean("enableSku", true),
                    enableBarcode = obj.optBoolean("enableBarcode", true),
                    allowNegativeStock = obj.optBoolean("allowNegativeStock", false),

                    itemNameEnabled = true,
                    skuEnabled = obj.optBoolean("skuEnabled", true),
                    categoryEnabled = obj.optBoolean("categoryEnabled", true),
                    brandEnabled = obj.optBoolean("brandEnabled", false),
                    unitEnabled = obj.optBoolean("unitEnabled", true),
                    descriptionEnabled = obj.optBoolean("descriptionEnabled", false),
                    purchasePriceEnabled = obj.optBoolean("purchasePriceEnabled", true),
                    salePriceEnabled = obj.optBoolean("salePriceEnabled", true),
                    vatEnabled = obj.optBoolean("vatEnabled", true),
                    barcodeEnabled = obj.optBoolean("barcodeEnabled", true),
                    itemImageEnabled = obj.optBoolean("itemImageEnabled", false),

                    defaultSalePriceType = DefaultSalePriceType.fromName(obj.optString("defaultSalePriceType", "SALE_PRICE")),

                    openingStockEnabled = obj.optBoolean("openingStockEnabled", true),
                    stockAdjustmentEnabled = obj.optBoolean("stockAdjustmentEnabled", true),
                    stockInEnabled = obj.optBoolean("stockInEnabled", true),
                    stockOutEnabled = obj.optBoolean("stockOutEnabled", true),
                    stockLedgerEnabled = obj.optBoolean("stockLedgerEnabled", true),
                    stockTransferEnabled = obj.optBoolean("stockTransferEnabled", false),

                    allowManualStockAdjustment = obj.optBoolean("allowManualStockAdjustment", true),
                    requireAdjustmentReason = obj.optBoolean("requireAdjustmentReason", true),
                    requireApprovalForAdjustment = obj.optBoolean("requireApprovalForAdjustment", false),

                    lowStockAlertEnabled = obj.optBoolean("lowStockAlertEnabled", true),
                    outOfStockAlertEnabled = obj.optBoolean("outOfStockAlertEnabled", true),
                    defaultLowStockThreshold = obj.optInt("defaultLowStockThreshold", 10),
                    defaultMeasurementUnit = obj.optString("defaultMeasurementUnit", "Pcs"),

                    batchTrackingEnabled = obj.optBoolean("batchTrackingEnabled", false),
                    expiryTrackingEnabled = obj.optBoolean("expiryTrackingEnabled", false),
                    requireBatchOnPurchase = obj.optBoolean("requireBatchOnPurchase", false),
                    requireBatchOnSale = obj.optBoolean("requireBatchOnSale", false),
                    allowMultipleBatches = obj.optBoolean("allowMultipleBatches", true),
                    expiryAlertEnabled = obj.optBoolean("expiryAlertEnabled", false),
                    nearExpiryAlertEnabled = obj.optBoolean("nearExpiryAlertEnabled", false),
                    nearExpiryWarningDays = obj.optInt("nearExpiryWarningDays", 30),
                    criticalExpiryWarningDays = obj.optInt("criticalExpiryWarningDays", 7),

                    expiredItemSaleAction = ExpiredItemAction.fromName(obj.optString("expiredItemSaleAction", "BLOCK_SALE")),
                    stockValuationMethod = StockValuationMethod.fromName(obj.optString("stockValuationMethod", "PURCHASE_PRICE")),

                    decreaseStockOnSale = obj.optBoolean("decreaseStockOnSale", true),
                    allowSaleWithoutStock = obj.optBoolean("allowSaleWithoutStock", false),
                    increaseStockOnPurchase = obj.optBoolean("increaseStockOnPurchase", true),

                    permissions = InventoryPermissions.fromJson(obj.optJSONObject("permissions"))
                )
            } catch (e: Exception) {
                null
            }
        }

        fun createDefaultsFor(preset: BusinessInventoryPreset): InventorySettings {
            return when (preset) {
                BusinessInventoryPreset.PHARMACY -> InventorySettings(
                    businessPreset = BusinessInventoryPreset.PHARMACY,
                    inventoryModuleEnabled = true,
                    trackStockAutomatically = true,
                    showStockValue = true,
                    showStockStatus = true,
                    enableCategories = true,
                    enableSku = true,
                    enableBarcode = true,
                    allowNegativeStock = false,
                    batchTrackingEnabled = true,
                    expiryTrackingEnabled = true,
                    requireBatchOnPurchase = true,
                    requireBatchOnSale = true,
                    allowMultipleBatches = true,
                    expiryAlertEnabled = true,
                    nearExpiryAlertEnabled = true,
                    nearExpiryWarningDays = 30,
                    criticalExpiryWarningDays = 7,
                    expiredItemSaleAction = ExpiredItemAction.BLOCK_SALE,
                    vatEnabled = true,
                    lowStockAlertEnabled = true,
                    defaultLowStockThreshold = 10,
                    defaultMeasurementUnit = "Box"
                )
                BusinessInventoryPreset.RETAIL -> InventorySettings(
                    businessPreset = BusinessInventoryPreset.RETAIL,
                    inventoryModuleEnabled = true,
                    trackStockAutomatically = true,
                    showStockValue = true,
                    showStockStatus = true,
                    enableCategories = true,
                    enableSku = true,
                    enableBarcode = true,
                    allowNegativeStock = false,
                    batchTrackingEnabled = false,
                    expiryTrackingEnabled = false,
                    lowStockAlertEnabled = true,
                    defaultLowStockThreshold = 10,
                    defaultMeasurementUnit = "Pcs"
                )
                BusinessInventoryPreset.WHOLESALE -> InventorySettings(
                    businessPreset = BusinessInventoryPreset.WHOLESALE,
                    inventoryModuleEnabled = true,
                    trackStockAutomatically = true,
                    showStockValue = true,
                    showStockStatus = true,
                    enableCategories = true,
                    enableSku = true,
                    enableBarcode = true,
                    allowNegativeStock = false,
                    batchTrackingEnabled = true,
                    expiryTrackingEnabled = false,
                    lowStockAlertEnabled = true,
                    defaultLowStockThreshold = 25,
                    defaultMeasurementUnit = "Box"
                )
                BusinessInventoryPreset.SERVICE -> InventorySettings(
                    businessPreset = BusinessInventoryPreset.SERVICE,
                    inventoryModuleEnabled = false,
                    trackStockAutomatically = false,
                    showStockValue = false,
                    showStockStatus = false,
                    enableCategories = false,
                    enableSku = false,
                    enableBarcode = false,
                    batchTrackingEnabled = false,
                    expiryTrackingEnabled = false,
                    lowStockAlertEnabled = false,
                    decreaseStockOnSale = false
                )
                BusinessInventoryPreset.PERSONAL -> InventorySettings(
                    businessPreset = BusinessInventoryPreset.PERSONAL,
                    inventoryModuleEnabled = false,
                    trackStockAutomatically = false,
                    showStockValue = false,
                    showStockStatus = false,
                    enableCategories = false,
                    enableSku = false,
                    enableBarcode = false,
                    batchTrackingEnabled = false,
                    expiryTrackingEnabled = false,
                    lowStockAlertEnabled = false,
                    decreaseStockOnSale = false
                )
            }
        }
    }
}
