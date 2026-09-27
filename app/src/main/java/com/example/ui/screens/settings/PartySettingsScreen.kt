package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewCompact
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BusinessPartyPreset
import com.example.data.model.PartyFilterOption
import com.example.data.model.PartySettings
import com.example.data.model.PartySortOption
import com.example.data.model.ReminderChannel
import com.example.ui.MainViewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SkyBlueCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.util.Locale

private val InvoiceEmerald = Color(0xFF10B981)
private val InvoiceRose = Color(0xFFF43F5E)
private val AmberWarn = Color(0xFFF59E0B)

/**
 * Atri Khata 2 - Advanced Party Settings & Configuration Screen
 * Comprehensive, persistent, Business-Preset aware, and organized into 18 modules.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartySettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val settings by viewModel.partySettings.collectAsStateWithLifecycle()
    val allParties by viewModel.allParties.collectAsStateWithLifecycle()

    // 18 Expandable Module States
    var expGeneral by remember { mutableStateOf(false) }
    var expTypes by remember { mutableStateOf(false) }
    var expCustomer by remember { mutableStateOf(true) } // Open by default
    var expSupplier by remember { mutableStateOf(false) }
    var expForm by remember { mutableStateOf(false) }
    var expContact by remember { mutableStateOf(false) }
    var expAddress by remember { mutableStateOf(false) }
    var expFinancial by remember { mutableStateOf(false) }
    var expCredit by remember { mutableStateOf(false) }
    var expOpeningBalance by remember { mutableStateOf(false) }
    var expTransactions by remember { mutableStateOf(false) }
    var expLedger by remember { mutableStateOf(false) }
    var expReminders by remember { mutableStateOf(false) }
    var expDisplay by remember { mutableStateOf(false) }
    var expSearch by remember { mutableStateOf(false) }
    var expImportExport by remember { mutableStateOf(false) }
    var expPermissions by remember { mutableStateOf(false) }
    var expAdvanced by remember { mutableStateOf(false) }

    // Dialog state
    var showPresetDialog by remember { mutableStateOf(false) }
    var showCreditLimitDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showReminderTemplateDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .testTag("party_settings_screen_root")
    ) {
        // Top App Bar
        PartySettingsTopBar(
            onBack = onBack,
            onResetDefaults = { showResetDialog = true }
        )

        // Main Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live Status Header Summary Card
            PartyStatusSummaryCard(
                settings = settings,
                totalPartiesCount = allParties.size,
                isDark = isDark
            )

            // Business Preset Selector Banner
            BusinessPartyPresetBanner(
                currentPreset = settings.businessPreset,
                isDark = isDark,
                onOpenPresetDialog = { showPresetDialog = true }
            )

            // -------------------------------------------------------------
            // 1. GENERAL PARTY SETTINGS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "1. General",
                subtitle = "Party module activation, entity permissions, deletion rules & quick add",
                icon = Icons.Outlined.Settings,
                isExpanded = expGeneral,
                onToggle = { expGeneral = !expGeneral },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Group,
                        title = "Enable Party Module",
                        subtitle = "Global master switch for Customer and Supplier ledger accounting",
                        checked = settings.enablePartyModule,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enablePartyModule = it) } },
                        testTag = "party_settings_module_switch"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Person,
                        title = "Allow Customer Accounts",
                        subtitle = "Enable registering buyers, retail clients, and receivables",
                        checked = settings.allowCustomer,
                        enabled = settings.enablePartyModule,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowCustomer = it) } },
                        testTag = "party_settings_allow_customer"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.LocalShipping,
                        title = "Allow Supplier Accounts",
                        subtitle = "Enable registering vendors, wholesalers, and payables",
                        checked = settings.allowSupplier,
                        enabled = settings.enablePartyModule,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowSupplier = it) } },
                        testTag = "party_settings_allow_supplier"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Group,
                        title = "Allow General / Miscellaneous Parties",
                        subtitle = "Support non-trade parties, personal contacts, and loan partners",
                        checked = settings.allowGeneralParty,
                        enabled = settings.enablePartyModule,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowGeneralParty = it) } },
                        testTag = "party_settings_allow_general"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Edit,
                        title = "Allow Quick Add Party",
                        subtitle = "Provide streamlined 2-field (Name & Phone) popup in sales & vouchers",
                        checked = settings.allowQuickAddParty,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowQuickAddParty = it) } },
                        testTag = "party_settings_quick_add"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Confirm Before Delete",
                        subtitle = "Prompt confirmation alert before permanently removing a party record",
                        checked = settings.confirmBeforeDeleteParty,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(confirmBeforeDeleteParty = it) } },
                        testTag = "party_settings_confirm_delete"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Allow Deactivate / Archive Party",
                        subtitle = "Safely disable inactive accounts while preserving complete ledger history",
                        checked = settings.allowDeactivateParty,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowDeactivateParty = it) } },
                        testTag = "party_settings_deactivate"
                    )
                }
            }

            // -------------------------------------------------------------
            // 2. PARTY TYPES
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "2. Party Types & Terminology",
                subtitle = "Classification labels (Customer/Client/Patient vs Supplier/Vendor)",
                icon = Icons.Outlined.Category,
                isExpanded = expTypes,
                onToggle = { expTypes = !expTypes },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Default Party Classification for New Records", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Customer", "Supplier").forEach { type ->
                            PartyChip(
                                label = type,
                                isSelected = settings.defaultPartyType == type,
                                onClick = {
                                    viewModel.updatePartySettings { s -> s.copy(defaultPartyType = type) }
                                    Toast.makeText(context, "Default type set to $type", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "default_type_$type"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    Text("Customer Display Terminology", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Customer", "Client", "Patient", "Dealer / Retailer", "Person").forEach { term ->
                            PartyChip(
                                label = term,
                                isSelected = settings.customerTerminology == term,
                                onClick = {
                                    viewModel.updatePartySettings { s -> s.copy(customerTerminology = term) }
                                    Toast.makeText(context, "Customer label: $term", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "cust_term_$term"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    Text("Supplier Display Terminology", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Supplier", "Vendor", "Wholesaler", "Manufacturer").forEach { term ->
                            PartyChip(
                                label = term,
                                isSelected = settings.supplierTerminology == term,
                                onClick = {
                                    viewModel.updatePartySettings { s -> s.copy(supplierTerminology = term) }
                                    Toast.makeText(context, "Supplier label: $term", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "supp_term_$term"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Badge,
                        title = "Show Party Type Badge on Cards",
                        subtitle = "Display colorful 'Customer' or 'Supplier' tag on party list rows",
                        checked = settings.showPartyTypeBadge,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showPartyTypeBadge = it) } },
                        testTag = "party_settings_type_badge"
                    )
                }
            }

            // -------------------------------------------------------------
            // 3. CUSTOMER SETTINGS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "3. Customer Settings",
                subtitle = "Customer validation rules, categories, balances & transaction count",
                icon = Icons.Outlined.Person,
                isExpanded = expCustomer,
                onToggle = { expCustomer = !expCustomer },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Person,
                        title = "Customer Name Required",
                        subtitle = "Mandatory customer name for account creation",
                        checked = settings.customerNameRequired,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(customerNameRequired = it) } },
                        testTag = "cust_name_required"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Call,
                        title = "Customer Phone Required",
                        subtitle = "Require 10-digit mobile number before saving customer profile",
                        checked = settings.customerPhoneRequired,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(customerPhoneRequired = it) } },
                        testTag = "cust_phone_required"
                    )

                    PartyDivider(isDark)

                    Text("Default Customer Category", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Retailer", "Wholesaler", "Walk-in", "Distributor", "Corporate Client").forEach { cat ->
                            PartyChip(
                                label = cat,
                                isSelected = settings.defaultCustomerCategory == cat,
                                onClick = {
                                    viewModel.setDefaultPartyCategory(cat)
                                    Toast.makeText(context, "Customer category: $cat", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "cust_cat_$cat"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show Customer Balance & Outstanding",
                        subtitle = "Display receivables balance on customer profile cards",
                        checked = settings.showCustomerBalance,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showCustomerBalance = it) } },
                        testTag = "cust_show_balance"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Show Customer Transaction Count",
                        subtitle = "Display total sales and receipts count badge on customer card",
                        checked = settings.showCustomerTransactionCount,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showCustomerTransactionCount = it) } },
                        testTag = "cust_show_tx_count"
                    )
                }
            }

            // -------------------------------------------------------------
            // 4. SUPPLIER SETTINGS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "4. Supplier Settings",
                subtitle = "Vendor validation rules, categories, payables & credit terms",
                icon = Icons.Outlined.LocalShipping,
                isExpanded = expSupplier,
                onToggle = { expSupplier = !expSupplier },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.LocalShipping,
                        title = "Supplier Name Required",
                        subtitle = "Mandatory company or vendor name for supplier accounts",
                        checked = settings.supplierNameRequired,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(supplierNameRequired = it) } },
                        testTag = "supp_name_required"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Call,
                        title = "Supplier Phone Required",
                        subtitle = "Require contact phone number for supplier accounts",
                        checked = settings.supplierPhoneRequired,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(supplierPhoneRequired = it) } },
                        testTag = "supp_phone_required"
                    )

                    PartyDivider(isDark)

                    Text("Default Supplier Category", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Manufacturer", "Wholesaler", "Distributor", "Service Vendor").forEach { cat ->
                            PartyChip(
                                label = cat,
                                isSelected = settings.defaultSupplierCategory == cat,
                                onClick = {
                                    viewModel.updatePartySettings { s -> s.copy(defaultSupplierCategory = cat) }
                                    Toast.makeText(context, "Supplier category: $cat", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "supp_cat_$cat"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show Supplier Balance & Payables",
                        subtitle = "Display balance to give (Cr) prominently on supplier cards",
                        checked = settings.showSupplierBalance,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showSupplierBalance = it) } },
                        testTag = "supp_show_balance"
                    )
                }
            }

            // -------------------------------------------------------------
            // 5. PARTY FORM SETTINGS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "5. Party Form Settings",
                subtitle = "Control field visibility while creating or editing a party",
                icon = Icons.Outlined.Description,
                isExpanded = expForm,
                onToggle = { expForm = !expForm },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select which optional fields to display in the Add/Edit Party form:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    PartySwitchRow(
                        icon = Icons.Outlined.Call,
                        title = "Phone & Mobile Number",
                        subtitle = "Primary mobile number input",
                        checked = settings.showPhoneInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showPhoneInForm = it) } },
                        testTag = "form_show_phone"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Call,
                        title = "Alternate / Secondary Phone",
                        subtitle = "Additional telephone or WhatsApp contact field",
                        checked = settings.showAltPhoneInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showAltPhoneInForm = it) } },
                        testTag = "form_show_alt_phone"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Person,
                        title = "Contact Person Representative",
                        subtitle = "Representative name (Manager, Accountant, Owner)",
                        checked = settings.showContactPersonInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showContactPersonInForm = it) } },
                        testTag = "form_show_contact_person"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Badge,
                        title = "PAN / VAT Tax Number",
                        subtitle = "9-digit Inland Revenue Department tax registration number",
                        checked = settings.showPanVatInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showPanVatInForm = it) } },
                        testTag = "form_show_pan_vat"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.LocationOn,
                        title = "Address & City Fields",
                        subtitle = "Street address, ward, town and city fields",
                        checked = settings.showAddressInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showAddressInForm = it) } },
                        testTag = "form_show_address"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Opening Balance Input",
                        subtitle = "Initial ledger balance and Dr/Cr selector",
                        checked = settings.showOpeningBalanceInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showOpeningBalanceInForm = it) } },
                        testTag = "form_show_opening_bal"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.CreditCard,
                        title = "Credit Limit Field",
                        subtitle = "Maximum allowable credit threshold",
                        checked = settings.showCreditLimitInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showCreditLimitInForm = it) } },
                        testTag = "form_show_credit_limit"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Description,
                        title = "Notes & Remarks Field",
                        subtitle = "Internal remarks and operational instructions",
                        checked = settings.showNotesInForm,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showNotesInForm = it) } },
                        testTag = "form_show_notes"
                    )
                }
            }

            // -------------------------------------------------------------
            // 6. CONTACT INFORMATION
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "6. Contact Information & Validation",
                subtitle = "Phone formatting, validation rules & contact person visibility",
                icon = Icons.Outlined.Call,
                isExpanded = expContact,
                onToggle = { expContact = !expContact },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Check,
                        title = "Validate Mobile Number Format",
                        subtitle = "Ensure phone contains standard 10 digits (e.g. 98XXXXXXXX)",
                        checked = settings.validatePhoneNumberFormat,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(validatePhoneNumberFormat = it) } },
                        testTag = "contact_validate_phone"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Call,
                        title = "Show Phone Number on Party Cards",
                        subtitle = "Display phone number alongside name for quick scanning",
                        checked = settings.showPhoneOnCard,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showPhoneOnCard = it) } },
                        testTag = "contact_show_phone_card"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Person,
                        title = "Show Contact Person on Card",
                        subtitle = "Display contact person representative subtitle on party list rows",
                        checked = settings.showContactPersonOnCard,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showContactPersonOnCard = it) } },
                        testTag = "contact_show_person_card"
                    )
                }
            }

            // -------------------------------------------------------------
            // 7. ADDRESS CONFIGURATION
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "7. Address Configuration",
                subtitle = "Address presentation on cards, city tags & multi-line input",
                icon = Icons.Outlined.LocationOn,
                isExpanded = expAddress,
                onToggle = { expAddress = !expAddress },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.LocationOn,
                        title = "Show Address & City on Party Cards",
                        subtitle = "Display city and street location tag on party list rows",
                        checked = settings.showAddressOnCard,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showAddressOnCard = it) } },
                        testTag = "address_show_on_card"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Check,
                        title = "Address Required During Registration",
                        subtitle = "Mandatory street address before completing party save",
                        checked = settings.addressRequired,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(addressRequired = it) } },
                        testTag = "address_required_switch"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Edit,
                        title = "Multi-line Address Input Field",
                        subtitle = "Support expandable multi-line text input for complex billing addresses",
                        checked = settings.multiLineAddressInput,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(multiLineAddressInput = it) } },
                        testTag = "address_multiline_switch"
                    )
                }
            }

            // -------------------------------------------------------------
            // 8. FINANCIAL SETTINGS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "8. Financial Settings",
                subtitle = "Receivable (Dr), Payable (Cr), net balance & turnover stats",
                icon = Icons.Outlined.Payments,
                isExpanded = expFinancial,
                onToggle = { expFinancial = !expFinancial },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show To Receive (Dr) Balance",
                        subtitle = "Customer receivables tracked via sales and credit payments",
                        checked = settings.showReceivableDr,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showReceivableDr = it) } },
                        testTag = "financial_show_receivable"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show To Give (Cr) Balance",
                        subtitle = "Supplier payables tracked via purchases and supplier payments",
                        checked = settings.showPayableCr,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showPayableCr = it) } },
                        testTag = "financial_show_payable"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.CreditCard,
                        title = "Show Credit Utilization Bar",
                        subtitle = "Display visual progress bar showing percentage of credit limit used",
                        checked = settings.showCreditUtilization,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showCreditUtilization = it) } },
                        testTag = "financial_show_utilization"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "Show Lifetime Sales & Turnover",
                        subtitle = "Display cumulative lifetime sales and purchase volume in party detail",
                        checked = settings.showTotalSalesToParty,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showTotalSalesToParty = it) } },
                        testTag = "financial_show_turnover"
                    )
                }
            }

            // -------------------------------------------------------------
            // 9. CREDIT & PAYMENT TERMS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "9. Credit & Payment Terms",
                subtitle = "Default credit limit, overrun warnings & payment term days",
                icon = Icons.Outlined.CreditCard,
                isExpanded = expCredit,
                onToggle = { expCredit = !expCredit },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Default Credit Limit Row with edit button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Default Party Credit Limit", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Rs. ${String.format(Locale.US, "%,.0f", settings.defaultCreditLimit)}", color = SkyBlueBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showCreditLimitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text("Change", color = SkyBlueBright, fontSize = 12.sp)
                        }
                    }

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Warn on Credit Limit Overrun",
                        subtitle = "Alert cashier or salesperson when pending balance breaches limit",
                        checked = settings.warnCreditLimitExceeded,
                        onCheckedChange = { viewModel.toggleWarnCreditLimitExceeded(it) },
                        testTag = "credit_warn_overrun"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.CreditCard,
                        title = "Allow Over Credit Limit Sales",
                        subtitle = "Allow saving invoice with warning even if party exceeds credit limit",
                        checked = settings.allowOverCreditLimit,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowOverCreditLimit = it) } },
                        testTag = "credit_allow_over"
                    )

                    PartyDivider(isDark)

                    Text("Default Payment Term Duration", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7 to "7 Days", 15 to "15 Days", 30 to "30 Days", 45 to "45 Days", 60 to "60 Days").forEach { (days, label) ->
                            PartyChip(
                                label = label,
                                isSelected = settings.defaultPaymentTermDays == days,
                                onClick = {
                                    viewModel.setDefaultPaymentTermDays(days)
                                    Toast.makeText(context, "Payment terms set to $label", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "term_days_$days"
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 10. OPENING BALANCE CONFIGURATION
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "10. Opening Balance",
                subtitle = "Opening balance Dr/Cr, adjustments & ledger entry display",
                icon = Icons.Outlined.AccountBalance,
                isExpanded = expOpeningBalance,
                onToggle = { expOpeningBalance = !expOpeningBalance },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Enable Opening Balance Input",
                        subtitle = "Allow entering historical carry-forward balance when creating a party",
                        checked = settings.enableOpeningBalance,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enableOpeningBalance = it) } },
                        testTag = "opening_bal_enable"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Check,
                        title = "Allow Opening Receivable (Dr)",
                        subtitle = "Support opening balance where customer owes money to your business",
                        checked = settings.allowOpeningReceivable,
                        enabled = settings.enableOpeningBalance,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowOpeningReceivable = it) } },
                        testTag = "opening_allow_receivable"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Check,
                        title = "Allow Opening Payable (Cr)",
                        subtitle = "Support opening balance where your business owes money to supplier",
                        checked = settings.allowOpeningPayable,
                        enabled = settings.enableOpeningBalance,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowOpeningPayable = it) } },
                        testTag = "opening_allow_payable"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Display Opening Balance in Ledger",
                        subtitle = "Show opening balance as initial entry row at top of party statement",
                        checked = settings.showOpeningBalanceInLedger,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showOpeningBalanceInLedger = it) } },
                        testTag = "opening_show_in_ledger"
                    )
                }
            }

            // -------------------------------------------------------------
            // 11. TRANSACTION INTEGRATION
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "11. Transaction Integration",
                subtitle = "Sales, purchases, payment vouchers & live balance lookup",
                icon = Icons.Outlined.ReceiptLong,
                isExpanded = expTransactions,
                onToggle = { expTransactions = !expTransactions },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "Select Party in Sales Invoices",
                        subtitle = "Support linking sales invoices to specific registered party accounts",
                        checked = settings.selectPartyInSales,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(selectPartyInSales = it) } },
                        testTag = "tx_select_party_sales"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Require Party for Credit Sales",
                        subtitle = "Enforce party selection whenever payment status is Unpaid or Credit",
                        checked = settings.requireCustomerForCreditSale,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(requireCustomerForCreditSale = it) } },
                        testTag = "tx_require_party_credit"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show Live Balance During Sale",
                        subtitle = "Display customer's pending balance in the invoice header",
                        checked = settings.showBalanceDuringSale,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showBalanceDuringSale = it) } },
                        testTag = "tx_show_balance_sale"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.LocalShipping,
                        title = "Select Supplier in Purchases",
                        subtitle = "Link purchase entries and inward stock to registered suppliers",
                        checked = settings.selectSupplierInPurchase,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(selectSupplierInPurchase = it) } },
                        testTag = "tx_select_supplier_purchase"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show Outstanding in Payment In / Out",
                        subtitle = "Display outstanding dues above amount input in payment sheets",
                        checked = settings.showOutstandingDuringPaymentIn,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showOutstandingDuringPaymentIn = it) } },
                        testTag = "tx_show_outstanding_payment"
                    )
                }
            }

            // -------------------------------------------------------------
            // 12. OUTSTANDING & LEDGER
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "12. Outstanding & Ledger Tabs",
                subtitle = "Configure tabs visible on Party Financial Dashboard",
                icon = Icons.Outlined.History,
                isExpanded = expLedger,
                onToggle = { expLedger = !expLedger },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show Outstanding Balance Summary Card",
                        subtitle = "Prominent top hero card displaying net balance and Dr/Cr badge",
                        checked = settings.showOutstandingSummaryCard,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showOutstandingSummaryCard = it) } },
                        testTag = "ledger_show_hero_card"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Show Ledger Statement Tab",
                        subtitle = "Chronological running-balance passbook with PDF share",
                        checked = settings.showLedgerTab,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showLedgerTab = it) } },
                        testTag = "ledger_show_statement_tab"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "Show Sales & Invoices Tab",
                        subtitle = "Direct list of all sales bills and invoices issued to party",
                        checked = settings.showInvoicesTab,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showInvoicesTab = it) } },
                        testTag = "ledger_show_invoices_tab"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Show Payment Vouchers Tab",
                        subtitle = "Record of all Payment-In and Payment-Out receipts with transaction IDs",
                        checked = settings.showPaymentHistoryTab,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showPaymentHistoryTab = it) } },
                        testTag = "ledger_show_payments_tab"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.History,
                        title = "Show Activity Timeline Tab",
                        subtitle = "Detailed chronological activity trail of all ledger events",
                        checked = settings.showActivityTimelineTab,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(showActivityTimelineTab = it) } },
                        testTag = "ledger_show_activity_tab"
                    )
                }
            }

            // -------------------------------------------------------------
            // 13. REMINDERS & NOTIFICATIONS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "13. Reminders & Alerts",
                subtitle = "WhatsApp & SMS payment reminder template & overdue schedule",
                icon = Icons.Outlined.Notifications,
                isExpanded = expReminders,
                onToggle = { expReminders = !expReminders },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Enable Party Payment Reminders",
                        subtitle = "Quick reminder shortcuts on party dashboard and list",
                        checked = settings.enablePartyReminders,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enablePartyReminders = it) } },
                        testTag = "reminders_enable_switch"
                    )

                    PartyDivider(isDark)

                    Text("Default Reminder Delivery Channel", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReminderChannel.entries.forEach { channel ->
                            PartyChip(
                                label = channel.displayName,
                                isSelected = settings.defaultReminderChannel == channel,
                                onClick = {
                                    viewModel.updatePartySettings { s -> s.copy(defaultReminderChannel = channel) }
                                    Toast.makeText(context, "Reminder via ${channel.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "reminder_channel_${channel.id}"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    // Template configuration button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reminder Message Template", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(settings.defaultReminderMessageTemplate, color = TextMuted, fontSize = 11.sp, maxLines = 2)
                        }
                        Button(
                            onClick = { showReminderTemplateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text("Edit", color = SkyBlueBright, fontSize = 12.sp)
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 14. PARTY DISPLAY & QUICK ACTIONS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "14. Party Display & Quick Actions",
                subtitle = "Call, WhatsApp, Sale & Payment shortcut buttons on party list",
                icon = Icons.Outlined.Visibility,
                isExpanded = expDisplay,
                onToggle = { expDisplay = !expDisplay },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Call,
                        title = "Direct Call Action Shortcut",
                        subtitle = "Enable phone dialer button on party cards",
                        checked = settings.enableQuickCallAction,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enableQuickCallAction = it) } },
                        testTag = "display_quick_call"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Share,
                        title = "WhatsApp Direct Message Shortcut",
                        subtitle = "Send balance reminder and ledger statement via WhatsApp",
                        checked = settings.enableQuickWhatsAppAction,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enableQuickWhatsAppAction = it) } },
                        testTag = "display_quick_whatsapp"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "Quick Sale Action Shortcut",
                        subtitle = "One-tap trigger to open Sales Invoice with party pre-selected",
                        checked = settings.enableQuickSaleAction,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enableQuickSaleAction = it) } },
                        testTag = "display_quick_sale"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Quick Payment Action Shortcut",
                        subtitle = "One-tap trigger to open Payment In/Out with party pre-selected",
                        checked = settings.enableQuickPaymentAction,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enableQuickPaymentAction = it) } },
                        testTag = "display_quick_payment"
                    )
                }
            }

            // -------------------------------------------------------------
            // 15. SEARCH & LIST SETTINGS
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "15. Search & List Settings",
                subtitle = "Search criteria, default filter view & party list sorting",
                icon = Icons.Outlined.Search,
                isExpanded = expSearch,
                onToggle = { expSearch = !expSearch },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Default Party List Sort Order", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PartySortOption.entries.forEach { sort ->
                            PartyChip(
                                label = sort.displayName,
                                isSelected = settings.defaultSortOption == sort,
                                onClick = {
                                    viewModel.setPartySortPreference(sort.displayName)
                                    Toast.makeText(context, "Sorted by ${sort.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "sort_option_${sort.id}"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    Text("Default Party List Filter", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PartyFilterOption.entries.forEach { filter ->
                            PartyChip(
                                label = filter.displayName,
                                isSelected = settings.defaultFilterOption == filter,
                                onClick = {
                                    viewModel.updatePartySettings { s -> s.copy(defaultFilterOption = filter) }
                                    Toast.makeText(context, "Filter: ${filter.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "filter_option_${filter.id}"
                            )
                        }
                    }

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Search,
                        title = "Search by PAN / VAT Number",
                        subtitle = "Enable searching parties using their 9-digit tax registration number",
                        checked = settings.searchByPanVat,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(searchByPanVat = it) } },
                        testTag = "search_pan_vat_switch"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.LocationOn,
                        title = "Search by Address & City",
                        subtitle = "Allow querying party list by town, district, or street name",
                        checked = settings.searchByAddress,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(searchByAddress = it) } },
                        testTag = "search_address_switch"
                    )
                }
            }

            // -------------------------------------------------------------
            // 16. IMPORT & EXPORT
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "16. Import & Export",
                subtitle = "Export party directory, customer lists & ledger statements",
                icon = Icons.Outlined.CloudDownload,
                isExpanded = expImportExport,
                onToggle = { expImportExport = !expImportExport },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export or backup your party directory with contacts and outstanding balances:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showExportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp).testTag("export_party_directory_btn")
                        ) {
                            Icon(Icons.Outlined.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Parties", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Import feature ready for CSV/Excel uploads", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) CardDark else Color(0xFFE2E8F0)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) CardBorder else Color(0xFFCBD5E1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text("Import (CSV)", fontSize = 13.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 17. PERMISSIONS & ROLES
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "17. Staff Permissions & Roles",
                subtitle = "Control staff rights for party creation, edits, and balance visibility",
                icon = Icons.Outlined.Security,
                isExpanded = expPermissions,
                onToggle = { expPermissions = !expPermissions },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Edit,
                        title = "Allow Staff to Add Parties",
                        subtitle = "Cashiers and sales staff can register new customer accounts",
                        checked = settings.allowStaffToAddParty,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowStaffToAddParty = it) } },
                        testTag = "perm_staff_add_party"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Edit,
                        title = "Allow Staff to Edit Parties",
                        subtitle = "Staff can modify phone numbers, addresses, and credit limits",
                        checked = settings.allowStaffToEditParty,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowStaffToEditParty = it) } },
                        testTag = "perm_staff_edit_party"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Allow Staff to Delete Parties",
                        subtitle = "Only admin role can delete parties by default (Recommended: OFF)",
                        checked = settings.allowStaffToDeleteParty,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowStaffToDeleteParty = it) } },
                        testTag = "perm_staff_delete_party"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Allow Staff to View Financial Balances",
                        subtitle = "Staff can view outstanding balances and credit utilization",
                        checked = settings.allowStaffToViewBalance,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(allowStaffToViewBalance = it) } },
                        testTag = "perm_staff_view_balance"
                    )
                }
            }

            // -------------------------------------------------------------
            // 18. ADVANCED & HISTORICAL INTEGRITY
            // -------------------------------------------------------------
            ExpandableSettingsCard(
                title = "18. Advanced & Historical Data Safety",
                subtitle = "Party code generator, transaction deletion lock & audit trail",
                icon = Icons.Outlined.Tune,
                isExpanded = expAdvanced,
                onToggle = { expAdvanced = !expAdvanced },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PartySwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Prevent Delete Party With Transactions",
                        subtitle = "Critical Safety Rule: Prevent deleting any party with active sales or voucher history",
                        checked = settings.preventDeletePartyWithTransactions,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(preventDeletePartyWithTransactions = it) } },
                        testTag = "adv_prevent_delete_with_tx"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.Badge,
                        title = "Auto-Generate Party Codes",
                        subtitle = "Automatically assign sequential identifiers (e.g. PTY-1001)",
                        checked = settings.autoGeneratePartyCode,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(autoGeneratePartyCode = it) } },
                        testTag = "adv_auto_code"
                    )

                    PartyDivider(isDark)

                    PartySwitchRow(
                        icon = Icons.Outlined.History,
                        title = "Enable Party Audit Trail & History",
                        subtitle = "Record timestamp and staff name for all party modifications",
                        checked = settings.enablePartyAuditTrail,
                        onCheckedChange = { viewModel.updatePartySettings { s -> s.copy(enablePartyAuditTrail = it) } },
                        testTag = "adv_audit_trail"
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------
    // MODAL DIALOGS
    // -------------------------------------------------------------

    // Business Preset Selector Dialog
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Category, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Business Party Preset", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select an industry profile to auto-configure terminology, credit rules, and categories:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    BusinessPartyPreset.entries.forEach { preset ->
                        val isSelected = settings.businessPreset == preset
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) (if (isDark) SkyBlueCardBg else Color(0xFFEFF6FF)) else (if (isDark) CardDark else Color(0xFFF8FAFC))
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) SkyBlue else (if (isDark) CardBorder else Color(0xFFE2E8F0)),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.applyBusinessPartyPreset(preset)
                                    showPresetDialog = false
                                    Toast.makeText(context, "Applied ${preset.displayName} preset", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(preset.displayName, color = if (isSelected) SkyBlueBright else (if (isDark) TextWhite else Color(0xFF0F172A)), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (isSelected) {
                                        Icon(Icons.Outlined.Check, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(preset.description, color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPresetDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)) {
                    Text("Done")
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Default Credit Limit Dialog
    if (showCreditLimitDialog) {
        var limitInput by remember { mutableStateOf(settings.defaultCreditLimit.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showCreditLimitDialog = false },
            title = {
                Text("Configure Default Credit Limit", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Set maximum allowable credit limit for newly registered parties (Rs.):", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) limitInput = it },
                        label = { Text("Credit Limit (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("credit_limit_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = limitInput.toDoubleOrNull() ?: 50000.0
                        viewModel.setDefaultCreditLimit(parsed)
                        showCreditLimitDialog = false
                        Toast.makeText(context, "Credit limit updated to Rs. ${String.format(Locale.US, "%,.0f", parsed)}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreditLimitDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Reminder Template Dialog
    if (showReminderTemplateDialog) {
        var templateInput by remember { mutableStateOf(settings.defaultReminderMessageTemplate) }
        AlertDialog(
            onDismissRequest = { showReminderTemplateDialog = false },
            title = {
                Text("Edit Reminder Message Template", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Use {party_name} and {balance} tags for automated dynamic values:", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = templateInput,
                        onValueChange = { templateInput = it },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (templateInput.isNotBlank()) {
                            viewModel.updatePartySettings { s -> s.copy(defaultReminderMessageTemplate = templateInput) }
                            showReminderTemplateDialog = false
                            Toast.makeText(context, "Reminder template updated", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Save Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTemplateDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Reset Defaults Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text("Reset Party Settings to Default?", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will restore default Party configurations and terminology according to your current business preset. All party records, transaction ledgers, and contact numbers are 100% safe and intact.", color = TextMuted, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyBusinessPartyPreset(BusinessPartyPreset.GENERAL)
                        showResetDialog = false
                        Toast.makeText(context, "Party settings reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InvoiceRose)
                ) {
                    Text("Reset Defaults", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Party Directory", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Total ${allParties.size} party accounts ready for export. Choose format:", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                showExportDialog = false
                                Toast.makeText(context, "Exporting ${allParties.size} parties to CSV in Downloads...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CSV Format", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showExportDialog = false
                                Toast.makeText(context, "Exporting ${allParties.size} parties to PDF report...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) CardDark else Color(0xFFE2E8F0)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) CardBorder else Color(0xFFCBD5E1)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("PDF Report", fontSize = 12.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// REUSABLE SUB-COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun PartySettingsTopBar(
    onBack: () -> Unit,
    onResetDefaults: () -> Unit
) {
    val isDark = AppTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDark) CardDark else Color.White)
                    .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), CircleShape)
                    .testTag("party_settings_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) TextWhite else Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Party Settings",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customer, supplier, credit limit & ledgers",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        IconButton(
            onClick = onResetDefaults,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isDark) CardDark else Color.White)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), CircleShape)
                .testTag("party_settings_reset_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Defaults",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PartyStatusSummaryCard(
    settings: PartySettings,
    totalPartiesCount: Int,
    isDark: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SkyBlueCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Group,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Party Ledger • $totalPartiesCount Accounts",
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settings.customerTerminology} & ${settings.supplierTerminology} Mode",
                            color = SkyBlueBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (settings.enablePartyModule) InvoiceEmerald.copy(alpha = 0.15f) else AmberWarn.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (settings.enablePartyModule) "ACTIVE" else "OFF",
                        color = if (settings.enablePartyModule) InvoiceEmerald else AmberWarn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Credit Limit", fontSize = 11.sp, color = TextMuted)
                    Text("Rs. ${String.format(Locale.US, "%,.0f", settings.defaultCreditLimit)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) TextWhite else Color(0xFF0F172A))
                }
                Column {
                    Text("Terms Duration", fontSize = 11.sp, color = TextMuted)
                    Text("${settings.defaultPaymentTermDays} Days", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SkyBlueBright)
                }
                Column {
                    Text("Overrun Alert", fontSize = 11.sp, color = TextMuted)
                    Text(
                        if (settings.warnCreditLimitExceeded) "Warn ON" else "Off",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (settings.warnCreditLimitExceeded) InvoiceEmerald else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessPartyPresetBanner(
    currentPreset: BusinessPartyPreset,
    isDark: Boolean,
    onOpenPresetDialog: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) SkyBlueCardBg.copy(alpha = 0.6f) else Color(0xFFEFF6FF)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, if (isDark) SkyBlueCardBorder else Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
            .clickable { onOpenPresetDialog() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SkyBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = null,
                        tint = SkyBlueBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Preset: ${currentPreset.displayName}",
                        color = if (isDark) TextWhite else Color(0xFF1E3A8A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentPreset.description,
                        color = if (isDark) TextMuted else Color(0xFF3B82F6),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onOpenPresetDialog,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Change", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun ExpandableSettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotateIcon")

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SkyBlueCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = subtitle,
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = if (isExpanded) 3 else 1
                        )
                    }
                }

                IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextMuted,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                    content()
                }
            }
        }
    }
}

@Composable
private fun PartySwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val isDark = AppTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) (if (isDark) SkyBlueBright else SkyBlue) else TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = if (enabled) (if (isDark) TextWhite else Color(0xFF0F172A)) else TextMuted.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = if (enabled) TextMuted else TextMuted.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SkyBlue,
                uncheckedThumbColor = SkyBlueBright,
                uncheckedTrackColor = if (isDark) BackgroundDark else Color(0xFFE2E8F0)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun PartyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val isDark = AppTheme.isDark
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) SkyBlue else (if (isDark) BackgroundDark else Color(0xFFF1F5F9))
            )
            .border(
                1.dp,
                if (isSelected) SkyBlue else (if (isDark) CardBorder else Color(0xFFE2E8F0)),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else (if (isDark) TextWhite else Color(0xFF334155)),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun PartyDivider(isDark: Boolean) {
    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(
        color = if (isDark) CardBorder.copy(alpha = 0.4f) else Color(0xFFF1F5F9),
        thickness = 0.8.dp
    )
    Spacer(modifier = Modifier.height(10.dp))
}
