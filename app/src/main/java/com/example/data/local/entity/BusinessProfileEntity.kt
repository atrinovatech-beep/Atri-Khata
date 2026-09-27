package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profiles")
data class BusinessProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessName: String = "My Business",
    val businessType: String = "General Trade",
    val panVatNumber: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val stateProvince: String = "",
    val country: String = "Nepal",
    val currencySymbol: String = "Rs.",
    val defaultVatRate: Double = 13.0,
    val isVatEnabled: Boolean = true,
    val invoicePrefix: String = "INV",
    val invoiceTerms: String = "1. Goods once sold will not be returned after 7 days.\n2. Payment is due within 15 days of invoice date.",
    val invoiceHeader: String = "Tax Invoice / Bill of Supply",
    val invoiceFooter: String = "Thank you for choosing Atri Khata! Visit again.",
    val logoUri: String? = null,
    val signatureUri: String? = null,
    val bankName: String? = null,
    val bankAccountNumber: String? = null,
    val bankBranch: String? = null,
    val qrCodePayload: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
