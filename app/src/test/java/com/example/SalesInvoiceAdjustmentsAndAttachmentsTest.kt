package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.InvoiceAttachmentEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.repository.BusinessRepository
import com.example.util.BillDiscountType
import com.example.util.InvoiceCalculationEngine
import com.example.util.InvoiceLineItemInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SalesInvoiceAdjustmentsAndAttachmentsTest {

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
    fun testInvoiceCalculationEngineNoAdjustments() {
        val items = listOf(
            InvoiceLineItemInput(quantity = 2.0, unitPrice = 500.0) // 1000.0
        )
        val result = InvoiceCalculationEngine.calculate(
            items = items,
            isDiscountApplied = false,
            isVatApplied = false,
            isExtraChargeApplied = false,
            isRoundOffApplied = false
        )

        assertEquals(1000.0, result.grossItemAmount, 0.001)
        assertEquals(0.0, result.itemDiscountTotal, 0.001)
        assertEquals(0.0, result.billDiscountAmount, 0.001)
        assertEquals(1000.0, result.taxableAmount, 0.001)
        assertEquals(0.0, result.vatAmount, 0.001)
        assertEquals(0.0, result.extraChargeAmount, 0.001)
        assertEquals(1000.0, result.preRoundTotal, 0.001)
        assertEquals(0.0, result.roundOffAmount, 0.001)
        assertEquals(1000.0, result.grandTotal, 0.001)
    }

    @Test
    fun testInvoiceCalculationEngineAllAdjustments() {
        // Items: 2 x 500 = 1000. With item discount 50 -> itemNet = 950.
        // Bill discount: 10% -> 95.0. Taxable base = 855.0.
        // VAT: 13% -> 855.0 * 0.13 = 111.15.
        // Extra charge: 100.0. PreRound = 855 + 111.15 + 100 = 1066.15.
        // Round off: normal nearest -> 1066.00, roundOff = -0.15.
        val items = listOf(
            InvoiceLineItemInput(quantity = 2.0, unitPrice = 500.0, discountAmount = 50.0)
        )
        val result = InvoiceCalculationEngine.calculate(
            items = items,
            isDiscountApplied = true,
            billDiscountType = BillDiscountType.PERCENTAGE,
            billDiscountValue = 10.0,
            isVatApplied = true,
            vatRate = 13.0,
            isExtraChargeApplied = true,
            extraChargeDescription = "Delivery Charge",
            extraChargeAmount = 100.0,
            isRoundOffApplied = true,
            roundingMethod = "Normal (Nearest 1.00)"
        )

        assertEquals(1000.0, result.grossItemAmount, 0.001)
        assertEquals(50.0, result.itemDiscountTotal, 0.001)
        assertEquals(950.0, result.itemNetAmount, 0.001)
        assertEquals(95.0, result.billDiscountAmount, 0.001)
        assertEquals(855.0, result.taxableAmount, 0.001)
        assertEquals(111.15, result.vatAmount, 0.001)
        assertEquals(100.0, result.extraChargeAmount, 0.001)
        assertEquals(1066.15, result.preRoundTotal, 0.001)
        assertEquals(-0.15, result.roundOffAmount, 0.001)
        assertEquals(1066.0, result.grandTotal, 0.001)
    }

    @Test
    fun testInvoicePersistenceWithAdjustmentsAndAttachments() = runBlocking {
        val partyId = db.partyDao().insertParty(
            PartyEntity(name = "Ktm Medical Store", phone = "+977 9841234567", type = "Customer")
        )

        val invoice = SalesInvoiceEntity(
            invoiceNumber = "INV-TEST-001",
            partyId = partyId,
            partyNameSnapshot = "Ktm Medical Store",
            invoiceDateBS = "2081-12-10",
            invoiceDateAD = "2025-03-24",
            dateMillis = System.currentTimeMillis(),
            invoiceStatus = "Final",
            subtotal = 1000.0,
            discountAmount = 100.0,
            discountPercent = 10.0,
            taxableAmount = 900.0,
            vatRate = 13.0,
            vatAmount = 117.0,
            roundOff = -0.0,
            grandTotal = 1067.0,
            paidAmount = 1067.0,
            dueAmount = 0.0,
            paymentStatus = "Paid",
            paymentMethod = "Cash",
            isDiscountApplied = true,
            isVatApplied = true,
            isExtraChargeApplied = true,
            extraChargeAmount = 50.0,
            extraChargeDescription = "Packing Charge",
            preRoundTotal = 1067.0,
            isRoundOffApplied = false
        )

        val items = listOf(
            SalesInvoiceItemEntity(
                invoiceId = 0L,
                productNameSnapshot = "Paracetamol 500mg",
                quantity = 10.0,
                unit = "Strip",
                rate = 100.0,
                discount = 100.0,
                discountPercent = 10.0,
                vatRate = 13.0,
                vatAmount = 117.0,
                lineTotal = 900.0
            )
        )

        val attachments = listOf(
            InvoiceAttachmentEntity(
                fileName = "delivery_receipt.jpg",
                fileType = "IMAGE",
                mimeType = "image/jpeg",
                uriString = "/data/user/0/attachments/delivery_receipt.jpg",
                fileSize = 102400L
            ),
            InvoiceAttachmentEntity(
                fileName = "tax_clearance.pdf",
                fileType = "PDF",
                mimeType = "application/pdf",
                uriString = "/data/user/0/attachments/tax_clearance.pdf",
                fileSize = 204800L
            )
        )

        val invoiceId = repository.insertSalesInvoice(
            invoice = invoice,
            items = items,
            payments = emptyList(),
            activities = emptyList(),
            attachments = attachments
        )

        assertTrue(invoiceId > 0)

        // Retrieve with details relation
        val details = db.salesInvoiceDao().getInvoiceWithDetailsById(invoiceId).first()
        assertNotNull(details)
        assertEquals("INV-TEST-001", details!!.invoice.invoiceNumber)
        assertTrue(details.invoice.isDiscountApplied)
        assertTrue(details.invoice.isVatApplied)
        assertTrue(details.invoice.isExtraChargeApplied)
        assertFalse(details.invoice.isRoundOffApplied)
        assertEquals(50.0, details.invoice.extraChargeAmount, 0.001)
        assertEquals("Packing Charge", details.invoice.extraChargeDescription)

        // Check attachments relation
        assertEquals(2, details.attachments.size)
        val imageAtt = details.attachments.find { it.fileType == "IMAGE" }
        val pdfAtt = details.attachments.find { it.fileType == "PDF" }
        assertNotNull(imageAtt)
        assertNotNull(pdfAtt)
        assertEquals("delivery_receipt.jpg", imageAtt!!.fileName)
        assertEquals("tax_clearance.pdf", pdfAtt!!.fileName)

        // Test delete attachment
        repository.deleteInvoiceAttachment(pdfAtt)
        val updatedDetails = db.salesInvoiceDao().getInvoiceWithDetailsById(invoiceId).first()
        assertEquals(1, updatedDetails!!.attachments.size)
        assertEquals("delivery_receipt.jpg", updatedDetails.attachments.first().fileName)
    }
}
