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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material.icons.outlined.ViewHeadline
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
import com.example.data.model.BusinessInvoicePreset
import com.example.data.model.InvoiceSettings
import com.example.ui.MainViewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SkyBlueCardBorder
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

private val InvoiceEmerald = Color(0xFF10B981)
private val InvoiceRose = Color(0xFFF43F5E)
private val AmberWarn = Color(0xFFF59E0B)

/**
 * Atri Khata 2 - Advanced Invoice Settings & Configuration Screen
 * Comprehensive, persistent, Business-Preset aware, theme-safe, and future-ready.
 * Covers all 20 standard sections requested.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InvoiceSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val settings by viewModel.invoiceSettings.collectAsStateWithLifecycle()
    val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()

    // Dialog state management
    var showPresetDialog by remember { mutableStateOf(false) }
    var selectedPresetToApply by remember { mutableStateOf(settings.businessPreset) }
    var showPrefixDialog by remember { mutableStateOf(false) }
    var showStartingNumDialog by remember { mutableStateOf(false) }
    var showPanDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showHeaderDialog by remember { mutableStateOf(false) }
    var showFooterDialog by remember { mutableStateOf(false) }
    var showBusinessInfoDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Section expansion state
    var expandedGeneral by remember { mutableStateOf(true) }
    var expandedNumbering by remember { mutableStateOf(false) }
    var expandedTypes by remember { mutableStateOf(false) }
    var expandedHeader by remember { mutableStateOf(false) }
    var expandedBusinessInfo by remember { mutableStateOf(false) }
    var expandedParty by remember { mutableStateOf(false) }
    var expandedItems by remember { mutableStateOf(false) }
    var expandedPricing by remember { mutableStateOf(false) }
    var expandedVatTax by remember { mutableStateOf(false) }
    var expandedPayment by remember { mutableStateOf(false) }
    var expandedDueOutstanding by remember { mutableStateOf(false) }
    var expandedTermsNotes by remember { mutableStateOf(false) }
    var expandedSignature by remember { mutableStateOf(false) }
    var expandedPrintPdf by remember { mutableStateOf(false) }
    var expandedSharing by remember { mutableStateOf(false) }
    var expandedLayout by remember { mutableStateOf(false) }
    var expandedFooter by remember { mutableStateOf(false) }
    var expandedHistory by remember { mutableStateOf(false) }
    var expandedAdvanced by remember { mutableStateOf(false) }
    var expandedPermissions by remember { mutableStateOf(false) }

    val bgColor = if (isDark) BackgroundDark else Color(0xFFF8FAFC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp)
        ) {
            // TOP BAR
            InvoiceSettingsTopBar(
                onBack = onBack,
                onResetDefaults = { showResetDialog = true }
            )

            // BUSINESS PRESET SELECTOR BANNER
            InvoicePresetBanner(
                currentPreset = settings.businessPreset,
                onSelectPresetClick = { showPresetDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // SECTION 1: GENERAL INVOICE SETTINGS
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "1. General Invoice Settings",
                subtitle = "Master switch, status, previews and confirmation controls",
                icon = Icons.Outlined.Tune,
                expanded = expandedGeneral,
                onToggleExpand = { expandedGeneral = !expandedGeneral }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Enable Invoice Module",
                    subtitle = "Master switch for generating sales memos and tax invoices",
                    checked = settings.enableInvoicing,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(enableInvoicing = it) } },
                    testTag = "invoice_enable_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Auto Generate Invoice Number",
                    subtitle = "Assign sequential series code automatically on new sales",
                    checked = settings.autoGenerateInvoiceNumber,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(autoGenerateInvoiceNumber = it) } },
                    testTag = "invoice_auto_gen_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Preview Before Save",
                    subtitle = "Display full bill sheet breakdown before finalizing",
                    checked = settings.showInvoicePreviewBeforeSave,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showInvoicePreviewBeforeSave = it) } },
                    testTag = "invoice_preview_before_save_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Confirm Before Delete",
                    subtitle = "Show warning dialog before deleting an invoice record",
                    checked = settings.confirmBeforeDelete,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(confirmBeforeDelete = it) } },
                    testTag = "invoice_confirm_delete_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Confirm Before Cancel",
                    subtitle = "Prompt for confirmation prior to voiding a confirmed bill",
                    checked = settings.confirmBeforeCancel,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(confirmBeforeCancel = it) } },
                    testTag = "invoice_confirm_cancel_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Allow Edit Saved Invoice",
                    subtitle = "Permit modifications to existing recorded invoices",
                    checked = settings.allowEditSavedInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowEditSavedInvoice = it) } },
                    testTag = "invoice_allow_edit_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Allow Delete Saved Invoice",
                    subtitle = "Permit permanently removing invoice history records",
                    checked = settings.allowDeleteSavedInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowDeleteSavedInvoice = it) } },
                    testTag = "invoice_allow_delete_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Allow Duplicate Invoice",
                    subtitle = "Enable 1-tap cloning of past invoices for fast re-orders",
                    checked = settings.allowDuplicateInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowDuplicateInvoice = it) } },
                    testTag = "invoice_allow_duplicate_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Invoice Status Badge",
                    subtitle = "Display Confirmed, Paid, Partial or Cancelled badge",
                    checked = settings.showInvoiceStatusBadge,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showInvoiceStatusBadge = it) } },
                    testTag = "invoice_show_status_badge_switch"
                )
                InvoiceDivider()

                Text("Default Invoice Type", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Tax Invoice", "Sales Invoice / Bill of Supply", "Service Invoice", "Proforma Invoice").forEach { type ->
                        InvoiceSelectableChip(
                            label = type,
                            isSelected = settings.defaultInvoiceType == type,
                            onClick = {
                                viewModel.updateInvoiceSettings { s -> s.copy(defaultInvoiceType = type) }
                                Toast.makeText(context, "Default type set to $type", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 2: INVOICE NUMBERING
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "2. Invoice Numbering & Series",
                subtitle = "Series prefix, padding, fiscal year and sequence format",
                icon = Icons.Outlined.ReceiptLong,
                expanded = expandedNumbering,
                onToggleExpand = { expandedNumbering = !expandedNumbering }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Automatic Number Generation",
                    subtitle = "Increment invoice counter upon creating new sales",
                    checked = settings.autoNumbering,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(autoNumbering = it) } },
                    testTag = "invoice_auto_numbering_switch"
                )
                InvoiceDivider()

                InvoiceActionRow(
                    title = "Invoice Prefix",
                    value = settings.invoicePrefix,
                    onClick = { showPrefixDialog = true }
                )
                InvoiceDivider()

                InvoiceActionRow(
                    title = "Starting Counter Number",
                    value = "#${settings.startingNumber}",
                    onClick = { showStartingNumDialog = true }
                )
                InvoiceDivider()

                InvoiceSettingSwitchItem(
                    title = "Include Fiscal Year in Series",
                    subtitle = "Prefix invoice numbers with current Bikram Sambat year (e.g. 2082)",
                    checked = settings.includeFiscalYear,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(includeFiscalYear = it) } },
                    testTag = "invoice_include_fy_switch"
                )
                InvoiceDivider()

                Text("Fiscal Year Code Format", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("YYYY" to "4 Digits (e.g. 2082)", "YY/YY" to "Slash (e.g. 81/82)").forEach { (fmt, label) ->
                        InvoiceSelectableChip(
                            label = label,
                            isSelected = settings.fiscalYearFormat == fmt,
                            onClick = { viewModel.updateInvoiceSettings { s -> s.copy(fiscalYearFormat = fmt) } }
                        )
                    }
                }

                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Reset Number Every Fiscal Year",
                    subtitle = "Restart serial sequence from 0001 upon new Nepali fiscal period",
                    checked = settings.resetNumberFiscalYear,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(resetNumberFiscalYear = it) } },
                    testTag = "invoice_reset_fy_switch"
                )
                InvoiceDivider()

                InvoiceSettingSwitchItem(
                    title = "Prevent Duplicate Numbers",
                    subtitle = "Enforce unique database index constraint to prevent duplicates",
                    checked = settings.preventDuplicateNumber,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(preventDuplicateNumber = it) } },
                    testTag = "invoice_prevent_dup_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 3: INVOICE TYPE
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "3. Supported Invoice Types",
                subtitle = "Enable or disable tax invoices, cash memos and proforma",
                icon = Icons.Outlined.GridView,
                expanded = expandedTypes,
                onToggleExpand = { expandedTypes = !expandedTypes }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Tax Invoice (VAT Registered 13%)",
                    subtitle = "Full official IRD-compliant tax invoice with VAT breakdown",
                    checked = settings.allowTaxInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowTaxInvoice = it) } },
                    testTag = "invoice_allow_tax_invoice_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Sales Invoice / Bill of Supply",
                    subtitle = "Simplified sales memo for non-VAT or exempt transactions",
                    checked = settings.allowSalesInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowSalesInvoice = it) } },
                    testTag = "invoice_allow_sales_invoice_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Service Invoice",
                    subtitle = "Tailored for consulting, repairs, labor and professional fees",
                    checked = settings.allowServiceInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowServiceInvoice = it) } },
                    testTag = "invoice_allow_service_invoice_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Proforma Invoice / Quotation",
                    subtitle = "Commercial estimate / quote without altering accounting ledger",
                    checked = settings.allowProformaInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowProformaInvoice = it) } },
                    testTag = "invoice_allow_proforma_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Require Party PAN for Tax Invoice",
                    subtitle = "Mandate entry of Customer PAN when creating Tax Invoices",
                    checked = settings.requirePanForTaxInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(requirePanForTaxInvoice = it) } },
                    testTag = "invoice_require_pan_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 4: INVOICE HEADER
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "4. Invoice Header",
                subtitle = "Logo, titles, address, contact and date display",
                icon = Icons.Outlined.ViewHeadline,
                expanded = expandedHeader,
                onToggleExpand = { expandedHeader = !expandedHeader }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Business Logo",
                    subtitle = "Render custom crest monogram or uploaded company emblem",
                    checked = settings.showBusinessLogo,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showBusinessLogo = it) } },
                    testTag = "invoice_show_logo_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Business Name",
                    subtitle = "Prominently display company name on top header",
                    checked = settings.showBusinessName,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showBusinessName = it) } },
                    testTag = "invoice_show_name_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Business Address",
                    subtitle = "Include physical business address & city",
                    checked = settings.showBusinessAddress,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showBusinessAddress = it) } },
                    testTag = "invoice_show_address_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Phone Number",
                    subtitle = "Display official phone/mobile number in header",
                    checked = settings.showPhone,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPhone = it) } },
                    testTag = "invoice_show_phone_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Email Address",
                    subtitle = "Display official billing email in header",
                    checked = settings.showEmail,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showEmail = it) } },
                    testTag = "invoice_show_email_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Website",
                    subtitle = "Display website URL if configured in profile",
                    checked = settings.showWebsite,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showWebsite = it) } },
                    testTag = "invoice_show_website_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show PAN / VAT Header",
                    subtitle = "Display 9-digit business tax registration number",
                    checked = settings.showPanVatHeader,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPanVatHeader = it) } },
                    testTag = "invoice_show_pan_header_switch"
                )
                InvoiceDivider()

                InvoiceActionRow(
                    title = "Custom Invoice Title",
                    value = settings.customInvoiceTitle,
                    onClick = { showHeaderDialog = true }
                )
                InvoiceDivider()

                InvoiceSettingSwitchItem(
                    title = "Show Bikram Sambat Date (BS)",
                    subtitle = "Display official Nepali calendar date (e.g. 2082-06-08)",
                    checked = settings.showInvoiceDateBs,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showInvoiceDateBs = it) } },
                    testTag = "invoice_show_date_bs_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Gregorian Date (AD)",
                    subtitle = "Display international standard AD date alongside BS date",
                    checked = settings.showInvoiceDateAd,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showInvoiceDateAd = it) } },
                    testTag = "invoice_show_date_ad_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 5: BUSINESS INFORMATION (Directly connected to BusinessProfile)
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "5. Business Information & Identity",
                subtitle = "Connected to Business Profile — no duplicate records",
                icon = Icons.Outlined.Storefront,
                expanded = expandedBusinessInfo,
                onToggleExpand = { expandedBusinessInfo = !expandedBusinessInfo }
            ) {
                val profile = businessProfile
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) CardDark else Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Text(
                        text = profile?.businessName ?: settings.businessName,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PAN / VAT: ${profile?.panVatNumber ?: settings.panVatNumber}   •   Phone: ${profile?.phone ?: settings.phone}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Address: ${profile?.address ?: settings.address}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Bank: ${profile?.bankName ?: "Nabil Bank"} (${profile?.bankAccountNumber ?: "A/C Configured"})",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showBusinessInfoDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Business Profile", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 6: CUSTOMER / PARTY INFORMATION
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "6. Customer / Party Information",
                subtitle = "Billed-to details, PAN, contact and ledger balances",
                icon = Icons.Outlined.People,
                expanded = expandedParty,
                onToggleExpand = { expandedParty = !expandedParty }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Customer / Party Name",
                    subtitle = "Display name on invoice header",
                    checked = settings.showPartyName,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPartyName = it) } },
                    testTag = "invoice_show_party_name_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Customer Phone",
                    subtitle = "Print customer phone number on invoice",
                    checked = settings.showPartyPhone,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPartyPhone = it) } },
                    testTag = "invoice_show_party_phone_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Customer Address",
                    subtitle = "Print customer delivery / billing address",
                    checked = settings.showPartyAddress,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPartyAddress = it) } },
                    testTag = "invoice_show_party_address_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Customer PAN / VAT",
                    subtitle = "Print client tax ID for commercial B2B compliance",
                    checked = settings.showPartyPanVat,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPartyPanVat = it) } },
                    testTag = "invoice_show_party_pan_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Outstanding Ledger Balance",
                    subtitle = "Print previous unpaid balance and net statement summary",
                    checked = settings.showOutstandingBalance,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showOutstandingBalance = it) } },
                    testTag = "invoice_show_outstanding_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Allow Walk-in / Cash Customers",
                    subtitle = "Permit issuing quick invoices without registering a party record",
                    checked = settings.allowWalkInCustomer,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowWalkInCustomer = it) } },
                    testTag = "invoice_allow_walkin_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 7: INVOICE ITEMS
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "7. Invoice Items Table",
                subtitle = "S.N., SKU, description, rates, discounts and batch info",
                icon = Icons.Outlined.Inventory2,
                expanded = expandedItems,
                onToggleExpand = { expandedItems = !expandedItems }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Serial Number (S.N.)",
                    subtitle = "Numbered item row indexing (1, 2, 3...)",
                    checked = settings.showItemSerialNumber,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showItemSerialNumber = it) } },
                    testTag = "invoice_show_sn_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Item Code / SKU",
                    subtitle = "Print barcode/SKU code beside product description",
                    checked = settings.showItemCodeSku,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showItemCodeSku = it) } },
                    testTag = "invoice_show_sku_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Item Description",
                    subtitle = "Print full product / service title",
                    checked = settings.showItemDescription,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showItemDescription = it) } },
                    testTag = "invoice_show_desc_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Quantity & Unit",
                    subtitle = "Display units (pcs, kg, packet, box)",
                    checked = settings.showQuantity && settings.showUnit,
                    onCheckedChange = {
                        viewModel.updateInvoiceSettings { s -> s.copy(showQuantity = it, showUnit = it) }
                    },
                    testTag = "invoice_show_qty_unit_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Unit Rate / Price",
                    subtitle = "Display price per single quantity unit",
                    checked = settings.showUnitPriceRate,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showUnitPriceRate = it) } },
                    testTag = "invoice_show_rate_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Line Item Discount",
                    subtitle = "Print per-item discount deductions",
                    checked = settings.showItemDiscount,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showItemDiscount = it) } },
                    testTag = "invoice_show_item_disc_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Batch Number & Expiry Date",
                    subtitle = "Ideal for pharmacy, medical and perishable products",
                    checked = settings.showBatchNumber && settings.showExpiryDate,
                    onCheckedChange = {
                        viewModel.updateInvoiceSettings { s -> s.copy(showBatchNumber = it, showExpiryDate = it) }
                    },
                    testTag = "invoice_show_batch_expiry_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 8: PRICING & CALCULATION
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "8. Pricing & Calculation",
                subtitle = "Subtotal, bill discount, auto round-off and totals",
                icon = Icons.Outlined.Calculate,
                expanded = expandedPricing,
                onToggleExpand = { expandedPricing = !expandedPricing }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Subtotal",
                    subtitle = "Display raw total before discount and VAT",
                    checked = settings.showSubtotal,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showSubtotal = it) } },
                    testTag = "invoice_show_subtotal_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Enable Bill-Level Discount",
                    subtitle = "Allow overall discount percentage or flat amount deduction",
                    checked = settings.enableBillDiscount,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(enableBillDiscount = it) } },
                    testTag = "invoice_enable_bill_discount_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Automatic Round Off",
                    subtitle = "Round final grand total to the nearest whole rupee",
                    checked = settings.showRoundOff,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showRoundOff = it) } },
                    testTag = "invoice_auto_round_off_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Grand Total",
                    subtitle = "Highlight final payable bill total",
                    checked = settings.showGrandTotal,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showGrandTotal = it) } },
                    testTag = "invoice_show_grand_total_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Amount in Words",
                    subtitle = "Print English/Nepali words representation of total",
                    checked = settings.showAmountInWords,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showAmountInWords = it) } },
                    testTag = "invoice_show_words_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 9: VAT / TAX
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "9. VAT / Tax Configuration",
                subtitle = "Nepal standard 13% VAT, exemptions and breakdown",
                icon = Icons.Outlined.Percent,
                expanded = expandedVatTax,
                onToggleExpand = { expandedVatTax = !expandedVatTax }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Enable VAT / Tax on Invoices",
                    subtitle = "Calculate tax automatically on taxable products",
                    checked = settings.enableVat,
                    onCheckedChange = { viewModel.toggleTaxEnabled(it) },
                    testTag = "invoice_tax_switch"
                )

                if (settings.enableVat) {
                    InvoiceDivider()
                    Text("Default Tax Rate", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.0 to "0% (Exempt)", 5.0 to "5%", 13.0 to "13% (Standard Nepal VAT)", 18.0 to "18%").forEach { (rate, label) ->
                            InvoiceSelectableChip(
                                label = label,
                                isSelected = settings.defaultVatRate == rate,
                                onClick = {
                                    viewModel.setTaxRate(rate)
                                    Toast.makeText(context, "Tax rate set to $label", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    InvoiceDivider()
                    InvoiceSettingSwitchItem(
                        title = "Show Detailed VAT Breakdown",
                        subtitle = "Display Taxable Amount, Exempt Amount & VAT (13%)",
                        checked = settings.showVatBreakdown,
                        onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showVatBreakdown = it) } },
                        testTag = "invoice_vat_breakdown_switch"
                    )

                    InvoiceDivider()
                    InvoiceActionRow(
                        title = "Business PAN / VAT Number",
                        value = settings.panVatNumber,
                        onClick = { showPanDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 10: PAYMENT INFORMATION
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "10. Payment & Bank Information",
                subtitle = "Payment modes, bank account, and Fonepay QR code",
                icon = Icons.Outlined.Payments,
                expanded = expandedPayment,
                onToggleExpand = { expandedPayment = !expandedPayment }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Payment Mode",
                    subtitle = "Print Cash, Bank, QR or Credit on invoice",
                    checked = settings.showPaymentMode,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPaymentMode = it) } },
                    testTag = "invoice_show_payment_mode_switch"
                )
                InvoiceDivider()

                Text("Default Payment Mode", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "Bank Transfer", "UPI / QR", "Credit", "Cheque").forEach { mode ->
                        InvoiceSelectableChip(
                            label = mode,
                            isSelected = settings.defaultPaymentMode == mode,
                            onClick = {
                                viewModel.setDefaultPaymentMode(mode)
                                viewModel.updateInvoiceSettings { s -> s.copy(defaultPaymentMode = mode) }
                            }
                        )
                    }
                }

                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Bank Account Details",
                    subtitle = "Include bank name, branch & account number for payments",
                    checked = settings.showBankDetails,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showBankDetails = it) } },
                    testTag = "invoice_show_bank_details_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Instant Payment QR Code",
                    subtitle = "Render Fonepay / eSewa QR on invoice for instant settlement",
                    checked = settings.showPaymentQrCode,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPaymentQrCode = it) } },
                    testTag = "invoice_show_qr_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 11: DUE & OUTSTANDING
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "11. Due Date & Outstanding",
                subtitle = "Credit terms, payment due dates and overdue alerts",
                icon = Icons.Outlined.CalendarMonth,
                expanded = expandedDueOutstanding,
                onToggleExpand = { expandedDueOutstanding = !expandedDueOutstanding }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Due Date",
                    subtitle = "Print agreed payment deadline (BS / AD)",
                    checked = settings.showDueDate,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showDueDate = it) } },
                    testTag = "invoice_show_due_date_switch"
                )
                InvoiceDivider()

                Text("Default Credit Period", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7 to "7 Days", 15 to "15 Days", 30 to "30 Days", 45 to "45 Days").forEach { (days, label) ->
                        InvoiceSelectableChip(
                            label = label,
                            isSelected = settings.defaultCreditDays == days,
                            onClick = { viewModel.updateInvoiceSettings { s -> s.copy(defaultCreditDays = days) } }
                        )
                    }
                }

                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Highlight Overdue Status",
                    subtitle = "Emphasize past-due invoices with amber indicator badge",
                    checked = settings.highlightOverdue,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(highlightOverdue = it) } },
                    testTag = "invoice_highlight_overdue_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 12: TERMS & NOTES
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "12. Terms, Conditions & Remarks",
                subtitle = "Standard return policy, customer notes and remarks",
                icon = Icons.Outlined.Description,
                expanded = expandedTermsNotes,
                onToggleExpand = { expandedTermsNotes = !expandedTermsNotes }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Terms & Conditions",
                    subtitle = "Print business terms & conditions block at invoice bottom",
                    checked = settings.showTermsAndConditions,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showTermsAndConditions = it) } },
                    testTag = "invoice_show_terms_switch"
                )
                InvoiceDivider()

                Column {
                    Text("Standard Terms Text", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) CardDark else Color(0xFFF1F5F9))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = settings.defaultTerms,
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showTermsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Edit Terms", fontSize = 12.sp)
                    }
                }

                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Customer Remarks",
                    subtitle = "Display transaction-specific memo note on the bill",
                    checked = settings.showCustomerRemarks,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showCustomerRemarks = it) } },
                    testTag = "invoice_show_remarks_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 13: SIGNATURE
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "13. Signatory & Authority",
                subtitle = "Authorized seal box, receiver's sign & digital stamps",
                icon = Icons.Outlined.Check,
                expanded = expandedSignature,
                onToggleExpand = { expandedSignature = !expandedSignature }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Authorized Signatory Box",
                    subtitle = "Include signature & date box at bottom right of invoice",
                    checked = settings.showAuthorizedSignatureBox,
                    onCheckedChange = { viewModel.toggleShowSignatureLine(it) },
                    testTag = "invoice_show_signature_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Customer Receiver Signature Box",
                    subtitle = "Include customer acknowledgment sign on delivery",
                    checked = settings.showCustomerSignatureBox,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showCustomerSignatureBox = it) } },
                    testTag = "invoice_show_cust_sign_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Digital Signature / Stamp",
                    subtitle = "Render digital stamp if configured in profile",
                    checked = settings.showDigitalSignature,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showDigitalSignature = it) } },
                    testTag = "invoice_show_digital_stamp_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Prepared By Staff Name",
                    subtitle = "Print salesperson or administrator username",
                    checked = settings.showPreparedBy,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPreparedBy = it) } },
                    testTag = "invoice_show_prepared_by_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 14: PRINT & PDF
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "14. Print & PDF Document Preferences",
                subtitle = "A4, 80mm POS Thermal, auto-print and palettes",
                icon = Icons.Outlined.Print,
                expanded = expandedPrintPdf,
                onToggleExpand = { expandedPrintPdf = !expandedPrintPdf }
            ) {
                Text("Default Paper Size & Printer Layout", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("A4 (Standard)", "80mm Thermal POS", "58mm Thermal POS").forEach { size ->
                        InvoiceSelectableChip(
                            label = size,
                            isSelected = settings.defaultPaperSize == size,
                            onClick = {
                                viewModel.setReceiptPaperSize(size)
                                viewModel.updateInvoiceSettings { s -> s.copy(defaultPaperSize = size) }
                            }
                        )
                    }
                }

                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Auto Print After Saving",
                    subtitle = "Automatically open Android print spooler upon invoice creation",
                    checked = settings.autoPrintOnSave,
                    onCheckedChange = {
                        viewModel.toggleAutoPrintOnSave(it)
                        viewModel.updateInvoiceSettings { s -> s.copy(autoPrintOnSave = it) }
                    },
                    testTag = "invoice_auto_print_switch"
                )

                InvoiceDivider()
                Text("PDF Color Theme", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Corporate Navy", "Sky Blue Modern", "Classic Monochrome").forEach { theme ->
                        InvoiceSelectableChip(
                            label = theme,
                            isSelected = settings.pdfColorTheme == theme,
                            onClick = { viewModel.updateInvoiceSettings { s -> s.copy(pdfColorTheme = theme) } }
                        )
                    }
                }

                InvoiceDivider()
                Text("Document Watermark", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("None", "PAID", "DUPLICATE", "ORIGINAL").forEach { mark ->
                        InvoiceSelectableChip(
                            label = mark,
                            isSelected = settings.pdfWatermark == mark,
                            onClick = { viewModel.updateInvoiceSettings { s -> s.copy(pdfWatermark = mark) } }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 15: SHARING
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "15. Sharing & Export Options",
                subtitle = "WhatsApp direct send, PDF share and text memos",
                icon = Icons.Outlined.Share,
                expanded = expandedSharing,
                onToggleExpand = { expandedSharing = !expandedSharing }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Enable Document Sharing",
                    subtitle = "Allow exporting bills to system apps and messengers",
                    checked = settings.enableSharing,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(enableSharing = it) } },
                    testTag = "invoice_enable_sharing_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "WhatsApp Quick Share",
                    subtitle = "Include direct WhatsApp action in invoice detail screen",
                    checked = settings.includeWhatsAppShareButton,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(includeWhatsAppShareButton = it) } },
                    testTag = "invoice_whatsapp_share_switch"
                )
                InvoiceDivider()

                Text("Default Share Format", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PDF Document", "Text Receipt Summary", "Both").forEach { fmt ->
                        InvoiceSelectableChip(
                            label = fmt,
                            isSelected = settings.shareFormat == fmt,
                            onClick = { viewModel.updateInvoiceSettings { s -> s.copy(shareFormat = fmt) } }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 16: INVOICE LAYOUT
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "16. Invoice Layout & Visual Styling",
                subtitle = "Template style, font density, grid and alignments",
                icon = Icons.Outlined.GridView,
                expanded = expandedLayout,
                onToggleExpand = { expandedLayout = !expandedLayout }
            ) {
                Text("Design Template", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Modern Corporate", "Classic Clean", "Compact POS").forEach { tmpl ->
                        InvoiceSelectableChip(
                            label = tmpl,
                            isSelected = settings.layoutTemplate == tmpl,
                            onClick = { viewModel.updateInvoiceSettings { s -> s.copy(layoutTemplate = tmpl) } }
                        )
                    }
                }

                InvoiceDivider()
                Text("Content Density", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Standard", "Compact").forEach { density ->
                        InvoiceSelectableChip(
                            label = density,
                            isSelected = settings.fontScaleDensity == density,
                            onClick = { viewModel.updateInvoiceSettings { s -> s.copy(fontScaleDensity = density) } }
                        )
                    }
                }

                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Table Grid Borders",
                    subtitle = "Draw subtle borders between item columns for clean reading",
                    checked = settings.showTableBorders,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showTableBorders = it) } },
                    testTag = "invoice_table_borders_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 17: INVOICE FOOTER
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "17. Invoice Footer",
                subtitle = "Salutations, greeting messages and page numbering",
                icon = Icons.Outlined.VerticalAlignBottom,
                expanded = expandedFooter,
                onToggleExpand = { expandedFooter = !expandedFooter }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Show Footer Greetings Text",
                    subtitle = "Print thank-you message at the very bottom",
                    checked = settings.showFooterText,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showFooterText = it) } },
                    testTag = "invoice_show_footer_switch"
                )
                InvoiceDivider()

                InvoiceActionRow(
                    title = "Footer Message",
                    value = settings.footerMessage,
                    onClick = { showFooterDialog = true }
                )
                InvoiceDivider()

                InvoiceSettingSwitchItem(
                    title = "Computer Generated Invoice Notice",
                    subtitle = "Display \"This is a computer generated invoice and requires no signature\"",
                    checked = settings.showComputerGeneratedNotice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showComputerGeneratedNotice = it) } },
                    testTag = "invoice_comp_gen_notice_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Page Numbers",
                    subtitle = "Print \"Page 1 of 1\" on multi-page PDF documents",
                    checked = settings.showPageNumbers,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showPageNumbers = it) } },
                    testTag = "invoice_show_page_num_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 18: INVOICE HISTORY & AUDIT
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "18. Invoice History & Audit",
                subtitle = "Activity logging, modifications and reason tracking",
                icon = Icons.Outlined.History,
                expanded = expandedHistory,
                onToggleExpand = { expandedHistory = !expandedHistory }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Track Activity History",
                    subtitle = "Record timestamps and actions when an invoice is edited, printed or cancelled",
                    checked = settings.trackActivityHistory,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(trackActivityHistory = it) } },
                    testTag = "invoice_track_activity_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Show Activity Timeline in Invoice Details",
                    subtitle = "Display activity log drawer in invoice screen",
                    checked = settings.showActivityLogOnInvoice,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(showActivityLogOnInvoice = it) } },
                    testTag = "invoice_show_activity_log_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Require Reason Before Cancelling",
                    subtitle = "Prompt staff for cancellation reason (e.g. Return, Wrong Entry)",
                    checked = settings.requireReasonForCancel,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(requireReasonForCancel = it) } },
                    testTag = "invoice_require_cancel_reason_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 19: ADVANCED RULES
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "19. Advanced Safety & Rules",
                subtitle = "Backdating, locking paid invoices and validation",
                icon = Icons.Outlined.Security,
                expanded = expandedAdvanced,
                onToggleExpand = { expandedAdvanced = !expandedAdvanced }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Allow Backdated Invoices",
                    subtitle = "Permit choosing past dates when creating new invoices",
                    checked = settings.allowBackdatedInvoices,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(allowBackdatedInvoices = it) } },
                    testTag = "invoice_allow_backdate_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Lock Fully Paid Invoices",
                    subtitle = "Prevent edits to invoices that have zero remaining due balance",
                    checked = settings.lockPaidInvoices,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(lockPaidInvoices = it) } },
                    testTag = "invoice_lock_paid_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Strict Duplicate Number Validation",
                    subtitle = "Block saving an invoice if the number is already used in history",
                    checked = settings.strictDuplicateNumberCheck,
                    onCheckedChange = { viewModel.updateInvoiceSettings { s -> s.copy(strictDuplicateNumberCheck = it) } },
                    testTag = "invoice_strict_dup_switch"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // SECTION 20: PERMISSIONS
            // =========================================================================
            InvoiceSettingSectionCard(
                title = "20. Staff & Role Permissions",
                subtitle = "Control staff capabilities for creating, printing and deleting",
                icon = Icons.Outlined.Lock,
                expanded = expandedPermissions,
                onToggleExpand = { expandedPermissions = !expandedPermissions }
            ) {
                InvoiceSettingSwitchItem(
                    title = "Can Create Invoices",
                    subtitle = "Allow cashiers and sales staff to create new bills",
                    checked = settings.permissions.canCreateInvoice,
                    onCheckedChange = {
                        viewModel.updateInvoiceSettings { s ->
                            s.copy(permissions = s.permissions.copy(canCreateInvoice = it))
                        }
                    },
                    testTag = "perm_create_inv_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Can Edit Saved Invoices",
                    subtitle = "Allow modifying line items or prices on existing bills",
                    checked = settings.permissions.canEditInvoice,
                    onCheckedChange = {
                        viewModel.updateInvoiceSettings { s ->
                            s.copy(permissions = s.permissions.copy(canEditInvoice = it))
                        }
                    },
                    testTag = "perm_edit_inv_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Can Cancel / Void Invoices",
                    subtitle = "Allow cancelling confirmed invoices with reason recording",
                    checked = settings.permissions.canCancelInvoice,
                    onCheckedChange = {
                        viewModel.updateInvoiceSettings { s ->
                            s.copy(permissions = s.permissions.copy(canCancelInvoice = it))
                        }
                    },
                    testTag = "perm_cancel_inv_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Can Delete Invoice Records",
                    subtitle = "Restricted to owner/admin only by default",
                    checked = settings.permissions.canDeleteInvoice,
                    onCheckedChange = {
                        viewModel.updateInvoiceSettings { s ->
                            s.copy(permissions = s.permissions.copy(canDeleteInvoice = it))
                        }
                    },
                    testTag = "perm_delete_inv_switch"
                )
                InvoiceDivider()
                InvoiceSettingSwitchItem(
                    title = "Can Print & Export Invoices",
                    subtitle = "Allow generating PDF copies and thermal print slips",
                    checked = settings.permissions.canPrintInvoice,
                    onCheckedChange = {
                        viewModel.updateInvoiceSettings { s ->
                            s.copy(permissions = s.permissions.copy(canPrintInvoice = it))
                        }
                    },
                    testTag = "perm_print_inv_switch"
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // DIALOGS
    // -------------------------------------------------------------------------

    // Preset Selector Dialog
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = {
                Text(
                    text = "Select Business Invoice Preset",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Presets preconfigure numbering, paper size, VAT breakdown and signatory options to match your business type.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    BusinessInvoicePreset.entries.forEach { preset ->
                        val isSelected = selectedPresetToApply == preset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SkyBlueCardBg else Color.Transparent)
                                .border(1.dp, if (isSelected) SkyBlueCardBorder else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedPresetToApply = preset }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.displayName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF0F172A),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = preset.description,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyBusinessInvoicePreset(selectedPresetToApply)
                        showPresetDialog = false
                        Toast.makeText(context, "Applied ${selectedPresetToApply.displayName} preset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Apply Preset") }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Prefix Dialog
    if (showPrefixDialog) {
        var prefixInput by remember { mutableStateOf(settings.invoicePrefix) }
        AlertDialog(
            onDismissRequest = { showPrefixDialog = false },
            title = { Text("Invoice Series Prefix", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                Column {
                    Text("Enter series prefix for new sales invoices (e.g. INV-, SALE-, TAX-):", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = prefixInput,
                        onValueChange = { prefixInput = it.uppercase() },
                        singleLine = true,
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
                        if (prefixInput.isNotBlank()) {
                            viewModel.setSalesPrefix(prefixInput)
                            viewModel.updateInvoiceSettings { s -> s.copy(invoicePrefix = prefixInput) }
                            showPrefixDialog = false
                            Toast.makeText(context, "Prefix updated to $prefixInput", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showPrefixDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Starting Number Dialog
    if (showStartingNumDialog) {
        var startNumInput by remember { mutableStateOf(settings.startingNumber.toString()) }
        AlertDialog(
            onDismissRequest = { showStartingNumDialog = false },
            title = { Text("Starting Counter Number", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                Column {
                    Text("Enter starting number for newly created invoice series (e.g. 1001):", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = startNumInput,
                        onValueChange = { startNumInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
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
                        val num = startNumInput.toIntOrNull()
                        if (num != null && num > 0) {
                            viewModel.updateInvoiceSettings { s -> s.copy(startingNumber = num) }
                            showStartingNumDialog = false
                            Toast.makeText(context, "Starting number set to $num", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showStartingNumDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // PAN Dialog
    if (showPanDialog) {
        var panInput by remember { mutableStateOf(settings.panVatNumber) }
        AlertDialog(
            onDismissRequest = { showPanDialog = false },
            title = { Text("Business PAN / VAT Number", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = panInput,
                    onValueChange = { panInput = it },
                    label = { Text("9-Digit PAN (e.g. 609823415)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPanVatNumber(panInput.trim())
                        showPanDialog = false
                        Toast.makeText(context, "PAN saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showPanDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        var termsInput by remember { mutableStateOf(settings.defaultTerms) }
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Invoice Terms & Conditions", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = termsInput,
                    onValueChange = { termsInput = it },
                    minLines = 4,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setInvoiceTerms(termsInput.trim())
                        showTermsDialog = false
                        Toast.makeText(context, "Terms updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Header Title Dialog
    if (showHeaderDialog) {
        var titleInput by remember { mutableStateOf(settings.customInvoiceTitle) }
        AlertDialog(
            onDismissRequest = { showHeaderDialog = false },
            title = { Text("Invoice Header Title", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it.uppercase() },
                    label = { Text("Title (e.g. TAX INVOICE, BILL OF SUPPLY)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            viewModel.setInvoiceHeader(titleInput.trim())
                            showHeaderDialog = false
                            Toast.makeText(context, "Header title saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showHeaderDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Footer Dialog
    if (showFooterDialog) {
        var footerInput by remember { mutableStateOf(settings.footerMessage) }
        AlertDialog(
            onDismissRequest = { showFooterDialog = false },
            title = { Text("Invoice Footer Message", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = footerInput,
                    onValueChange = { footerInput = it },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setInvoiceFooter(footerInput.trim())
                        showFooterDialog = false
                        Toast.makeText(context, "Footer saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showFooterDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Business Profile Edit Dialog
    if (showBusinessInfoDialog) {
        val currentProfile = businessProfile
        var nameInput by remember { mutableStateOf(currentProfile?.businessName ?: settings.businessName) }
        var panInput by remember { mutableStateOf(currentProfile?.panVatNumber ?: settings.panVatNumber) }
        var phoneInput by remember { mutableStateOf(currentProfile?.phone ?: settings.phone) }
        var emailInput by remember { mutableStateOf(currentProfile?.email ?: settings.email) }
        var addressInput by remember { mutableStateOf(currentProfile?.address ?: settings.address) }

        AlertDialog(
            onDismissRequest = { showBusinessInfoDialog = false },
            title = {
                Text(
                    text = "Edit Business Profile",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Updates business identity across all invoices and reports.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Business Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = panInput,
                        onValueChange = { panInput = it },
                        label = { Text("PAN / VAT Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Address") },
                        singleLine = true,
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
                        val baseProfile = currentProfile ?: com.example.data.local.entity.BusinessProfileEntity()
                        val updated = baseProfile.copy(
                            businessName = nameInput.trim(),
                            panVatNumber = panInput.trim(),
                            phone = phoneInput.trim(),
                            email = emailInput.trim(),
                            address = addressInput.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updateBusinessProfile(updated)
                        showBusinessInfoDialog = false
                        Toast.makeText(context, "Business Profile updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save Profile") }
            },
            dismissButton = {
                TextButton(onClick = { showBusinessInfoDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Invoice Settings?",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will restore standard invoice rules, numbering, and appearance defaults according to your current business profile. All invoice records and accounting histories remain completely safe.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyBusinessInvoicePreset(BusinessInvoicePreset.GENERAL)
                        showResetDialog = false
                        Toast.makeText(context, "Invoice settings reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InvoiceRose)
                ) { Text("Reset to Defaults", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// REUSABLE SUB-COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun InvoiceSettingsTopBar(
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
                    .testTag("invoice_settings_back_button")
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
                    text = "Invoice Settings",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Appearance, numbering, VAT, PDF & printing",
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
                .testTag("invoice_settings_reset_button")
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
private fun InvoicePresetBanner(
    currentPreset: BusinessInvoicePreset,
    onSelectPresetClick: () -> Unit
) {
    val isDark = AppTheme.isDark
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) SkyBlueCardBg else Color(0xFFEFF6FF)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, if (isDark) SkyBlueCardBorder else Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
            .clickable { onSelectPresetClick() }
            .testTag("invoice_preset_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SkyBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = null,
                        tint = SkyBlueBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Preset: ${currentPreset.displayName}",
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SkyBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = SkyBlueBright,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentPreset.description,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Switch",
                tint = SkyBlueBright,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InvoiceSettingSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    val isDark = AppTheme.isDark
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrowRotation")

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDark) BackgroundDark else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = subtitle,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(
                        color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun InvoiceSettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val isDark = AppTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = if (isDark) TextWhite else Color(0xFF0F172A),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SkyBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun InvoiceActionRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val isDark = AppTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isDark) TextWhite else Color(0xFF0F172A),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = SkyBlueBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
        ) {
            Text("Edit", color = SkyBlueBright, fontSize = 11.5.sp)
        }
    }
}

@Composable
private fun InvoiceSelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = AppTheme.isDark
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SkyBlueCardBg else if (isDark) CardDark else Color(0xFFF1F5F9))
            .border(
                1.dp,
                if (isSelected) SkyBlueCardBorder else if (isDark) CardBorder else Color(0xFFE2E8F0),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF0F172A),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun InvoiceDivider() {
    val isDark = AppTheme.isDark
    HorizontalDivider(
        color = if (isDark) CardBorder.copy(alpha = 0.4f) else Color(0xFFF1F5F9),
        thickness = 0.8.dp,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}
