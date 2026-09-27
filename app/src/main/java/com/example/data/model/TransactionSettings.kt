package com.example.data.model

import org.json.JSONObject

/**
 * Atri Khata 2 - Business Transaction Presets
 */
enum class BusinessTransactionPreset(
    val id: String,
    val displayName: String,
    val description: String
) {
    RETAIL(
        id = "retail",
        displayName = "Retail & POS Store",
        description = "Counter sales, walk-in customers, instant receipts, fast cash/QR"
    ),
    WHOLESALE(
        id = "wholesale",
        displayName = "Wholesale & Distribution",
        description = "B2B credit sales, party ledger balances, due dates, purchase orders"
    ),
    PHARMACY(
        id = "pharmacy",
        displayName = "Pharmacy & Clinic",
        description = "Patient/doctor references, strict VAT invoicing, expiry & batch tracking"
    ),
    SERVICE(
        id = "service",
        displayName = "Service Business",
        description = "Service billing, hourly/fixed charges, no physical stock reduction"
    ),
    DISTRIBUTOR(
        id = "distributor",
        displayName = "Distributor & Agency",
        description = "Salesperson tracking, credit limits, multi-party receivables, stock links"
    ),
    PERSONAL(
        id = "personal",
        displayName = "Personal Khata",
        description = "Personal finance, income, expense, money transfers, personal loans"
    ),
    GENERAL(
        id = "general",
        displayName = "General Trading",
        description = "Standard balanced invoicing, payments, expenses, and ledgers"
    );

    companion object {
        fun fromBusinessType(businessType: String): BusinessTransactionPreset {
            val lower = businessType.lowercase()
            return when {
                lower.contains("personal") -> PERSONAL
                lower.contains("pharmacy") || lower.contains("medical") || lower.contains("clinic") -> PHARMACY
                lower.contains("wholesale") || lower.contains("bulk") -> WHOLESALE
                lower.contains("service") || lower.contains("consult") || lower.contains("repair") -> SERVICE
                lower.contains("distribut") || lower.contains("agency") -> DISTRIBUTOR
                lower.contains("retail") || lower.contains("grocery") || lower.contains("shop") -> RETAIL
                else -> GENERAL
            }
        }

        fun fromId(id: String): BusinessTransactionPreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        }
    }
}

/**
 * Transaction Permissions Model for Staff & Role-Based Access Control
 */
data class TransactionPermissions(
    val canViewTransactions: Boolean = true,
    val canCreateSales: Boolean = true,
    val canEditSales: Boolean = true,
    val canDeleteSales: Boolean = false,
    val canCancelSales: Boolean = true,
    val canCreatePurchase: Boolean = true,
    val canEditPurchase: Boolean = true,
    val canDeletePurchase: Boolean = false,
    val canCreatePaymentIn: Boolean = true,
    val canCreatePaymentOut: Boolean = true,
    val canCreateExpense: Boolean = true,
    val canViewProfit: Boolean = false,
    val canModifySettings: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("canViewTransactions", canViewTransactions)
            put("canCreateSales", canCreateSales)
            put("canEditSales", canEditSales)
            put("canDeleteSales", canDeleteSales)
            put("canCancelSales", canCancelSales)
            put("canCreatePurchase", canCreatePurchase)
            put("canEditPurchase", canEditPurchase)
            put("canDeletePurchase", canDeletePurchase)
            put("canCreatePaymentIn", canCreatePaymentIn)
            put("canCreatePaymentOut", canCreatePaymentOut)
            put("canCreateExpense", canCreateExpense)
            put("canViewProfit", canViewProfit)
            put("canModifySettings", canModifySettings)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): TransactionPermissions {
            if (json == null) return TransactionPermissions()
            return TransactionPermissions(
                canViewTransactions = json.optBoolean("canViewTransactions", true),
                canCreateSales = json.optBoolean("canCreateSales", true),
                canEditSales = json.optBoolean("canEditSales", true),
                canDeleteSales = json.optBoolean("canDeleteSales", false),
                canCancelSales = json.optBoolean("canCancelSales", true),
                canCreatePurchase = json.optBoolean("canCreatePurchase", true),
                canEditPurchase = json.optBoolean("canEditPurchase", true),
                canDeletePurchase = json.optBoolean("canDeletePurchase", false),
                canCreatePaymentIn = json.optBoolean("canCreatePaymentIn", true),
                canCreatePaymentOut = json.optBoolean("canCreatePaymentOut", true),
                canCreateExpense = json.optBoolean("canCreateExpense", true),
                canViewProfit = json.optBoolean("canViewProfit", false),
                canModifySettings = json.optBoolean("canModifySettings", false)
            )
        }
    }
}

/**
 * Atri Khata 2 - Advanced Transaction Settings & Configuration
 * Safe, persistent, business-preset aware, and future-ready.
 */
