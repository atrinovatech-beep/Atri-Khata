package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.NepaliDateUtils

@Entity(
    tableName = "sales_invoices",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = StaffMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["salespersonId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = BusinessProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessProfileId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("partyId"),
        Index("salespersonId"),
        Index("businessProfileId"),
        Index("invoiceNumber", unique = true),
        Index("dateMillis")
    ]
)
data class SalesInvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String, // e.g. "INV-2082-021"
    val partyId: Long? = null,
    val partyNameSnapshot: String,
    val partyPhoneSnapshot: String? = null,
    val partyAddressSnapshot: String? = null,
    val partyPanSnapshot: String? = null,
    val invoiceDateBS: String = "",
    val invoiceDateAD: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val invoiceStatus: String = "Confirmed", // "Draft", "Confirmed", "Paid", "Partially Paid", "Cancelled"
    val subtotal: Double = 0.0,
    val isDiscountApplied: Boolean = false,
    val discountAmount: Double = 0.0,
    val discountPercent: Double = 0.0,
    val taxableAmount: Double = 0.0,
    val isVatApplied: Boolean = false,
    val vatRate: Double = 0.0, // e.g. 13.0
    val vatAmount: Double = 0.0,
    val isExtraChargeApplied: Boolean = false,
    val extraChargeAmount: Double = 0.0,
    val extraChargeDescription: String? = null,
    val preRoundTotal: Double = 0.0,
    val isRoundOffApplied: Boolean = false,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentStatus: String = "Unpaid", // "Unpaid", "Partially Paid", "Paid"
    val paymentMethod: String = "Credit", // "Cash", "Bank Transfer", "UPI / Online", "Credit", "Cheque"
    val paymentAccountId: String? = null, // e.g. "Nabil Bank A/C", "Cash Counter"
    val dueDateBS: String? = null,
    val dueDateAD: String? = null,
    val dueDateMillis: Long? = null,
    val salespersonId: Long? = null,
    val salespersonName: String? = null,
    val referenceNumber: String? = null, // e.g. PO number, challan number
    val remarks: String? = null,
    val isTaxInvoice: Boolean = true,
    val businessProfileId: Long? = null,
    val createdBy: String = "Admin",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayBsDate: String
        get() = if (invoiceDateBS.isNotBlank()) invoiceDateBS else NepaliDateUtils.formatBsDate(dateMillis)

    val displayAdDate: String
        get() = if (invoiceDateAD.isNotBlank()) invoiceDateAD else NepaliDateUtils.formatAdDate(dateMillis)
}
