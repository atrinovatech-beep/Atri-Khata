package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.model.InventorySettings
import com.example.data.model.PartySettings
import com.example.data.model.PurchaseDiscountType
import com.example.data.model.PurchaseFinancialSummary
import com.example.data.model.PurchaseLineItem
import com.example.data.model.PurchaseOtherCharge
import com.example.data.model.PurchaseType
import com.example.data.model.TransactionSettings
import com.example.util.NepaliDateUtils
import java.text.DecimalFormat
import java.util.Locale
import java.util.UUID

/**
 * Atri Khata 2 - Advanced Purchase Form Sheet & Full Composable
 * Dynamically configurable via Settings -> Transaction, Party, Inventory, Invoice
 */
@Composable
fun PurchaseInvoiceSheet(
    parties: List<PartyEntity>,
    inventoryItems: List<InventoryItemEntity> = emptyList(),
    transactionSettings: TransactionSettings = TransactionSettings(),
    inventorySettings: InventorySettings = InventorySettings(),
    partySettings: PartySettings = PartySettings(),
    preselectedParty: PartyEntity? = null,
    onSavePurchase: (
        purchaseNumber: String,
        party: PartyEntity?,
        customPartyName: String,
        lineItems: List<PurchaseLineItem>,
        subtotal: Double,
        discountAmount: Double,
        taxableAmount: Double,
        vatAmount: Double,
        otherCharges: Double,
        roundOff: Double,
        grandTotal: Double,
        paidAmount: Double,
        paymentMethod: String,
        supplierBillNumber: String,
        referenceNumber: String,
        purchaseDateMillis: Long,
        purchaseDateBs: String,
        purchaseDateAd: String,
        dueDateMillis: Long?,
        dueDateBs: String,
        purchaseType: String,
        warehouse: String,
        notes: String,
        attachmentUri: String?,
        updateStockCost: Boolean,
        isSaveAndNew: Boolean
    ) -> Unit,
    onCancel: () -> Unit,
    onQuickAddParty: (PartyEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val numFormat = remember { DecimalFormat("#,##,##0.00") }

    // ---------------------------------------------------------
    // THEME PALETTE (Slate / Corporate Indigo / Amber Accents)
    // ---------------------------------------------------------
    val isDark = com.example.ui.theme.AppTheme.isDark
    val formNavyBg = if (isDark) Color(0xFF0A0F1D) else Color(0xFFF8FAFC)
    val formCardBg = if (isDark) Color(0xFF131E36) else Color.White
    val formCardBorder = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
    val formPrimaryBlue = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val formSkyBlue = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val formEmerald = Color(0xFF10B981)
    val formAmber = Color(0xFFF59E0B)
    val formRose = Color(0xFFEF4444)
    val formTextPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val formTextSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val formTextMuted = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

    // ---------------------------------------------------------
    // 1. PURCHASE HEADER & NUMBERING
    // ---------------------------------------------------------
    val currentYearBs = remember { NepaliDateUtils.adToBs(System.currentTimeMillis()).year }
    var purchaseNumber by remember {
        val prefix = transactionSettings.purchasePrefix.ifBlank { "PUR-" }
        val randomNum = (1000..9999).random()
        val generated = if (transactionSettings.includeFiscalYearInInvoice) {
            "$prefix$currentYearBs-$randomNum"
        } else {
            "$prefix$randomNum"
        }
        mutableStateOf(generated)
    }

    var purchaseDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var purchaseDateBs by remember { mutableStateOf(NepaliDateUtils.formatBsDate(System.currentTimeMillis())) }
    var purchaseDateAd by remember { mutableStateOf(NepaliDateUtils.formatAdDate(System.currentTimeMillis())) }
    var isBsDateMode by remember { mutableStateOf(true) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // 2. SUPPLIER / PARTY SELECTION
    // ---------------------------------------------------------
    var selectedParty by remember {
        mutableStateOf(
            preselectedParty ?: parties.firstOrNull { it.type.equals("Supplier", ignoreCase = true) }
            ?: parties.firstOrNull()
        )
    }
    var customSupplierName by remember { mutableStateOf("") }
    var isDirectCounterVendor by remember { mutableStateOf(selectedParty == null) }
    var showSupplierPickerModal by remember { mutableStateOf(false) }
    var showQuickAddSupplierModal by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // 3. PURCHASE INFORMATION
    // ---------------------------------------------------------
    var supplierInvoiceNumber by remember { mutableStateOf("") } // Supplier's bill #
    var referenceNumber by remember { mutableStateOf("") } // PO or Challan #
    var selectedPurchaseType by remember { mutableStateOf(PurchaseType.CASH_PURCHASE) }
    var selectedWarehouse by remember { mutableStateOf("Main Store / Warehouse 1") }
    var creditDays by remember { mutableIntStateOf(transactionSettings.defaultCreditDays.coerceAtLeast(15)) }
    var customDueDateBs by remember {
        val calendar = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 15) }
        mutableStateOf(NepaliDateUtils.formatBsDate(calendar.timeInMillis))
    }
    var dueDateMillis by remember {
        val calendar = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 15) }
        mutableLongStateOf(calendar.timeInMillis)
    }

    // ---------------------------------------------------------
    // 4. ITEMS & LINES
    // ---------------------------------------------------------
    val lineItems = remember {
        mutableStateListOf<PurchaseLineItem>().apply {
            if (inventoryItems.isNotEmpty()) {
                val first = inventoryItems.first()
                add(
                    PurchaseLineItem(
                        itemId = first.id,
                        name = first.name,
                        sku = first.sku,
                        unit = first.unit,
                        quantity = 1.0,
                        unitPrice = if (first.purchasePrice > 0) first.purchasePrice else 850.0,
                        mrp = if (first.salePrice > 0) first.salePrice else 1150.0,
                        currentStock = first.stockQuantity,
                        isTaxable = transactionSettings.enableVat
                    )
                )
            } else {
                add(
                    PurchaseLineItem(
                        name = "Commercial Goods / Stock Inward",
                        sku = "PRD-001",
                        unit = "pcs",
                        quantity = 1.0,
                        unitPrice = 1200.0,
                        mrp = 1500.0,
                        isTaxable = transactionSettings.enableVat
                    )
                )
            }
        }
    }

    var showAddItemModal by remember { mutableStateOf(false) }
    var editingLineItemIndex by remember { mutableIntStateOf(-1) }

    // ---------------------------------------------------------
    // 5. BATCH & EXPIRY (Contextual)
    // ---------------------------------------------------------
    val isBatchTrackingActive = remember(inventorySettings) {
        inventorySettings.batchTrackingEnabled ||
                inventorySettings.expiryTrackingEnabled ||
                inventorySettings.businessPreset.id == "pharmacy"
    }

    // ---------------------------------------------------------
    // 6. PRICING & STOCK COST SYNC
    // ---------------------------------------------------------
    var updateStockCostOnPurchase by remember {
        mutableStateOf(transactionSettings.updateStockCostOnPurchase)
    }

    // ---------------------------------------------------------
    // 7. OVERALL DISCOUNT
    // ---------------------------------------------------------
    var overallDiscountType by remember { mutableStateOf(PurchaseDiscountType.PERCENTAGE) }
    var overallDiscountValue by remember { mutableDoubleStateOf(transactionSettings.defaultDiscountPercent) }

    // ---------------------------------------------------------
    // 8. VAT / TAX SETTINGS
    // ---------------------------------------------------------
    var enableVatOnPurchase by remember { mutableStateOf(transactionSettings.enableVat) }
    var vatRatePercent by remember { mutableDoubleStateOf(transactionSettings.defaultVatRate) }
    var isVatInclusive by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // 9. OTHER CHARGES (Freight, Labour, Insurance)
    // ---------------------------------------------------------
    val otherCharges = remember { mutableStateListOf<PurchaseOtherCharge>() }
    var showAddChargeDialog by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // 10. PAYMENT & CASH/BANK ACCOUNTS
    // ---------------------------------------------------------
    val paymentModes = remember {
        listOf(
            "Cash-in-Hand",
            "Bank Transfer",
            "Digital Wallet",
            "Credit (Khata Due)"
        )
    }
    var selectedPaymentMode by remember {
        mutableStateOf(transactionSettings.defaultPaymentMode.ifBlank { "Cash-in-Hand" })
    }
    var paidAmountInput by remember { mutableStateOf("") }
    var isManualPaidAmountOverride by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // 12. REFERENCE & 13. REMARKS & 14. ATTACHMENT
    // ---------------------------------------------------------
    var purchaseRemarks by remember { mutableStateOf("") }
    var attachedDocumentName by remember { mutableStateOf<String?>(null) }

    // ---------------------------------------------------------
    // 15. DYNAMIC FINANCIAL CALCULATIONS
    // ---------------------------------------------------------
    val financialSummary by remember {
        derivedStateOf {
            val totalCount = lineItems.size
            val totalQty = lineItems.sumOf { it.quantity }
            val gross = lineItems.sumOf { it.grossSubtotal }

            // Line-level discounts
            val lineDiscounts = lineItems.sumOf { it.computedDiscount }
            val netAfterLineDiscounts = (gross - lineDiscounts).coerceAtLeast(0.0)

            // Bill-level overall discount
            val billDiscount = if (overallDiscountType == PurchaseDiscountType.PERCENTAGE) {
                (netAfterLineDiscounts * overallDiscountValue / 100.0)
            } else {
                overallDiscountValue
            }
            val totalDiscount = lineDiscounts + billDiscount
            val taxableBase = (gross - totalDiscount).coerceAtLeast(0.0)

            // VAT calculation
            val vatAmt = if (enableVatOnPurchase) {
                if (isVatInclusive) {
                    taxableBase - (taxableBase / (1 + vatRatePercent / 100.0))
                } else {
                    taxableBase * vatRatePercent / 100.0
                }
            } else {
                0.0
            }

            // Other charges
            val chargesTotal = otherCharges.sumOf { it.amount }

            // Raw Total
            val rawTotal = if (isVatInclusive) {
                taxableBase + chargesTotal
            } else {
                taxableBase + vatAmt + chargesTotal
            }

            // Round-off
            val roundedGrandTotal = kotlin.math.round(rawTotal * 100.0) / 100.0
            val roundOffVal = roundedGrandTotal - rawTotal

            // Paid vs Due
            val effectivePaid = if (selectedPaymentMode.contains("Credit", ignoreCase = true) || selectedPurchaseType == PurchaseType.CREDIT_PURCHASE) {
                if (isManualPaidAmountOverride) paidAmountInput.toDoubleOrNull() ?: 0.0 else 0.0
            } else {
                if (isManualPaidAmountOverride) paidAmountInput.toDoubleOrNull() ?: roundedGrandTotal else roundedGrandTotal
            }
            val due = (roundedGrandTotal - effectivePaid).coerceAtLeast(0.0)

            PurchaseFinancialSummary(
                totalItemsCount = totalCount,
                totalQuantity = totalQty,
                grossSubtotal = gross,
                totalDiscount = totalDiscount,
                taxableAmount = taxableBase,
                totalVat = vatAmt,
                otherChargesTotal = chargesTotal,
                roundOff = roundOffVal,
                grandTotal = roundedGrandTotal,
                paidAmount = effectivePaid,
                dueAmount = due
            )
        }
    }

    // ---------------------------------------------------------
    // VALIDATION & SUBMIT HELPER
    // ---------------------------------------------------------
    fun submitPurchase(isSaveAndNew: Boolean) {
        val finalPartyName = selectedParty?.name ?: customSupplierName.trim()
        if (transactionSettings.supplierRequired && finalPartyName.isBlank()) {
            Toast.makeText(context, "Please select or enter a Supplier Name", Toast.LENGTH_SHORT).show()
            return
        }
        if (lineItems.isEmpty()) {
            Toast.makeText(context, "Please add at least one item to the purchase", Toast.LENGTH_SHORT).show()
            return
        }

        onSavePurchase(
            purchaseNumber,
            selectedParty,
            finalPartyName,
            lineItems.toList(),
            financialSummary.grossSubtotal,
            financialSummary.totalDiscount,
            financialSummary.taxableAmount,
            financialSummary.totalVat,
            financialSummary.otherChargesTotal,
            financialSummary.roundOff,
            financialSummary.grandTotal,
            financialSummary.paidAmount,
            selectedPaymentMode,
            supplierInvoiceNumber.trim(),
            referenceNumber.trim(),
            purchaseDateMillis,
            purchaseDateBs,
            purchaseDateAd,
            dueDateMillis,
            customDueDateBs,
            selectedPurchaseType.displayName,
            selectedWarehouse,
            purchaseRemarks.trim(),
            attachedDocumentName,
            updateStockCostOnPurchase,
            isSaveAndNew
        )

        if (isSaveAndNew) {
            // Reset for next entry
            val nextPrefix = transactionSettings.purchasePrefix.ifBlank { "PUR-" }
            purchaseNumber = "$nextPrefix$currentYearBs-${(1000..9999).random()}"
            supplierInvoiceNumber = ""
            referenceNumber = ""
            purchaseRemarks = ""
            paidAmountInput = ""
            isManualPaidAmountOverride = false
            Toast.makeText(context, "Purchase saved! Ready for next voucher", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------
    // MAIN SURFACE UI
    // ---------------------------------------------------------
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("purchase_form_sheet"),
        color = formNavyBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // =====================================================
            // 1. PURCHASE HEADER
            // =====================================================
            PurchaseHeaderBar(
                purchaseNumber = purchaseNumber,
                totalAmount = financialSummary.grandTotal,
                currencyFormatter = numFormat,
                onBack = onCancel,
                onRegenerateNumber = {
                    val prefix = transactionSettings.purchasePrefix.ifBlank { "PUR-" }
                    purchaseNumber = "$prefix$currentYearBs-${(1000..9999).random()}"
                }
            )

            // =====================================================
            // SCROLLABLE FORM BODY (Sections 2 to 15)
            // =====================================================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // -------------------------------------------------
                // SECTION 2: SUPPLIER / PARTY SELECTION CARD
                // -------------------------------------------------
                item {
                    SupplierSelectionCard(
                        selectedParty = selectedParty,
                        customSupplierName = customSupplierName,
                        isDirectCounterVendor = isDirectCounterVendor,
                        partySettings = partySettings,
                        transactionSettings = transactionSettings,
                        currencyFormatter = numFormat,
                        onOpenPartyPicker = { showSupplierPickerModal = true },
                        onQuickAddParty = { showQuickAddSupplierModal = true },
                        onCustomSupplierChange = { customSupplierName = it },
                        onDirectCounterVendorChange = {
                            isDirectCounterVendor = it
                            if (it) selectedParty = null
                        },
                        onClearSelectedParty = { selectedParty = null }
                    )
                }

                // -------------------------------------------------
                // SECTION 3: PURCHASE INFORMATION CARD
                // -------------------------------------------------
                item {
                    PurchaseInformationCard(
                        purchaseNumber = purchaseNumber,
                        onPurchaseNumberChange = { purchaseNumber = it },
                        purchaseDateBs = purchaseDateBs,
                        purchaseDateAd = purchaseDateAd,
                        isBsMode = isBsDateMode,
                        onToggleDateMode = { isBsDateMode = !isBsDateMode },
                        onOpenDatePicker = { showDatePickerDialog = true },
                        supplierInvoiceNumber = supplierInvoiceNumber,
                        onSupplierInvoiceChange = { supplierInvoiceNumber = it },
                        referenceNumber = referenceNumber,
                        onReferenceChange = { referenceNumber = it },
                        selectedPurchaseType = selectedPurchaseType,
                        onPurchaseTypeChange = { selectedPurchaseType = it },
                        creditDays = creditDays,
                        onCreditDaysChange = { days ->
                            creditDays = days
                            val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, days) }
                            dueDateMillis = cal.timeInMillis
                            customDueDateBs = NepaliDateUtils.formatBsDate(cal.timeInMillis)
                        },
                        dueDateBs = customDueDateBs,
                        selectedWarehouse = selectedWarehouse,
                        onWarehouseChange = { selectedWarehouse = it },
                        transactionSettings = transactionSettings
                    )
                }

                // -------------------------------------------------
                // SECTION 4: ITEMS LIST & ADD ITEM BUTTON
                // -------------------------------------------------
                item {
                    PurchaseItemsSectionCard(
                        lineItems = lineItems,
                        inventoryItems = inventoryItems,
                        isBatchTrackingActive = isBatchTrackingActive,
                        currencyFormatter = numFormat,
                        onAddItemClick = { showAddItemModal = true },
                        onEditItemClick = { index ->
                            editingLineItemIndex = index
                            showAddItemModal = true
                        },
                        onDeleteItemClick = { index ->
                            if (lineItems.size > 1) {
                                lineItems.removeAt(index)
                            } else {
                                Toast.makeText(context, "At least 1 item is required", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onQuantityChange = { index, newQty ->
                            val item = lineItems[index]
                            lineItems[index] = item.copy(quantity = newQty.coerceAtLeast(0.01))
                        },
                        onRateChange = { index, newRate ->
                            val item = lineItems[index]
                            lineItems[index] = item.copy(unitPrice = newRate.coerceAtLeast(0.0))
                        }
                    )
                }

                // -------------------------------------------------
                // SECTION 6: PRICING & STOCK COST UPDATE TOGGLE
                // -------------------------------------------------
                item {
                    PricingAndStockCostCard(
                        updateStockCost = updateStockCostOnPurchase,
                        onUpdateStockCostChange = { updateStockCostOnPurchase = it },
                        transactionSettings = transactionSettings,
                        inventorySettings = inventorySettings
                    )
                }

                // -------------------------------------------------
                // SECTION 7 & 8: DISCOUNT, VAT & TAXATION CARD
                // -------------------------------------------------
                item {
                    DiscountAndTaxCard(
                        discountType = overallDiscountType,
                        onDiscountTypeChange = { overallDiscountType = it },
                        discountValue = overallDiscountValue,
                        onDiscountValueChange = { overallDiscountValue = it },
                        enableVat = enableVatOnPurchase,
                        onEnableVatChange = { enableVatOnPurchase = it },
                        vatRate = vatRatePercent,
                        onVatRateChange = { vatRatePercent = it },
                        isVatInclusive = isVatInclusive,
                        onVatInclusiveChange = { isVatInclusive = it },
                        summary = financialSummary,
                        currencyFormatter = numFormat,
                        transactionSettings = transactionSettings
                    )
                }

                // -------------------------------------------------
                // SECTION 9: OTHER CHARGES (Freight, Labour, etc.)
                // -------------------------------------------------
                item {
                    OtherChargesCard(
                        otherCharges = otherCharges,
                        currencyFormatter = numFormat,
                        onAddCharge = { showAddChargeDialog = true },
                        onRemoveCharge = { index -> otherCharges.removeAt(index) }
                    )
                }

                // -------------------------------------------------
                // SECTION 10 & 11: PAYMENT & PAYABLE / DUE CARD
                // -------------------------------------------------
                item {
                    PaymentAndDueCard(
                        paymentModes = paymentModes,
                        selectedPaymentMode = selectedPaymentMode,
                        onPaymentModeChange = { selectedPaymentMode = it },
                        paidAmountInput = paidAmountInput,
                        onPaidAmountChange = {
                            paidAmountInput = it
                            isManualPaidAmountOverride = true
                        },
                        grandTotal = financialSummary.grandTotal,
                        paidAmount = financialSummary.paidAmount,
                        dueAmount = financialSummary.dueAmount,
                        currencyFormatter = numFormat,
                        supplierName = selectedParty?.name ?: customSupplierName.ifBlank { "Supplier" },
                        onQuickPaySelect = { percent ->
                            isManualPaidAmountOverride = true
                            paidAmountInput = when (percent) {
                                100 -> financialSummary.grandTotal.toString()
                                50 -> (financialSummary.grandTotal / 2.0).toString()
                                else -> "0.0"
                            }
                        }
                    )
                }

                // -------------------------------------------------
                // SECTION 12, 13 & 14: REMARKS & ATTACHMENT CARD
                // -------------------------------------------------
                item {
                    RemarksAndAttachmentCard(
                        remarks = purchaseRemarks,
                        onRemarksChange = { purchaseRemarks = it },
                        attachedDocumentName = attachedDocumentName,
                        onAttachDocument = { attachedDocumentName = "Supplier_Bill_${System.currentTimeMillis() % 10000}.pdf" },
                        onRemoveAttachment = { attachedDocumentName = null },
                        transactionSettings = transactionSettings
                    )
                }

                // -------------------------------------------------
                // SECTION 15: SUMMARY FINANCIAL KPI BREAKDOWN
                // -------------------------------------------------
                item {
                    PurchaseSummaryKpiCard(
                        summary = financialSummary,
                        currencyFormatter = numFormat
                    )
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }
            }

            // =====================================================
            // 16. FIXED SAVE ACTIONS BOTTOM BAR
            // =====================================================
            PurchaseSaveActionsBar(
                grandTotal = financialSummary.grandTotal,
                dueAmount = financialSummary.dueAmount,
                currencyFormatter = numFormat,
                onCancel = onCancel,
                onSaveAndNew = { submitPurchase(isSaveAndNew = true) },
                onSavePurchase = { submitPurchase(isSaveAndNew = false) }
            )
        }
    }

    // =========================================================
    // MODAL DIALOGS
    // =========================================================

    // 1. Supplier Selection Picker Dialog
    if (showSupplierPickerModal) {
        SupplierPickerModal(
            parties = parties,
            selectedParty = selectedParty,
            onSelect = {
                selectedParty = it
                customSupplierName = ""
                isDirectCounterVendor = false
                showSupplierPickerModal = false
            },
            onDismiss = { showSupplierPickerModal = false },
            onQuickAdd = {
                showSupplierPickerModal = false
                showQuickAddSupplierModal = true
            }
        )
    }

    // 2. Quick Add Supplier Dialog
    if (showQuickAddSupplierModal) {
        QuickAddSupplierDialog(
            onDismiss = { showQuickAddSupplierModal = false },
            onSave = { newParty ->
                onQuickAddParty(newParty)
                selectedParty = newParty
                isDirectCounterVendor = false
                showQuickAddSupplierModal = false
                Toast.makeText(context, "Supplier '${newParty.name}' created and selected", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. Add / Edit Line Item Modal
    if (showAddItemModal) {
        AddEditPurchaseItemModal(
            inventoryItems = inventoryItems,
            existingItem = if (editingLineItemIndex in lineItems.indices) lineItems[editingLineItemIndex] else null,
            isBatchTrackingActive = isBatchTrackingActive,
            defaultTaxable = enableVatOnPurchase,
            onDismiss = {
                showAddItemModal = false
                editingLineItemIndex = -1
            },
            onSaveLineItem = { lineItem ->
                if (editingLineItemIndex in lineItems.indices) {
                    lineItems[editingLineItemIndex] = lineItem
                } else {
                    lineItems.add(lineItem)
                }
                showAddItemModal = false
                editingLineItemIndex = -1
            }
        )
    }

    // 4. Add Other Charge Dialog
    if (showAddChargeDialog) {
        AddOtherChargeDialog(
            onDismiss = { showAddChargeDialog = false },
            onAdd = { name, amount ->
                otherCharges.add(PurchaseOtherCharge(name = name, amount = amount))
                showAddChargeDialog = false
            }
        )
    }

    // 5. Date Picker Modal
    if (showDatePickerDialog) {
        PurchaseDatePickerDialog(
            currentMillis = purchaseDateMillis,
            onDismiss = { showDatePickerDialog = false },
            onSelectDate = { millis, bsStr, adStr ->
                purchaseDateMillis = millis
                purchaseDateBs = bsStr
                purchaseDateAd = adStr
                showDatePickerDialog = false
            }
        )
    }
}

// =============================================================================
// 1. PURCHASE HEADER BAR
// =============================================================================
@Composable
private fun PurchaseHeaderBar(
    purchaseNumber: String,
    totalAmount: Double,
    currencyFormatter: DecimalFormat,
    onBack: () -> Unit,
    onRegenerateNumber: () -> Unit
) {
    Surface(
        color = Color(0xFF10192E),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_close_purchase")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = Color(0xFFF8FAFC)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "New Purchase",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E3A8A).copy(alpha = 0.6f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VOUCHER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onRegenerateNumber() }
                    ) {
                        Text(
                            text = purchaseNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Total Amount Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F2E5C))
                    .border(1.dp, Color(0xFF2563EB), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Rs. ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = currencyFormatter.format(totalAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// =============================================================================
// 2. SUPPLIER / PARTY SELECTION CARD
// =============================================================================
@Composable
private fun SupplierSelectionCard(
    selectedParty: PartyEntity?,
    customSupplierName: String,
    isDirectCounterVendor: Boolean,
    partySettings: PartySettings,
    transactionSettings: TransactionSettings,
    currencyFormatter: DecimalFormat,
    onOpenPartyPicker: () -> Unit,
    onQuickAddParty: () -> Unit,
    onCustomSupplierChange: (String) -> Unit,
    onDirectCounterVendorChange: (Boolean) -> Unit,
    onClearSelectedParty: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A8A).copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Supplier / Party",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = if (transactionSettings.supplierRequired) "Required for Khata & Ledger" else "Optional",
                            fontSize = 11.sp,
                            color = if (transactionSettings.supplierRequired) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onQuickAddParty,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("btn_quick_add_supplier")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Selected Supplier View OR Selection Triggers
            if (selectedParty != null) {
                Surface(
                    color = Color(0xFF0C1427),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedParty.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = selectedParty.name,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF8FAFC)
                                    )
                                    Text(
                                        text = selectedParty.phone.ifBlank { "No phone recorded" },
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = onOpenPartyPicker, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Change", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = onClearSelectedParty, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Supplier Ledger KPI Details
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Outstanding Payable", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "Rs. ${currencyFormatter.format(selectedParty.balanceToGive)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedParty.balanceToGive > 0) Color(0xFFF59E0B) else Color(0xFF10B981)
                                )
                            }
                            if (selectedParty.panVatNumber.isNotBlank()) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PAN / VAT", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        text = selectedParty.panVatNumber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Selector Button
                Button(
                    onClick = onOpenPartyPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_select_supplier")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Supplier from Khata", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct / Cash Vendor Alternative
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDirectCounterVendor,
                        onCheckedChange = onDirectCounterVendorChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2563EB),
                            uncheckedColor = Color(0xFF64748B)
                        )
                    )
                    Text(
                        text = "Cash / Unregistered Counter Vendor",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }

                if (isDirectCounterVendor) {
                    OutlinedTextField(
                        value = customSupplierName,
                        onValueChange = onCustomSupplierChange,
                        label = { Text("Vendor Name / Description", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Local Wholesale Counter, Kalimati Vendor", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("input_custom_supplier")
                    )
                }
            }
        }
    }
}

// =============================================================================
// 3. PURCHASE INFORMATION CARD
// =============================================================================
@Composable
private fun PurchaseInformationCard(
    purchaseNumber: String,
    onPurchaseNumberChange: (String) -> Unit,
    purchaseDateBs: String,
    purchaseDateAd: String,
    isBsMode: Boolean,
    onToggleDateMode: () -> Unit,
    onOpenDatePicker: () -> Unit,
    supplierInvoiceNumber: String,
    onSupplierInvoiceChange: (String) -> Unit,
    referenceNumber: String,
    onReferenceChange: (String) -> Unit,
    selectedPurchaseType: PurchaseType,
    onPurchaseTypeChange: (PurchaseType) -> Unit,
    creditDays: Int,
    onCreditDaysChange: (Int) -> Unit,
    dueDateBs: String,
    selectedWarehouse: String,
    onWarehouseChange: (String) -> Unit,
    transactionSettings: TransactionSettings
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Purchase Information", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Purchase Number & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = purchaseNumber,
                    onValueChange = onPurchaseNumberChange,
                    label = { Text("Purchase No.", fontSize = 11.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Date Chip Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C1427))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .clickable { onOpenDatePicker() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBsMode) "Date (BS)" else "Date (AD)",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (isBsMode) "Switch AD" else "Switch BS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.clickable { onToggleDateMode() }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBsMode) purchaseDateBs else purchaseDateAd,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Supplier Bill No. & PO/Challan Reference
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = supplierInvoiceNumber,
                    onValueChange = onSupplierInvoiceChange,
                    label = { Text("Supplier Bill #", fontSize = 11.5.sp) },
                    placeholder = { Text("e.g. BILL-8921", fontSize = 11.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.weight(1f).testTag("input_supplier_bill_no")
                )

                if (transactionSettings.showPurchaseReference) {
                    OutlinedTextField(
                        value = referenceNumber,
                        onValueChange = onReferenceChange,
                        label = { Text("Ref / PO #", fontSize = 11.5.sp) },
                        placeholder = { Text("PO-2026-X", fontSize = 11.5.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Purchase Type (Cash vs Credit vs Inward)
            Text("Purchase Type", fontSize = 11.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PurchaseType.entries.forEach { pType ->
                    val isSelected = selectedPurchaseType == pType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0C1427))
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable { onPurchaseTypeChange(pType) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pType.displayName.split(" ").take(2).joinToString(" "),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Credit Days / Due Date selector if credit purchase
            if (selectedPurchaseType == PurchaseType.CREDIT_PURCHASE || transactionSettings.showPurchaseDueDate) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Credit Terms: $creditDays Days (Due: $dueDateBs)",
                        fontSize = 11.5.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 45, 60).forEach { days ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (creditDays == days) Color(0xFF0284C7) else Color(0xFF0C1427))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                    .clickable { onCreditDaysChange(days) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$days D",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 4. ITEMS SECTION & ADD ITEM BUTTON
// =============================================================================
@Composable
private fun PurchaseItemsSectionCard(
    lineItems: List<PurchaseLineItem>,
    inventoryItems: List<InventoryItemEntity>,
    isBatchTrackingActive: Boolean,
    currencyFormatter: DecimalFormat,
    onAddItemClick: () -> Unit,
    onEditItemClick: (Int) -> Unit,
    onDeleteItemClick: (Int) -> Unit,
    onQuantityChange: (Int, Double) -> Unit,
    onRateChange: (Int, Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Items / Products (${lineItems.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                }

                Button(
                    onClick = onAddItemClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_add_purchase_item")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Line Items List
            lineItems.forEachIndexed { index, item ->
                PurchaseLineItemRow(
                    index = index,
                    item = item,
                    currencyFormatter = currencyFormatter,
                    isBatchTrackingActive = isBatchTrackingActive,
                    onEdit = { onEditItemClick(index) },
                    onDelete = { onDeleteItemClick(index) },
                    onIncrementQty = { onQuantityChange(index, item.quantity + 1.0) },
                    onDecrementQty = { onQuantityChange(index, (item.quantity - 1.0).coerceAtLeast(1.0)) }
                )
                if (index < lineItems.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Individual Purchase Line Item Card Row
// -----------------------------------------------------------------------------
@Composable
private fun PurchaseLineItemRow(
    index: Int,
    item: PurchaseLineItem,
    currencyFormatter: DecimalFormat,
    isBatchTrackingActive: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onIncrementQty: () -> Unit,
    onDecrementQty: () -> Unit
) {
    Surface(
        color = Color(0xFF0C1427),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Title & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.name.ifBlank { "Unspecified Item" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.sku.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(item.sku, fontSize = 9.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Item", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quantity stepper & Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable { onDecrementQty() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Minus", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable { onIncrementQty() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                // Price x Qty = Subtotal
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "@ Rs. ${currencyFormatter.format(item.unitPrice)}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Rs. ${currencyFormatter.format(item.lineTotal)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            // Batch & Expiry Tag (if tracked)
            if (isBatchTrackingActive && (item.batchNumber.isNotBlank() || item.expiryDate.isNotBlank())) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.batchNumber.isNotBlank()) {
                        Text(
                            text = "Batch: ${item.batchNumber}",
                            fontSize = 10.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                    if (item.expiryDate.isNotBlank()) {
                        Text(
                            text = "Exp: ${item.expiryDate}",
                            fontSize = 10.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 6. PRICING & STOCK COST CARD
// =============================================================================
@Composable
private fun PricingAndStockCostCard(
    updateStockCost: Boolean,
    onUpdateStockCostChange: (Boolean) -> Unit,
    transactionSettings: TransactionSettings,
    inventorySettings: InventorySettings
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AttachMoney, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Update Item Cost in Inventory",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Automatically sets new purchase price as product master cost",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Switch(
                    checked = updateStockCost,
                    onCheckedChange = onUpdateStockCostChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2563EB)
                    )
                )
            }
        }
    }
}

// =============================================================================
// 7 & 8. DISCOUNT, VAT & TAXATION CARD
// =============================================================================
@Composable
private fun DiscountAndTaxCard(
    discountType: PurchaseDiscountType,
    onDiscountTypeChange: (PurchaseDiscountType) -> Unit,
    discountValue: Double,
    onDiscountValueChange: (Double) -> Unit,
    enableVat: Boolean,
    onEnableVatChange: (Boolean) -> Unit,
    vatRate: Double,
    onVatRateChange: (Double) -> Unit,
    isVatInclusive: Boolean,
    onVatInclusiveChange: (Boolean) -> Unit,
    summary: PurchaseFinancialSummary,
    currencyFormatter: DecimalFormat,
    transactionSettings: TransactionSettings
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocalOffer, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Discount & VAT / Tax", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Bill Discount Row
            if (transactionSettings.showPurchaseDiscount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bill Discount", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (discountType == PurchaseDiscountType.PERCENTAGE) "$discountValue%" else "Rs. $discountValue",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0.0, 5.0, 10.0).forEach { disc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (discountValue == disc && discountType == PurchaseDiscountType.PERCENTAGE) Color(0xFF0284C7) else Color(0xFF0C1427))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                    .clickable {
                                        onDiscountTypeChange(PurchaseDiscountType.PERCENTAGE)
                                        onDiscountValueChange(disc)
                                    }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (disc == 0.0) "None" else "$disc%",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(10.dp))
            }

            // VAT / Tax Row
            if (transactionSettings.showPurchaseVat) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = enableVat,
                            onCheckedChange = onEnableVatChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2563EB)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Apply VAT", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = if (enableVat) "Nepal Standard: 13% Taxable Base" else "Tax Exempt / 0%",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    if (enableVat) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(13.0, 0.0).forEach { rate ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (vatRate == rate) Color(0xFF0284C7) else Color(0xFF0C1427))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                    .clickable { onVatRateChange(rate) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (rate == 0.0) "0%" else "13%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// =============================================================================
// 9. OTHER CHARGES CARD
// =============================================================================
@Composable
private fun OtherChargesCard(
    otherCharges: List<PurchaseOtherCharge>,
    currencyFormatter: DecimalFormat,
    onAddCharge: () -> Unit,
    onRemoveCharge: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Freight & Other Charges", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                }

                Text(
                    text = "+ Add Charge",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.clickable { onAddCharge() }
                )
            }

            if (otherCharges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                otherCharges.forEachIndexed { idx, charge ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(charge.name, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+ Rs. ${currencyFormatter.format(charge.amount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { onRemoveCharge(idx) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 10 & 11. PAYMENT & PAYABLE / DUE CARD
// =============================================================================
@Composable
private fun PaymentAndDueCard(
    paymentModes: List<String>,
    selectedPaymentMode: String,
    onPaymentModeChange: (String) -> Unit,
    paidAmountInput: String,
    onPaidAmountChange: (String) -> Unit,
    grandTotal: Double,
    paidAmount: Double,
    dueAmount: Double,
    currencyFormatter: DecimalFormat,
    supplierName: String,
    onQuickPaySelect: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Payments, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Payment & Due Settlement", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentModes.forEach { mode ->
                    val isSelected = selectedPaymentMode.equals(mode, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0C1427))
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable { onPaymentModeChange(mode) }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = mode,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Paid Amount field + Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = paidAmountInput.ifBlank { if (paidAmount > 0) currencyFormatter.format(paidAmount) else "" },
                    onValueChange = onPaidAmountChange,
                    label = { Text("Paid Amount (Rs.)", fontSize = 11.5.sp) },
                    placeholder = { Text("0.00", fontSize = 11.5.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.weight(1f).testTag("input_paid_amount")
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0284C7))
                            .clickable { onQuickPaySelect(100) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("Full Paid", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0C1427))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                            .clickable { onQuickPaySelect(0) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("Credit (Rs. 0)", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Due Banner
            Surface(
                color = if (dueAmount > 0) Color(0xFF2E1906) else Color(0xFF0B291A),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (dueAmount > 0) Color(0xFFF59E0B) else Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (dueAmount > 0) Icons.Outlined.WarningAmber else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (dueAmount > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dueAmount > 0) "Payable to $supplierName Khata:" else "Purchase Paid & Settled in Full",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    if (dueAmount > 0) {
                        Text(
                            text = "Rs. ${currencyFormatter.format(dueAmount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 12, 13 & 14. REMARKS & ATTACHMENT CARD
// =============================================================================
@Composable
private fun RemarksAndAttachmentCard(
    remarks: String,
    onRemarksChange: (String) -> Unit,
    attachedDocumentName: String?,
    onAttachDocument: () -> Unit,
    onRemoveAttachment: () -> Unit,
    transactionSettings: TransactionSettings
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (transactionSettings.showPurchaseRemarks) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Description, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remarks & Notes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF8FAFC))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = onRemarksChange,
                    placeholder = { Text("Enter delivery terms, transport driver info, quality notes...", fontSize = 12.sp) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_purchase_remarks")
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Attachment Row
            if (transactionSettings.enableAttachments) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bill Document / Photo", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    }

                    if (attachedDocumentName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(attachedDocumentName, fontSize = 11.sp, color = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onRemoveAttachment, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onAttachDocument,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Attach File", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 15. SUMMARY KPI CARD
// =============================================================================
@Composable
private fun PurchaseSummaryKpiCard(
    summary: PurchaseFinancialSummary,
    currencyFormatter: DecimalFormat
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1427)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Purchase Financial Breakdown",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            SummaryRow("Items Subtotal (Gross)", "Rs. ${currencyFormatter.format(summary.grossSubtotal)}")
            if (summary.totalDiscount > 0) {
                SummaryRow("Total Discount (-)", "- Rs. ${currencyFormatter.format(summary.totalDiscount)}", valueColor = Color(0xFF10B981))
            }
            SummaryRow("Taxable Amount", "Rs. ${currencyFormatter.format(summary.taxableAmount)}")
            if (summary.totalVat > 0) {
                SummaryRow("VAT 13% (+)", "+ Rs. ${currencyFormatter.format(summary.totalVat)}", valueColor = Color(0xFF38BDF8))
            }
            if (summary.otherChargesTotal > 0) {
                SummaryRow("Other Charges (+)", "+ Rs. ${currencyFormatter.format(summary.otherChargesTotal)}")
            }
            if (summary.roundOff != 0.0) {
                SummaryRow("Round Off", "Rs. ${currencyFormatter.format(summary.roundOff)}")
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow("Grand Total", "Rs. ${currencyFormatter.format(summary.grandTotal)}", isBold = true, fontSize = 15.sp, valueColor = Color.White)
            SummaryRow("Amount Paid", "Rs. ${currencyFormatter.format(summary.paidAmount)}", valueColor = Color(0xFF10B981))
            SummaryRow("Balance Payable (Due)", "Rs. ${currencyFormatter.format(summary.dueAmount)}", isBold = true, valueColor = if (summary.dueAmount > 0) Color(0xFFF59E0B) else Color(0xFF10B981))
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    valueColor: Color = Color(0xFFE2E8F0)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) Color.White else Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

// =============================================================================
// 16. FIXED SAVE ACTIONS BOTTOM BAR
// =============================================================================
@Composable
private fun PurchaseSaveActionsBar(
    grandTotal: Double,
    dueAmount: Double,
    currencyFormatter: DecimalFormat,
    onCancel: () -> Unit,
    onSaveAndNew: () -> Unit,
    onSavePurchase: () -> Unit
) {
    Surface(
        color = Color(0xFF10192E),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(44.dp).testTag("btn_cancel_purchase")
            ) {
                Text("Cancel", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onSaveAndNew,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(44.dp).testTag("btn_save_and_new_purchase")
            ) {
                Text("Save & New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSavePurchase,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_save_purchase")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Purchase", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// MODAL DIALOG 1: SUPPLIER PICKER MODAL
// =============================================================================
@Composable
private fun SupplierPickerModal(
    parties: List<PartyEntity>,
    selectedParty: PartyEntity?,
    onSelect: (PartyEntity) -> Unit,
    onDismiss: () -> Unit,
    onQuickAdd: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredParties = remember(parties, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val supplierPool = parties.filter {
            it.type.equals("Supplier", ignoreCase = true) ||
            it.type.equals("Both", ignoreCase = true) ||
            it.type.equals("General", ignoreCase = true)
        }.ifEmpty { parties }

        if (q.isBlank()) supplierPool
        else supplierPool.filter { it.name.lowercase().contains(q) || it.phone.contains(q) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Select Supplier", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search supplier by name or phone...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onQuickAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Quick Add New Supplier", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredParties) { _, party ->
                        Surface(
                            color = if (selectedParty?.id == party.id) Color(0xFF1E3A8A) else Color(0xFF0C1427),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(party) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(party.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(party.phone.ifBlank { "No phone" }, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Text(
                                    text = "Payable: Rs. ${party.balanceToGive}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// MODAL DIALOG 2: QUICK ADD SUPPLIER
// =============================================================================
@Composable
private fun QuickAddSupplierDialog(
    onDismiss: () -> Unit,
    onSave: (PartyEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var panVat by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quick Add Supplier", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Supplier Name *", fontSize = 11.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontSize = 11.5.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = panVat,
                        onValueChange = { panVat = it },
                        label = { Text("PAN / VAT", fontSize = 11.5.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = { openingBalance = it },
                        label = { Text("Opening Balance (Rs.)", fontSize = 11.5.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val opBal = openingBalance.toDoubleOrNull() ?: 0.0
                        val newParty = PartyEntity(
                            name = name.trim(),
                            phone = phone.trim(),
                            type = "Supplier",
                            category = "Wholesaler",
                            balanceToGive = opBal,
                            balanceToReceive = 0.0,
                            openingBalance = opBal,
                            panVatNumber = panVat.trim(),
                            address = address.trim(),
                            createdAt = System.currentTimeMillis()
                        )
                        onSave(newParty)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("Save Supplier", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// MODAL DIALOG 3: ADD / EDIT PURCHASE LINE ITEM
// =============================================================================
@Composable
private fun AddEditPurchaseItemModal(
    inventoryItems: List<InventoryItemEntity>,
    existingItem: PurchaseLineItem?,
    isBatchTrackingActive: Boolean,
    defaultTaxable: Boolean,
    onDismiss: () -> Unit,
    onSaveLineItem: (PurchaseLineItem) -> Unit
) {
    var selectedExistingItem by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var itemName by remember { mutableStateOf(existingItem?.name ?: "") }
    var sku by remember { mutableStateOf(existingItem?.sku ?: "") }
    var unit by remember { mutableStateOf(existingItem?.unit ?: "pcs") }
    var quantityText by remember { mutableStateOf(existingItem?.quantity?.toString() ?: "1.0") }
    var rateText by remember { mutableStateOf(existingItem?.unitPrice?.toString() ?: "") }
    var mrpText by remember { mutableStateOf(if ((existingItem?.mrp ?: 0.0) > 0) existingItem?.mrp.toString() else "") }
    var discountPercentText by remember { mutableStateOf(if ((existingItem?.discountPercent ?: 0.0) > 0) existingItem?.discountPercent.toString() else "") }
    var batchNumber by remember { mutableStateOf(existingItem?.batchNumber ?: "") }
    var expiryDate by remember { mutableStateOf(existingItem?.expiryDate ?: "") }
    var isTaxable by remember { mutableStateOf(existingItem?.isTaxable ?: defaultTaxable) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (existingItem != null) "Edit Line Item" else "Add Item to Purchase",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Inventory Item Selector if available
                    if (inventoryItems.isNotEmpty() && existingItem == null) {
                        item {
                            Text("Or Pick Existing Product:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                inventoryItems.take(8).forEach { inv ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (selectedExistingItem?.id == inv.id) Color(0xFF1E3A8A) else Color(0xFF0C1427))
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                            .clickable {
                                                selectedExistingItem = inv
                                                itemName = inv.name
                                                sku = inv.sku
                                                unit = inv.unit
                                                rateText = if (inv.purchasePrice > 0) inv.purchasePrice.toString() else rateText
                                                mrpText = if (inv.salePrice > 0) inv.salePrice.toString() else mrpText
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(inv.name, fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Item Name *", fontSize = 11.5.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_item_name")
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("SKU / Code", fontSize = 11.5.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit", fontSize = 11.5.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it },
                                label = { Text("Quantity *", fontSize = 11.5.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f).testTag("input_item_qty")
                            )

                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it },
                                label = { Text("Purchase Rate (Rs.) *", fontSize = 11.5.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f).testTag("input_item_rate")
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mrpText,
                                onValueChange = { mrpText = it },
                                label = { Text("MRP / Sale Price", fontSize = 11.5.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = discountPercentText,
                                onValueChange = { discountPercentText = it },
                                label = { Text("Disc %", fontSize = 11.5.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Batch & Expiry (if active)
                    if (isBatchTrackingActive) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = batchNumber,
                                    onValueChange = { batchNumber = it },
                                    label = { Text("Batch No.", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = expiryDate,
                                    onValueChange = { expiryDate = it },
                                    label = { Text("Expiry (MM/YY)", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isTaxable,
                                onCheckedChange = { isTaxable = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2563EB),
                                    uncheckedColor = Color(0xFF64748B)
                                )
                            )
                            Text("Subject to 13% VAT", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (itemName.isBlank()) return@Button
                        val qty = quantityText.toDoubleOrNull() ?: 1.0
                        val rate = rateText.toDoubleOrNull() ?: 0.0
                        val mrp = mrpText.toDoubleOrNull() ?: 0.0
                        val disc = discountPercentText.toDoubleOrNull() ?: 0.0

                        val finalItem = PurchaseLineItem(
                            id = existingItem?.id ?: UUID.randomUUID().toString(),
                            itemId = selectedExistingItem?.id ?: existingItem?.itemId,
                            name = itemName.trim(),
                            sku = sku.trim(),
                            unit = unit.trim().ifBlank { "pcs" },
                            quantity = qty,
                            unitPrice = rate,
                            mrp = mrp,
                            discountPercent = disc,
                            isTaxable = isTaxable,
                            batchNumber = batchNumber.trim(),
                            expiryDate = expiryDate.trim()
                        )
                        onSaveLineItem(finalItem)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Add to Purchase Voucher", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// MODAL DIALOG 4: ADD OTHER CHARGE
// =============================================================================
@Composable
private fun AddOtherChargeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var chargeName by remember { mutableStateOf("Freight & Transportation") }
    var chargeAmountText by remember { mutableStateOf("") }

    val presetCharges = listOf("Freight & Transportation", "Labour & Loading", "Insurance / Handling", "Custom Duty")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add Ancillary Charge", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCharges.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (chargeName == preset) Color(0xFF1E3A8A) else Color(0xFF0C1427))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                .clickable { chargeName = preset }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(preset, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = chargeName,
                    onValueChange = { chargeName = it },
                    label = { Text("Charge Description", fontSize = 11.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = chargeAmountText,
                    onValueChange = { chargeAmountText = it },
                    label = { Text("Charge Amount (Rs.) *", fontSize = 11.5.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val amt = chargeAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onAdd(chargeName.trim(), amt)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Add Charge", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// MODAL DIALOG 5: DATE PICKER DIALOG (BS & AD Dual Support)
// =============================================================================
@Composable
private fun PurchaseDatePickerDialog(
    currentMillis: Long,
    onDismiss: () -> Unit,
    onSelectDate: (Long, String, String) -> Unit
) {
    var selectedOffsetDays by remember { mutableIntStateOf(0) }
    val baseCal = java.util.Calendar.getInstance()

    val quickOptions = listOf(
        Pair("Today", 0),
        Pair("Yesterday", -1),
        Pair("2 Days Ago", -2),
        Pair("3 Days Ago", -3),
        Pair("1 Week Ago", -7)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131E36)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Purchase Date", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                quickOptions.forEach { (label, offset) ->
                    val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, offset) }
                    val bsStr = NepaliDateUtils.formatBsDate(cal.timeInMillis)
                    val adStr = NepaliDateUtils.formatAdDate(cal.timeInMillis)

                    Surface(
                        color = if (selectedOffsetDays == offset) Color(0xFF1E3A8A) else Color(0xFF0C1427),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedOffsetDays = offset
                                onSelectDate(cal.timeInMillis, bsStr, adStr)
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(bsStr, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                Text(adStr, fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }
}
