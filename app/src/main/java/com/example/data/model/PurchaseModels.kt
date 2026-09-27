package com.example.data.model

import java.util.UUID

/**
 * Atri Khata 2 - Purchase Type
 */
enum class PurchaseType(val id: String, val displayName: String, val description: String) {
    CASH_PURCHASE("cash", "Cash Purchase", "Paid immediately via cash, bank, or online wallet"),
    CREDIT_PURCHASE("credit", "Credit Purchase (Khata)", "Payable to supplier on payment terms / credit period"),
    INWARD_STOCK("inward", "Delivery Challan / Inward Stock", "Stock received without immediate bill settlement")
}

/**
 * Discount mode for Purchase line or bill
 */
enum class PurchaseDiscountType(val displayName: String, val symbol: String) {
    PERCENTAGE("Percentage", "%"),
    FIXED_AMOUNT("Fixed Amount", "Rs.")
}

/**
 * Line item in the Purchase Form
 */
data class PurchaseLineItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: Long? = null,
    var name: String = "",
    var sku: String = "",
    var unit: String = "pcs",
    var quantity: Double = 1.0,
    var unitPrice: Double = 0.0, // Purchase rate / cost
    var mrp: Double = 0.0, // Maximum Retail Price (for margin analysis)
    var discountPercent: Double = 0.0,
    var discountAmount: Double = 0.0,
    var isTaxable: Boolean = true,
    var vatRate: Double = 13.0,
    var batchNumber: String = "",
    var expiryDate: String = "",
    var manufacturingDate: String = "",
    val currentStock: Double? = null
) {
    val grossSubtotal: Double
        get() = quantity * unitPrice

    val computedDiscount: Double
        get() = if (discountPercent > 0) (grossSubtotal * discountPercent / 100.0) else discountAmount

    val netSubtotal: Double
        get() = (grossSubtotal - computedDiscount).coerceAtLeast(0.0)

    val vatAmount: Double
        get() = if (isTaxable) (netSubtotal * vatRate / 100.0) else 0.0

    val lineTotal: Double
        get() = netSubtotal + vatAmount

    val profitMarginPercent: Double
        get() = if (mrp > unitPrice && mrp > 0) ((mrp - unitPrice) / mrp * 100.0) else 0.0
}

/**
 * Ancillary purchase charges (Freight, Labor, Insurance, Customs, Handling)
 */
data class PurchaseOtherCharge(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Freight / Transport",
    var amount: Double = 0.0
)

/**
 * Overall Purchase summary calculation
 */
data class PurchaseFinancialSummary(
    val totalItemsCount: Int = 0,
    val totalQuantity: Double = 0.0,
    val grossSubtotal: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val totalVat: Double = 0.0,
    val otherChargesTotal: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0
)
