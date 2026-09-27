package com.example.data.model

import org.json.JSONObject

/**
 * Atri Khata 2 - Business Invoice Presets
 * Tailors invoice configuration to specific industry workflows.
 */
enum class BusinessInvoicePreset(
    val id: String,
    val displayName: String,
    val description: String
) {
    RETAIL(
        id = "retail",
        displayName = "Retail & POS Store",
        description = "Quick counter receipts, walk-in cash customers, thermal 80mm receipts, standard VAT"
    ),
    WHOLESALE(
        id = "wholesale",
        displayName = "Wholesale & Distribution",
        description = "Formal A4 invoices, B2B party PAN, credit due dates, SKU codes, bank details & terms"
    ),
    PHARMACY(
        id = "pharmacy",
        displayName = "Pharmacy & Clinic",
        description = "Batch numbers, expiry dates, doctor/patient info, strict 13% VAT breakdown"
    ),
    SERVICE(
        id = "service",
        displayName = "Service Business",
        description = "Clean professional service billing, hourly/consulting descriptions, bank QR code"
    ),
    DISTRIBUTOR(
        id = "distributor",
        displayName = "Distributor & Agency",
        description = "Multi-item orders, salesperson credit tracking, formal signatures, ledger balance"
    ),
    PERSONAL(
        id = "personal",
        displayName = "Personal Khata",
        description = "Simplified receipts for personal transactions, household billing and expense memos"
    ),
    GENERAL(
        id = "general",
        displayName = "General Trading",
        description = "Standard balanced invoicing with VAT calculation, terms, signatures, and PDF export"
    );

    companion object {
        fun fromBusinessType(businessType: String): BusinessInvoicePreset {
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

        fun fromId(id: String): BusinessInvoicePreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        }
    }
}

/**
 * Invoice Role & Staff Permissions
 */
data class InvoicePermissions(
    val canCreateInvoice: Boolean = true,
    val canEditInvoice: Boolean = true,
    val canDeleteInvoice: Boolean = false,
    val canCancelInvoice: Boolean = true,
    val canPrintInvoice: Boolean = true,
    val canShareInvoice: Boolean = true,
    val canChangeInvoiceSettings: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("canCreateInvoice", canCreateInvoice)
            put("canEditInvoice", canEditInvoice)
            put("canDeleteInvoice", canDeleteInvoice)
            put("canCancelInvoice", canCancelInvoice)
            put("canPrintInvoice", canPrintInvoice)
            put("canShareInvoice", canShareInvoice)
            put("canChangeInvoiceSettings", canChangeInvoiceSettings)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject?): InvoicePermissions {
            if (json == null) return InvoicePermissions()
            return InvoicePermissions(
                canCreateInvoice = json.optBoolean("canCreateInvoice", true),
                canEditInvoice = json.optBoolean("canEditInvoice", true),
                canDeleteInvoice = json.optBoolean("canDeleteInvoice", false),
                canCancelInvoice = json.optBoolean("canCancelInvoice", true),
                canPrintInvoice = json.optBoolean("canPrintInvoice", true),
                canShareInvoice = json.optBoolean("canShareInvoice", true),
                canChangeInvoiceSettings = json.optBoolean("canChangeInvoiceSettings", false)
            )
        }
    }
}

/**
 * Atri Khata 2 - Advanced Invoice Settings & Configuration Model
 * Covering all 20 sections requested:
 * 1. General Invoice Settings
 * 2. Invoice Numbering
 * 3. Invoice Type
 * 4. Invoice Header
 * 5. Business Information
 * 6. Customer / Party Information
 * 7. Invoice Items
 * 8. Pricing & Calculation
 * 9. VAT / Tax
 * 10. Payment Information
 * 11. Due & Outstanding
 * 12. Terms & Notes
 * 13. Signature
 * 14. Print & PDF
 * 15. Sharing
 * 16. Invoice Layout
 * 17. Invoice Footer
 * 18. Invoice History & Audit
 * 19. Advanced
 * 20. Permissions
 */
