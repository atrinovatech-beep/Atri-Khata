package com.example.data.model

import org.json.JSONObject

/**
 * Atri Khata 2 - Business Party Presets
 * Adapts Party behavior to industry-specific workflows and Personal Mode.
 */
enum class BusinessPartyPreset(
    val id: String,
    val displayName: String,
    val description: String,
    val defaultCustomerTerm: String,
    val defaultSupplierTerm: String
) {
    RETAIL(
        id = "retail",
        displayName = "Retail & POS Store",
        description = "Walk-in customers, quick add, balance tracking, supplier dues",
        defaultCustomerTerm = "Customer",
        defaultSupplierTerm = "Supplier"
    ),
    WHOLESALE(
        id = "wholesale",
        displayName = "Wholesale & B2B Distribution",
        description = "Large credit limits, strict payment terms, company PAN/VAT, ledger statements",
        defaultCustomerTerm = "Customer",
        defaultSupplierTerm = "Supplier"
    ),
    PHARMACY(
        id = "pharmacy",
        displayName = "Pharmacy & Healthcare",
        description = "Patients, clinics, doctors, pharmaceutical distributors & drug suppliers",
        defaultCustomerTerm = "Patient / Customer",
        defaultSupplierTerm = "Supplier / Distributor"
    ),
    SERVICE(
        id = "service",
        displayName = "Service Business",
        description = "Corporate clients, individual service accounts, external vendors",
        defaultCustomerTerm = "Client",
        defaultSupplierTerm = "Vendor"
    ),
    DISTRIBUTOR(
        id = "distributor",
        displayName = "Distributor & Agency",
        description = "Dealer networks, retailer accounts, sales representative routing",
        defaultCustomerTerm = "Dealer / Retailer",
        defaultSupplierTerm = "Manufacturer"
    ),
    PERSONAL(
        id = "personal",
        displayName = "Personal Khata",
        description = "Friends, family, loans given, loans taken, personal khata",
        defaultCustomerTerm = "Person / Contact",
        defaultSupplierTerm = "Contact"
    ),
    GENERAL(
        id = "general",
        displayName = "General Trading",
        description = "Standard balanced customer and supplier account ledgers",
        defaultCustomerTerm = "Customer",
        defaultSupplierTerm = "Supplier"
    );

    companion object {
        fun fromBusinessType(businessType: String): BusinessPartyPreset {
            val lower = businessType.lowercase()
            return when {
                lower.contains("pharmacy") || lower.contains("medical") || lower.contains("clinic") || lower.contains("health") -> PHARMACY
                lower.contains("wholesale") || lower.contains("distribut") -> WHOLESALE
                lower.contains("service") || lower.contains("consult") || lower.contains("agency") -> SERVICE
                lower.contains("personal") -> PERSONAL
                lower.contains("retail") || lower.contains("shop") || lower.contains("mart") -> RETAIL
                else -> GENERAL
            }
        }

        fun fromId(id: String?): BusinessPartyPreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        }
    }
}

/**
 * Atri Khata 2 - Party Sort Preferences
 */
enum class PartySortOption(val id: String, val displayName: String) {
    NAME_ASC("name_asc", "Name (A-Z)"),
    NAME_DESC("name_desc", "Name (Z-A)"),
    HIGHEST_BALANCE("highest_balance", "Highest Balance"),
    LOWEST_BALANCE("lowest_balance", "Lowest Balance"),
    RECENT_ACTIVITY("recent_activity", "Recent Activity"),
    NEWEST_FIRST("newest_first", "Newest Registered");

    companion object {
        fun fromId(id: String?): PartySortOption = entries.firstOrNull { it.id == id } ?: NAME_ASC
        fun fromDisplayName(name: String?): PartySortOption = entries.firstOrNull { it.displayName == name } ?: NAME_ASC
    }
}

/**
 * Atri Khata 2 - Party Filter Preferences
 */
enum class PartyFilterOption(val id: String, val displayName: String) {
    ALL("all", "All Parties"),
    CUSTOMER("customer", "Customers Only"),
    SUPPLIER("supplier", "Suppliers Only"),
    RECEIVABLE("receivable", "To Receive (Dr)"),
    PAYABLE("payable", "To Give (Cr)"),
    ZERO_BALANCE("zero_balance", "Zero Balance");

    companion object {
        fun fromId(id: String?): PartyFilterOption = entries.firstOrNull { it.id == id } ?: ALL
    }
}

/**
 * Atri Khata 2 - Reminder Delivery Channel
 */
enum class ReminderChannel(val id: String, val displayName: String) {
    WHATSAPP("whatsapp", "WhatsApp Message"),
    SMS("sms", "Cellular SMS"),
    PHONE_CALL("call", "Phone Call Direct");

    companion object {
        fun fromId(id: String?): ReminderChannel = entries.firstOrNull { it.id == id } ?: WHATSAPP
    }
}

/**
 * Comprehensive Party configuration data class for Atri Khata 2.
 * Controls Party creation, customer/supplier classification, financial rules,
 * form layout, transaction integration, ledger views, and safety checks.
 */
