package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InvoiceAttachmentEntity
import com.example.data.local.entity.PartyEntity
import com.example.data.local.entity.PaymentAllocationEntity
import com.example.data.local.entity.SalesInvoiceEntity
import com.example.data.local.entity.SalesInvoiceItemEntity
import com.example.data.model.InventorySettings
import com.example.data.model.InvoiceSettings
import com.example.data.model.PartySettings
import com.example.data.model.TransactionSettings
import com.example.ui.theme.AppTheme
import com.example.util.AttachmentUtils
import com.example.util.BillDiscountType
import com.example.util.InvoiceCalculationEngine
import com.example.util.InvoiceLineItemInput
import com.example.util.NepaliDateUtils
import java.io.File
import java.text.DecimalFormat
import java.util.Locale
import java.util.UUID

/**
 * Clean data model for invoice line items in the Sales Form
 */
data class InvoiceLineItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: Long? = null,
    val name: String,
    val sku: String = "",
    val unit: String = "pcs",
    var quantity: Double = 1.0,
    var unitPrice: Double = 0.0,
    var discountPercent: Double = 0.0,
    var discount: Double = 0.0,
    val stockAvailable: Double? = null,
    val batchNumber: String? = null,
    val expiryDate: String? = null
) {
    val subtotal: Double
        get() {
            val gross = quantity * unitPrice
            val disc = if (discountPercent > 0) gross * (discountPercent / 100.0) else discount
            return (gross - disc).coerceAtLeast(0.0)
        }
}

data class CashBankAccountOption(
    val name: String,
    val subtitle: String,
    val type: String,
    val icon: ImageVector,
    val balance: Double? = null
)

val defaultSalesCashBankAccounts = listOf(
    CashBankAccountOption("Cash", "Liquid Counter Cash", "Cash", Icons.Outlined.Payments, null),
    CashBankAccountOption("Bank Transfer", "Direct Bank / Cheque", "Bank", Icons.Outlined.AccountBalance, null),
    CashBankAccountOption("Digital Wallet", "eSewa / Fonepay / Khalti QR", "Wallet", Icons.Outlined.QrCodeScanner, null),
    CashBankAccountOption("Credit (Khata Due)", "Unpaid Customer Khata", "Credit", Icons.Outlined.CreditCard, null)
)

/**
 * Simple, professional, 100% theme-aware Sales Invoice Form
 */