data class InvoiceSettings(
    val businessPreset: BusinessInvoicePreset = BusinessInvoicePreset.GENERAL,

    // 1. General Invoice Settings
    val enableInvoicing: Boolean = true,
    val autoGenerateInvoiceNumber: Boolean = true,
    val showInvoicePreviewBeforeSave: Boolean = false,
    val confirmBeforeCancel: Boolean = true,
    val confirmBeforeDelete: Boolean = true,
    val allowEditSavedInvoice: Boolean = true,
    val allowDeleteSavedInvoice: Boolean = true,
    val allowDuplicateInvoice: Boolean = true,
    val showInvoiceStatusBadge: Boolean = true,
    val defaultInvoiceType: String = "Tax Invoice", // "Tax Invoice", "Sales Invoice / Bill of Supply", "Proforma Invoice", "Service Invoice"

    // 2. Invoice Numbering
    val autoNumbering: Boolean = true,
    val invoicePrefix: String = "INV-",
    val startingNumber: Int = 1001,
    val numberPaddingLength: Int = 4, // e.g. 0001 or 1001
    val includeFiscalYear: Boolean = true,
    val fiscalYearFormat: String = "YYYY", // "YYYY" e.g. 2082, "YY/YY" e.g. 82/83
    val numberSeparator: String = "-",
    val resetNumberFiscalYear: Boolean = true,
    val preventDuplicateNumber: Boolean = true,

    // 3. Invoice Type
    val allowTaxInvoice: Boolean = true,
    val allowSalesInvoice: Boolean = true,
    val allowServiceInvoice: Boolean = true,
    val allowProformaInvoice: Boolean = true,
    val requirePanForTaxInvoice: Boolean = false,

    // 4. Invoice Header
    val showBusinessLogo: Boolean = true,
    val showBusinessName: Boolean = true,
    val showBusinessAddress: Boolean = true,
    val showPhone: Boolean = true,
    val showEmail: Boolean = true,
    val showWebsite: Boolean = false,
    val showPanVatHeader: Boolean = true,
    val showRegistrationNumber: Boolean = false,
    val showBranchInfo: Boolean = true,
    val showInvoiceTitle: Boolean = true,
    val customInvoiceTitle: String = "TAX INVOICE",
    val showInvoiceNumber: Boolean = true,
    val showInvoiceDateBs: Boolean = true,
    val showInvoiceDateAd: Boolean = true,

    // 5. Business Information (Synced with BusinessProfile)
    val businessName: String = "Atri Nova Tech Enterprises",
    val panVatNumber: String = "609823415",
    val phone: String = "+977 9852020149",
    val email: String = "accounts@atrinova.com",
    val address: String = "Main Road, Biratnagar-6, Nepal",
    val branchName: String = "Corporate Branch",
    val currencySymbol: String = "Rs.",

    // 6. Customer / Party Information
    val showPartyName: Boolean = true,
    val showPartyPhone: Boolean = true,
    val showPartyAddress: Boolean = true,
    val showPartyEmail: Boolean = false,
    val showPartyPanVat: Boolean = true,
    val showPartyCustomerCode: Boolean = true,
    val showOutstandingBalance: Boolean = true,
    val allowWalkInCustomer: Boolean = true,
    val requirePartySelection: Boolean = false,

    // 7. Invoice Items
    val showItemSerialNumber: Boolean = true,
    val showItemCodeSku: Boolean = true,
    val showItemDescription: Boolean = true,
    val showQuantity: Boolean = true,
    val showUnit: Boolean = true,
    val showUnitPriceRate: Boolean = true,
    val showItemDiscount: Boolean = true,
    val showItemTaxRate: Boolean = true,
    val showItemTotalAmount: Boolean = true,
    val showBatchNumber: Boolean = false,
    val showExpiryDate: Boolean = false,

    // 8. Pricing & Calculation
    val showSubtotal: Boolean = true,
    val enableItemDiscount: Boolean = true,
    val enableBillDiscount: Boolean = true,
    val defaultDiscountType: String = "Amount", // "Percentage", "Amount"
    val defaultDiscountPercent: Double = 0.0,
    val showDiscountOnInvoice: Boolean = true,
    val enableExtraCharge: Boolean = true,
    val showExtraChargeOnInvoice: Boolean = true,
    val defaultExtraChargeName: String = "Delivery Charge",
    val defaultExtraChargeAmount: Double = 0.0,
    val showRoundOff: Boolean = true,
    val roundingMethod: String = "Normal (Nearest 1.00)", // "Normal (Nearest 1.00)", "Always Up", "Always Down"
    val showGrandTotal: Boolean = true,
    val showAmountInWords: Boolean = true,

    // 9. VAT / Tax
    val enableVat: Boolean = true,
    val defaultVatRate: Double = 13.0,
    val allowVatRateEditing: Boolean = true,
    val showVatBreakdown: Boolean = true,
    val showTaxableAmount: Boolean = true,
    val showNonTaxableAmount: Boolean = true,
    val showVatNumber: Boolean = true,
    val taxLabel: String = "VAT",

    // Attachments
    val allowImageAttachment: Boolean = true,
    val allowPdfAttachment: Boolean = true,
    val showAttachmentsOnInvoiceDetail: Boolean = true,

    // 10. Payment Information
    val showPaymentMode: Boolean = true,
    val showPaidAmount: Boolean = true,
    val showDueAmount: Boolean = true,
    val showBankDetails: Boolean = true,
    val showPaymentQrCode: Boolean = true,
    val defaultPaymentMode: String = "Cash",

    // 11. Due & Outstanding
    val showDueDate: Boolean = true,
    val defaultCreditDays: Int = 15,
    val highlightOverdue: Boolean = true,
    val showPreviousBalance: Boolean = true,
    val showNewBalance: Boolean = true,

    // 12. Terms & Notes
    val showTermsAndConditions: Boolean = true,
    val defaultTerms: String = "1. Goods once sold will not be returned after 7 days.\n2. Payment is due within agreed terms.\n3. Subject to local jurisdiction.",
    val showCustomerRemarks: Boolean = true,
    val showInternalNotes: Boolean = false,

    // 13. Signature
    val showAuthorizedSignatureBox: Boolean = true,
    val signatoryTitle: String = "Authorized Signatory",
    val showCustomerSignatureBox: Boolean = false,
    val customerSignatoryTitle: String = "Receiver's Signature",
    val showDigitalSignature: Boolean = true,
    val showPreparedBy: Boolean = true,

    // 14. Print & PDF
    val defaultPaperSize: String = "A4 (Standard)", // "A4 (Standard)", "80mm Thermal POS", "58mm Thermal POS"
    val autoPrintOnSave: Boolean = false,
    val pdfColorTheme: String = "Corporate Navy", // "Corporate Navy", "Sky Blue Modern", "Classic Monochrome"
    val pdfWatermark: String = "None", // "None", "PAID", "DUPLICATE", "ORIGINAL"
    val printCopiesCount: Int = 1,
    val pageOrientation: String = "Portrait", // "Portrait", "Landscape"

    // 15. Sharing
    val enableSharing: Boolean = true,
    val shareFormat: String = "PDF Document", // "PDF Document", "Text Receipt Summary", "Both"
    val includeWhatsAppShareButton: Boolean = true,
    val enableEmailSharing: Boolean = true,

    // 16. Invoice Layout
    val layoutTemplate: String = "Modern Corporate", // "Modern Corporate", "Classic Clean", "Compact POS"
    val fontScaleDensity: String = "Standard", // "Standard", "Compact"
    val headerAlignment: String = "Left", // "Left", "Center"
    val showTableBorders: Boolean = true,

    // 17. Invoice Footer
    val showFooterText: Boolean = true,
    val footerMessage: String = "Thank you for choosing Atri Khata! Visit again.",
    val showComputerGeneratedNotice: Boolean = true,
    val showPageNumbers: Boolean = true,

    // 18. Invoice History & Audit
    val trackActivityHistory: Boolean = true,
    val showActivityLogOnInvoice: Boolean = true,
    val requireReasonForCancel: Boolean = true,
    val requireReasonForEdit: Boolean = false,

    // 19. Advanced
    val allowBackdatedInvoices: Boolean = true,
    val lockPaidInvoices: Boolean = false,
    val strictDuplicateNumberCheck: Boolean = true,
    val allowNegativeQuantities: Boolean = false,

    // 20. Permissions
    val permissions: InvoicePermissions = InvoicePermissions()
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("businessPreset", businessPreset.id)

        // 1. General
        root.put("enableInvoicing", enableInvoicing)
        root.put("autoGenerateInvoiceNumber", autoGenerateInvoiceNumber)
        root.put("showInvoicePreviewBeforeSave", showInvoicePreviewBeforeSave)
        root.put("confirmBeforeCancel", confirmBeforeCancel)
        root.put("confirmBeforeDelete", confirmBeforeDelete)
        root.put("allowEditSavedInvoice", allowEditSavedInvoice)
        root.put("allowDeleteSavedInvoice", allowDeleteSavedInvoice)
        root.put("allowDuplicateInvoice", allowDuplicateInvoice)
        root.put("showInvoiceStatusBadge", showInvoiceStatusBadge)
        root.put("defaultInvoiceType", defaultInvoiceType)

        // 2. Numbering
        root.put("autoNumbering", autoNumbering)
        root.put("invoicePrefix", invoicePrefix)
        root.put("startingNumber", startingNumber)
        root.put("numberPaddingLength", numberPaddingLength)
        root.put("includeFiscalYear", includeFiscalYear)
        root.put("fiscalYearFormat", fiscalYearFormat)
        root.put("numberSeparator", numberSeparator)
        root.put("resetNumberFiscalYear", resetNumberFiscalYear)
        root.put("preventDuplicateNumber", preventDuplicateNumber)

        // 3. Invoice Type
        root.put("allowTaxInvoice", allowTaxInvoice)
        root.put("allowSalesInvoice", allowSalesInvoice)
        root.put("allowServiceInvoice", allowServiceInvoice)
        root.put("allowProformaInvoice", allowProformaInvoice)
        root.put("requirePanForTaxInvoice", requirePanForTaxInvoice)

        // 4. Header
        root.put("showBusinessLogo", showBusinessLogo)
        root.put("showBusinessName", showBusinessName)
        root.put("showBusinessAddress", showBusinessAddress)
        root.put("showPhone", showPhone)
        root.put("showEmail", showEmail)
        root.put("showWebsite", showWebsite)
        root.put("showPanVatHeader", showPanVatHeader)
        root.put("showRegistrationNumber", showRegistrationNumber)
        root.put("showBranchInfo", showBranchInfo)
        root.put("showInvoiceTitle", showInvoiceTitle)
        root.put("customInvoiceTitle", customInvoiceTitle)
        root.put("showInvoiceNumber", showInvoiceNumber)
        root.put("showInvoiceDateBs", showInvoiceDateBs)
        root.put("showInvoiceDateAd", showInvoiceDateAd)

        // 5. Business Information
        root.put("businessName", businessName)
        root.put("panVatNumber", panVatNumber)
        root.put("phone", phone)
        root.put("email", email)
        root.put("address", address)
        root.put("branchName", branchName)
        root.put("currencySymbol", currencySymbol)

        // 6. Customer / Party
        root.put("showPartyName", showPartyName)
        root.put("showPartyPhone", showPartyPhone)
        root.put("showPartyAddress", showPartyAddress)
        root.put("showPartyEmail", showPartyEmail)
        root.put("showPartyPanVat", showPartyPanVat)
        root.put("showPartyCustomerCode", showPartyCustomerCode)
        root.put("showOutstandingBalance", showOutstandingBalance)
        root.put("allowWalkInCustomer", allowWalkInCustomer)
        root.put("requirePartySelection", requirePartySelection)

        // 7. Items
        root.put("showItemSerialNumber", showItemSerialNumber)
        root.put("showItemCodeSku", showItemCodeSku)
        root.put("showItemDescription", showItemDescription)
        root.put("showQuantity", showQuantity)
        root.put("showUnit", showUnit)
        root.put("showUnitPriceRate", showUnitPriceRate)
        root.put("showItemDiscount", showItemDiscount)
        root.put("showItemTaxRate", showItemTaxRate)
        root.put("showItemTotalAmount", showItemTotalAmount)
        root.put("showBatchNumber", showBatchNumber)
        root.put("showExpiryDate", showExpiryDate)

        // 8. Pricing & Calculation
        root.put("showSubtotal", showSubtotal)
        root.put("enableItemDiscount", enableItemDiscount)
        root.put("enableBillDiscount", enableBillDiscount)
        root.put("defaultDiscountType", defaultDiscountType)
        root.put("defaultDiscountPercent", defaultDiscountPercent)
        root.put("showDiscountOnInvoice", showDiscountOnInvoice)
        root.put("enableExtraCharge", enableExtraCharge)
        root.put("showExtraChargeOnInvoice", showExtraChargeOnInvoice)
        root.put("defaultExtraChargeName", defaultExtraChargeName)
        root.put("defaultExtraChargeAmount", defaultExtraChargeAmount)
        root.put("showRoundOff", showRoundOff)
        root.put("roundingMethod", roundingMethod)
        root.put("showGrandTotal", showGrandTotal)
        root.put("showAmountInWords", showAmountInWords)

        // 9. VAT / Tax
        root.put("enableVat", enableVat)
        root.put("defaultVatRate", defaultVatRate)
        root.put("allowVatRateEditing", allowVatRateEditing)
        root.put("showVatBreakdown", showVatBreakdown)
        root.put("showTaxableAmount", showTaxableAmount)
        root.put("showNonTaxableAmount", showNonTaxableAmount)
        root.put("showVatNumber", showVatNumber)
        root.put("taxLabel", taxLabel)

        // Attachments
        root.put("allowImageAttachment", allowImageAttachment)
        root.put("allowPdfAttachment", allowPdfAttachment)
        root.put("showAttachmentsOnInvoiceDetail", showAttachmentsOnInvoiceDetail)

        // 10. Payment
        root.put("showPaymentMode", showPaymentMode)
        root.put("showPaidAmount", showPaidAmount)
        root.put("showDueAmount", showDueAmount)
        root.put("showBankDetails", showBankDetails)
        root.put("showPaymentQrCode", showPaymentQrCode)
        root.put("defaultPaymentMode", defaultPaymentMode)

        // 11. Due & Outstanding
        root.put("showDueDate", showDueDate)
        root.put("defaultCreditDays", defaultCreditDays)
        root.put("highlightOverdue", highlightOverdue)
        root.put("showPreviousBalance", showPreviousBalance)
        root.put("showNewBalance", showNewBalance)

        // 12. Terms & Notes
        root.put("showTermsAndConditions", showTermsAndConditions)
        root.put("defaultTerms", defaultTerms)
        root.put("showCustomerRemarks", showCustomerRemarks)
        root.put("showInternalNotes", showInternalNotes)

        // 13. Signature
        root.put("showAuthorizedSignatureBox", showAuthorizedSignatureBox)
        root.put("signatoryTitle", signatoryTitle)
        root.put("showCustomerSignatureBox", showCustomerSignatureBox)
        root.put("customerSignatoryTitle", customerSignatoryTitle)
        root.put("showDigitalSignature", showDigitalSignature)
        root.put("showPreparedBy", showPreparedBy)

        // 14. Print & PDF
        root.put("defaultPaperSize", defaultPaperSize)
        root.put("autoPrintOnSave", autoPrintOnSave)
        root.put("pdfColorTheme", pdfColorTheme)
        root.put("pdfWatermark", pdfWatermark)
        root.put("printCopiesCount", printCopiesCount)
        root.put("pageOrientation", pageOrientation)

        // 15. Sharing
        root.put("enableSharing", enableSharing)
        root.put("shareFormat", shareFormat)
        root.put("includeWhatsAppShareButton", includeWhatsAppShareButton)
        root.put("enableEmailSharing", enableEmailSharing)

        // 16. Layout
        root.put("layoutTemplate", layoutTemplate)
        root.put("fontScaleDensity", fontScaleDensity)
        root.put("headerAlignment", headerAlignment)
        root.put("showTableBorders", showTableBorders)

        // 17. Footer
        root.put("showFooterText", showFooterText)
        root.put("footerMessage", footerMessage)
        root.put("showComputerGeneratedNotice", showComputerGeneratedNotice)
        root.put("showPageNumbers", showPageNumbers)

        // 18. History
        root.put("trackActivityHistory", trackActivityHistory)
        root.put("showActivityLogOnInvoice", showActivityLogOnInvoice)
        root.put("requireReasonForCancel", requireReasonForCancel)
        root.put("requireReasonForEdit", requireReasonForEdit)

        // 19. Advanced
        root.put("allowBackdatedInvoices", allowBackdatedInvoices)
        root.put("lockPaidInvoices", lockPaidInvoices)
        root.put("strictDuplicateNumberCheck", strictDuplicateNumberCheck)
        root.put("allowNegativeQuantities", allowNegativeQuantities)

        // 20. Permissions
        root.put("permissions", permissions.toJsonObject())

        return root.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String?): InvoiceSettings? {
            if (jsonStr.isNullOrBlank()) return null
            return try {
                val json = JSONObject(jsonStr)
                InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.fromId(json.optString("businessPreset", "general")),

                    // 1. General
                    enableInvoicing = json.optBoolean("enableInvoicing", true),
                    autoGenerateInvoiceNumber = json.optBoolean("autoGenerateInvoiceNumber", true),
                    showInvoicePreviewBeforeSave = json.optBoolean("showInvoicePreviewBeforeSave", false),
                    confirmBeforeCancel = json.optBoolean("confirmBeforeCancel", true),
                    confirmBeforeDelete = json.optBoolean("confirmBeforeDelete", true),
                    allowEditSavedInvoice = json.optBoolean("allowEditSavedInvoice", true),
                    allowDeleteSavedInvoice = json.optBoolean("allowDeleteSavedInvoice", true),
                    allowDuplicateInvoice = json.optBoolean("allowDuplicateInvoice", true),
                    showInvoiceStatusBadge = json.optBoolean("showInvoiceStatusBadge", true),
                    defaultInvoiceType = json.optString("defaultInvoiceType", "Tax Invoice"),

                    // 2. Numbering
                    autoNumbering = json.optBoolean("autoNumbering", true),
                    invoicePrefix = json.optString("invoicePrefix", "INV-"),
                    startingNumber = json.optInt("startingNumber", 1001),
                    numberPaddingLength = json.optInt("numberPaddingLength", 4),
                    includeFiscalYear = json.optBoolean("includeFiscalYear", true),
                    fiscalYearFormat = json.optString("fiscalYearFormat", "YYYY"),
                    numberSeparator = json.optString("numberSeparator", "-"),
                    resetNumberFiscalYear = json.optBoolean("resetNumberFiscalYear", true),
                    preventDuplicateNumber = json.optBoolean("preventDuplicateNumber", true),

                    // 3. Invoice Type
                    allowTaxInvoice = json.optBoolean("allowTaxInvoice", true),
                    allowSalesInvoice = json.optBoolean("allowSalesInvoice", true),
                    allowServiceInvoice = json.optBoolean("allowServiceInvoice", true),
                    allowProformaInvoice = json.optBoolean("allowProformaInvoice", true),
                    requirePanForTaxInvoice = json.optBoolean("requirePanForTaxInvoice", false),

                    // 4. Header
                    showBusinessLogo = json.optBoolean("showBusinessLogo", true),
                    showBusinessName = json.optBoolean("showBusinessName", true),
                    showBusinessAddress = json.optBoolean("showBusinessAddress", true),
                    showPhone = json.optBoolean("showPhone", true),
                    showEmail = json.optBoolean("showEmail", true),
                    showWebsite = json.optBoolean("showWebsite", false),
                    showPanVatHeader = json.optBoolean("showPanVatHeader", true),
                    showRegistrationNumber = json.optBoolean("showRegistrationNumber", false),
                    showBranchInfo = json.optBoolean("showBranchInfo", true),
                    showInvoiceTitle = json.optBoolean("showInvoiceTitle", true),
                    customInvoiceTitle = json.optString("customInvoiceTitle", "TAX INVOICE"),
                    showInvoiceNumber = json.optBoolean("showInvoiceNumber", true),
                    showInvoiceDateBs = json.optBoolean("showInvoiceDateBs", true),
                    showInvoiceDateAd = json.optBoolean("showInvoiceDateAd", true),

                    // 5. Business Information
                    businessName = json.optString("businessName", "Atri Nova Tech Enterprises"),
                    panVatNumber = json.optString("panVatNumber", "609823415"),
                    phone = json.optString("phone", "+977 9852020149"),
                    email = json.optString("email", "accounts@atrinova.com"),
                    address = json.optString("address", "Main Road, Biratnagar-6, Nepal"),
                    branchName = json.optString("branchName", "Corporate Branch"),
                    currencySymbol = json.optString("currencySymbol", "Rs."),

                    // 6. Customer / Party
                    showPartyName = json.optBoolean("showPartyName", true),
                    showPartyPhone = json.optBoolean("showPartyPhone", true),
                    showPartyAddress = json.optBoolean("showPartyAddress", true),
                    showPartyEmail = json.optBoolean("showPartyEmail", false),
                    showPartyPanVat = json.optBoolean("showPartyPanVat", true),
                    showPartyCustomerCode = json.optBoolean("showPartyCustomerCode", true),
                    showOutstandingBalance = json.optBoolean("showOutstandingBalance", true),
                    allowWalkInCustomer = json.optBoolean("allowWalkInCustomer", true),
                    requirePartySelection = json.optBoolean("requirePartySelection", false),

                    // 7. Items
                    showItemSerialNumber = json.optBoolean("showItemSerialNumber", true),
                    showItemCodeSku = json.optBoolean("showItemCodeSku", true),
                    showItemDescription = json.optBoolean("showItemDescription", true),
                    showQuantity = json.optBoolean("showQuantity", true),
                    showUnit = json.optBoolean("showUnit", true),
                    showUnitPriceRate = json.optBoolean("showUnitPriceRate", true),
                    showItemDiscount = json.optBoolean("showItemDiscount", true),
                    showItemTaxRate = json.optBoolean("showItemTaxRate", true),
                    showItemTotalAmount = json.optBoolean("showItemTotalAmount", true),
                    showBatchNumber = json.optBoolean("showBatchNumber", false),
                    showExpiryDate = json.optBoolean("showExpiryDate", false),

                    // 8. Pricing & Calculation
                    showSubtotal = json.optBoolean("showSubtotal", true),
                    enableItemDiscount = json.optBoolean("enableItemDiscount", true),
                    enableBillDiscount = json.optBoolean("enableBillDiscount", true),
                    defaultDiscountPercent = json.optDouble("defaultDiscountPercent", 0.0),
                    showRoundOff = json.optBoolean("showRoundOff", true),
                    showGrandTotal = json.optBoolean("showGrandTotal", true),
                    showAmountInWords = json.optBoolean("showAmountInWords", true),

                    // 9. VAT / Tax
                    enableVat = json.optBoolean("enableVat", true),
                    defaultVatRate = json.optDouble("defaultVatRate", 13.0),
                    showVatBreakdown = json.optBoolean("showVatBreakdown", true),
                    showTaxableAmount = json.optBoolean("showTaxableAmount", true),
                    showNonTaxableAmount = json.optBoolean("showNonTaxableAmount", true),
                    showVatNumber = json.optBoolean("showVatNumber", true),
                    taxLabel = json.optString("taxLabel", "VAT"),

                    // 10. Payment
                    showPaymentMode = json.optBoolean("showPaymentMode", true),
                    showPaidAmount = json.optBoolean("showPaidAmount", true),
                    showDueAmount = json.optBoolean("showDueAmount", true),
                    showBankDetails = json.optBoolean("showBankDetails", true),
                    showPaymentQrCode = json.optBoolean("showPaymentQrCode", true),
                    defaultPaymentMode = json.optString("defaultPaymentMode", "Cash"),

                    // 11. Due & Outstanding
                    showDueDate = json.optBoolean("showDueDate", true),
                    defaultCreditDays = json.optInt("defaultCreditDays", 15),
                    highlightOverdue = json.optBoolean("highlightOverdue", true),
                    showPreviousBalance = json.optBoolean("showPreviousBalance", true),
                    showNewBalance = json.optBoolean("showNewBalance", true),

                    // 12. Terms & Notes
                    showTermsAndConditions = json.optBoolean("showTermsAndConditions", true),
                    defaultTerms = json.optString(
                        "defaultTerms",
                        "1. Goods once sold will not be returned after 7 days.\n2. Payment is due within agreed terms.\n3. Subject to local jurisdiction."
                    ),
                    showCustomerRemarks = json.optBoolean("showCustomerRemarks", true),
                    showInternalNotes = json.optBoolean("showInternalNotes", false),

                    // 13. Signature
                    showAuthorizedSignatureBox = json.optBoolean("showAuthorizedSignatureBox", true),
                    signatoryTitle = json.optString("signatoryTitle", "Authorized Signatory"),
                    showCustomerSignatureBox = json.optBoolean("showCustomerSignatureBox", false),
                    customerSignatoryTitle = json.optString("customerSignatoryTitle", "Receiver's Signature"),
                    showDigitalSignature = json.optBoolean("showDigitalSignature", true),
                    showPreparedBy = json.optBoolean("showPreparedBy", true),

                    // 14. Print & PDF
                    defaultPaperSize = json.optString("defaultPaperSize", "A4 (Standard)"),
                    autoPrintOnSave = json.optBoolean("autoPrintOnSave", false),
                    pdfColorTheme = json.optString("pdfColorTheme", "Corporate Navy"),
                    pdfWatermark = json.optString("pdfWatermark", "None"),
                    printCopiesCount = json.optInt("printCopiesCount", 1),
                    pageOrientation = json.optString("pageOrientation", "Portrait"),

                    // 15. Sharing
                    enableSharing = json.optBoolean("enableSharing", true),
                    shareFormat = json.optString("shareFormat", "PDF Document"),
                    includeWhatsAppShareButton = json.optBoolean("includeWhatsAppShareButton", true),
                    enableEmailSharing = json.optBoolean("enableEmailSharing", true),

                    // 16. Layout
                    layoutTemplate = json.optString("layoutTemplate", "Modern Corporate"),
                    fontScaleDensity = json.optString("fontScaleDensity", "Standard"),
                    headerAlignment = json.optString("headerAlignment", "Left"),
                    showTableBorders = json.optBoolean("showTableBorders", true),

                    // 17. Footer
                    showFooterText = json.optBoolean("showFooterText", true),
                    footerMessage = json.optString("footerMessage", "Thank you for choosing Atri Khata! Visit again."),
                    showComputerGeneratedNotice = json.optBoolean("showComputerGeneratedNotice", true),
                    showPageNumbers = json.optBoolean("showPageNumbers", true),

                    // 18. History
                    trackActivityHistory = json.optBoolean("trackActivityHistory", true),
                    showActivityLogOnInvoice = json.optBoolean("showActivityLogOnInvoice", true),
                    requireReasonForCancel = json.optBoolean("requireReasonForCancel", true),
                    requireReasonForEdit = json.optBoolean("requireReasonForEdit", false),

                    // 19. Advanced
                    allowBackdatedInvoices = json.optBoolean("allowBackdatedInvoices", true),
                    lockPaidInvoices = json.optBoolean("lockPaidInvoices", false),
                    strictDuplicateNumberCheck = json.optBoolean("strictDuplicateNumberCheck", true),
                    allowNegativeQuantities = json.optBoolean("allowNegativeQuantities", false),

                    // 20. Permissions
                    permissions = InvoicePermissions.fromJsonObject(json.optJSONObject("permissions"))
                )
            } catch (_: Exception) {
                null
            }
        }

        fun createDefaultsFor(preset: BusinessInvoicePreset): InvoiceSettings {
            return when (preset) {
                BusinessInvoicePreset.RETAIL -> InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.RETAIL,
                    defaultPaperSize = "80mm Thermal POS",
                    defaultInvoiceType = "Sales Invoice / Bill of Supply",
                    showItemCodeSku = false,
                    showPartyAddress = false,
                    showPartyEmail = false,
                    allowWalkInCustomer = true,
                    showAuthorizedSignatureBox = false,
                    showCustomerSignatureBox = false,
                    showPaymentQrCode = true,
                    showComputerGeneratedNotice = true,
                    layoutTemplate = "Compact POS"
                )
                BusinessInvoicePreset.WHOLESALE -> InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.WHOLESALE,
                    defaultPaperSize = "A4 (Standard)",
                    defaultInvoiceType = "Tax Invoice",
                    showItemCodeSku = true,
                    showPartyPanVat = true,
                    showOutstandingBalance = true,
                    showDueDate = true,
                    defaultCreditDays = 30,
                    showBankDetails = true,
                    showAuthorizedSignatureBox = true,
                    showCustomerSignatureBox = true,
                    layoutTemplate = "Modern Corporate"
                )
                BusinessInvoicePreset.PHARMACY -> InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.PHARMACY,
                    defaultPaperSize = "A4 (Standard)",
                    defaultInvoiceType = "Tax Invoice",
                    showBatchNumber = true,
                    showExpiryDate = true,
                    enableVat = true,
                    showVatBreakdown = true,
                    showItemTaxRate = true,
                    showBankDetails = true
                )
                BusinessInvoicePreset.SERVICE -> InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.SERVICE,
                    defaultPaperSize = "A4 (Standard)",
                    defaultInvoiceType = "Service Invoice",
                    showItemCodeSku = false,
                    showUnit = false,
                    showPaymentQrCode = true,
                    showBankDetails = true,
                    layoutTemplate = "Classic Clean"
                )
                BusinessInvoicePreset.DISTRIBUTOR -> InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.DISTRIBUTOR,
                    defaultPaperSize = "A4 (Standard)",
                    defaultInvoiceType = "Tax Invoice",
                    showItemCodeSku = true,
                    showOutstandingBalance = true,
                    showPreviousBalance = true,
                    showPreparedBy = true,
                    showAuthorizedSignatureBox = true,
                    showCustomerSignatureBox = true
                )
                BusinessInvoicePreset.PERSONAL -> InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.PERSONAL,
                    defaultPaperSize = "A4 (Standard)",
                    defaultInvoiceType = "Sales Invoice / Bill of Supply",
                    enableVat = false,
                    showPanVatHeader = false,
                    showPartyPanVat = false,
                    showAuthorizedSignatureBox = false,
                    showTermsAndConditions = false
                )
                BusinessInvoicePreset.GENERAL -> InvoiceSettings(
                    businessPreset = BusinessInvoicePreset.GENERAL
                )
            }
        }
    }
}