data class PartySettings(
    val businessPreset: BusinessPartyPreset = BusinessPartyPreset.GENERAL,

    // -------------------------------------------------------------
    // 1. GENERAL PARTY SETTINGS
    // -------------------------------------------------------------
    val enablePartyModule: Boolean = true,
    val allowCustomer: Boolean = true,
    val allowSupplier: Boolean = true,
    val allowGeneralParty: Boolean = true,
    val allowClient: Boolean = true,
    val allowVendor: Boolean = true,
    val allowQuickAddParty: Boolean = true,
    val allowDuplicatePartyName: Boolean = false,
    val confirmBeforeDeleteParty: Boolean = true,
    val confirmBeforeDeactivate: Boolean = true,
    val allowEditParty: Boolean = true,
    val allowDeleteParty: Boolean = true,
    val allowDeactivateParty: Boolean = true,

    // -------------------------------------------------------------
    // 2. PARTY TYPES & TERMINOLOGY
    // -------------------------------------------------------------
    val defaultPartyType: String = "Customer", // Customer, Supplier
    val customerTerminology: String = "Customer", // Customer, Client, Patient
    val supplierTerminology: String = "Supplier", // Supplier, Vendor, Wholesaler
    val showPartyTypeBadge: Boolean = true,
    val allowBothCustomerAndSupplier: Boolean = true,

    // -------------------------------------------------------------
    // 3. CUSTOMER SETTINGS
    // -------------------------------------------------------------
    val enableCustomer: Boolean = true,
    val customerNameRequired: Boolean = true,
    val customerPhoneRequired: Boolean = false,
    val customerAddressRequired: Boolean = false,
    val customerCodePrefix: String = "CUST-",
    val defaultCustomerCategory: String = "Retailer", // Retailer, Wholesaler, Walk-in, Distributor
    val allowCreditCustomer: Boolean = true,
    val allowWalkInCustomer: Boolean = true,
    val showCustomerBalance: Boolean = true,
    val showCustomerCreditLimit: Boolean = true,
    val showCustomerOutstanding: Boolean = true,
    val showCustomerTransactionCount: Boolean = true,

    // -------------------------------------------------------------
    // 4. SUPPLIER SETTINGS
    // -------------------------------------------------------------
    val enableSupplier: Boolean = true,
    val supplierNameRequired: Boolean = true,
    val supplierPhoneRequired: Boolean = false,
    val supplierAddressRequired: Boolean = false,
    val supplierCodePrefix: String = "SUPP-",
    val defaultSupplierCategory: String = "Wholesaler", // Manufacturer, Wholesaler, Distributor, Service
    val allowCreditSupplier: Boolean = true,
    val showSupplierBalance: Boolean = true,
    val showSupplierOutstanding: Boolean = true,
    val showSupplierTransactionCount: Boolean = true,

    // -------------------------------------------------------------
    // 5. PARTY FORM SETTINGS (Field Visibility)
    // -------------------------------------------------------------
    val showCodeInForm: Boolean = true,
    val showPhoneInForm: Boolean = true,
    val showAltPhoneInForm: Boolean = true,
    val showEmailInForm: Boolean = true,
    val showContactPersonInForm: Boolean = true,
    val showPanVatInForm: Boolean = true,
    val showAddressInForm: Boolean = true,
    val showCityInForm: Boolean = true,
    val showOpeningBalanceInForm: Boolean = true,
    val showCreditLimitInForm: Boolean = true,
    val showCategoryInForm: Boolean = true,
    val showNotesInForm: Boolean = true,

    // -------------------------------------------------------------
    // 6. CONTACT INFORMATION & VALIDATION
    // -------------------------------------------------------------
    val showPhoneOnCard: Boolean = true,
    val showContactPersonOnCard: Boolean = true,
    val validatePhoneNumberFormat: Boolean = true,
    val validateEmailFormat: Boolean = false,
    val allowMultipleContactNumbers: Boolean = true,

    // -------------------------------------------------------------
    // 7. ADDRESS CONFIGURATION
    // -------------------------------------------------------------
    val showAddressOnCard: Boolean = true,
    val addressRequired: Boolean = false,
    val showCityOnCard: Boolean = true,
    val multiLineAddressInput: Boolean = true,

    // -------------------------------------------------------------
    // 8. FINANCIAL SETTINGS
    // -------------------------------------------------------------
    val showOpeningBalance: Boolean = true,
    val showCurrentBalance: Boolean = true,
    val showReceivableDr: Boolean = true,
    val showPayableCr: Boolean = true,
    val showNetBalance: Boolean = true,
    val showCreditUtilization: Boolean = true,
    val showTotalSalesToParty: Boolean = true,
    val showTotalPurchaseFromParty: Boolean = true,
    val showTotalReceivedFromParty: Boolean = true,
    val showTotalPaidToParty: Boolean = true,

    // -------------------------------------------------------------
    // 9. CREDIT & PAYMENT TERMS
    // -------------------------------------------------------------
    val enableCreditLimit: Boolean = true,
    val defaultCreditLimit: Double = 50000.0,
    val warnCreditLimitExceeded: Boolean = true,
    val allowOverCreditLimit: Boolean = true,
    val enableCreditSales: Boolean = true,
    val enableCreditPurchase: Boolean = true,
    val defaultPaymentTermDays: Int = 30,
    val showDueDateOnLedger: Boolean = true,

    // -------------------------------------------------------------
    // 10. OPENING BALANCE CONFIGURATION
    // -------------------------------------------------------------
    val enableOpeningBalance: Boolean = true,
    val allowOpeningReceivable: Boolean = true,
    val allowOpeningPayable: Boolean = true,
    val allowOpeningBalanceAdjustment: Boolean = true,
    val showOpeningBalanceInLedger: Boolean = true,

    // -------------------------------------------------------------
    // 11. TRANSACTION INTEGRATION
    // -------------------------------------------------------------
    val selectPartyInSales: Boolean = true,
    val requireCustomerForCreditSale: Boolean = true,
    val showBalanceDuringSale: Boolean = true,
    val showCreditLimitDuringSale: Boolean = true,
    val showOutstandingDuringSale: Boolean = true,
    val selectSupplierInPurchase: Boolean = true,
    val showSupplierBalanceDuringPurchase: Boolean = true,
    val showOutstandingDuringPaymentIn: Boolean = true,
    val showOutstandingDuringPaymentOut: Boolean = true,

    // -------------------------------------------------------------
    // 12. OUTSTANDING & LEDGER (Party Detail Dashboard)
    // -------------------------------------------------------------
    val showOutstandingSummaryCard: Boolean = true,
    val showLedgerTab: Boolean = true,
    val showSalesHistoryTab: Boolean = true,
    val showPurchaseHistoryTab: Boolean = true,
    val showPaymentHistoryTab: Boolean = true,
    val showInvoicesTab: Boolean = true,
    val showActivityTimelineTab: Boolean = true,

    // -------------------------------------------------------------
    // 13. REMINDERS & NOTIFICATIONS
    // -------------------------------------------------------------
    val enablePartyReminders: Boolean = true,
    val reminderBeforeDueDateDays: Int = 3,
    val reminderAfterDueDateDays: Int = 1,
    val defaultReminderChannel: ReminderChannel = ReminderChannel.WHATSAPP,
    val defaultReminderMessageTemplate: String = "Dear {party_name}, this is a gentle reminder regarding your pending balance of Rs. {balance} with Atri Khata. Thank you!",

    // -------------------------------------------------------------
    // 14. PARTY DISPLAY & QUICK ACTIONS
    // -------------------------------------------------------------
    val showBalanceOnPartyCard: Boolean = true,
    val showCreditLimitOnPartyCard: Boolean = true,
    val showLastTransactionOnPartyCard: Boolean = true,
    val showTransactionCountOnPartyCard: Boolean = true,
    val enableQuickCallAction: Boolean = true,
    val enableQuickWhatsAppAction: Boolean = true,
    val enableQuickSaleAction: Boolean = true,
    val enableQuickPaymentAction: Boolean = true,

    // -------------------------------------------------------------
    // 15. SEARCH & LIST SETTINGS
    // -------------------------------------------------------------
    val searchByName: Boolean = true,
    val searchByPhone: Boolean = true,
    val searchByPanVat: Boolean = true,
    val searchByAddress: Boolean = true,
    val defaultFilterOption: PartyFilterOption = PartyFilterOption.ALL,
    val defaultSortOption: PartySortOption = PartySortOption.NAME_ASC,

    // -------------------------------------------------------------
    // 16. IMPORT & EXPORT
    // -------------------------------------------------------------
    val allowExportPartyDirectory: Boolean = true,
    val includeContactInExport: Boolean = true,
    val includeFinancialSummaryInExport: Boolean = true,

    // -------------------------------------------------------------
    // 17. PERMISSIONS & ROLES INTEGRATION
    // -------------------------------------------------------------
    val allowStaffToAddParty: Boolean = true,
    val allowStaffToEditParty: Boolean = true,
    val allowStaffToDeleteParty: Boolean = false,
    val allowStaffToViewBalance: Boolean = true,

    // -------------------------------------------------------------
    // 18. ADVANCED & HISTORICAL INTEGRITY
    // -------------------------------------------------------------
    val autoGeneratePartyCode: Boolean = true,
    val partyCodePrefix: String = "PTY-",
    val preventDeletePartyWithTransactions: Boolean = true,
    val requireDeactivationReason: Boolean = false,
    val enablePartyAuditTrail: Boolean = true
) {
    /**
     * Serializes this PartySettings instance to JSON for local persistence.
     */
    fun toJsonString(): String {
        val json = JSONObject()
        json.put("businessPreset", businessPreset.id)

        // 1. General
        json.put("enablePartyModule", enablePartyModule)
        json.put("allowCustomer", allowCustomer)
        json.put("allowSupplier", allowSupplier)
        json.put("allowGeneralParty", allowGeneralParty)
        json.put("allowClient", allowClient)
        json.put("allowVendor", allowVendor)
        json.put("allowQuickAddParty", allowQuickAddParty)
        json.put("allowDuplicatePartyName", allowDuplicatePartyName)
        json.put("confirmBeforeDeleteParty", confirmBeforeDeleteParty)
        json.put("confirmBeforeDeactivate", confirmBeforeDeactivate)
        json.put("allowEditParty", allowEditParty)
        json.put("allowDeleteParty", allowDeleteParty)
        json.put("allowDeactivateParty", allowDeactivateParty)

        // 2. Types
        json.put("defaultPartyType", defaultPartyType)
        json.put("customerTerminology", customerTerminology)
        json.put("supplierTerminology", supplierTerminology)
        json.put("showPartyTypeBadge", showPartyTypeBadge)
        json.put("allowBothCustomerAndSupplier", allowBothCustomerAndSupplier)

        // 3. Customer
        json.put("enableCustomer", enableCustomer)
        json.put("customerNameRequired", customerNameRequired)
        json.put("customerPhoneRequired", customerPhoneRequired)
        json.put("customerAddressRequired", customerAddressRequired)
        json.put("customerCodePrefix", customerCodePrefix)
        json.put("defaultCustomerCategory", defaultCustomerCategory)
        json.put("allowCreditCustomer", allowCreditCustomer)
        json.put("allowWalkInCustomer", allowWalkInCustomer)
        json.put("showCustomerBalance", showCustomerBalance)
        json.put("showCustomerCreditLimit", showCustomerCreditLimit)
        json.put("showCustomerOutstanding", showCustomerOutstanding)
        json.put("showCustomerTransactionCount", showCustomerTransactionCount)

        // 4. Supplier
        json.put("enableSupplier", enableSupplier)
        json.put("supplierNameRequired", supplierNameRequired)
        json.put("supplierPhoneRequired", supplierPhoneRequired)
        json.put("supplierAddressRequired", supplierAddressRequired)
        json.put("supplierCodePrefix", supplierCodePrefix)
        json.put("defaultSupplierCategory", defaultSupplierCategory)
        json.put("allowCreditSupplier", allowCreditSupplier)
        json.put("showSupplierBalance", showSupplierBalance)
        json.put("showSupplierOutstanding", showSupplierOutstanding)
        json.put("showSupplierTransactionCount", showSupplierTransactionCount)

        // 5. Form
        json.put("showCodeInForm", showCodeInForm)
        json.put("showPhoneInForm", showPhoneInForm)
        json.put("showAltPhoneInForm", showAltPhoneInForm)
        json.put("showEmailInForm", showEmailInForm)
        json.put("showContactPersonInForm", showContactPersonInForm)
        json.put("showPanVatInForm", showPanVatInForm)
        json.put("showAddressInForm", showAddressInForm)
        json.put("showCityInForm", showCityInForm)
        json.put("showOpeningBalanceInForm", showOpeningBalanceInForm)
        json.put("showCreditLimitInForm", showCreditLimitInForm)
        json.put("showCategoryInForm", showCategoryInForm)
        json.put("showNotesInForm", showNotesInForm)

        // 6. Contact
        json.put("showPhoneOnCard", showPhoneOnCard)
        json.put("showContactPersonOnCard", showContactPersonOnCard)
        json.put("validatePhoneNumberFormat", validatePhoneNumberFormat)
        json.put("validateEmailFormat", validateEmailFormat)
        json.put("allowMultipleContactNumbers", allowMultipleContactNumbers)

        // 7. Address
        json.put("showAddressOnCard", showAddressOnCard)
        json.put("addressRequired", addressRequired)
        json.put("showCityOnCard", showCityOnCard)
        json.put("multiLineAddressInput", multiLineAddressInput)

        // 8. Financial
        json.put("showOpeningBalance", showOpeningBalance)
        json.put("showCurrentBalance", showCurrentBalance)
        json.put("showReceivableDr", showReceivableDr)
        json.put("showPayableCr", showPayableCr)
        json.put("showNetBalance", showNetBalance)
        json.put("showCreditUtilization", showCreditUtilization)
        json.put("showTotalSalesToParty", showTotalSalesToParty)
        json.put("showTotalPurchaseFromParty", showTotalPurchaseFromParty)
        json.put("showTotalReceivedFromParty", showTotalReceivedFromParty)
        json.put("showTotalPaidToParty", showTotalPaidToParty)

        // 9. Credit
        json.put("enableCreditLimit", enableCreditLimit)
        json.put("defaultCreditLimit", defaultCreditLimit)
        json.put("warnCreditLimitExceeded", warnCreditLimitExceeded)
        json.put("allowOverCreditLimit", allowOverCreditLimit)
        json.put("enableCreditSales", enableCreditSales)
        json.put("enableCreditPurchase", enableCreditPurchase)
        json.put("defaultPaymentTermDays", defaultPaymentTermDays)
        json.put("showDueDateOnLedger", showDueDateOnLedger)

        // 10. Opening Balance
        json.put("enableOpeningBalance", enableOpeningBalance)
        json.put("allowOpeningReceivable", allowOpeningReceivable)
        json.put("allowOpeningPayable", allowOpeningPayable)
        json.put("allowOpeningBalanceAdjustment", allowOpeningBalanceAdjustment)
        json.put("showOpeningBalanceInLedger", showOpeningBalanceInLedger)

        // 11. Transaction Integration
        json.put("selectPartyInSales", selectPartyInSales)
        json.put("requireCustomerForCreditSale", requireCustomerForCreditSale)
        json.put("showBalanceDuringSale", showBalanceDuringSale)
        json.put("showCreditLimitDuringSale", showCreditLimitDuringSale)
        json.put("showOutstandingDuringSale", showOutstandingDuringSale)
        json.put("selectSupplierInPurchase", selectSupplierInPurchase)
        json.put("showSupplierBalanceDuringPurchase", showSupplierBalanceDuringPurchase)
        json.put("showOutstandingDuringPaymentIn", showOutstandingDuringPaymentIn)
        json.put("showOutstandingDuringPaymentOut", showOutstandingDuringPaymentOut)

        // 12. Ledger & Tabs
        json.put("showOutstandingSummaryCard", showOutstandingSummaryCard)
        json.put("showLedgerTab", showLedgerTab)
        json.put("showSalesHistoryTab", showSalesHistoryTab)
        json.put("showPurchaseHistoryTab", showPurchaseHistoryTab)
        json.put("showPaymentHistoryTab", showPaymentHistoryTab)
        json.put("showInvoicesTab", showInvoicesTab)
        json.put("showActivityTimelineTab", showActivityTimelineTab)

        // 13. Reminders
        json.put("enablePartyReminders", enablePartyReminders)
        json.put("reminderBeforeDueDateDays", reminderBeforeDueDateDays)
        json.put("reminderAfterDueDateDays", reminderAfterDueDateDays)
        json.put("defaultReminderChannel", defaultReminderChannel.id)
        json.put("defaultReminderMessageTemplate", defaultReminderMessageTemplate)

        // 14. Display & Actions
        json.put("showBalanceOnPartyCard", showBalanceOnPartyCard)
        json.put("showCreditLimitOnPartyCard", showCreditLimitOnPartyCard)
        json.put("showLastTransactionOnPartyCard", showLastTransactionOnPartyCard)
        json.put("showTransactionCountOnPartyCard", showTransactionCountOnPartyCard)
        json.put("enableQuickCallAction", enableQuickCallAction)
        json.put("enableQuickWhatsAppAction", enableQuickWhatsAppAction)
        json.put("enableQuickSaleAction", enableQuickSaleAction)
        json.put("enableQuickPaymentAction", enableQuickPaymentAction)

        // 15. Search & Filter
        json.put("searchByName", searchByName)
        json.put("searchByPhone", searchByPhone)
        json.put("searchByPanVat", searchByPanVat)
        json.put("searchByAddress", searchByAddress)
        json.put("defaultFilterOption", defaultFilterOption.id)
        json.put("defaultSortOption", defaultSortOption.id)

        // 16. Import / Export
        json.put("allowExportPartyDirectory", allowExportPartyDirectory)
        json.put("includeContactInExport", includeContactInExport)
        json.put("includeFinancialSummaryInExport", includeFinancialSummaryInExport)

        // 17. Permissions
        json.put("allowStaffToAddParty", allowStaffToAddParty)
        json.put("allowStaffToEditParty", allowStaffToEditParty)
        json.put("allowStaffToDeleteParty", allowStaffToDeleteParty)
        json.put("allowStaffToViewBalance", allowStaffToViewBalance)

        // 18. Advanced
        json.put("autoGeneratePartyCode", autoGeneratePartyCode)
        json.put("partyCodePrefix", partyCodePrefix)
        json.put("preventDeletePartyWithTransactions", preventDeletePartyWithTransactions)
        json.put("requireDeactivationReason", requireDeactivationReason)
        json.put("enablePartyAuditTrail", enablePartyAuditTrail)

        return json.toString()
    }

    companion object {
        /**
         * Creates intelligent defaults tailored to specific Business Types and Personal Mode.
         */
        fun createDefaultsFor(preset: BusinessPartyPreset): PartySettings {
            return when (preset) {
                BusinessPartyPreset.RETAIL -> PartySettings(
                    businessPreset = preset,
                    customerTerminology = "Customer",
                    supplierTerminology = "Supplier",
                    allowCustomer = true,
                    allowSupplier = true,
                    defaultCustomerCategory = "Retailer",
                    defaultSupplierCategory = "Wholesaler",
                    defaultCreditLimit = 25000.0,
                    defaultPaymentTermDays = 15,
                    allowWalkInCustomer = true,
                    showPanVatInForm = true
                )
                BusinessPartyPreset.WHOLESALE -> PartySettings(
                    businessPreset = preset,
                    customerTerminology = "Customer",
                    supplierTerminology = "Supplier",
                    allowCustomer = true,
                    allowSupplier = true,
                    defaultCustomerCategory = "Wholesaler",
                    defaultSupplierCategory = "Manufacturer",
                    defaultCreditLimit = 200000.0,
                    defaultPaymentTermDays = 30,
                    warnCreditLimitExceeded = true,
                    showPanVatInForm = true,
                    showCodeInForm = true
                )
                BusinessPartyPreset.PHARMACY -> PartySettings(
                    businessPreset = preset,
                    customerTerminology = "Patient / Customer",
                    supplierTerminology = "Pharma Distributor",
                    allowCustomer = true,
                    allowSupplier = true,
                    defaultCustomerCategory = "Walk-in Patient",
                    defaultSupplierCategory = "Pharmaceutical Distributor",
                    defaultCreditLimit = 15000.0,
                    defaultPaymentTermDays = 15,
                    showPanVatInForm = true,
                    allowWalkInCustomer = true
                )
                BusinessPartyPreset.SERVICE -> PartySettings(
                    businessPreset = preset,
                    customerTerminology = "Client",
                    supplierTerminology = "Vendor",
                    allowCustomer = true,
                    allowSupplier = true,
                    defaultCustomerCategory = "Corporate Client",
                    defaultSupplierCategory = "Service Vendor",
                    defaultCreditLimit = 100000.0,
                    defaultPaymentTermDays = 30,
                    showPanVatInForm = true,
                    allowWalkInCustomer = false
                )
                BusinessPartyPreset.DISTRIBUTOR -> PartySettings(
                    businessPreset = preset,
                    customerTerminology = "Retail Partner",
                    supplierTerminology = "Manufacturer",
                    allowCustomer = true,
                    allowSupplier = true,
                    defaultCustomerCategory = "Retailer",
                    defaultSupplierCategory = "Manufacturer",
                    defaultCreditLimit = 150000.0,
                    defaultPaymentTermDays = 45,
                    warnCreditLimitExceeded = true,
                    showCodeInForm = true
                )
                BusinessPartyPreset.PERSONAL -> PartySettings(
                    businessPreset = preset,
                    customerTerminology = "Person / Contact",
                    supplierTerminology = "Contact",
                    allowCustomer = true,
                    allowSupplier = false,
                    allowGeneralParty = true,
                    defaultCustomerCategory = "Friend / Family",
                    defaultSupplierCategory = "General",
                    defaultCreditLimit = 0.0,
                    enableCreditLimit = false,
                    showPanVatInForm = false,
                    showCodeInForm = false,
                    showOpeningBalanceInForm = true,
                    allowCreditCustomer = false,
                    showTotalPurchaseFromParty = false
                )
                BusinessPartyPreset.GENERAL -> PartySettings(
                    businessPreset = preset,
                    customerTerminology = "Customer",
                    supplierTerminology = "Supplier",
                    defaultCreditLimit = 50000.0,
                    defaultPaymentTermDays = 30
                )
            }
        }

        /**
         * Reconstructs PartySettings from JSON safely falling back to defaults.
         */
        fun fromJsonString(jsonString: String?): PartySettings? {
            if (jsonString.isNullOrBlank()) return null
            return try {
                val json = JSONObject(jsonString)
                val preset = BusinessPartyPreset.fromId(json.optString("businessPreset", "general"))
                PartySettings(
                    businessPreset = preset,
                    enablePartyModule = json.optBoolean("enablePartyModule", true),
                    allowCustomer = json.optBoolean("allowCustomer", true),
                    allowSupplier = json.optBoolean("allowSupplier", true),
                    allowGeneralParty = json.optBoolean("allowGeneralParty", true),
                    allowClient = json.optBoolean("allowClient", true),
                    allowVendor = json.optBoolean("allowVendor", true),
                    allowQuickAddParty = json.optBoolean("allowQuickAddParty", true),
                    allowDuplicatePartyName = json.optBoolean("allowDuplicatePartyName", false),
                    confirmBeforeDeleteParty = json.optBoolean("confirmBeforeDeleteParty", true),
                    confirmBeforeDeactivate = json.optBoolean("confirmBeforeDeactivate", true),
                    allowEditParty = json.optBoolean("allowEditParty", true),
                    allowDeleteParty = json.optBoolean("allowDeleteParty", true),
                    allowDeactivateParty = json.optBoolean("allowDeactivateParty", true),

                    defaultPartyType = json.optString("defaultPartyType", "Customer"),
                    customerTerminology = json.optString("customerTerminology", preset.defaultCustomerTerm),
                    supplierTerminology = json.optString("supplierTerminology", preset.defaultSupplierTerm),
                    showPartyTypeBadge = json.optBoolean("showPartyTypeBadge", true),
                    allowBothCustomerAndSupplier = json.optBoolean("allowBothCustomerAndSupplier", true),

                    enableCustomer = json.optBoolean("enableCustomer", true),
                    customerNameRequired = json.optBoolean("customerNameRequired", true),
                    customerPhoneRequired = json.optBoolean("customerPhoneRequired", false),
                    customerAddressRequired = json.optBoolean("customerAddressRequired", false),
                    customerCodePrefix = json.optString("customerCodePrefix", "CUST-"),
                    defaultCustomerCategory = json.optString("defaultCustomerCategory", "Retailer"),
                    allowCreditCustomer = json.optBoolean("allowCreditCustomer", true),
                    allowWalkInCustomer = json.optBoolean("allowWalkInCustomer", true),
                    showCustomerBalance = json.optBoolean("showCustomerBalance", true),
                    showCustomerCreditLimit = json.optBoolean("showCustomerCreditLimit", true),
                    showCustomerOutstanding = json.optBoolean("showCustomerOutstanding", true),
                    showCustomerTransactionCount = json.optBoolean("showCustomerTransactionCount", true),

                    enableSupplier = json.optBoolean("enableSupplier", true),
                    supplierNameRequired = json.optBoolean("supplierNameRequired", true),
                    supplierPhoneRequired = json.optBoolean("supplierPhoneRequired", false),
                    supplierAddressRequired = json.optBoolean("supplierAddressRequired", false),
                    supplierCodePrefix = json.optString("supplierCodePrefix", "SUPP-"),
                    defaultSupplierCategory = json.optString("defaultSupplierCategory", "Wholesaler"),
                    allowCreditSupplier = json.optBoolean("allowCreditSupplier", true),
                    showSupplierBalance = json.optBoolean("showSupplierBalance", true),
                    showSupplierOutstanding = json.optBoolean("showSupplierOutstanding", true),
                    showSupplierTransactionCount = json.optBoolean("showSupplierTransactionCount", true),

                    showCodeInForm = json.optBoolean("showCodeInForm", true),
                    showPhoneInForm = json.optBoolean("showPhoneInForm", true),
                    showAltPhoneInForm = json.optBoolean("showAltPhoneInForm", true),
                    showEmailInForm = json.optBoolean("showEmailInForm", true),
                    showContactPersonInForm = json.optBoolean("showContactPersonInForm", true),
                    showPanVatInForm = json.optBoolean("showPanVatInForm", true),
                    showAddressInForm = json.optBoolean("showAddressInForm", true),
                    showCityInForm = json.optBoolean("showCityInForm", true),
                    showOpeningBalanceInForm = json.optBoolean("showOpeningBalanceInForm", true),
                    showCreditLimitInForm = json.optBoolean("showCreditLimitInForm", true),
                    showCategoryInForm = json.optBoolean("showCategoryInForm", true),
                    showNotesInForm = json.optBoolean("showNotesInForm", true),

                    showPhoneOnCard = json.optBoolean("showPhoneOnCard", true),
                    showContactPersonOnCard = json.optBoolean("showContactPersonOnCard", true),
                    validatePhoneNumberFormat = json.optBoolean("validatePhoneNumberFormat", true),
                    validateEmailFormat = json.optBoolean("validateEmailFormat", false),
                    allowMultipleContactNumbers = json.optBoolean("allowMultipleContactNumbers", true),

                    showAddressOnCard = json.optBoolean("showAddressOnCard", true),
                    addressRequired = json.optBoolean("addressRequired", false),
                    showCityOnCard = json.optBoolean("showCityOnCard", true),
                    multiLineAddressInput = json.optBoolean("multiLineAddressInput", true),

                    showOpeningBalance = json.optBoolean("showOpeningBalance", true),
                    showCurrentBalance = json.optBoolean("showCurrentBalance", true),
                    showReceivableDr = json.optBoolean("showReceivableDr", true),
                    showPayableCr = json.optBoolean("showPayableCr", true),
                    showNetBalance = json.optBoolean("showNetBalance", true),
                    showCreditUtilization = json.optBoolean("showCreditUtilization", true),
                    showTotalSalesToParty = json.optBoolean("showTotalSalesToParty", true),
                    showTotalPurchaseFromParty = json.optBoolean("showTotalPurchaseFromParty", true),
                    showTotalReceivedFromParty = json.optBoolean("showTotalReceivedFromParty", true),
                    showTotalPaidToParty = json.optBoolean("showTotalPaidToParty", true),

                    enableCreditLimit = json.optBoolean("enableCreditLimit", true),
                    defaultCreditLimit = json.optDouble("defaultCreditLimit", 50000.0),
                    warnCreditLimitExceeded = json.optBoolean("warnCreditLimitExceeded", true),
                    allowOverCreditLimit = json.optBoolean("allowOverCreditLimit", true),
                    enableCreditSales = json.optBoolean("enableCreditSales", true),
                    enableCreditPurchase = json.optBoolean("enableCreditPurchase", true),
                    defaultPaymentTermDays = json.optInt("defaultPaymentTermDays", 30),
                    showDueDateOnLedger = json.optBoolean("showDueDateOnLedger", true),

                    enableOpeningBalance = json.optBoolean("enableOpeningBalance", true),
                    allowOpeningReceivable = json.optBoolean("allowOpeningReceivable", true),
                    allowOpeningPayable = json.optBoolean("allowOpeningPayable", true),
                    allowOpeningBalanceAdjustment = json.optBoolean("allowOpeningBalanceAdjustment", true),
                    showOpeningBalanceInLedger = json.optBoolean("showOpeningBalanceInLedger", true),

                    selectPartyInSales = json.optBoolean("selectPartyInSales", true),
                    requireCustomerForCreditSale = json.optBoolean("requireCustomerForCreditSale", true),
                    showBalanceDuringSale = json.optBoolean("showBalanceDuringSale", true),
                    showCreditLimitDuringSale = json.optBoolean("showCreditLimitDuringSale", true),
                    showOutstandingDuringSale = json.optBoolean("showOutstandingDuringSale", true),
                    selectSupplierInPurchase = json.optBoolean("selectSupplierInPurchase", true),
                    showSupplierBalanceDuringPurchase = json.optBoolean("showSupplierBalanceDuringPurchase", true),
                    showOutstandingDuringPaymentIn = json.optBoolean("showOutstandingDuringPaymentIn", true),
                    showOutstandingDuringPaymentOut = json.optBoolean("showOutstandingDuringPaymentOut", true),

                    showOutstandingSummaryCard = json.optBoolean("showOutstandingSummaryCard", true),
                    showLedgerTab = json.optBoolean("showLedgerTab", true),
                    showSalesHistoryTab = json.optBoolean("showSalesHistoryTab", true),
                    showPurchaseHistoryTab = json.optBoolean("showPurchaseHistoryTab", true),
                    showPaymentHistoryTab = json.optBoolean("showPaymentHistoryTab", true),
                    showInvoicesTab = json.optBoolean("showInvoicesTab", true),
                    showActivityTimelineTab = json.optBoolean("showActivityTimelineTab", true),

                    enablePartyReminders = json.optBoolean("enablePartyReminders", true),
                    reminderBeforeDueDateDays = json.optInt("reminderBeforeDueDateDays", 3),
                    reminderAfterDueDateDays = json.optInt("reminderAfterDueDateDays", 1),
                    defaultReminderChannel = ReminderChannel.fromId(json.optString("defaultReminderChannel", "whatsapp")),
                    defaultReminderMessageTemplate = json.optString("defaultReminderMessageTemplate", "Dear {party_name}, this is a gentle reminder regarding your pending balance of Rs. {balance} with Atri Khata. Thank you!"),

                    showBalanceOnPartyCard = json.optBoolean("showBalanceOnPartyCard", true),
                    showCreditLimitOnPartyCard = json.optBoolean("showCreditLimitOnPartyCard", true),
                    showLastTransactionOnPartyCard = json.optBoolean("showLastTransactionOnPartyCard", true),
                    showTransactionCountOnPartyCard = json.optBoolean("showTransactionCountOnPartyCard", true),
                    enableQuickCallAction = json.optBoolean("enableQuickCallAction", true),
                    enableQuickWhatsAppAction = json.optBoolean("enableQuickWhatsAppAction", true),
                    enableQuickSaleAction = json.optBoolean("enableQuickSaleAction", true),
                    enableQuickPaymentAction = json.optBoolean("enableQuickPaymentAction", true),

                    searchByName = json.optBoolean("searchByName", true),
                    searchByPhone = json.optBoolean("searchByPhone", true),
                    searchByPanVat = json.optBoolean("searchByPanVat", true),
                    searchByAddress = json.optBoolean("searchByAddress", true),
                    defaultFilterOption = PartyFilterOption.fromId(json.optString("defaultFilterOption", "all")),
                    defaultSortOption = PartySortOption.fromId(json.optString("defaultSortOption", "name_asc")),

                    allowExportPartyDirectory = json.optBoolean("allowExportPartyDirectory", true),
                    includeContactInExport = json.optBoolean("includeContactInExport", true),
                    includeFinancialSummaryInExport = json.optBoolean("includeFinancialSummaryInExport", true),

                    allowStaffToAddParty = json.optBoolean("allowStaffToAddParty", true),
                    allowStaffToEditParty = json.optBoolean("allowStaffToEditParty", true),
                    allowStaffToDeleteParty = json.optBoolean("allowStaffToDeleteParty", false),
                    allowStaffToViewBalance = json.optBoolean("allowStaffToViewBalance", true),

                    autoGeneratePartyCode = json.optBoolean("autoGeneratePartyCode", true),
                    partyCodePrefix = json.optString("partyCodePrefix", "PTY-"),
                    preventDeletePartyWithTransactions = json.optBoolean("preventDeletePartyWithTransactions", true),
                    requireDeactivationReason = json.optBoolean("requireDeactivationReason", false),
                    enablePartyAuditTrail = json.optBoolean("enablePartyAuditTrail", true)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