@Composable
fun SalesInvoiceSheet(
    parties: List<PartyEntity>,
    inventoryItems: List<InventoryItemEntity> = emptyList(),
    cashBankAccounts: List<CashBankAccountOption> = defaultSalesCashBankAccounts,
    transactionSettings: TransactionSettings = TransactionSettings(),
    invoiceSettings: InvoiceSettings = InvoiceSettings(),
    inventorySettings: InventorySettings = InventorySettings(),
    partySettings: PartySettings = PartySettings(),
    onSaveInvoice: ((invoice: SalesInvoiceEntity, items: List<SalesInvoiceItemEntity>, payments: List<PaymentAllocationEntity>, isSaveAndNew: Boolean, attachments: List<InvoiceAttachmentEntity>) -> Unit)? = null,
    onSave: ((partyName: String, amount: Double, mode: String, invNum: String, note: String, dateMillis: Long, dateBs: String, dateAd: String) -> Unit)? = null,
    onQuickAddParty: ((PartyEntity) -> Unit)? = null,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val currencyFmt = remember { DecimalFormat("#,##,##0.00") }

    // Helper to generate invoice numbers based on settings
    fun generateNextInvoiceNumber(): String {
        val randomDigits = (1000..9999).random()
        val bsYear = NepaliDateUtils.adToBs(System.currentTimeMillis()).year
        val prefix = invoiceSettings.invoicePrefix.ifBlank { transactionSettings.salesInvoicePrefix.ifBlank { "INV" } }
        val separator = invoiceSettings.numberSeparator.ifBlank { "-" }
        return if (invoiceSettings.includeFiscalYear) {
            "$prefix$separator$bsYear$separator$randomDigits"
        } else {
            "$prefix$separator$randomDigits"
        }
    }

    // ---------------- Header & State Variables ----------------
    var invoiceNumber by remember { mutableStateOf(generateNextInvoiceNumber()) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDateBs by remember { mutableStateOf(NepaliDateUtils.formatBsDate(System.currentTimeMillis())) }
    var selectedDateAd by remember { mutableStateOf(NepaliDateUtils.formatAdDate(System.currentTimeMillis())) }
    var isBsDateMode by remember { mutableStateOf(true) }

    // ---------------- Validation Error States ----------------
    var customerError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var itemsError by remember { mutableStateOf<String?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showAddCustomerSheet by remember { mutableStateOf(false) }

    // ---------------- Party Selection ----------------
    var selectedParty by remember { mutableStateOf<PartyEntity?>(null) }
    var isCustomerDropdownOpen by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }

    // ---------------- Items ----------------
    val lineItems = remember { mutableStateListOf<InvoiceLineItem>() }
    var showAddItemDialog by remember { mutableStateOf(false) }

    // ---------------- Payment ----------------
    var selectedPaymentMode by remember {
        val defMode = transactionSettings.defaultPaymentMode
        mutableStateOf(if (defMode.isNotBlank()) defMode else "Cash")
    }
    var paidAmountText by remember { mutableStateOf("") }
    var isPaidUserEdited by remember { mutableStateOf(false) }

    // ---------------- Collapsible More Details ----------------
    var isMoreDetailsExpanded by remember { mutableStateOf(false) }
    var referenceNumber by remember { mutableStateOf("") }
    var salesperson by remember { mutableStateOf("") }
    var servicePerson by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var dueDateBs by remember { mutableStateOf("") }

    // ---------------- Save State ----------------
    var isSaving by remember { mutableStateOf(false) }

    // ---------------- Attachments State ----------------
    var attachments by remember { mutableStateOf<List<InvoiceAttachmentEntity>>(emptyList()) }
    var previewImageAttachment by remember { mutableStateOf<InvoiceAttachmentEntity?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val att = AttachmentUtils.saveUriToInternalStorage(context, it, forcedPdf = false)
            if (att != null) {
                attachments = attachments + att
            } else {
                Toast.makeText(context, "Failed to attach image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val att = AttachmentUtils.saveUriToInternalStorage(context, it, forcedPdf = true)
            if (att != null) {
                attachments = attachments + att
            } else {
                Toast.makeText(context, "Failed to attach PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------- Sales Adjustments (Discount, VAT, Extra Charge, Round Off) ----------------
    var isBillDiscountApplied by remember { mutableStateOf(false) }
    var billDiscountType by remember { mutableStateOf(BillDiscountType.AMOUNT) }
    var billDiscountInputText by remember { mutableStateOf("") }
    var billDiscountRemarks by remember { mutableStateOf("") }

    var isVatApplied by remember { mutableStateOf(false) }
    var vatRateText by remember { mutableStateOf(if (invoiceSettings.defaultVatRate > 0) invoiceSettings.defaultVatRate.toString() else "13.0") }

    var isExtraChargeApplied by remember { mutableStateOf(false) }
    var extraChargeDescription by remember { mutableStateOf(invoiceSettings.defaultExtraChargeName.ifBlank { "Delivery Charge" }) }
    var extraChargeAmountText by remember { mutableStateOf("") }

    var isRoundOffApplied by remember { mutableStateOf(false) }
    var selectedRoundingMethod by remember { mutableStateOf(invoiceSettings.roundingMethod.ifBlank { "Normal (Nearest 1.00)" }) }
    val isDiscountEnabled = transactionSettings.showDiscount

    // ---------------- Calculations ----------------
    val calculationResult by remember {
        derivedStateOf {
            val itemsInput = lineItems.map { line ->
                InvoiceLineItemInput(
                    quantity = line.quantity,
                    unitPrice = line.unitPrice,
                    discountPercent = line.discountPercent,
                    discountAmount = line.discount
                )
            }
            val billDiscVal = billDiscountInputText.toDoubleOrNull() ?: 0.0
            val vRate = vatRateText.toDoubleOrNull() ?: invoiceSettings.defaultVatRate
            val extraAmt = extraChargeAmountText.toDoubleOrNull() ?: 0.0

            InvoiceCalculationEngine.calculate(
                items = itemsInput,
                isDiscountApplied = isBillDiscountApplied,
                billDiscountType = billDiscountType,
                billDiscountValue = billDiscVal,
                isVatApplied = isVatApplied,
                vatRate = vRate,
                isExtraChargeApplied = isExtraChargeApplied,
                extraChargeDescription = extraChargeDescription,
                extraChargeAmount = extraAmt,
                isRoundOffApplied = isRoundOffApplied,
                roundingMethod = selectedRoundingMethod
            )
        }
    }

    val subtotal = calculationResult.grossItemAmount
    val itemDiscountTotal = calculationResult.itemDiscountTotal
    val billDiscountAmount = calculationResult.billDiscountAmount
    val totalDiscount = calculationResult.totalDiscountAmount
    val taxableAmount = calculationResult.taxableAmount
    val vatAmount = calculationResult.vatAmount
    val effectiveVatRate = calculationResult.vatRate
    val extraChargeAmount = calculationResult.extraChargeAmount
    val preRoundTotal = calculationResult.preRoundTotal
    val roundOff = calculationResult.roundOffAmount
    val grandTotal = calculationResult.grandTotal

    // Default paid amount to grandTotal for Cash sale unless user edited it
    val paidAmount = if (isPaidUserEdited) {
        paidAmountText.toDoubleOrNull() ?: 0.0
    } else {
        if (selectedPaymentMode == "Credit (Khata Due)") 0.0 else grandTotal
    }
    val dueAmount = (grandTotal - paidAmount).coerceAtLeast(0.0)

    // Form Validation Function
    fun validateForm(): Boolean {
        var isValid = true

        // 1. Mandatory Customer validation
        if (selectedParty == null) {
            customerError = "Customer is required. Please select or add a customer."
            isValid = false
        } else {
            customerError = null
        }

        // 2. Mandatory Date validation
        if (selectedDateBs.isBlank() && selectedDateAd.isBlank()) {
            dateError = "Invoice Date is required."
            isValid = false
        } else {
            dateError = null
        }

        // 3. Mandatory Items validation
        if (lineItems.isEmpty()) {
            itemsError = "Please add at least one item to save the invoice."
            isValid = false
        } else if (lineItems.any { it.quantity <= 0 }) {
            itemsError = "Each item must have a quantity greater than 0."
            isValid = false
        } else if (lineItems.any { it.unitPrice < 0 }) {
            itemsError = "Item rate cannot be negative."
            isValid = false
        } else {
            itemsError = null
        }

        if (!isValid) {
            val missing = mutableListOf<String>()
            if (selectedParty == null) missing.add("Customer")
            if (selectedDateBs.isBlank() && selectedDateAd.isBlank()) missing.add("Date")
            if (lineItems.isEmpty()) missing.add("Items")
            val msg = if (missing.isNotEmpty()) {
                "Please fill mandatory fields: ${missing.joinToString(", ")}"
            } else {
                itemsError ?: customerError ?: dateError ?: "Please correct errors in form"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        return isValid
    }

    // Save Execution Function
    fun performSave(isSaveAndNew: Boolean) {
        if (isSaving) return
        if (!validateForm()) return

        isSaving = true
        val partyName = selectedParty?.name ?: "Customer"

        val invoiceEntity = SalesInvoiceEntity(
            invoiceNumber = invoiceNumber,
            partyId = selectedParty?.id,
            partyNameSnapshot = partyName,
            partyPhoneSnapshot = selectedParty?.phone,
            partyAddressSnapshot = selectedParty?.address,
            partyPanSnapshot = selectedParty?.panVatNumber,
            invoiceDateBS = selectedDateBs,
            invoiceDateAD = selectedDateAd,
            dateMillis = selectedDateMillis,
            invoiceStatus = if (dueAmount <= 0.0) "Paid" else if (paidAmount > 0.0) "Partially Paid" else "Confirmed",
            subtotal = subtotal,
            isDiscountApplied = isBillDiscountApplied && billDiscountAmount > 0,
            discountAmount = totalDiscount,
            discountPercent = if (subtotal > 0) (totalDiscount / subtotal) * 100.0 else 0.0,
            taxableAmount = taxableAmount,
            isVatApplied = isVatApplied && vatAmount > 0,
            vatRate = if (isVatApplied) effectiveVatRate else 0.0,
            vatAmount = vatAmount,
            isExtraChargeApplied = isExtraChargeApplied && extraChargeAmount > 0,
            extraChargeAmount = extraChargeAmount,
            extraChargeDescription = if (isExtraChargeApplied) extraChargeDescription else null,
            preRoundTotal = preRoundTotal,
            isRoundOffApplied = isRoundOffApplied,
            roundOff = roundOff,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            paymentStatus = if (dueAmount <= 0.0) "Paid" else if (paidAmount > 0.0) "Partially Paid" else "Unpaid",
            paymentMethod = selectedPaymentMode,
            paymentAccountId = selectedPaymentMode,
            dueDateBS = dueDateBs.ifBlank { null },
            salespersonName = salesperson.ifBlank { null },
            referenceNumber = referenceNumber.ifBlank { null },
            remarks = if (billDiscountRemarks.isNotBlank()) {
                if (remarks.isNotBlank()) "$remarks | Discount: $billDiscountRemarks" else "Discount: $billDiscountRemarks"
            } else remarks.ifBlank { null },
            isTaxInvoice = isVatApplied,
            createdBy = "Admin"
        )

        val itemEntities = lineItems.map { line ->
            SalesInvoiceItemEntity(
                invoiceId = 0L,
                productId = line.itemId,
                productNameSnapshot = line.name,
                productCodeSnapshot = line.sku,
                quantity = line.quantity,
                unit = line.unit,
                rate = line.unitPrice,
                discount = line.discount,
                discountPercent = line.discountPercent,
                vatRate = if (isVatApplied) effectiveVatRate else 0.0,
                vatAmount = if (isVatApplied) line.subtotal * (effectiveVatRate / 100.0) else 0.0,
                lineTotal = line.subtotal,
                batchNumber = line.batchNumber,
                expiryDate = line.expiryDate
            )
        }

        val paymentEntities = if (paidAmount > 0.0) {
            listOf(
                PaymentAllocationEntity(
                    invoiceId = 0L,
                    partyId = selectedParty?.id,
                    paymentAmount = paidAmount,
                    paymentDateMillis = selectedDateMillis,
                    paymentDateBS = selectedDateBs,
                    paymentDateAD = selectedDateAd,
                    paymentMethod = selectedPaymentMode,
                    transactionReference = referenceNumber.ifBlank { null },
                    remarks = "Sales payment at invoice creation",
                    recordedBy = "Admin"
                )
            )
        } else emptyList()

        if (onSaveInvoice != null) {
            onSaveInvoice(invoiceEntity, itemEntities, paymentEntities, isSaveAndNew, attachments)
        } else if (onSave != null) {
            onSave(
                partyName,
                grandTotal,
                selectedPaymentMode,
                invoiceNumber,
                remarks,
                selectedDateMillis,
                selectedDateBs,
                selectedDateAd
            )
        }

        Toast.makeText(context, "Sale #$invoiceNumber saved!", Toast.LENGTH_SHORT).show()

        if (isSaveAndNew) {
            // Clear form and prepare for next sale
            invoiceNumber = generateNextInvoiceNumber()
            lineItems.clear()
            selectedParty = null
            paidAmountText = ""
            isPaidUserEdited = false
            referenceNumber = ""
            salesperson = ""
            remarks = ""
            attachments = emptyList()
            customerError = null
            dateError = null
            itemsError = null
            isSaving = false
        } else {
            isSaving = false
            onCancel()
        }
    }

    // ---------------- MAIN SURFACE ----------------
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sales_form_root"),
        color = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ---------------- 1. HEADER ----------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(width = 0.5.dp, color = colors.cardBorder)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_back_sales")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "New Sale",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Inv #$invoiceNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary
                    )
                }
            }

            // ---------------- SCROLLABLE BODY ----------------
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // ---------------- 2. TOP SECTION: DATE, INVOICE NO, CUSTOMER / PARTY ----------------
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Date & Invoice No Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Date Field
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Date",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.inputBackground)
                                            .border(
                                                1.dp,
                                                if (dateError != null) colors.error else colors.inputBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { showDatePickerDialog = true }
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CalendarMonth,
                                                contentDescription = null,
                                                tint = if (dateError != null) colors.error else colors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isBsDateMode) selectedDateBs else selectedDateAd,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = if (isBsDateMode) "BS" else "AD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                    if (dateError != null) {
                                        Text(
                                            text = dateError!!,
                                            color = colors.error,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                        )
                                    }
                                }

                                // Invoice No Field
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Invoice No.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.inputBackground)
                                            .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 11.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = invoiceNumber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Customer / Party Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Customer / Party",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textSecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { showAddCustomerSheet = true }
                                        .testTag("btn_add_customer")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Add Customer",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Customer Selector Box
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.inputBackground)
                                        .border(
                                            1.dp,
                                            if (customerError != null) colors.error else colors.inputBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { isCustomerDropdownOpen = true }
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (selectedParty != null) colors.primary else if (customerError != null) colors.error else colors.textMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        if (selectedParty == null) {
                                            Text(
                                                text = "Select Customer",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colors.textMuted
                                            )
                                        } else {
                                            Column {
                                                Text(
                                                    text = selectedParty!!.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (selectedParty?.phone?.isNotBlank() == true) {
                                                    Text(
                                                        text = selectedParty!!.phone,
                                                        fontSize = 11.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (selectedParty != null) {
                                            IconButton(
                                                onClick = {
                                                    selectedParty = null
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear Customer",
                                                    tint = colors.textMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Select",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Dropdown Menu with Search, Walk-in option, and Party list
                                DropdownMenu(
                                    expanded = isCustomerDropdownOpen,
                                    onDismissRequest = {
                                        isCustomerDropdownOpen = false
                                        customerSearchQuery = ""
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(colors.surface)
                                ) {
                                    OutlinedTextField(
                                        value = customerSearchQuery,
                                        onValueChange = { customerSearchQuery = it },
                                        placeholder = { Text("Search customer...", fontSize = 12.sp, color = colors.textMuted) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.inputBackground,
                                            unfocusedContainerColor = colors.inputBackground,
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.inputBorder
                                        )
                                    )

                                    // Walk-in option
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("Walk-in Cash Customer", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                                Text("General counter sale", fontSize = 11.sp, color = colors.textMuted)
                                            }
                                        },
                                        onClick = {
                                            selectedParty = PartyEntity(
                                                id = 0L,
                                                name = "Walk-in Cash Customer",
                                                type = "Customer",
                                                phone = "",
                                                address = "Counter",
                                                panVatNumber = "",
                                                openingBalance = 0.0,
                                                category = "Retail",
                                                registerDate = System.currentTimeMillis()
                                            )
                                            customerError = null
                                            isCustomerDropdownOpen = false
                                            customerSearchQuery = ""
                                        }
                                    )

                                    val filteredParties = parties.filter {
                                        it.name.contains(customerSearchQuery, ignoreCase = true) ||
                                            it.phone.contains(customerSearchQuery)
                                    }

                                    filteredParties.forEach { party ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(party.name, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                                    if (party.phone.isNotBlank()) {
                                                        Text(party.phone, fontSize = 11.sp, color = colors.textSecondary)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedParty = party
                                                customerError = null
                                                isCustomerDropdownOpen = false
                                                customerSearchQuery = ""
                                            }
                                        )
                                    }
                                }
                            }

                            if (customerError != null) {
                                Text(
                                    text = customerError!!,
                                    color = colors.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // ---------------- 3. ITEMS SECTION ----------------
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (itemsError != null) colors.error else colors.cardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Items (${lineItems.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Button(
                                    onClick = { showAddItemDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_add_item")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Item",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (lineItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.inputBackground)
                                        .border(
                                            1.dp,
                                            if (itemsError != null) colors.error.copy(alpha = 0.5f) else colors.cardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.Inventory2,
                                            contentDescription = null,
                                            tint = if (itemsError != null) colors.error else colors.textMuted,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "No Items Added Yet",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Tap '+ Add Item' to select from inventory",
                                            fontSize = 11.sp,
                                            color = colors.textMuted
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    lineItems.forEachIndexed { index, item ->
                                        ItemRowCard(
                                            item = item,
                                            isDiscountEnabled = isDiscountEnabled,
                                            onQuantityChanged = { newQty ->
                                                if (newQty <= 0) {
                                                    lineItems.removeAt(index)
                                                } else {
                                                    lineItems[index] = item.copy(quantity = newQty)
                                                }
                                                if (lineItems.isNotEmpty()) itemsError = null
                                            },
                                            onRemove = {
                                                lineItems.removeAt(index)
                                            }
                                        )
                                    }
                                }
                            }

                            if (itemsError != null) {
                                Surface(
                                    color = colors.error.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = colors.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = itemsError!!,
                                            color = colors.error,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------------- 4. ADJUSTMENTS & CALCULATION SECTION ----------------
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Section Title & Adjustment Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Adjustments & Total",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Apply as needed",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // 4 Optional Adjustment Toggle Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    AdjustmentPillChip(
                                        label = "Discount",
                                        isActive = isBillDiscountApplied,
                                        onClick = { isBillDiscountApplied = !isBillDiscountApplied }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    AdjustmentPillChip(
                                        label = "VAT",
                                        isActive = isVatApplied,
                                        onClick = { isVatApplied = !isVatApplied }
                                    )
                                }
                                Box(modifier = Modifier.weight(1.2f)) {
                                    AdjustmentPillChip(
                                        label = "Extra Charge",
                                        isActive = isExtraChargeApplied,
                                        onClick = { isExtraChargeApplied = !isExtraChargeApplied }
                                    )
                                }
                                Box(modifier = Modifier.weight(1.1f)) {
                                    AdjustmentPillChip(
                                        label = "Round Off",
                                        isActive = isRoundOffApplied,
                                        onClick = { isRoundOffApplied = !isRoundOffApplied }
                                    )
                                }
                            }

                            // 1. Bill Discount Form (when active)
                            AnimatedVisibility(
                                visible = isBillDiscountApplied,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.inputBackground)
                                        .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Bill Discount",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        IconButton(
                                            onClick = { isBillDiscountApplied = false },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Discount",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mode selector (% or Rs.)
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.surface)
                                                .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clickable { billDiscountType = BillDiscountType.PERCENTAGE }
                                                    .background(if (billDiscountType == BillDiscountType.PERCENTAGE) colors.primary else Color.Transparent)
                                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "%",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (billDiscountType == BillDiscountType.PERCENTAGE) Color.White else colors.textPrimary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clickable { billDiscountType = BillDiscountType.AMOUNT }
                                                    .background(if (billDiscountType == BillDiscountType.AMOUNT) colors.primary else Color.Transparent)
                                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "Rs.",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (billDiscountType == BillDiscountType.AMOUNT) Color.White else colors.textPrimary
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = billDiscountInputText,
                                            onValueChange = { billDiscountInputText = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = {
                                                Text(
                                                    text = if (billDiscountType == BillDiscountType.PERCENTAGE) "Rate % (e.g. 5)" else "Amount in Rs.",
                                                    fontSize = 12.sp
                                                )
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = colors.surface,
                                                unfocusedContainerColor = colors.surface,
                                                focusedBorderColor = colors.primary,
                                                unfocusedBorderColor = colors.inputBorder
                                            )
                                        )
                                    }

                                    if (billDiscountAmount > 0) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Discount: - Rs. ${currencyFmt.format(billDiscountAmount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF059669)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = billDiscountRemarks,
                                        onValueChange = { billDiscountRemarks = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Reason / note (e.g. Festival offer)", fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = colors.surface,
                                            unfocusedContainerColor = colors.surface,
                                            focusedBorderColor = colors.inputBorder,
                                            unfocusedBorderColor = colors.inputBorder
                                        )
                                    )
                                }
                            }

                            // 2. VAT Form (when active)
                            AnimatedVisibility(
                                visible = isVatApplied,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.inputBackground)
                                        .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "VAT (Value Added Tax)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        IconButton(
                                            onClick = { isVatApplied = false },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove VAT",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Taxable Base Amount:",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = "Rs. ${currencyFmt.format(taxableAmount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = vatRateText,
                                            onValueChange = { if (invoiceSettings.allowVatRateEditing) vatRateText = it },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("VAT Rate %", fontSize = 11.sp) },
                                            readOnly = !invoiceSettings.allowVatRateEditing,
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = colors.surface,
                                                unfocusedContainerColor = colors.surface,
                                                focusedBorderColor = colors.primary,
                                                unfocusedBorderColor = colors.inputBorder
                                            )
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "VAT Amount",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                            Text(
                                                text = "+ Rs. ${currencyFmt.format(vatAmount)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.primary
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Extra Charge Form (when active)
                            AnimatedVisibility(
                                visible = isExtraChargeApplied,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.inputBackground)
                                        .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Extra Charge",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        IconButton(
                                            onClick = { isExtraChargeApplied = false },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Extra Charge",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick charge presets
                                    val presets = listOf("Delivery Charge", "Service Charge", "Packing Charge", "Transport Charge", "Other Charge")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        presets.take(4).forEach { preset ->
                                            val isSelected = extraChargeDescription.equals(preset, ignoreCase = true)
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { extraChargeDescription = preset },
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.primary else colors.inputBorder)
                                            ) {
                                                Text(
                                                    text = preset.replace(" Charge", ""),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (isSelected) colors.primary else colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = extraChargeDescription,
                                            onValueChange = { extraChargeDescription = it },
                                            modifier = Modifier.weight(1.2f),
                                            label = { Text("Charge Description", fontSize = 11.sp) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = colors.surface,
                                                unfocusedContainerColor = colors.surface,
                                                focusedBorderColor = colors.primary,
                                                unfocusedBorderColor = colors.inputBorder
                                            )
                                        )
                                        OutlinedTextField(
                                            value = extraChargeAmountText,
                                            onValueChange = { extraChargeAmountText = it },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("Amount (Rs.)", fontSize = 11.sp) },
                                            placeholder = { Text("0.00", fontSize = 11.sp) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = colors.surface,
                                                unfocusedContainerColor = colors.surface,
                                                focusedBorderColor = colors.primary,
                                                unfocusedBorderColor = colors.inputBorder
                                            )
                                        )
                                    }
                                }
                            }

                            // 4. Round Off Form (when active)
                            AnimatedVisibility(
                                visible = isRoundOffApplied,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.inputBackground)
                                        .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Round Off Adjustment",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        IconButton(
                                            onClick = { isRoundOffApplied = false },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Round Off",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Rounding method chips
                                    val methods = listOf("Normal (Nearest 1.00)", "Always Up", "Always Down")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        methods.forEach { method ->
                                            val isSelected = selectedRoundingMethod == method
                                            val label = when (method) {
                                                "Normal (Nearest 1.00)" -> "Nearest"
                                                "Always Up" -> "Ceil Up"
                                                else -> "Floor Down"
                                            }
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { selectedRoundingMethod = method },
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.primary else colors.inputBorder)
                                            ) {
                                                Text(
                                                    text = label,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (isSelected) colors.primary else colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Pre-Round: Rs. ${currencyFmt.format(preRoundTotal)}",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                        val sign = if (roundOff > 0) "+" else ""
                                        Text(
                                            text = "Adjustment: $sign Rs. ${currencyFmt.format(roundOff)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (roundOff >= 0) colors.primary else Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = colors.cardBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            // ---------------- SUMMARY BREAKDOWN ----------------
                            // Subtotal
                            SummaryRow(
                                label = "Subtotal",
                                value = "Rs. ${currencyFmt.format(subtotal)}",
                                labelColor = colors.textSecondary,
                                valueColor = colors.textPrimary
                            )

                            // Item-level Discount (if any)
                            if (itemDiscountTotal > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                SummaryRow(
                                    label = "Item Discount",
                                    value = "- Rs. ${currencyFmt.format(itemDiscountTotal)}",
                                    labelColor = colors.textSecondary,
                                    valueColor = Color(0xFF059669)
                                )
                            }

                            // Bill Discount (only if enabled & > 0)
                            if (isBillDiscountApplied && billDiscountAmount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val label = if (billDiscountType == BillDiscountType.PERCENTAGE) {
                                    "Bill Discount (${billDiscountInputText}%)"
                                } else {
                                    "Bill Discount"
                                }
                                SummaryRow(
                                    label = label,
                                    value = "- Rs. ${currencyFmt.format(billDiscountAmount)}",
                                    labelColor = colors.textSecondary,
                                    valueColor = Color(0xFF059669)
                                )
                            }

                            // Taxable Base (shown if VAT is active or discounts exist)
                            if (isVatApplied || (isBillDiscountApplied && billDiscountAmount > 0) || itemDiscountTotal > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                SummaryRow(
                                    label = "Taxable Base",
                                    value = "Rs. ${currencyFmt.format(taxableAmount)}",
                                    labelColor = colors.textSecondary,
                                    valueColor = colors.textPrimary
                                )
                            }

                            // VAT (only if active)
                            if (isVatApplied && vatAmount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                SummaryRow(
                                    label = "VAT (${effectiveVatRate.toInt()}%)",
                                    value = "+ Rs. ${currencyFmt.format(vatAmount)}",
                                    labelColor = colors.textSecondary,
                                    valueColor = colors.textPrimary
                                )
                            }

                            // Extra Charge (only if active & > 0)
                            if (isExtraChargeApplied && extraChargeAmount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val chargeLabel = extraChargeDescription.ifBlank { "Extra Charge" }
                                SummaryRow(
                                    label = chargeLabel,
                                    value = "+ Rs. ${currencyFmt.format(extraChargeAmount)}",
                                    labelColor = colors.textSecondary,
                                    valueColor = colors.textPrimary
                                )
                            }

                            // Round Off (only if active & != 0)
                            if (isRoundOffApplied && roundOff != 0.0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val sign = if (roundOff > 0) "+" else ""
                                SummaryRow(
                                    label = "Round Off",
                                    value = "$sign Rs. ${currencyFmt.format(roundOff)}",
                                    labelColor = colors.textSecondary,
                                    valueColor = if (roundOff >= 0) colors.primary else Color(0xFFDC2626)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = colors.cardBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grand Total
                            SummaryRow(
                                label = "Grand Total",
                                value = "Rs. ${currencyFmt.format(grandTotal)}",
                                labelColor = colors.textPrimary,
                                valueColor = colors.primary,
                                isBold = true,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // ---------------- 5. PAYMENT SECTION ----------------
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Payment",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Payment Mode Selector (Cash, Bank, Wallet, Credit)
                            Text(
                                text = "Payment Mode",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val modes = listOf("Cash", "Bank Transfer", "Digital Wallet", "Credit (Khata Due)")
                                modes.forEach { mode ->
                                    val isSelected = selectedPaymentMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) colors.primary else colors.inputBackground)
                                            .border(1.dp, if (isSelected) colors.primary else colors.inputBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedPaymentMode = mode
                                                if (mode == "Credit (Khata Due)") {
                                                    paidAmountText = "0"
                                                    isPaidUserEdited = true
                                                } else {
                                                    paidAmountText = ""
                                                    isPaidUserEdited = false
                                                }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (mode.contains("Credit")) "Credit" else mode.split(" ").first(),
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else colors.textPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Paid Amount & Due Amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Paid Amount (Rs.)",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = if (isPaidUserEdited) paidAmountText else currencyFmt.format(paidAmount),
                                        onValueChange = {
                                            paidAmountText = it
                                            isPaidUserEdited = true
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.inputBackground,
                                            unfocusedContainerColor = colors.inputBackground,
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.inputBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Due Amount",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val dueBoxBg = if (dueAmount > 0) {
                                        if (AppTheme.isDark) Color(0x33EF4444) else Color(0xFFFEF2F2)
                                    } else {
                                        if (AppTheme.isDark) Color(0x3310B981) else Color(0xFFECFDF5)
                                    }
                                    val dueBoxBorder = if (dueAmount > 0) {
                                        if (AppTheme.isDark) Color(0x66EF4444) else Color(0xFFFECACA)
                                    } else {
                                        if (AppTheme.isDark) Color(0x6610B981) else Color(0xFFA7F3D0)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(dueBoxBg)
                                            .border(1.dp, dueBoxBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "Rs. ${currencyFmt.format(dueAmount)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dueAmount > 0) Color(0xFFDC2626) else Color(0xFF059669)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------------- 6. ATTACHMENTS (IMAGES & PDFS) ----------------
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attachments_section_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Attachments (${attachments.size})",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // + Add Image
                                    OutlinedButton(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary),
                                        modifier = Modifier.height(34.dp).testTag("btn_add_image_attachment")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+ Image",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primary
                                        )
                                    }

                                    // + Add PDF
                                    OutlinedButton(
                                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                                        modifier = Modifier.height(34.dp).testTag("btn_add_pdf_attachment")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+ PDF",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (attachments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.inputBackground)
                                        .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                                        .padding(vertical = 14.dp, horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Attach bill photos, receipts, or PDF documents (optional)",
                                        fontSize = 12.sp,
                                        color = colors.textMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    attachments.forEach { attachment ->
                                        AttachmentRowItem(
                                            attachment = attachment,
                                            onPreviewImage = { previewImageAttachment = attachment },
                                            onOpenPdf = { AttachmentUtils.openAttachment(context, attachment) },
                                            onRemove = { attachments = attachments - attachment }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------------- 7. MORE DETAILS (COLLAPSIBLE) ----------------
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isMoreDetailsExpanded = !isMoreDetailsExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isMoreDetailsExpanded) "－ Less Details" else "＋ More Details",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                                Icon(
                                    imageVector = if (isMoreDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = isMoreDetailsExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = referenceNumber,
                                        onValueChange = { referenceNumber = it },
                                        label = { Text("Reference / PO Number", fontSize = 12.sp, color = colors.textSecondary) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.inputBackground,
                                            unfocusedContainerColor = colors.inputBackground,
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.inputBorder
                                        )
                                    )

                                    OutlinedTextField(
                                        value = salesperson,
                                        onValueChange = { salesperson = it },
                                        label = { Text("Salesperson", fontSize = 12.sp, color = colors.textSecondary) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.inputBackground,
                                            unfocusedContainerColor = colors.inputBackground,
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.inputBorder
                                        )
                                    )

                                    OutlinedTextField(
                                        value = servicePerson,
                                        onValueChange = { servicePerson = it },
                                        label = { Text("Service Person / Technician", fontSize = 12.sp, color = colors.textSecondary) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.inputBackground,
                                            unfocusedContainerColor = colors.inputBackground,
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.inputBorder
                                        )
                                    )

                                    OutlinedTextField(
                                        value = remarks,
                                        onValueChange = { remarks = it },
                                        label = { Text("Remarks / Notes", fontSize = 12.sp, color = colors.textSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.inputBackground,
                                            unfocusedContainerColor = colors.inputBackground,
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.inputBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(10.dp)) }
            }

            // ---------------- 7. SUMMARY & SAVE ACTIONS ----------------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Rs. ${currencyFmt.format(grandTotal)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (dueAmount > 0) "Due: Rs. ${currencyFmt.format(dueAmount)}" else "Fully Paid",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (dueAmount > 0) Color(0xFFDC2626) else Color(0xFF059669)
                            )
                            Text(
                                text = "Paid: Rs. ${currencyFmt.format(paidAmount)}",
                                fontSize = 11.5.sp,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons: [ Save & New ]  [ Save Sale ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { performSave(isSaveAndNew = true) },
                            enabled = !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_save_and_new"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.primary)
                        ) {
                            Text(
                                text = "Save & New",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        Button(
                            onClick = { performSave(isSaveAndNew = false) },
                            enabled = !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_save_sale"),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save Sale",
                                    fontSize = 14.sp,
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

    // ---------------- ADD ITEM DIALOG ----------------
    if (showAddItemDialog) {
        AddItemSelectionDialog(
            inventoryItems = inventoryItems,
            isDiscountEnabled = isDiscountEnabled,
            onItemAdded = { newItem ->
                lineItems.add(newItem)
                itemsError = null
                showAddItemDialog = false
            },
            onDismiss = { showAddItemDialog = false }
        )
    }

    // ---------------- ADD CUSTOMER SHEET (EXISTING PARTIES FORM) ----------------
    if (showAddCustomerSheet) {
        AddPartySheet(
            initialType = "Customer",
            onSaveParty = { newParty ->
                onQuickAddParty?.invoke(newParty)
                selectedParty = newParty
                customerError = null
                showAddCustomerSheet = false
                Toast.makeText(context, "Customer '${newParty.name}' selected", Toast.LENGTH_SHORT).show()
            },
            onCancel = { showAddCustomerSheet = false }
        )
    }

    // ---------------- DATE PICKER DIALOG (DUAL DATE SELECTOR) ----------------
    if (showDatePickerDialog) {
        Dialog(onDismissRequest = { showDatePickerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Invoice Date",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        IconButton(
                            onClick = { showDatePickerDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DualDateSelector(
                        initialDateMillis = selectedDateMillis,
                        onDateChanged = { millis, bs, ad ->
                            selectedDateMillis = millis
                            selectedDateBs = bs
                            selectedDateAd = ad
                            dateError = null
                            showDatePickerDialog = false
                        }
                    )
                }
            }
        }
    }

    // ---------------- ATTACHMENT IMAGE PREVIEW DIALOG ----------------
    if (previewImageAttachment != null) {
        val att = previewImageAttachment!!
        Dialog(onDismissRequest = { previewImageAttachment = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = colors.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = att.fileName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(
                                onClick = { AttachmentUtils.shareAttachment(context, att) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { previewImageAttachment = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val file = File(att.uriString)
                        AsyncImage(
                            model = file,
                            contentDescription = att.fileName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AttachmentUtils.formatFileSize(att.fileSize),
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        OutlinedButton(
                            onClick = { previewImageAttachment = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Close", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern row item for displaying attached images and PDF documents
 */
@Composable
private fun AttachmentRowItem(
    attachment: InvoiceAttachmentEntity,
    onPreviewImage: () -> Unit,
    onOpenPdf: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = AppTheme.colors
    val isImage = attachment.fileType == "IMAGE"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.inputBackground)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .clickable {
                if (isImage) onPreviewImage() else onOpenPdf()
            }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isImage) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.cardBorder),
                    contentAlignment = Alignment.Center
                ) {
                    val file = File(attachment.uriString)
                    AsyncImage(
                        model = file,
                        contentDescription = attachment.fileName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.fileName,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isImage) "Image" else "PDF Document",
                        fontSize = 10.5.sp,
                        color = if (isImage) colors.primary else Color(0xFFDC2626),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " • ${AttachmentUtils.formatFileSize(attachment.fileSize)}",
                        fontSize = 10.5.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (isImage) onPreviewImage() else onOpenPdf()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "View",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Compact line item card
 */
@Composable
private fun ItemRowCard(
    item: InvoiceLineItem,
    isDiscountEnabled: Boolean,
    onQuantityChanged: (Double) -> Unit,
    onRemove: () -> Unit
) {
    val colors = AppTheme.colors
    val currencyFmt = remember { DecimalFormat("#,##,##0.00") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.inputBackground),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.sku.isNotBlank()) {
                        Text(
                            text = "SKU: ${item.sku} • ${item.unit}",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove Item",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Qty Stepper | Rate | Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                ) {
                    IconButton(
                        onClick = { onQuantityChanged(item.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChanged(item.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Rate
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Rate",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "Rs. ${currencyFmt.format(item.unitPrice)}",
                        fontSize = 12.sp,
                        color = colors.textPrimary
                    )
                }

                // Subtotal
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "Rs. ${currencyFmt.format(item.subtotal)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
            }

            if (isDiscountEnabled && (item.discountPercent > 0 || item.discount > 0)) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discount: ${if (item.discountPercent > 0) "${item.discountPercent}%" else "Rs. ${item.discount}"}",
                    fontSize = 10.5.sp,
                    color = Color(0xFF059669)
                )
            }
        }
    }
}

/**
 * Clean dialog for adding an item from inventory or typing a custom item
 */
@Composable
private fun AddItemSelectionDialog(
    inventoryItems: List<InventoryItemEntity>,
    isDiscountEnabled: Boolean,
    onItemAdded: (InvoiceLineItem) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<InventoryItemEntity?>(null) }

    // Item fields
    var customName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var rateText by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0") }

    val filteredItems = remember(searchQuery, inventoryItems) {
        if (searchQuery.isBlank()) inventoryItems
        else inventoryItems.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Item",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedItem == null) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search inventory...", fontSize = 12.sp, color = colors.textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedContainerColor = colors.inputBackground,
                            unfocusedContainerColor = colors.inputBackground,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.inputBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (inventoryItems.isEmpty()) {
                        // Empty inventory state with quick custom item creation
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Items Available",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Add an inventory item or enter custom details below.",
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text("Item Name", fontSize = 12.sp, color = colors.textSecondary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedContainerColor = colors.inputBackground,
                                    unfocusedContainerColor = colors.inputBackground,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.inputBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = { quantityText = it },
                                    label = { Text("Qty", fontSize = 12.sp, color = colors.textSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary,
                                        focusedContainerColor = colors.inputBackground,
                                        unfocusedContainerColor = colors.inputBackground,
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.inputBorder
                                    )
                                )
                                OutlinedTextField(
                                    value = rateText,
                                    onValueChange = { rateText = it },
                                    label = { Text("Rate (Rs.)", fontSize = 12.sp, color = colors.textSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary,
                                        focusedContainerColor = colors.inputBackground,
                                        unfocusedContainerColor = colors.inputBackground,
                                        focusedBorderColor = colors.primary,
                                        unfocusedBorderColor = colors.inputBorder
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val name = customName.ifBlank { "Custom Item" }
                                    val qty = quantityText.toDoubleOrNull() ?: 1.0
                                    val rate = rateText.toDoubleOrNull() ?: 0.0
                                    onItemAdded(
                                        InvoiceLineItem(
                                            name = name,
                                            unit = "pcs",
                                            quantity = qty,
                                            unitPrice = rate
                                        )
                                    )
                                },
                                enabled = customName.isNotBlank() && (rateText.toDoubleOrNull() ?: 0.0) >= 0,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Custom Item", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Inventory list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredItems) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.inputBackground)
                                        .border(0.5.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedItem = item
                                            rateText = item.salePrice.toString()
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                        Text("Stock: ${item.stockQuantity} ${item.unit} • SKU: ${item.sku}", fontSize = 11.sp, color = colors.textSecondary)
                                    }
                                    Text("Rs. ${item.salePrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                }
                            }
                        }
                    }
                } else {
                    // Selected item details form
                    val item = selectedItem!!
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Stock Available: ${item.stockQuantity} ${item.unit}",
                        fontSize = 11.5.sp,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity", fontSize = 12.sp, color = colors.textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedContainerColor = colors.inputBackground,
                                unfocusedContainerColor = colors.inputBackground,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.inputBorder
                            )
                        )

                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { rateText = it },
                            label = { Text("Rate (Rs.)", fontSize = 12.sp, color = colors.textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedContainerColor = colors.inputBackground,
                                unfocusedContainerColor = colors.inputBackground,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.inputBorder
                            )
                        )
                    }

                    if (isDiscountEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            label = { Text("Discount (%)", fontSize = 12.sp, color = colors.textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedContainerColor = colors.inputBackground,
                                unfocusedContainerColor = colors.inputBackground,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.inputBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { selectedItem = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Back", color = colors.textPrimary)
                        }

                        Button(
                            onClick = {
                                val qty = quantityText.toDoubleOrNull() ?: 1.0
                                val rate = rateText.toDoubleOrNull() ?: item.salePrice
                                val disc = discountText.toDoubleOrNull() ?: 0.0
                                onItemAdded(
                                    InvoiceLineItem(
                                        itemId = item.id,
                                        name = item.name,
                                        sku = item.sku,
                                        unit = item.unit,
                                        quantity = qty,
                                        unitPrice = rate,
                                        discountPercent = disc,
                                        stockAvailable = item.stockQuantity
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add to Sale", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick Add Customer Dialog
 */
@Composable
private fun QuickAddCustomerDialog(
    onSave: (PartyEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Customer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *", fontSize = 12.sp, color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.inputBackground,
                        unfocusedContainerColor = colors.inputBackground,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.inputBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", fontSize = 12.sp, color = colors.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.inputBackground,
                        unfocusedContainerColor = colors.inputBackground,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.inputBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / City", fontSize = 12.sp, color = colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.inputBackground,
                        unfocusedContainerColor = colors.inputBackground,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.inputBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val party = PartyEntity(
                            name = name.trim(),
                            type = "Customer",
                            phone = phone.trim(),
                            address = address.trim(),
                            panVatNumber = pan.trim(),
                            registerDate = System.currentTimeMillis()
                        )
                        onSave(party)
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Customer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Clean key-value summary row
 */
@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = labelColor
        )
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Clean, modern chip button for enabling/disabling optional sales adjustments
 */
@Composable
private fun AdjustmentPillChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) colors.primary.copy(alpha = 0.12f) else colors.inputBackground,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isActive) colors.primary else colors.inputBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                tint = if (isActive) colors.primary else colors.textSecondary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) colors.primary else colors.textPrimary
            )
        }
    }
}

