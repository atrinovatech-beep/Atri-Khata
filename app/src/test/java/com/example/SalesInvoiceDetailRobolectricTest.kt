package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.local.relation.SalesInvoiceWithDetails
import com.example.data.repository.BusinessRepository
import com.example.ui.MainViewModel
import com.example.ui.screens.SalesInvoiceDetailScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class SalesInvoiceDetailRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var repository: BusinessRepository

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        context = application
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        val db = AppDatabase.getDatabase(application, testScope)
        repository = BusinessRepository(
            db.partyDao(),
            db.transactionDao(),
            db.inventoryDao(),
            db.staffDao(),
            db.salesInvoiceDao(),
            db.businessProfileDao()
        )
    }

    private fun createSampleInvoice(
        itemCount: Int = 2,
        withParty: Boolean = true
    ): Pair<Long, SalesInvoiceWithDetails> = runBlocking {
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        val db = AppDatabase.getDatabase(application, testScope)
        var partyEntity: PartyEntity? = null
        var partyId: Long? = null
        if (withParty) {
            val party = PartyEntity(
                name = "Himalayan Trading Corp",
                phone = "+977 9851000111",
                type = "Customer",
                panVatNumber = "300998877",
                address = "New Road, Kathmandu"
            )
            partyId = db.partyDao().insertParty(party)
            partyEntity = party.copy(id = partyId)
        }

        val profile = BusinessProfileEntity(
            businessName = "Atri Nova Tech Enterprises",
            address = "Putalisadak, Kathmandu",
            phone = "+977 01 4422331",
            email = "info@atri.com.np",
            panVatNumber = "601234567"
        )
        val profileId = db.businessProfileDao().insertProfile(profile)
        val profileEntity = profile.copy(id = profileId)

        val uniqueInvoiceNum = "INV-${System.currentTimeMillis()}-${(100..999).random()}"
        val invoice = SalesInvoiceEntity(
            invoiceNumber = uniqueInvoiceNum,
            partyId = partyId,
            partyNameSnapshot = if (withParty) "Himalayan Trading Corp" else "Walk-in Cash Customer",
            partyPhoneSnapshot = if (withParty) "+977 9851000111" else "",
            invoiceDateBS = "2082/06/25",
            invoiceDateAD = "17 Sep 2026",
            dueDateAD = "24 Sep 2026",
            subtotal = 5000.0,
            discountAmount = 200.0,
            vatAmount = 624.0,
            grandTotal = 5424.0,
            paidAmount = 1000.0,
            dueAmount = 4424.0,
            paymentStatus = "Partially Paid",
            paymentMethod = "Bank Transfer",
            businessProfileId = profileId,
            remarks = "Priority delivery requested via courier.",
            createdBy = "Sunil Adhikari"
        )

        val items = (1..itemCount).map { i ->
            SalesInvoiceItemEntity(
                invoiceId = 0L,
                productId = null,
                productNameSnapshot = "Sample Item #$i Premium Grade",
                productCodeSnapshot = "SKU-00$i",
                quantity = 2.0,
                unit = "pcs",
                rate = 1000.0,
                discountPercent = 2.0,
                vatRate = 13.0,
                vatAmount = 260.0,
                lineTotal = 2260.0
            )
        }

        val invoiceId = repository.insertSalesInvoice(invoice, items)
        val savedInvoice = invoice.copy(id = invoiceId)
        val savedItems = items.map { it.copy(invoiceId = invoiceId) }

        val invoiceWithDetails = SalesInvoiceWithDetails(
            invoice = savedInvoice,
            party = partyEntity,
            salesperson = null,
            businessProfile = profileEntity,
            items = savedItems,
            payments = emptyList(),
            activities = emptyList()
        )

        Pair(invoiceId, invoiceWithDetails)
    }

    private fun waitForInvoiceContent() {
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("Loading invoice details...").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun testInvoiceDetailsMappingAndDisplay() {
        val (invoiceId, details) = createSampleInvoice()
        val viewModel = MainViewModel(application)

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                SalesInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = {},
                    initialInvoiceWithDetails = details
                )
            }
        }

        waitForInvoiceContent()

        // 1. Verify Top Bar Title
        composeTestRule.onNodeWithText("Sales Invoice").assertIsDisplayed()

        // 2. Verify Business Profile Information
        composeTestRule.onNodeWithText("Atri Nova Tech Enterprises").assertIsDisplayed()
        composeTestRule.onNodeWithText("PAN: 601234567").assertIsDisplayed()

        // Scroll to customer / party details in LazyColumn to compose off-screen items
        composeTestRule.onNodeWithTag("invoice_detail_lazy_column")
            .performScrollToNode(hasText("Himalayan Trading Corp"))

        // 3. Verify Customer / Party Details exist in hierarchy
        composeTestRule.onNodeWithText("Himalayan Trading Corp").assertExists()
        composeTestRule.onNodeWithText("Phone: +977 9851000111", substring = true).assertExists()

        // 4. Verify Item Details exist in hierarchy
        composeTestRule.onNodeWithTag("invoice_detail_lazy_column")
            .performScrollToNode(hasText("Sample Item #1 Premium Grade"))
        composeTestRule.onNodeWithText("Sample Item #1 Premium Grade").assertExists()

        // 5. Verify Amounts: Paid and Due exist in hierarchy
        composeTestRule.onNodeWithTag("invoice_detail_lazy_column")
            .performScrollToNode(hasText("Due Balance"))
        composeTestRule.onNodeWithText("Due Balance").assertExists()
        composeTestRule.onNodeWithText("Rs. 4,424.00").assertExists()

        // 6. Verify Payment Method exists in hierarchy
        composeTestRule.onNodeWithTag("invoice_detail_lazy_column")
            .performScrollToNode(hasText("Payment Method", substring = true))
        composeTestRule.onNodeWithText("Bank Transfer").assertExists()
    }

    @Test
    fun testMissingPartyGracefulHandling() {
        val (invoiceId, details) = createSampleInvoice(withParty = false)
        val viewModel = MainViewModel(application)

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                SalesInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = {},
                    initialInvoiceWithDetails = details
                )
            }
        }

        waitForInvoiceContent()

        // Scroll down in LazyColumn to the customer party card
        composeTestRule.onNodeWithTag("invoice_detail_lazy_column")
            .performScrollToNode(hasText("Walk-in Cash Customer"))

        // Should display Walk-in Cash Customer snapshot gracefully without crashing
        composeTestRule.onNodeWithText("Walk-in Cash Customer").assertExists()
        composeTestRule.onNodeWithText("Sales Invoice").assertIsDisplayed()
    }

    @Test
    fun testInvoiceWithoutItemsGracefulHandling() {
        val (invoiceId, details) = createSampleInvoice(itemCount = 0)
        val viewModel = MainViewModel(application)

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                SalesInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = {},
                    initialInvoiceWithDetails = details
                )
            }
        }

        waitForInvoiceContent()

        // Scroll down to empty items card
        composeTestRule.onNodeWithTag("invoice_detail_lazy_column")
            .performScrollToNode(hasTestTag("empty_items_card"))

        // Should gracefully show empty items card notice without crashing
        composeTestRule.onNodeWithTag("empty_items_card").assertExists()
        composeTestRule.onNodeWithText("No item details available for this invoice.").assertExists()
    }

    @Test
    fun testDarkThemeRendering() {
        val (invoiceId, details) = createSampleInvoice()
        val viewModel = MainViewModel(application)

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                SalesInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = {},
                    initialInvoiceWithDetails = details
                )
            }
        }

        waitForInvoiceContent()
        composeTestRule.onNodeWithText("Sales Invoice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Atri Nova Tech Enterprises").assertIsDisplayed()
    }

    @Test
    fun testPrintExportChooserDialogInteraction() {
        val (invoiceId, details) = createSampleInvoice()
        val viewModel = MainViewModel(application)

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                SalesInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = {},
                    initialInvoiceWithDetails = details
                )
            }
        }

        waitForInvoiceContent()

        // Click quick print button using unmerged tree
        composeTestRule.onNodeWithTag("quick_print_button", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        // Verify dialog contents
        composeTestRule.onNodeWithText("Print & Export Invoice").assertIsDisplayed()
        composeTestRule.onNodeWithText("A4 Tax Invoice").assertIsDisplayed()
        composeTestRule.onNodeWithText("80mm POS Receipt").assertIsDisplayed()

        // Check tags for A4 and POS action buttons exist
        composeTestRule.onNodeWithTag("btn_print_a4").assertExists()
        composeTestRule.onNodeWithTag("btn_download_a4").assertExists()
        composeTestRule.onNodeWithTag("btn_share_a4").assertExists()
        composeTestRule.onNodeWithTag("btn_preview_a4").assertExists()

        composeTestRule.onNodeWithTag("btn_print_pos").assertExists()
        composeTestRule.onNodeWithTag("btn_download_pos").assertExists()
        composeTestRule.onNodeWithTag("btn_share_pos").assertExists()
        composeTestRule.onNodeWithTag("btn_preview_pos").assertExists()

        // Dismiss dialog
        composeTestRule.onNodeWithTag("close_print_export_dialog_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Print & Export Invoice").assertDoesNotExist()
    }

    @Test
    fun testRecordPaymentDialogFlow() {
        val (invoiceId, details) = createSampleInvoice()
        val viewModel = MainViewModel(application)

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                SalesInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = {},
                    initialInvoiceWithDetails = details
                )
            }
        }

        waitForInvoiceContent()

        // Click bottom Record Pay button
        composeTestRule.onNodeWithTag("record_payment_bottom_button", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        // Dialog should be visible
        composeTestRule.onNodeWithText("Record Payment").assertIsDisplayed()
        composeTestRule.onNodeWithTag("payment_amount_input").assertIsDisplayed()
    }
}
