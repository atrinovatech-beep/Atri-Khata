package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessProfileEntity
import com.example.data.local.entity.InvoiceActivityEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SalesInvoiceDataModelTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: BusinessRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BusinessRepository(
            db.partyDao(),
            db.transactionDao(),
            db.inventoryDao(),
            db.staffDao(),
            db.salesInvoiceDao(),
            db.businessProfileDao()
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSalesInvoiceCreationAndDetailsRelation() = runBlocking {
        // Insert Party
        val partyId = db.partyDao().insertParty(
            PartyEntity(
                name = "Neomed Pvt. Ltd.",
                phone = "+977 9851144220",
                type = "Customer"
            )
        )

        // Insert Business Profile
        val profileId = db.businessProfileDao().insertProfile(
            BusinessProfileEntity(
                businessName = "Atri Nova Tech Enterprises",
                panVatNumber = "601234567"
            )
        )

        // Create Invoice with Line Items
        val invoice = SalesInvoiceEntity(
            invoiceNumber = "INV-2082-001",
            partyId = partyId,
            partyNameSnapshot = "Neomed Pvt. Ltd.",
            partyPhoneSnapshot = "+977 9851144220",
            invoiceDateBS = "2082/06/21",
            invoiceDateAD = "13 Sep 2026",
            subtotal = 1100.0,
            grandTotal = 1100.0,
            paidAmount = 0.0,
            dueAmount = 1100.0,
            paymentStatus = "Unpaid",
            businessProfileId = profileId,
            createdBy = "Prakash Sharma"
        )

        val items = listOf(
            SalesInvoiceItemEntity(
                invoiceId = 0L,
                productId = null,
                productNameSnapshot = "Thermal Receipt Roll (80mm x 50m)",
                productCodeSnapshot = "ACC-ROLL-80",
                quantity = 10.0,
                unit = "box",
                rate = 85.0,
                lineTotal = 850.0
            ),
            SalesInvoiceItemEntity(
                invoiceId = 0L,
                productId = null,
                productNameSnapshot = "POS Thermal Head Cleaner Kit",
                productCodeSnapshot = "CLN-POS-01",
                quantity = 1.0,
                unit = "pcs",
                rate = 250.0,
                lineTotal = 250.0
            )
        )

        val invoiceId = repository.insertSalesInvoice(invoice, items)

        // Verify retrieval with details
        val details = repository.getInvoiceWithDetails(invoiceId).first()
        assertNotNull(details)
        assertEquals("INV-2082-001", details!!.invoice.invoiceNumber)
        assertEquals("Neomed Pvt. Ltd.", details.party?.name)
        assertEquals("Atri Nova Tech Enterprises", details.businessProfile?.businessName)
        assertEquals(2, details.items.size)
        assertEquals("Thermal Receipt Roll (80mm x 50m)", details.items[0].productNameSnapshot)
        assertEquals(1, details.activities.size)
        assertEquals("Created", details.activities[0].action)

        // Test payment recording and auto-recalculation
        repository.recordInvoicePayment(
            PaymentAllocationEntity(
                invoiceId = invoiceId,
                paymentAmount = 600.0,
                paymentMethod = "Cash",
                recordedBy = "Anita Basnet"
            )
        )

        val updatedDetails = repository.getInvoiceWithDetails(invoiceId).first()
        assertNotNull(updatedDetails)
        assertEquals(1, updatedDetails!!.payments.size)
        assertEquals(600.0, updatedDetails.invoice.paidAmount, 0.001)
        assertEquals(500.0, updatedDetails.invoice.dueAmount, 0.001)
        assertEquals("Partially Paid", updatedDetails.invoice.paymentStatus)
        assertEquals(2, updatedDetails.activities.size)

        // Test invoice cancellation
        repository.cancelSalesInvoice(invoiceId, "Customer request")
        val cancelledDetails = repository.getInvoiceWithDetails(invoiceId).first()
        assertNotNull(cancelledDetails)
        assertEquals("Cancelled", cancelledDetails!!.invoice.invoiceStatus)
        assertEquals(3, cancelledDetails.activities.size)

        // Test invoice duplication
        val duplicatedId = repository.duplicateSalesInvoice(invoiceId, "INV-2082-002")
        assertNotNull(duplicatedId)
        val duplicatedDetails = repository.getInvoiceWithDetails(duplicatedId!!).first()
        assertNotNull(duplicatedDetails)
        assertEquals("Draft", duplicatedDetails!!.invoice.invoiceStatus)
        assertEquals(0.0, duplicatedDetails.invoice.paidAmount, 0.001)
        assertEquals(duplicatedDetails.invoice.grandTotal, duplicatedDetails.invoice.dueAmount, 0.001)
        assertEquals(2, duplicatedDetails.items.size)
        assertEquals("Thermal Receipt Roll (80mm x 50m)", duplicatedDetails.items[0].productNameSnapshot)
        assertEquals(1, duplicatedDetails.activities.size)
        assertEquals("Created", duplicatedDetails.activities[0].action)
    }
}
