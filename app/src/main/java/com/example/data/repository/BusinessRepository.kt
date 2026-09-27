package com.example.data.repository

import com.example.data.local.dao.BusinessProfileDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.PartyDao
import com.example.data.local.dao.SalesInvoiceDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InvoiceActivityEntity
import com.example.data.local.entity.InvoiceAttachmentEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.local.entity.StaffMemberEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.relation.SalesInvoiceWithDetails
import kotlinx.coroutines.flow.Flow

class BusinessRepository(
    private val partyDao: PartyDao,
    private val transactionDao: TransactionDao,
    private val inventoryDao: InventoryDao,
    private val staffDao: StaffDao,
    private val salesInvoiceDao: SalesInvoiceDao,
    private val businessProfileDao: BusinessProfileDao
) {
    val allParties: Flow<List<PartyEntity>> = partyDao.getAllParties()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allInventoryItems: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItems()
    val lowStockItems: Flow<List<InventoryItemEntity>> = inventoryDao.getLowStockItems()
    val allInventoryCategories: Flow<List<String>> = inventoryDao.getAllCategories()
    val allStaff: Flow<List<StaffMemberEntity>> = staffDao.getAllStaff()
    val allSalesInvoices: Flow<List<SalesInvoiceEntity>> = salesInvoiceDao.getAllInvoices()
    val allInvoicesWithDetails: Flow<List<SalesInvoiceWithDetails>> = salesInvoiceDao.getAllInvoicesWithDetails()
    val businessProfile: Flow<BusinessProfileEntity?> = businessProfileDao.getBusinessProfile()

    val totalToReceive: Flow<Double?> = partyDao.getTotalToReceive()
    val totalToGive: Flow<Double?> = partyDao.getTotalToGive()
    val totalSales: Flow<Double?> = transactionDao.getTotalSales()
    val totalPurchases: Flow<Double?> = transactionDao.getTotalPurchases()
    val totalExpenses: Flow<Double?> = transactionDao.getTotalExpenses()
    val totalSalesInvoiceSum: Flow<Double?> = salesInvoiceDao.getTotalSalesSum()
    val totalSalesDueSum: Flow<Double?> = salesInvoiceDao.getTotalDueSum()

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)

    fun searchParties(query: String): Flow<List<PartyEntity>> =
        partyDao.searchParties(query)

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactions(query)

    fun searchInventory(query: String): Flow<List<InventoryItemEntity>> =
        inventoryDao.searchItems(query)

    suspend fun insertParty(party: PartyEntity): Long =
        partyDao.insertParty(party)

    suspend fun updateParty(party: PartyEntity) =
        partyDao.updateParty(party)

    suspend fun deleteParty(party: PartyEntity) =
        partyDao.deleteParty(party)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun insertItem(item: InventoryItemEntity): Long =
        inventoryDao.insertItem(item)

    suspend fun updateItem(item: InventoryItemEntity) =
        inventoryDao.updateItem(item)

    suspend fun deleteItem(item: InventoryItemEntity) =
        inventoryDao.deleteItem(item)

    fun getInventoryItemById(id: Long): Flow<InventoryItemEntity?> =
        inventoryDao.getItemByIdFlow(id)

    suspend fun getInventoryItemByIdSync(id: Long): InventoryItemEntity? =
        inventoryDao.getItemById(id)

    fun getInvoiceItemsForProduct(productId: Long): Flow<List<SalesInvoiceItemEntity>> =
        salesInvoiceDao.getInvoiceItemsForProduct(productId)

    suspend fun getItemInvoiceCount(productId: Long): Int =
        salesInvoiceDao.getItemInvoiceCount(productId)

    suspend fun insertStaff(staff: StaffMemberEntity): Long =
        staffDao.insertStaff(staff)

    suspend fun updateStaff(staff: StaffMemberEntity) =
        staffDao.updateStaff(staff)

    suspend fun deleteStaff(staff: StaffMemberEntity) =
        staffDao.deleteStaff(staff)

    // Sales Invoice Operations
    fun getInvoiceWithDetails(id: Long): Flow<SalesInvoiceWithDetails?> =
        salesInvoiceDao.getInvoiceWithDetailsById(id)

    suspend fun getInvoiceWithDetailsSync(id: Long): SalesInvoiceWithDetails? =
        salesInvoiceDao.getInvoiceWithDetailsByIdSync(id)

    fun getInvoiceByNumber(invoiceNumber: String): Flow<SalesInvoiceWithDetails?> =
        salesInvoiceDao.getInvoiceWithDetailsByNumber(invoiceNumber)

    fun getInvoicesForParty(partyId: Long): Flow<List<SalesInvoiceEntity>> =
        salesInvoiceDao.getInvoicesForParty(partyId)

    fun searchSalesInvoices(query: String): Flow<List<SalesInvoiceEntity>> =
        salesInvoiceDao.searchInvoices(query)

    suspend fun insertSalesInvoice(
        invoice: SalesInvoiceEntity,
        items: List<SalesInvoiceItemEntity>,
        payments: List<PaymentAllocationEntity> = emptyList(),
        activities: List<InvoiceActivityEntity> = emptyList(),
        attachments: List<InvoiceAttachmentEntity> = emptyList()
    ): Long {
        val invoiceId = salesInvoiceDao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        salesInvoiceDao.insertInvoiceItems(itemsWithId)

        if (payments.isNotEmpty()) {
            payments.forEach {
                salesInvoiceDao.insertPaymentAllocation(it.copy(invoiceId = invoiceId))
            }
        }

        if (attachments.isNotEmpty()) {
            val attachmentsWithId = attachments.map { it.copy(invoiceId = invoiceId) }
            salesInvoiceDao.insertAttachments(attachmentsWithId)
        }

        val initialActivities = if (activities.isEmpty()) {
            listOf(
                InvoiceActivityEntity(
                    invoiceId = invoiceId,
                    action = "Created",
                    description = "Sales Invoice #${invoice.invoiceNumber} created by ${invoice.createdBy}",
                    performedBy = invoice.createdBy
                )
            )
        } else {
            activities.map { it.copy(invoiceId = invoiceId) }
        }
        salesInvoiceDao.insertInvoiceActivities(initialActivities)
        return invoiceId
    }

    suspend fun insertInvoiceAttachments(attachments: List<InvoiceAttachmentEntity>) {
        salesInvoiceDao.insertAttachments(attachments)
    }

    suspend fun deleteInvoiceAttachment(attachment: InvoiceAttachmentEntity) {
        salesInvoiceDao.deleteAttachment(attachment)
    }

    fun getAttachmentsForInvoice(invoiceId: Long): Flow<List<InvoiceAttachmentEntity>> =
        salesInvoiceDao.getAttachmentsForInvoice(invoiceId)

    suspend fun getAttachmentsForInvoiceSync(invoiceId: Long): List<InvoiceAttachmentEntity> =
        salesInvoiceDao.getAttachmentsForInvoiceSync(invoiceId)

    suspend fun updateSalesInvoice(
        invoice: SalesInvoiceEntity,
        items: List<SalesInvoiceItemEntity>? = null
    ) {
        salesInvoiceDao.updateInvoice(invoice)
        if (items != null) {
            salesInvoiceDao.deleteInvoiceItems(invoice.id)
            salesInvoiceDao.insertInvoiceItems(items.map { it.copy(invoiceId = invoice.id) })
        }
        salesInvoiceDao.insertInvoiceActivity(
            InvoiceActivityEntity(
                invoiceId = invoice.id,
                action = "Edited",
                description = "Sales Invoice #${invoice.invoiceNumber} updated",
                performedBy = invoice.createdBy
            )
        )
    }

    suspend fun recordInvoicePayment(payment: PaymentAllocationEntity): Long {
        val id = salesInvoiceDao.insertPaymentAllocation(payment)
        salesInvoiceDao.insertInvoiceActivity(
            InvoiceActivityEntity(
                invoiceId = payment.invoiceId,
                action = "Payment Recorded",
                description = "Payment of Rs. ${payment.paymentAmount} recorded via ${payment.paymentMethod}",
                performedBy = payment.recordedBy
            )
        )
        // Automatically recalculate paid and due balances on the parent invoice
        val details = salesInvoiceDao.getInvoiceWithDetailsByIdSync(payment.invoiceId)
        details?.let { d ->
            val allPayments = d.payments
            val totalPaid = if (allPayments.any { it.id == id }) {
                allPayments.sumOf { it.paymentAmount }
            } else {
                allPayments.sumOf { it.paymentAmount } + payment.paymentAmount
            }
            val due = (d.invoice.grandTotal - totalPaid).coerceAtLeast(0.0)
            val newStatus = when {
                due <= 0.0 -> "Paid"
                totalPaid > 0.0 -> "Partially Paid"
                else -> "Due"
            }
            salesInvoiceDao.updateInvoice(
                d.invoice.copy(
                    paidAmount = totalPaid,
                    dueAmount = due,
                    paymentStatus = newStatus,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Update Party account balance
            val partyIdToUpdate = payment.partyId ?: d.invoice.partyId
            if (partyIdToUpdate != null && partyIdToUpdate > 0) {
                val party = partyDao.getPartyById(partyIdToUpdate)
                if (party != null) {
                    val newBalance = (party.balanceToReceive - payment.paymentAmount).coerceAtLeast(0.0)
                    partyDao.updateParty(party.copy(balanceToReceive = newBalance))
                }
            }

            // Insert matching TransactionEntity record for reporting, daybook & ledger
            val tx = TransactionEntity(
                partyId = partyIdToUpdate ?: 0L,
                partyName = d.party?.name ?: d.invoice.partyNameSnapshot,
                type = "Payment In",
                amount = payment.paymentAmount,
                dateMillis = payment.paymentDateMillis,
                dateBs = payment.displayBsDate,
                dateAd = payment.displayAdDate,
                paymentMethod = payment.paymentMethod,
                invoiceNumber = d.invoice.invoiceNumber,
                notes = payment.remarks ?: "Payment for Invoice #${d.invoice.invoiceNumber}",
                status = "Completed",
                salesInvoiceId = payment.invoiceId
            )
            transactionDao.insertTransaction(tx)
        }
        return id
    }

    suspend fun getInvoiceWithDetailsByIdSync(id: Long) =
        salesInvoiceDao.getInvoiceWithDetailsByIdSync(id)

    suspend fun deleteSalesInvoice(id: Long) {
        salesInvoiceDao.deleteInvoiceById(id)
    }

    suspend fun cancelSalesInvoice(id: Long, cancelledBy: String = "Admin") {
        val details = salesInvoiceDao.getInvoiceWithDetailsByIdSync(id)
        details?.let { d ->
            salesInvoiceDao.updateInvoice(
                d.invoice.copy(
                    invoiceStatus = "Cancelled",
                    updatedAt = System.currentTimeMillis()
                )
            )
            salesInvoiceDao.insertInvoiceActivity(
                InvoiceActivityEntity(
                    invoiceId = id,
                    action = "Cancelled",
                    description = "Sales Invoice #${d.invoice.invoiceNumber} marked as Cancelled by $cancelledBy",
                    performedBy = cancelledBy
                )
            )
        }
    }

    suspend fun duplicateSalesInvoice(id: Long, newInvoiceNumber: String, createdBy: String = "Admin"): Long? {
        val details = salesInvoiceDao.getInvoiceWithDetailsByIdSync(id) ?: return null
        val original = details.invoice
        val newInvoice = original.copy(
            id = 0L,
            invoiceNumber = newInvoiceNumber,
            invoiceStatus = "Draft",
            paymentStatus = "Unpaid",
            paidAmount = 0.0,
            dueAmount = original.grandTotal,
            dateMillis = System.currentTimeMillis(),
            createdBy = createdBy,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newId = salesInvoiceDao.insertInvoice(newInvoice)
        val duplicateItems = details.items.map { it.copy(id = 0L, invoiceId = newId, createdAt = System.currentTimeMillis()) }
        if (duplicateItems.isNotEmpty()) {
            salesInvoiceDao.insertInvoiceItems(duplicateItems)
        }
        salesInvoiceDao.insertInvoiceActivity(
            InvoiceActivityEntity(
                invoiceId = newId,
                action = "Created",
                description = "Duplicated from invoice #${original.invoiceNumber}",
                performedBy = createdBy
            )
        )
        return newId
    }

    suspend fun updateBusinessProfile(profile: BusinessProfileEntity) {
        if (profile.id == 0L && businessProfileDao.getProfileCount() == 0) {
            businessProfileDao.insertProfile(profile)
        } else {
            businessProfileDao.updateProfile(profile)
        }
    }
}
