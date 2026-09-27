package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.InvoiceActivityEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.local.relation.SalesInvoiceWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesInvoiceDao {
    @Query("SELECT * FROM sales_invoices ORDER BY dateMillis DESC")
    fun getAllInvoices(): Flow<List<SalesInvoiceEntity>>

    @Query("SELECT * FROM sales_invoices ORDER BY dateMillis DESC")
    suspend fun getAllInvoicesSync(): List<SalesInvoiceEntity>

    @Transaction
    @Query("SELECT * FROM sales_invoices ORDER BY dateMillis DESC")
    fun getAllInvoicesWithDetails(): Flow<List<SalesInvoiceWithDetails>>

    @Transaction
    @Query("SELECT * FROM sales_invoices WHERE id = :id")
    fun getInvoiceWithDetailsById(id: Long): Flow<SalesInvoiceWithDetails?>

    @Transaction
    @Query("SELECT * FROM sales_invoices WHERE id = :id")
    suspend fun getInvoiceWithDetailsByIdSync(id: Long): SalesInvoiceWithDetails?

    @Transaction
    @Query("SELECT * FROM sales_invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    fun getInvoiceWithDetailsByNumber(invoiceNumber: String): Flow<SalesInvoiceWithDetails?>

    @Query("SELECT * FROM sales_invoices WHERE partyId = :partyId ORDER BY dateMillis DESC")
    fun getInvoicesForParty(partyId: Long): Flow<List<SalesInvoiceEntity>>

    @Query("SELECT * FROM sales_invoices WHERE invoiceNumber LIKE '%' || :query || '%' OR partyNameSnapshot LIKE '%' || :query || '%' OR remarks LIKE '%' || :query || '%' ORDER BY dateMillis DESC")
    fun searchInvoices(query: String): Flow<List<SalesInvoiceEntity>>

    @Query("SELECT SUM(grandTotal) FROM sales_invoices WHERE invoiceStatus != 'Cancelled'")
    fun getTotalSalesSum(): Flow<Double?>

    @Query("SELECT SUM(dueAmount) FROM sales_invoices WHERE invoiceStatus != 'Cancelled'")
    fun getTotalDueSum(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales_invoices")
    suspend fun getInvoicesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: SalesInvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<SalesInvoiceItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentAllocation(payment: PaymentAllocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceActivity(activity: InvoiceActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceActivities(activities: List<InvoiceActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<com.example.data.local.entity.InvoiceAttachmentEntity>)

    @Query("SELECT * FROM invoice_attachments WHERE invoiceId = :invoiceId")
    fun getAttachmentsForInvoice(invoiceId: Long): Flow<List<com.example.data.local.entity.InvoiceAttachmentEntity>>

    @Query("SELECT * FROM invoice_attachments WHERE invoiceId = :invoiceId")
    suspend fun getAttachmentsForInvoiceSync(invoiceId: Long): List<com.example.data.local.entity.InvoiceAttachmentEntity>

    @Delete
    suspend fun deleteAttachment(attachment: com.example.data.local.entity.InvoiceAttachmentEntity)

    @Update
    suspend fun updateInvoice(invoice: SalesInvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: SalesInvoiceEntity)

    @Query("DELETE FROM sales_invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: Long)

    @Query("DELETE FROM sales_invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceItems(invoiceId: Long)

    @Query("SELECT * FROM sales_invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Long): Flow<List<SalesInvoiceItemEntity>>

    @Query("SELECT * FROM payment_allocations WHERE invoiceId = :invoiceId ORDER BY paymentDateMillis DESC")
    fun getPaymentsForInvoice(invoiceId: Long): Flow<List<PaymentAllocationEntity>>

    @Query("SELECT * FROM invoice_activities WHERE invoiceId = :invoiceId ORDER BY timestampMillis DESC")
    fun getActivitiesForInvoice(invoiceId: Long): Flow<List<InvoiceActivityEntity>>

    @Query("SELECT * FROM sales_invoice_items WHERE productId = :productId ORDER BY createdAt DESC")
    fun getInvoiceItemsForProduct(productId: Long): Flow<List<SalesInvoiceItemEntity>>

    @Query("SELECT COUNT(*) FROM sales_invoice_items WHERE productId = :productId")
    suspend fun getItemInvoiceCount(productId: Long): Int
}
