package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff_members")
data class StaffMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val role: String, // "Admin", "Partner", "Manager", "Accountant", "Sales Person", "Stock Manager", "Entry Person"
    val pin: String = "1234",
    val status: String = "Active", // "Active", "Inactive", "Invited"
    val avatarColor: Long = 0xFF00A3FF,
    val joinedAt: Long = System.currentTimeMillis(),
    
    // Granular Sales Vouchers Permissions
    val canCreateSalesInvoice: Boolean = true,
    val canEditSalesInvoice: Boolean = true,
    val canDeleteSalesInvoice: Boolean = false,
    val canViewCustomerLedger: Boolean = true,
    val canGiveDiscounts: Boolean = false,
    val canChangeSellingPrice: Boolean = false,

    // Granular Purchase Vouchers Permissions
    val canCreatePurchaseEntry: Boolean = false,
    val canEditPurchaseEntry: Boolean = false,
    val canDeletePurchaseEntry: Boolean = false,
    val canViewSupplierLedger: Boolean = false,

    // Financial & Cash drawer
    val canAccessCashDrawer: Boolean = false,
    val canViewFinancialReports: Boolean = false,
    val canManageInventory: Boolean = false
)