data class TransactionSettings(
    val businessPreset: BusinessTransactionPreset = BusinessTransactionPreset.GENERAL,

    // 1. General Transaction Settings
    val enableTransactions: Boolean = true,
    val autoSaveDraft: Boolean = true,
    val confirmBeforeDelete: Boolean = true,
    val confirmBeforeCancel: Boolean = true,
    val allowEditSavedTransaction: Boolean = true,
    val allowDeleteSavedTransaction: Boolean = true,
    val allowDuplicateTransaction: Boolean = true,
    val showTransactionConfirmation: Boolean = true,
    val defaultTransactionDateToday: Boolean = true,
    val autoFillLastUsedValues: Boolean = true,
    val rememberLastPaymentMode: Boolean = true,
    val rememberLastParty: Boolean = false,
    val rememberLastAccount: Boolean = true,
    val rememberLastSalesperson: Boolean = true,

    // 2. Transaction Types Enabled
    val salesEnabled: Boolean = true,
    val purchaseEnabled: Boolean = true,
    val paymentInEnabled: Boolean = true,
    val paymentOutEnabled: Boolean = true,
    val expenseEnabled: Boolean = true,
    val journalEnabled: Boolean = true,
    val adjustmentEnabled: Boolean = true,
    val showInQuickActions: Boolean = true,

    // 3. Sales Settings
    val showCustomerParty: Boolean = true,
    val showProductItem: Boolean = true,
    val showQuantity: Boolean = true,
    val showRate: Boolean = true,
    val showDiscount: Boolean = true,
    val showVat: Boolean = true,
    val showPaymentMode: Boolean = true,
    val showPaymentIn: Boolean = true,
    val showCreditSale: Boolean = true,
    val showDueDate: Boolean = true,
    val showSalesperson: Boolean = true,
    val showServicePerson: Boolean = false,
    val showReferenceNumber: Boolean = true,
    val showRemarks: Boolean = true,
    val showAttachment: Boolean = true,
    val showRoundOff: Boolean = true,
    val defaultPaymentMode: String = "Cash",
    val defaultDiscountPercent: Double = 0.0,
    val defaultVatRate: Double = 13.0,
    val defaultCreditDays: Int = 15,

    // 4. Purchase Settings
    val showSupplier: Boolean = true,
    val showPurchaseQuantity: Boolean = true,
    val showPurchaseRate: Boolean = true,
    val showPurchaseDiscount: Boolean = true,
    val showPurchaseVat: Boolean = true,
    val showPurchasePaymentMode: Boolean = true,
    val showCreditPurchase: Boolean = true,
    val showPurchaseDueDate: Boolean = true,
    val showPurchaseReference: Boolean = true,
    val showPurchaseRemarks: Boolean = true,
    val showPurchaseRoundOff: Boolean = true,

    // 5. Payment Settings
    val paymentInPartyRequired: Boolean = true,
    val paymentInShowAccount: Boolean = true,
    val paymentInShowReference: Boolean = true,
    val paymentInShowRemarks: Boolean = true,
    val paymentInEnableAllocation: Boolean = true,
    val paymentInEnablePartialPayment: Boolean = true,
    val paymentOutPartyRequired: Boolean = true,
    val paymentOutShowAccount: Boolean = true,
    val paymentOutShowReference: Boolean = true,
    val paymentOutShowRemarks: Boolean = true,
    val paymentOutEnableAllocation: Boolean = true,
    val paymentOutEnablePartialPayment: Boolean = true,

    // 6. Expense Settings
    val showExpenseCategory: Boolean = true,
    val showPaidFromAccount: Boolean = true,
    val showExpensePaymentMode: Boolean = true,
    val showExpenseVendor: Boolean = true,
    val showExpenseReference: Boolean = true,
    val showExpenseRemarks: Boolean = true,
    val showExpenseAttachment: Boolean = true,
    val allowRecurringExpense: Boolean = false,

    // 7. Invoice & Numbering
    val autoGenerateInvoiceNumber: Boolean = true,
    val salesInvoicePrefix: String = "INV-",
    val salesStartingNumber: Int = 1001,
    val salesNumberLength: Int = 4,
    val includeFiscalYearInInvoice: Boolean = true,
    val resetNumberFiscalYear: Boolean = true,
    val autoGeneratePurchaseNumber: Boolean = true,
    val purchasePrefix: String = "PUR-",
    val purchaseStartingNumber: Int = 101,
    val paymentInPrefix: String = "REC-",
    val paymentOutPrefix: String = "PAY-",
    val expensePrefix: String = "EXP-",

    // 8. Customer / Party Settings
    val customerRequired: Boolean = false,
    val allowWalkInCustomer: Boolean = true,
    val showPartyPhone: Boolean = true,
    val showPartyAddress: Boolean = true,
    val showCreditLimit: Boolean = true,
    val showOutstandingBalance: Boolean = true,
    val showPreviousBalance: Boolean = true,
    val showPaymentTerms: Boolean = true,
    val showPartyNotes: Boolean = false,
    val supplierRequired: Boolean = true,
    val showSupplierOutstanding: Boolean = true,

    // 9. Calculation & Tax Settings
    val showSubtotal: Boolean = true,
    val showDiscountCalculation: Boolean = true,
    val showTaxableAmount: Boolean = true,
    val showVatCalculation: Boolean = true,
    val showOtherCharges: Boolean = false,
    val showRoundOffCalculation: Boolean = true,
    val showGrandTotal: Boolean = true,
    val enableVat: Boolean = true,
    val showVatBreakdown: Boolean = true,

    // 10. Date & Due Date Settings
    val useCurrentDateDefault: Boolean = true,
    val allowBackdatedTransaction: Boolean = true,
    val allowFutureDatedTransaction: Boolean = false,
    val defaultCreditPeriodDays: Int = 30,
    val allowCustomDueDate: Boolean = true,
    val showTransactionTime: Boolean = true,

    // 11. Remarks & Reference Settings
    val referenceRequired: Boolean = false,
    val remarksRequired: Boolean = false,
    val showInternalNote: Boolean = false,
    val showCustomerNote: Boolean = true,

    // 12. Attachment Settings
    val enableAttachments: Boolean = true,
    val allowImageAttachment: Boolean = true,
    val allowPdfAttachment: Boolean = true,
    val allowMultipleAttachments: Boolean = false,
    val showAttachmentInDetails: Boolean = true,

    // 13. Stock Integration
    val decreaseStockOnSale: Boolean = true,
    val allowSaleWithoutStock: Boolean = false,
    val showAvailableStockInForm: Boolean = true,
    val increaseStockOnPurchase: Boolean = true,
    val updateStockCostOnPurchase: Boolean = true,

    // 14. Accounting Integration
    val updateReceivableOnSale: Boolean = true,
    val updatePayableOnPurchase: Boolean = true,
    val updateCashBankOnPayment: Boolean = true,
    val updatePartyBalanceOnPayment: Boolean = true,
    val updateExpenseOnExpenseEntry: Boolean = true,

    // 15. Transaction Display Settings
    val showTransactionType: Boolean = true,
    val showTransactionNumber: Boolean = true,
    val showPartyName: Boolean = true,
    val showAmount: Boolean = true,
    val showPaidAmount: Boolean = true,
    val showDueAmount: Boolean = true,
    val showDate: Boolean = true,
    val showPaymentModeInList: Boolean = true,
    val showStatus: Boolean = true,
    val showReferenceInList: Boolean = true,
    val showRemarksInList: Boolean = false,
    val listDensity: String = "Comfortable", // "Comfortable", "Compact"

    // 16. Transaction History Settings
    val showRecentTransactions: Boolean = true,
    val numberOfRecentTransactions: Int = 20,
    val showCancelledTransactions: Boolean = false,
    val showDraftTransactions: Boolean = true,
    val showAuditInfo: Boolean = false,

    // 17. Permissions
    val permissions: TransactionPermissions = TransactionPermissions(),

    // 18. Advanced Settings
    val requireConfirmBeforeDelete: Boolean = true,
    val requireReasonBeforeCancel: Boolean = true,
    val requireReasonBeforeEditOld: Boolean = false,
    val allowTransactionDateOverride: Boolean = true,
    val allowNegativePartyBalance: Boolean = true,
    val allowDuplicateInvoiceNumber: Boolean = false,
    val enableTransactionAudit: Boolean = true,
    val enableActivityTimeline: Boolean = true,
    val lockTransactionAfterDays: Int = 0 // 0 = disabled
) {

    fun toJsonString(): String {
        val root = JSONObject()
        root.put("businessPreset", businessPreset.id)

        // 1. General
        root.put("enableTransactions", enableTransactions)
        root.put("autoSaveDraft", autoSaveDraft)
        root.put("confirmBeforeDelete", confirmBeforeDelete)
        root.put("confirmBeforeCancel", confirmBeforeCancel)
        root.put("allowEditSavedTransaction", allowEditSavedTransaction)
        root.put("allowDeleteSavedTransaction", allowDeleteSavedTransaction)
        root.put("allowDuplicateTransaction", allowDuplicateTransaction)
        root.put("showTransactionConfirmation", showTransactionConfirmation)
        root.put("defaultTransactionDateToday", defaultTransactionDateToday)
        root.put("autoFillLastUsedValues", autoFillLastUsedValues)
        root.put("rememberLastPaymentMode", rememberLastPaymentMode)
        root.put("rememberLastParty", rememberLastParty)
        root.put("rememberLastAccount", rememberLastAccount)
        root.put("rememberLastSalesperson", rememberLastSalesperson)

        // 2. Types
        root.put("salesEnabled", salesEnabled)
        root.put("purchaseEnabled", purchaseEnabled)
        root.put("paymentInEnabled", paymentInEnabled)
        root.put("paymentOutEnabled", paymentOutEnabled)
        root.put("expenseEnabled", expenseEnabled)
        root.put("journalEnabled", journalEnabled)
        root.put("adjustmentEnabled", adjustmentEnabled)
        root.put("showInQuickActions", showInQuickActions)

        // 3. Sales
        root.put("showCustomerParty", showCustomerParty)
        root.put("showProductItem", showProductItem)
        root.put("showQuantity", showQuantity)
        root.put("showRate", showRate)
        root.put("showDiscount", showDiscount)
        root.put("showVat", showVat)
        root.put("showPaymentMode", showPaymentMode)
        root.put("showPaymentIn", showPaymentIn)
        root.put("showCreditSale", showCreditSale)
        root.put("showDueDate", showDueDate)
        root.put("showSalesperson", showSalesperson)
        root.put("showServicePerson", showServicePerson)
        root.put("showReferenceNumber", showReferenceNumber)
        root.put("showRemarks", showRemarks)
        root.put("showAttachment", showAttachment)
        root.put("showRoundOff", showRoundOff)
        root.put("defaultPaymentMode", defaultPaymentMode)
        root.put("defaultDiscountPercent", defaultDiscountPercent)
        root.put("defaultVatRate", defaultVatRate)
        root.put("defaultCreditDays", defaultCreditDays)

        // 4. Purchase
        root.put("showSupplier", showSupplier)
        root.put("showPurchaseQuantity", showPurchaseQuantity)
        root.put("showPurchaseRate", showPurchaseRate)
        root.put("showPurchaseDiscount", showPurchaseDiscount)
        root.put("showPurchaseVat", showPurchaseVat)
        root.put("showPurchasePaymentMode", showPurchasePaymentMode)
        root.put("showCreditPurchase", showCreditPurchase)
        root.put("showPurchaseDueDate", showPurchaseDueDate)
        root.put("showPurchaseReference", showPurchaseReference)
        root.put("showPurchaseRemarks", showPurchaseRemarks)
        root.put("showPurchaseRoundOff", showPurchaseRoundOff)

        // 5. Payment
        root.put("paymentInPartyRequired", paymentInPartyRequired)
        root.put("paymentInShowAccount", paymentInShowAccount)
        root.put("paymentInShowReference", paymentInShowReference)
        root.put("paymentInShowRemarks", paymentInShowRemarks)
        root.put("paymentInEnableAllocation", paymentInEnableAllocation)
        root.put("paymentInEnablePartialPayment", paymentInEnablePartialPayment)
        root.put("paymentOutPartyRequired", paymentOutPartyRequired)
        root.put("paymentOutShowAccount", paymentOutShowAccount)
        root.put("paymentOutShowReference", paymentOutShowReference)
        root.put("paymentOutShowRemarks", paymentOutShowRemarks)
        root.put("paymentOutEnableAllocation", paymentOutEnableAllocation)
        root.put("paymentOutEnablePartialPayment", paymentOutEnablePartialPayment)

        // 6. Expense
        root.put("showExpenseCategory", showExpenseCategory)
        root.put("showPaidFromAccount", showPaidFromAccount)
        root.put("showExpensePaymentMode", showExpensePaymentMode)
        root.put("showExpenseVendor", showExpenseVendor)
        root.put("showExpenseReference", showExpenseReference)
        root.put("showExpenseRemarks", showExpenseRemarks)
        root.put("showExpenseAttachment", showExpenseAttachment)
        root.put("allowRecurringExpense", allowRecurringExpense)

        // 7. Numbering
        root.put("autoGenerateInvoiceNumber", autoGenerateInvoiceNumber)
        root.put("salesInvoicePrefix", salesInvoicePrefix)
        root.put("salesStartingNumber", salesStartingNumber)
        root.put("salesNumberLength", salesNumberLength)
        root.put("includeFiscalYearInInvoice", includeFiscalYearInInvoice)
        root.put("resetNumberFiscalYear", resetNumberFiscalYear)
        root.put("autoGeneratePurchaseNumber", autoGeneratePurchaseNumber)
        root.put("purchasePrefix", purchasePrefix)
        root.put("purchaseStartingNumber", purchaseStartingNumber)
        root.put("paymentInPrefix", paymentInPrefix)
        root.put("paymentOutPrefix", paymentOutPrefix)
        root.put("expensePrefix", expensePrefix)

        // 8. Party
        root.put("customerRequired", customerRequired)
        root.put("allowWalkInCustomer", allowWalkInCustomer)
        root.put("showPartyPhone", showPartyPhone)
        root.put("showPartyAddress", showPartyAddress)
        root.put("showCreditLimit", showCreditLimit)
        root.put("showOutstandingBalance", showOutstandingBalance)
        root.put("showPreviousBalance", showPreviousBalance)
        root.put("showPaymentTerms", showPaymentTerms)
        root.put("showPartyNotes", showPartyNotes)
        root.put("supplierRequired", supplierRequired)
        root.put("showSupplierOutstanding", showSupplierOutstanding)

        // 9. Calculation & Tax
        root.put("showSubtotal", showSubtotal)
        root.put("showDiscountCalculation", showDiscountCalculation)
        root.put("showTaxableAmount", showTaxableAmount)
        root.put("showVatCalculation", showVatCalculation)
        root.put("showOtherCharges", showOtherCharges)
        root.put("showRoundOffCalculation", showRoundOffCalculation)
        root.put("showGrandTotal", showGrandTotal)
        root.put("enableVat", enableVat)
        root.put("showVatBreakdown", showVatBreakdown)

        // 10. Date & Due Date
        root.put("useCurrentDateDefault", useCurrentDateDefault)
        root.put("allowBackdatedTransaction", allowBackdatedTransaction)
        root.put("allowFutureDatedTransaction", allowFutureDatedTransaction)
        root.put("defaultCreditPeriodDays", defaultCreditPeriodDays)
        root.put("allowCustomDueDate", allowCustomDueDate)
        root.put("showTransactionTime", showTransactionTime)

        // 11. Remarks
        root.put("referenceRequired", referenceRequired)
        root.put("remarksRequired", remarksRequired)
        root.put("showInternalNote", showInternalNote)
        root.put("showCustomerNote", showCustomerNote)

        // 12. Attachments
        root.put("enableAttachments", enableAttachments)
        root.put("allowImageAttachment", allowImageAttachment)
        root.put("allowPdfAttachment", allowPdfAttachment)
        root.put("allowMultipleAttachments", allowMultipleAttachments)
        root.put("showAttachmentInDetails", showAttachmentInDetails)

        // 13. Stock
        root.put("decreaseStockOnSale", decreaseStockOnSale)
        root.put("allowSaleWithoutStock", allowSaleWithoutStock)
        root.put("showAvailableStockInForm", showAvailableStockInForm)
        root.put("increaseStockOnPurchase", increaseStockOnPurchase)
        root.put("updateStockCostOnPurchase", updateStockCostOnPurchase)

        // 14. Accounting
        root.put("updateReceivableOnSale", updateReceivableOnSale)
        root.put("updatePayableOnPurchase", updatePayableOnPurchase)
        root.put("updateCashBankOnPayment", updateCashBankOnPayment)
        root.put("updatePartyBalanceOnPayment", updatePartyBalanceOnPayment)
        root.put("updateExpenseOnExpenseEntry", updateExpenseOnExpenseEntry)

        // 15. Display
        root.put("showTransactionType", showTransactionType)
        root.put("showTransactionNumber", showTransactionNumber)
        root.put("showPartyName", showPartyName)
        root.put("showAmount", showAmount)
        root.put("showPaidAmount", showPaidAmount)
        root.put("showDueAmount", showDueAmount)
        root.put("showDate", showDate)
        root.put("showPaymentModeInList", showPaymentModeInList)
        root.put("showStatus", showStatus)
        root.put("showReferenceInList", showReferenceInList)
        root.put("showRemarksInList", showRemarksInList)
        root.put("listDensity", listDensity)

        // 16. History
        root.put("showRecentTransactions", showRecentTransactions)
        root.put("numberOfRecentTransactions", numberOfRecentTransactions)
        root.put("showCancelledTransactions", showCancelledTransactions)
        root.put("showDraftTransactions", showDraftTransactions)
        root.put("showAuditInfo", showAuditInfo)

        // 17. Permissions
        root.put("permissions", permissions.toJsonObject())

        // 18. Advanced
        root.put("requireConfirmBeforeDelete", requireConfirmBeforeDelete)
        root.put("requireReasonBeforeCancel", requireReasonBeforeCancel)
        root.put("requireReasonBeforeEditOld", requireReasonBeforeEditOld)
        root.put("allowTransactionDateOverride", allowTransactionDateOverride)
        root.put("allowNegativePartyBalance", allowNegativePartyBalance)
        root.put("allowDuplicateInvoiceNumber", allowDuplicateInvoiceNumber)
        root.put("enableTransactionAudit", enableTransactionAudit)
        root.put("enableActivityTimeline", enableActivityTimeline)
        root.put("lockTransactionAfterDays", lockTransactionAfterDays)

        return root.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String?): TransactionSettings? {
            if (jsonStr.isNullOrBlank()) return null
            return try {
                val root = JSONObject(jsonStr)
                val preset = BusinessTransactionPreset.fromId(root.optString("businessPreset", "general"))

                TransactionSettings(
                    businessPreset = preset,

                    // 1. General
                    enableTransactions = root.optBoolean("enableTransactions", true),
                    autoSaveDraft = root.optBoolean("autoSaveDraft", true),
                    confirmBeforeDelete = root.optBoolean("confirmBeforeDelete", true),
                    confirmBeforeCancel = root.optBoolean("confirmBeforeCancel", true),
                    allowEditSavedTransaction = root.optBoolean("allowEditSavedTransaction", true),
                    allowDeleteSavedTransaction = root.optBoolean("allowDeleteSavedTransaction", true),
                    allowDuplicateTransaction = root.optBoolean("allowDuplicateTransaction", true),
                    showTransactionConfirmation = root.optBoolean("showTransactionConfirmation", true),
                    defaultTransactionDateToday = root.optBoolean("defaultTransactionDateToday", true),
                    autoFillLastUsedValues = root.optBoolean("autoFillLastUsedValues", true),
                    rememberLastPaymentMode = root.optBoolean("rememberLastPaymentMode", true),
                    rememberLastParty = root.optBoolean("rememberLastParty", false),
                    rememberLastAccount = root.optBoolean("rememberLastAccount", true),
                    rememberLastSalesperson = root.optBoolean("rememberLastSalesperson", true),

                    // 2. Types
                    salesEnabled = root.optBoolean("salesEnabled", true),
                    purchaseEnabled = root.optBoolean("purchaseEnabled", true),
                    paymentInEnabled = root.optBoolean("paymentInEnabled", true),
                    paymentOutEnabled = root.optBoolean("paymentOutEnabled", true),
                    expenseEnabled = root.optBoolean("expenseEnabled", true),
                    journalEnabled = root.optBoolean("journalEnabled", true),
                    adjustmentEnabled = root.optBoolean("adjustmentEnabled", true),
                    showInQuickActions = root.optBoolean("showInQuickActions", true),

                    // 3. Sales
                    showCustomerParty = root.optBoolean("showCustomerParty", true),
                    showProductItem = root.optBoolean("showProductItem", true),
                    showQuantity = root.optBoolean("showQuantity", true),
                    showRate = root.optBoolean("showRate", true),
                    showDiscount = root.optBoolean("showDiscount", true),
                    showVat = root.optBoolean("showVat", true),
                    showPaymentMode = root.optBoolean("showPaymentMode", true),
                    showPaymentIn = root.optBoolean("showPaymentIn", true),
                    showCreditSale = root.optBoolean("showCreditSale", true),
                    showDueDate = root.optBoolean("showDueDate", true),
                    showSalesperson = root.optBoolean("showSalesperson", true),
                    showServicePerson = root.optBoolean("showServicePerson", false),
                    showReferenceNumber = root.optBoolean("showReferenceNumber", true),
                    showRemarks = root.optBoolean("showRemarks", true),
                    showAttachment = root.optBoolean("showAttachment", true),
                    showRoundOff = root.optBoolean("showRoundOff", true),
                    defaultPaymentMode = root.optString("defaultPaymentMode", "Cash"),
                    defaultDiscountPercent = root.optDouble("defaultDiscountPercent", 0.0),
                    defaultVatRate = root.optDouble("defaultVatRate", 13.0),
                    defaultCreditDays = root.optInt("defaultCreditDays", 15),

                    // 4. Purchase
                    showSupplier = root.optBoolean("showSupplier", true),
                    showPurchaseQuantity = root.optBoolean("showPurchaseQuantity", true),
                    showPurchaseRate = root.optBoolean("showPurchaseRate", true),
                    showPurchaseDiscount = root.optBoolean("showPurchaseDiscount", true),
                    showPurchaseVat = root.optBoolean("showPurchaseVat", true),
                    showPurchasePaymentMode = root.optBoolean("showPurchasePaymentMode", true),
                    showCreditPurchase = root.optBoolean("showCreditPurchase", true),
                    showPurchaseDueDate = root.optBoolean("showPurchaseDueDate", true),
                    showPurchaseReference = root.optBoolean("showPurchaseReference", true),
                    showPurchaseRemarks = root.optBoolean("showPurchaseRemarks", true),
                    showPurchaseRoundOff = root.optBoolean("showPurchaseRoundOff", true),

                    // 5. Payment
                    paymentInPartyRequired = root.optBoolean("paymentInPartyRequired", true),
                    paymentInShowAccount = root.optBoolean("paymentInShowAccount", true),
                    paymentInShowReference = root.optBoolean("paymentInShowReference", true),
                    paymentInShowRemarks = root.optBoolean("paymentInShowRemarks", true),
                    paymentInEnableAllocation = root.optBoolean("paymentInEnableAllocation", true),
                    paymentInEnablePartialPayment = root.optBoolean("paymentInEnablePartialPayment", true),
                    paymentOutPartyRequired = root.optBoolean("paymentOutPartyRequired", true),
                    paymentOutShowAccount = root.optBoolean("paymentOutShowAccount", true),
                    paymentOutShowReference = root.optBoolean("paymentOutShowReference", true),
                    paymentOutShowRemarks = root.optBoolean("paymentOutShowRemarks", true),
                    paymentOutEnableAllocation = root.optBoolean("paymentOutEnableAllocation", true),
                    paymentOutEnablePartialPayment = root.optBoolean("paymentOutEnablePartialPayment", true),

                    // 6. Expense
                    showExpenseCategory = root.optBoolean("showExpenseCategory", true),
                    showPaidFromAccount = root.optBoolean("showPaidFromAccount", true),
                    showExpensePaymentMode = root.optBoolean("showExpensePaymentMode", true),
                    showExpenseVendor = root.optBoolean("showExpenseVendor", true),
                    showExpenseReference = root.optBoolean("showExpenseReference", true),
                    showExpenseRemarks = root.optBoolean("showExpenseRemarks", true),
                    showExpenseAttachment = root.optBoolean("showExpenseAttachment", true),
                    allowRecurringExpense = root.optBoolean("allowRecurringExpense", false),

                    // 7. Numbering
                    autoGenerateInvoiceNumber = root.optBoolean("autoGenerateInvoiceNumber", true),
                    salesInvoicePrefix = root.optString("salesInvoicePrefix", "INV-"),
                    salesStartingNumber = root.optInt("salesStartingNumber", 1001),
                    salesNumberLength = root.optInt("salesNumberLength", 4),
                    includeFiscalYearInInvoice = root.optBoolean("includeFiscalYearInInvoice", true),
                    resetNumberFiscalYear = root.optBoolean("resetNumberFiscalYear", true),
                    autoGeneratePurchaseNumber = root.optBoolean("autoGeneratePurchaseNumber", true),
                    purchasePrefix = root.optString("purchasePrefix", "PUR-"),
                    purchaseStartingNumber = root.optInt("purchaseStartingNumber", 101),
                    paymentInPrefix = root.optString("paymentInPrefix", "REC-"),
                    paymentOutPrefix = root.optString("paymentOutPrefix", "PAY-"),
                    expensePrefix = root.optString("expensePrefix", "EXP-"),

                    // 8. Party
                    customerRequired = root.optBoolean("customerRequired", false),
                    allowWalkInCustomer = root.optBoolean("allowWalkInCustomer", true),
                    showPartyPhone = root.optBoolean("showPartyPhone", true),
                    showPartyAddress = root.optBoolean("showPartyAddress", true),
                    showCreditLimit = root.optBoolean("showCreditLimit", true),
                    showOutstandingBalance = root.optBoolean("showOutstandingBalance", true),
                    showPreviousBalance = root.optBoolean("showPreviousBalance", true),
                    showPaymentTerms = root.optBoolean("showPaymentTerms", true),
                    showPartyNotes = root.optBoolean("showPartyNotes", false),
                    supplierRequired = root.optBoolean("supplierRequired", true),
                    showSupplierOutstanding = root.optBoolean("showSupplierOutstanding", true),

                    // 9. Calculation & Tax
                    showSubtotal = root.optBoolean("showSubtotal", true),
                    showDiscountCalculation = root.optBoolean("showDiscountCalculation", true),
                    showTaxableAmount = root.optBoolean("showTaxableAmount", true),
                    showVatCalculation = root.optBoolean("showVatCalculation", true),
                    showOtherCharges = root.optBoolean("showOtherCharges", false),
                    showRoundOffCalculation = root.optBoolean("showRoundOffCalculation", true),
                    showGrandTotal = root.optBoolean("showGrandTotal", true),
                    enableVat = root.optBoolean("enableVat", true),
                    showVatBreakdown = root.optBoolean("showVatBreakdown", true),

                    // 10. Date & Due Date
                    useCurrentDateDefault = root.optBoolean("useCurrentDateDefault", true),
                    allowBackdatedTransaction = root.optBoolean("allowBackdatedTransaction", true),
                    allowFutureDatedTransaction = root.optBoolean("allowFutureDatedTransaction", false),
                    defaultCreditPeriodDays = root.optInt("defaultCreditPeriodDays", 30),
                    allowCustomDueDate = root.optBoolean("allowCustomDueDate", true),
                    showTransactionTime = root.optBoolean("showTransactionTime", true),

                    // 11. Remarks
                    referenceRequired = root.optBoolean("referenceRequired", false),
                    remarksRequired = root.optBoolean("remarksRequired", false),
                    showInternalNote = root.optBoolean("showInternalNote", false),
                    showCustomerNote = root.optBoolean("showCustomerNote", true),

                    // 12. Attachments
                    enableAttachments = root.optBoolean("enableAttachments", true),
                    allowImageAttachment = root.optBoolean("allowImageAttachment", true),
                    allowPdfAttachment = root.optBoolean("allowPdfAttachment", true),
                    allowMultipleAttachments = root.optBoolean("allowMultipleAttachments", false),
                    showAttachmentInDetails = root.optBoolean("showAttachmentInDetails", true),

                    // 13. Stock
                    decreaseStockOnSale = root.optBoolean("decreaseStockOnSale", true),
                    allowSaleWithoutStock = root.optBoolean("allowSaleWithoutStock", false),
                    showAvailableStockInForm = root.optBoolean("showAvailableStockInForm", true),
                    increaseStockOnPurchase = root.optBoolean("increaseStockOnPurchase", true),
                    updateStockCostOnPurchase = root.optBoolean("updateStockCostOnPurchase", true),

                    // 14. Accounting
                    updateReceivableOnSale = root.optBoolean("updateReceivableOnSale", true),
                    updatePayableOnPurchase = root.optBoolean("updatePayableOnPurchase", true),
                    updateCashBankOnPayment = root.optBoolean("updateCashBankOnPayment", true),
                    updatePartyBalanceOnPayment = root.optBoolean("updatePartyBalanceOnPayment", true),
                    updateExpenseOnExpenseEntry = root.optBoolean("updateExpenseOnExpenseEntry", true),

                    // 15. Display
                    showTransactionType = root.optBoolean("showTransactionType", true),
                    showTransactionNumber = root.optBoolean("showTransactionNumber", true),
                    showPartyName = root.optBoolean("showPartyName", true),
                    showAmount = root.optBoolean("showAmount", true),
                    showPaidAmount = root.optBoolean("showPaidAmount", true),
                    showDueAmount = root.optBoolean("showDueAmount", true),
                    showDate = root.optBoolean("showDate", true),
                    showPaymentModeInList = root.optBoolean("showPaymentModeInList", true),
                    showStatus = root.optBoolean("showStatus", true),
                    showReferenceInList = root.optBoolean("showReferenceInList", true),
                    showRemarksInList = root.optBoolean("showRemarksInList", false),
                    listDensity = root.optString("listDensity", "Comfortable"),

                    // 16. History
                    showRecentTransactions = root.optBoolean("showRecentTransactions", true),
                    numberOfRecentTransactions = root.optInt("numberOfRecentTransactions", 20),
                    showCancelledTransactions = root.optBoolean("showCancelledTransactions", false),
                    showDraftTransactions = root.optBoolean("showDraftTransactions", true),
                    showAuditInfo = root.optBoolean("showAuditInfo", false),

                    // 17. Permissions
                    permissions = TransactionPermissions.fromJsonObject(root.optJSONObject("permissions")),

                    // 18. Advanced
                    requireConfirmBeforeDelete = root.optBoolean("requireConfirmBeforeDelete", true),
                    requireReasonBeforeCancel = root.optBoolean("requireReasonBeforeCancel", true),
                    requireReasonBeforeEditOld = root.optBoolean("requireReasonBeforeEditOld", false),
                    allowTransactionDateOverride = root.optBoolean("allowTransactionDateOverride", true),
                    allowNegativePartyBalance = root.optBoolean("allowNegativePartyBalance", true),
                    allowDuplicateInvoiceNumber = root.optBoolean("allowDuplicateInvoiceNumber", false),
                    enableTransactionAudit = root.optBoolean("enableTransactionAudit", true),
                    enableActivityTimeline = root.optBoolean("enableActivityTimeline", true),
                    lockTransactionAfterDays = root.optInt("lockTransactionAfterDays", 0)
                )
            } catch (e: Exception) {
                null
            }
        }

        fun createDefaultsFor(preset: BusinessTransactionPreset): TransactionSettings {
            return when (preset) {
                BusinessTransactionPreset.RETAIL -> TransactionSettings(
                    businessPreset = BusinessTransactionPreset.RETAIL,
                    salesEnabled = true,
                    purchaseEnabled = true,
                    paymentInEnabled = true,
                    paymentOutEnabled = true,
                    expenseEnabled = true,
                    customerRequired = false,
                    allowWalkInCustomer = true,
                    showProductItem = true,
                    showQuantity = true,
                    showRate = true,
                    showDiscount = true,
                    showVat = true,
                    showPaymentMode = true,
                    showPaymentIn = true,
                    showCreditSale = true,
                    showDueDate = false,
                    showSalesperson = false,
                    showRoundOff = true,
                    decreaseStockOnSale = true,
                    defaultPaymentMode = "Cash"
                )

                BusinessTransactionPreset.WHOLESALE -> TransactionSettings(
                    businessPreset = BusinessTransactionPreset.WHOLESALE,
                    salesEnabled = true,
                    purchaseEnabled = true,
                    paymentInEnabled = true,
                    paymentOutEnabled = true,
                    expenseEnabled = true,
                    customerRequired = true,
                    allowWalkInCustomer = false,
                    showCreditSale = true,
                    showDueDate = true,
                    defaultCreditDays = 30,
                    showReferenceNumber = true,
                    referenceRequired = true,
                    showSalesperson = true,
                    showOutstandingBalance = true,
                    showPreviousBalance = true,
                    decreaseStockOnSale = true,
                    defaultPaymentMode = "Bank Transfer"
                )

                BusinessTransactionPreset.PHARMACY -> TransactionSettings(
                    businessPreset = BusinessTransactionPreset.PHARMACY,
                    salesEnabled = true,
                    purchaseEnabled = true,
                    paymentInEnabled = true,
                    paymentOutEnabled = true,
                    expenseEnabled = true,
                    customerRequired = false,
                    allowWalkInCustomer = true,
                    showReferenceNumber = true,
                    showRemarks = true,
                    showVat = true,
                    enableVat = true,
                    defaultVatRate = 13.0,
                    decreaseStockOnSale = true,
                    defaultPaymentMode = "Cash"
                )

                BusinessTransactionPreset.SERVICE -> TransactionSettings(
                    businessPreset = BusinessTransactionPreset.SERVICE,
                    salesEnabled = true,
                    purchaseEnabled = false,
                    paymentInEnabled = true,
                    paymentOutEnabled = true,
                    expenseEnabled = true,
                    customerRequired = true,
                    allowWalkInCustomer = true,
                    showProductItem = true,
                    showServicePerson = true,
                    showQuantity = true,
                    showRate = true,
                    showDueDate = true,
                    decreaseStockOnSale = false, // Service businesses do not deduct physical stock!
                    defaultPaymentMode = "Digital Wallet"
                )

                BusinessTransactionPreset.DISTRIBUTOR -> TransactionSettings(
                    businessPreset = BusinessTransactionPreset.DISTRIBUTOR,
                    salesEnabled = true,
                    purchaseEnabled = true,
                    paymentInEnabled = true,
                    paymentOutEnabled = true,
                    expenseEnabled = true,
                    customerRequired = true,
                    allowWalkInCustomer = false,
                    showCreditSale = true,
                    showDueDate = true,
                    showSalesperson = true,
                    showCreditLimit = true,
                    showOutstandingBalance = true,
                    decreaseStockOnSale = true,
                    defaultPaymentMode = "Bank Transfer"
                )

                BusinessTransactionPreset.PERSONAL -> TransactionSettings(
                    businessPreset = BusinessTransactionPreset.PERSONAL,
                    salesEnabled = false, // Business sales turned off in Personal mode
                    purchaseEnabled = false, // Business purchases turned off in Personal mode
                    paymentInEnabled = true, // Used for Personal Income / Inflow
                    paymentOutEnabled = true, // Used for Personal Expense / Outflow
                    expenseEnabled = true,
                    customerRequired = false,
                    allowWalkInCustomer = true,
                    showProductItem = false,
                    showDiscount = false,
                    showVat = false,
                    enableVat = false,
                    showCreditSale = false,
                    showDueDate = false,
                    showSalesperson = false,
                    showRoundOff = false,
                    decreaseStockOnSale = false,
                    defaultPaymentMode = "Cash"
                )

                BusinessTransactionPreset.GENERAL -> TransactionSettings(
                    businessPreset = BusinessTransactionPreset.GENERAL,
                    salesEnabled = true,
                    purchaseEnabled = true,
                    paymentInEnabled = true,
                    paymentOutEnabled = true,
                    expenseEnabled = true,
                    decreaseStockOnSale = true,
                    defaultPaymentMode = "Cash"
                )
            }
        }
    }
}
