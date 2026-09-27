package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val contactNumber: String = "", // Secondary phone / alt number
    val type: String = "Customer", // "Customer" or "Supplier"
    val category: String = "Retail", // Wholesale, Retail, Distributor, Walk-in / Manufacturer, Wholesaler, Service Provider
    val balanceToReceive: Double = 0.0,
    val balanceToGive: Double = 0.0,
    val openingBalance: Double = 0.0,
    val balanceType: String = "To Receive (Dr)", // "To Receive (Dr)" or "To Give (Cr)"
    val panVatNumber: String = "", // Tax registration number
    val email: String = "",
    val contactPerson: String = "", // Name of representative
    val address: String = "",
    val city: String = "",
    val creditLimit: Double = 50000.0,
    val notes: String = "",
    val registerDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

