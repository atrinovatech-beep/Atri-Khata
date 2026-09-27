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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewCompact
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
import com.example.data.model.BusinessTransactionPreset
import com.example.data.model.TransactionSettings
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
 * Atri Khata 2 - Advanced Transaction Settings & Configuration Screen
 * Comprehensive, persistent, Business-Preset aware, theme-safe, and future-ready.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val settings by viewModel.transactionSettings.collectAsStateWithLifecycle()

    // Dialog state management
    var showPresetDialog by remember { mutableStateOf(false) }
    var selectedPresetToApply by remember { mutableStateOf(settings.businessPreset) }
    var showPrefixDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var prefixDialogTitle by remember { mutableStateOf("Sales Invoice Prefix") }
    var currentEditingPrefix by remember { mutableStateOf(settings.salesInvoicePrefix) }
    var onSavePrefixAction by remember { mutableStateOf<(String) -> Unit>({}) }

    // Section expansion state
    var expandedGeneral by remember { mutableStateOf(true) }
    var expandedTypes by remember { mutableStateOf(false) }
    var expandedSales by remember { mutableStateOf(false) }
    var expandedPurchase by remember { mutableStateOf(false) }
    var expandedPayment by remember { mutableStateOf(false) }
    var expandedExpense by remember { mutableStateOf(false) }
    var expandedNumbering by remember { mutableStateOf(false) }
    var expandedParty by remember { mutableStateOf(false) }
    var expandedCalcTax by remember { mutableStateOf(false) }
    var expandedDateDue by remember { mutableStateOf(false) }
    var expandedRemarksRef by remember { mutableStateOf(false) }
    var expandedAttachment by remember { mutableStateOf(false) }
    var expandedStock by remember { mutableStateOf(false) }
    var expandedAccounting by remember { mutableStateOf(false) }
    var expandedDisplayHistory by remember { mutableStateOf(false) }
    var expandedPermissions by remember { mutableStateOf(false) }
    var expandedAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
            .testTag("transaction_settings_screen")
    ) {
        // Top Header
        TransactionSettingsTopBar(
            onBack = onBack,
            onResetDefaults = { showResetDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Transaction Status Summary Card
        TransactionStatusSummaryCard(settings = settings, isDark = isDark)

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Business Type Preset Selector Banner
        BusinessTransactionPresetBanner(
            currentPreset = settings.businessPreset,
            isDark = isDark,
            onOpenPresetDialog = {
                selectedPresetToApply = settings.businessPreset
                showPresetDialog = true
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: General Transaction Settings
        ExpandableSettingsSection(
            title = "General Transaction Settings",
            subtitle = "Transaction module availability, confirmation dialogs, and auto-remember rules.",
            icon = Icons.Outlined.Settings,
            isExpanded = expandedGeneral,
            onToggleExpand = { expandedGeneral = !expandedGeneral },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Transaction Module",
                    subtitle = "Master toggle for recording transactions across Atri Khata.",
                    checked = settings.enableTransactions,
                    onCheckedChange = {
                        viewModel.updateTransactionSettings { s -> s.copy(enableTransactions = it) }
                        Toast.makeText(
                            context,
                            if (it) "Transaction Module Enabled" else "Transactions Paused (Historical Data Safe)",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    testTag = "switch_enable_transactions"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Auto Save Draft",
                    subtitle = "Automatically preserve unsaved entries during multitasking.",
                    checked = settings.autoSaveDraft,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(autoSaveDraft = it) } },
                    testTag = "switch_auto_save_draft"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Confirm Before Delete",
                    subtitle = "Prompt confirmation before permanently removing a transaction.",
                    checked = settings.confirmBeforeDelete,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(confirmBeforeDelete = it) } },
                    testTag = "switch_confirm_delete"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Confirm Before Cancel",
                    subtitle = "Require confirmation before marking confirmed invoices as cancelled.",
                    checked = settings.confirmBeforeCancel,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(confirmBeforeCancel = it) } },
                    testTag = "switch_confirm_cancel"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Edit Saved Transaction",
                    subtitle = "Permit modifying items, amounts, or parties on confirmed transactions.",
                    checked = settings.allowEditSavedTransaction,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowEditSavedTransaction = it) } },
                    testTag = "switch_allow_edit_saved"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Duplicate Transaction",
                    subtitle = "Enable 1-tap duplication of existing invoices or vouchers.",
                    checked = settings.allowDuplicateTransaction,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowDuplicateTransaction = it) } },
                    testTag = "switch_allow_duplicate"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Default Date = Today",
                    subtitle = "Automatically populate new transactions with today's BS/AD date.",
                    checked = settings.defaultTransactionDateToday,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(defaultTransactionDateToday = it) } },
                    testTag = "switch_default_date_today"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Remember Last Payment Mode",
                    subtitle = "Auto-select last used payment mode (Cash, Bank, QR) on next entry.",
                    checked = settings.rememberLastPaymentMode,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(rememberLastPaymentMode = it) } },
                    testTag = "switch_remember_payment_mode"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Remember Last Account",
                    subtitle = "Auto-select primary Cash/Bank deposit account.",
                    checked = settings.rememberLastAccount,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(rememberLastAccount = it) } },
                    testTag = "switch_remember_account"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: Transaction Types
        ExpandableSettingsSection(
            title = "Transaction Types",
            subtitle = "Enable or disable specific transaction flows. Historical data remains protected.",
            icon = Icons.Outlined.CompareArrows,
            isExpanded = expandedTypes,
            onToggleExpand = { expandedTypes = !expandedTypes },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Sales Invoices",
                    subtitle = "Customer billing, POS receipts, and tax invoices.",
                    checked = settings.salesEnabled,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(salesEnabled = it) } },
                    testTag = "switch_sales_enabled"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Purchase Bills",
                    subtitle = "Supplier procurement, purchase orders, and inward inventory.",
                    checked = settings.purchaseEnabled,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(purchaseEnabled = it) } },
                    testTag = "switch_purchase_enabled"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Payment In (Customer Receipt)",
                    subtitle = "Receiving cash/bank payments against sales invoices.",
                    checked = settings.paymentInEnabled,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(paymentInEnabled = it) } },
                    testTag = "switch_payment_in_enabled"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Payment Out (Supplier Voucher)",
                    subtitle = "Paying suppliers, vendors, and settling payables.",
                    checked = settings.paymentOutEnabled,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(paymentOutEnabled = it) } },
                    testTag = "switch_payment_out_enabled"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Expense Entries",
                    subtitle = "Operating expenses, rent, salaries, utilities, and office costs.",
                    checked = settings.expenseEnabled,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(expenseEnabled = it) } },
                    testTag = "switch_expense_enabled"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show in Quick Actions",
                    subtitle = "Display enabled transaction shortcuts on the Home dashboard.",
                    checked = settings.showInQuickActions,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showInQuickActions = it) } },
                    testTag = "switch_show_quick_actions"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: Sales Form Settings
        ExpandableSettingsSection(
            title = "Sales Settings",
            subtitle = "Field visibility, default payment modes, credit days, and pricing defaults.",
            icon = Icons.Outlined.ReceiptLong,
            isExpanded = expandedSales,
            onToggleExpand = { expandedSales = !expandedSales },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Customer / Party Field",
                    subtitle = "Select registered customer or record party details on invoice.",
                    checked = settings.showCustomerParty,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showCustomerParty = it) } },
                    testTag = "switch_sales_show_party"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Product / Item Selector",
                    subtitle = "Include inventory catalog items and itemized lines in sales.",
                    checked = settings.showProductItem,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showProductItem = it) } },
                    testTag = "switch_sales_show_product"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Discount Field",
                    subtitle = "Allow item-wise or invoice-level cash/percentage discounts.",
                    checked = settings.showDiscount,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showDiscount = it) } },
                    testTag = "switch_sales_show_discount"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "VAT / Tax Field",
                    subtitle = "Calculate and display VAT amounts (13% Nepal standard).",
                    checked = settings.showVat,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showVat = it) } },
                    testTag = "switch_sales_show_vat"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Due Date Field",
                    subtitle = "Specify credit settlement deadline on invoices.",
                    checked = settings.showDueDate,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showDueDate = it) } },
                    testTag = "switch_sales_show_due_date"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Salesperson / Attendant Field",
                    subtitle = "Assign sales staff or field representative for commission/audit.",
                    checked = settings.showSalesperson,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showSalesperson = it) } },
                    testTag = "switch_sales_show_salesperson"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Round Off Total",
                    subtitle = "Automatically round off grand total to the nearest whole rupee.",
                    checked = settings.showRoundOff,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showRoundOff = it) } },
                    testTag = "switch_sales_show_round_off"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Default Payment Mode Picker
                Text("DEFAULT PAYMENT MODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "Bank Transfer", "Cheque", "Digital Wallet").forEach { mode ->
                        SelectableFormatChip(
                            label = mode,
                            isSelected = settings.defaultPaymentMode == mode,
                            onClick = {
                                viewModel.updateTransactionSettings { s -> s.copy(defaultPaymentMode = mode) }
                                Toast.makeText(context, "Default payment mode: $mode", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "chip_sales_mode_$mode"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Default Credit Period Picker
                Text("DEFAULT CREDIT PERIOD (DAYS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 15, 30, 45, 60).forEach { days ->
                        SelectableFormatChip(
                            label = "$days Days",
                            isSelected = settings.defaultCreditDays == days,
                            onClick = {
                                viewModel.updateTransactionSettings { s -> s.copy(defaultCreditDays = days) }
                                Toast.makeText(context, "Credit period set to $days days", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "chip_credit_days_$days"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 4: Purchase Settings
        ExpandableSettingsSection(
            title = "Purchase Settings",
            subtitle = "Supplier procurement forms, rate & VAT inputs, and inward delivery notes.",
            icon = Icons.Outlined.ShoppingCart,
            isExpanded = expandedPurchase,
            onToggleExpand = { expandedPurchase = !expandedPurchase },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Supplier Required",
                    subtitle = "Disallow anonymous purchases; supplier selection required.",
                    checked = settings.supplierRequired,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(supplierRequired = it) } },
                    testTag = "switch_purchase_supplier_req"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Purchase Rate & Discount",
                    subtitle = "Display purchase unit cost and supplier discounts.",
                    checked = settings.showPurchaseRate,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showPurchaseRate = it) } },
                    testTag = "switch_purchase_show_rate"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Purchase VAT & Taxes",
                    subtitle = "Track input VAT credit on inward purchase invoices.",
                    checked = settings.showPurchaseVat,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showPurchaseVat = it) } },
                    testTag = "switch_purchase_show_vat"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Purchase Due Date",
                    subtitle = "Track payable due date and supplier credit terms.",
                    checked = settings.showPurchaseDueDate,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showPurchaseDueDate = it) } },
                    testTag = "switch_purchase_due_date"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 5: Payment Settings
        ExpandableSettingsSection(
            title = "Payment & Receipt Settings",
            subtitle = "Payment In/Out configuration, invoice allocation, and partial receipts.",
            icon = Icons.Outlined.Payments,
            isExpanded = expandedPayment,
            onToggleExpand = { expandedPayment = !expandedPayment },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PAYMENT IN (CUSTOMER RECEIPTS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkyBlueBright)
                Spacer(modifier = Modifier.height(8.dp))

                SettingsSwitchRow(
                    title = "Invoice Allocation",
                    subtitle = "Allow linking received payment to specific unpaid sales invoices.",
                    checked = settings.paymentInEnableAllocation,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(paymentInEnableAllocation = it) } },
                    testTag = "switch_payment_in_allocation"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Partial Payment",
                    subtitle = "Support partial settlements against open invoice balances.",
                    checked = settings.paymentInEnablePartialPayment,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(paymentInEnablePartialPayment = it) } },
                    testTag = "switch_payment_in_partial"
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) CardBorder else Color(0xFFE2E8F0), thickness = 2.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("PAYMENT OUT (SUPPLIER PAYMENTS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberWarn)
                Spacer(modifier = Modifier.height(8.dp))

                SettingsSwitchRow(
                    title = "Supplier Required on Payment Out",
                    subtitle = "Require choosing a registered supplier before saving payment.",
                    checked = settings.paymentOutPartyRequired,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(paymentOutPartyRequired = it) } },
                    testTag = "switch_payment_out_party_req"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Supplier Bill Allocation",
                    subtitle = "Allocate payments against pending purchase bills.",
                    checked = settings.paymentOutEnableAllocation,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(paymentOutEnableAllocation = it) } },
                    testTag = "switch_payment_out_allocation"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 6: Expense Settings
        ExpandableSettingsSection(
            title = "Expense Settings",
            subtitle = "Expense categorization, source accounts, vendors, and recurring entries.",
            icon = Icons.Outlined.CreditCard,
            isExpanded = expandedExpense,
            onToggleExpand = { expandedExpense = !expandedExpense },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Expense Category",
                    subtitle = "Classify expense by ledger (Rent, Salary, Utilities, Travel).",
                    checked = settings.showExpenseCategory,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showExpenseCategory = it) } },
                    testTag = "switch_expense_category"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Paid From Account",
                    subtitle = "Specify the Cash drawer or Bank account used for payment.",
                    checked = settings.showPaidFromAccount,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showPaidFromAccount = it) } },
                    testTag = "switch_expense_account"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Vendor / Payee Field",
                    subtitle = "Record service provider or supplier who received the payment.",
                    checked = settings.showExpenseVendor,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showExpenseVendor = it) } },
                    testTag = "switch_expense_vendor"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Recurring Expense",
                    subtitle = "Mark monthly recurring costs (Future-ready architecture).",
                    checked = settings.allowRecurringExpense,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowRecurringExpense = it) } },
                    testTag = "switch_expense_recurring"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 7: Invoice & Series Numbering
        ExpandableSettingsSection(
            title = "Invoice & Series Numbering",
            subtitle = "Prefixes, starting numbers, fiscal year resets, and auto-generation.",
            icon = Icons.Outlined.Tune,
            isExpanded = expandedNumbering,
            onToggleExpand = { expandedNumbering = !expandedNumbering },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Auto Generate Invoice Number",
                    subtitle = "Automatically assign sequential numbers on save.",
                    checked = settings.autoGenerateInvoiceNumber,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(autoGenerateInvoiceNumber = it) } },
                    testTag = "switch_auto_invoice_num"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Include Fiscal Year in Prefix",
                    subtitle = "Append current Nepali fiscal year (e.g. INV-2082-####).",
                    checked = settings.includeFiscalYearInInvoice,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(includeFiscalYearInInvoice = it) } },
                    testTag = "switch_include_fiscal_year"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Prefix Editor Rows
                PrefixItemRow(
                    label = "Sales Invoice Prefix",
                    prefix = settings.salesInvoicePrefix,
                    example = "${settings.salesInvoicePrefix}1001",
                    isDark = isDark,
                    onEdit = {
                        prefixDialogTitle = "Sales Invoice Prefix"
                        currentEditingPrefix = settings.salesInvoicePrefix
                        onSavePrefixAction = { p -> viewModel.updateTransactionSettings { s -> s.copy(salesInvoicePrefix = p) } }
                        showPrefixDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrefixItemRow(
                    label = "Purchase Bill Prefix",
                    prefix = settings.purchasePrefix,
                    example = "${settings.purchasePrefix}0101",
                    isDark = isDark,
                    onEdit = {
                        prefixDialogTitle = "Purchase Bill Prefix"
                        currentEditingPrefix = settings.purchasePrefix
                        onSavePrefixAction = { p -> viewModel.updateTransactionSettings { s -> s.copy(purchasePrefix = p) } }
                        showPrefixDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrefixItemRow(
                    label = "Payment Receipt Prefix",
                    prefix = settings.paymentInPrefix,
                    example = "${settings.paymentInPrefix}0042",
                    isDark = isDark,
                    onEdit = {
                        prefixDialogTitle = "Payment Receipt Prefix"
                        currentEditingPrefix = settings.paymentInPrefix
                        onSavePrefixAction = { p -> viewModel.updateTransactionSettings { s -> s.copy(paymentInPrefix = p) } }
                        showPrefixDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 8: Customer & Party Settings
        ExpandableSettingsSection(
            title = "Customer / Party Settings",
            subtitle = "Walk-in rules, credit limits, outstanding balances, and customer notes.",
            icon = Icons.Outlined.Person,
            isExpanded = expandedParty,
            onToggleExpand = { expandedParty = !expandedParty },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Allow Walk-in / Counter Sales",
                    subtitle = "Permit cash transactions without creating a party account.",
                    checked = settings.allowWalkInCustomer,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowWalkInCustomer = it) } },
                    testTag = "switch_allow_walk_in"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Outstanding Balance",
                    subtitle = "Display current party receivable/payable balance inside form.",
                    checked = settings.showOutstandingBalance,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showOutstandingBalance = it) } },
                    testTag = "switch_show_outstanding"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Credit Limit",
                    subtitle = "Display approved party credit ceiling and warn if exceeded.",
                    checked = settings.showCreditLimit,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showCreditLimit = it) } },
                    testTag = "switch_show_credit_limit"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 9: Calculation & Tax Settings
        ExpandableSettingsSection(
            title = "Calculation & Tax Settings",
            subtitle = "Subtotal, VAT breakdowns, taxable amounts, and rounding rules.",
            icon = Icons.Outlined.Calculate,
            isExpanded = expandedCalcTax,
            onToggleExpand = { expandedCalcTax = !expandedCalcTax },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Enable VAT System",
                    subtitle = "Master switch for VAT calculations and tax invoices.",
                    checked = settings.enableVat,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(enableVat = it) } },
                    testTag = "switch_enable_vat"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show VAT Breakdown",
                    subtitle = "Display 13% Taxable Amount, Non-Taxable, and VAT in summaries.",
                    checked = settings.showVatBreakdown,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showVatBreakdown = it) } },
                    testTag = "switch_show_vat_breakdown"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Subtotal",
                    subtitle = "Display pre-tax, pre-discount raw items total.",
                    checked = settings.showSubtotal,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showSubtotal = it) } },
                    testTag = "switch_show_subtotal"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 10: Date & Due Date Settings
        ExpandableSettingsSection(
            title = "Date & Due Date Settings",
            subtitle = "Calendar defaults, backdated entry permissions, and credit periods.",
            icon = Icons.Outlined.CalendarMonth,
            isExpanded = expandedDateDue,
            onToggleExpand = { expandedDateDue = !expandedDateDue },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Allow Backdated Transactions",
                    subtitle = "Permit entering transactions for past dates within fiscal year.",
                    checked = settings.allowBackdatedTransaction,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowBackdatedTransaction = it) } },
                    testTag = "switch_allow_backdated"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Future-Dated Transactions",
                    subtitle = "Permit post-dated cheques or future scheduled invoices.",
                    checked = settings.allowFutureDatedTransaction,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowFutureDatedTransaction = it) } },
                    testTag = "switch_allow_future_dated"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Transaction Time",
                    subtitle = "Record and display exact hour/minute timestamp on bills.",
                    checked = settings.showTransactionTime,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showTransactionTime = it) } },
                    testTag = "switch_show_transaction_time"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 11: Remarks & Reference Settings
        ExpandableSettingsSection(
            title = "Remarks & Reference Settings",
            subtitle = "Reference voucher codes, public customer remarks, and internal notes.",
            icon = Icons.Outlined.Description,
            isExpanded = expandedRemarksRef,
            onToggleExpand = { expandedRemarksRef = !expandedRemarksRef },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Reference Number Required",
                    subtitle = "Disallow blank cheque/voucher/slip reference numbers.",
                    checked = settings.referenceRequired,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(referenceRequired = it) } },
                    testTag = "switch_ref_required"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Customer Note",
                    subtitle = "Display notes section printed on the customer's invoice.",
                    checked = settings.showCustomerNote,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showCustomerNote = it) } },
                    testTag = "switch_show_cust_note"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Internal Audit Note",
                    subtitle = "Private staff note not visible on printed customer receipts.",
                    checked = settings.showInternalNote,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showInternalNote = it) } },
                    testTag = "switch_show_internal_note"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 12: Attachment Settings
        ExpandableSettingsSection(
            title = "Attachment Settings",
            subtitle = "Receipt photos, supplier bills, payment screenshots, and documents.",
            icon = Icons.Outlined.AttachFile,
            isExpanded = expandedAttachment,
            onToggleExpand = { expandedAttachment = !expandedAttachment },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Enable Attachments",
                    subtitle = "Permit attaching photos or documents to transactions.",
                    checked = settings.enableAttachments,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(enableAttachments = it) } },
                    testTag = "switch_enable_attachments"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Camera & Images",
                    subtitle = "Support JPEG and PNG voucher photos.",
                    checked = settings.allowImageAttachment,
                    enabled = settings.enableAttachments,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowImageAttachment = it) } },
                    testTag = "switch_allow_images"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Attachment in Details",
                    subtitle = "Preview attached bills directly inside transaction detail view.",
                    checked = settings.showAttachmentInDetails,
                    enabled = settings.enableAttachments,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showAttachmentInDetails = it) } },
                    testTag = "switch_show_attach_details"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 13: Stock Integration
        ExpandableSettingsSection(
            title = "Stock Integration",
            subtitle = "Coordination between Sales/Purchase and physical inventory balances.",
            icon = Icons.Outlined.Inventory2,
            isExpanded = expandedStock,
            onToggleExpand = { expandedStock = !expandedStock },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Decrease Stock Automatically",
                    subtitle = "Deduct product inventory immediately upon saving a sales invoice.",
                    checked = settings.decreaseStockOnSale,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(decreaseStockOnSale = it) } },
                    testTag = "switch_decrease_stock_sale"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Increase Stock on Purchase",
                    subtitle = "Add purchased quantity directly to inventory balance.",
                    checked = settings.increaseStockOnPurchase,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(increaseStockOnPurchase = it) } },
                    testTag = "switch_increase_stock_purchase"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Available Stock in Sales Form",
                    subtitle = "Display live remaining item quantity while typing invoice items.",
                    checked = settings.showAvailableStockInForm,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showAvailableStockInForm = it) } },
                    testTag = "switch_show_available_stock"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 14: Accounting Integration
        ExpandableSettingsSection(
            title = "Accounting Integration",
            subtitle = "Frappe-style double entry posting and automated ledger balance updates.",
            icon = Icons.Outlined.AccountBalance,
            isExpanded = expandedAccounting,
            onToggleExpand = { expandedAccounting = !expandedAccounting },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Update Party Balance on Payment",
                    subtitle = "Recalculate customer/supplier balance immediately upon receipt/payment.",
                    checked = settings.updatePartyBalanceOnPayment,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(updatePartyBalanceOnPayment = it) } },
                    testTag = "switch_update_party_balance"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Update Cash/Bank Accounts",
                    subtitle = "Reflect payment movements in selected deposit or payout accounts.",
                    checked = settings.updateCashBankOnPayment,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(updateCashBankOnPayment = it) } },
                    testTag = "switch_update_cash_bank"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Post to Expense Ledger",
                    subtitle = "Update operating expense ledgers for P&L reports.",
                    checked = settings.updateExpenseOnExpenseEntry,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(updateExpenseOnExpenseEntry = it) } },
                    testTag = "switch_post_expense_ledger"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 15: Transaction Display & History Settings
        ExpandableSettingsSection(
            title = "Display & History Settings",
            subtitle = "List density, visible columns, recent transaction count, and draft visibility.",
            icon = Icons.Outlined.History,
            isExpanded = expandedDisplayHistory,
            onToggleExpand = { expandedDisplayHistory = !expandedDisplayHistory },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LIST DENSITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Comfortable", "Compact").forEach { density ->
                        SelectableFormatChip(
                            label = density,
                            isSelected = settings.listDensity == density,
                            onClick = {
                                viewModel.updateTransactionSettings { s -> s.copy(listDensity = density) }
                                Toast.makeText(context, "List layout set to $density", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "chip_density_$density"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSwitchRow(
                    title = "Show Payment Mode Badge",
                    subtitle = "Display Cash, Bank, or QR badges on transaction cards.",
                    checked = settings.showPaymentModeInList,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showPaymentModeInList = it) } },
                    testTag = "switch_show_mode_badge"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Reference Number",
                    subtitle = "Display voucher/cheque reference numbers on list cards.",
                    checked = settings.showReferenceInList,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showReferenceInList = it) } },
                    testTag = "switch_show_ref_list"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Cancelled Transactions",
                    subtitle = "Display cancelled invoices with strikethrough in transaction ledger.",
                    checked = settings.showCancelledTransactions,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(showCancelledTransactions = it) } },
                    testTag = "switch_show_cancelled"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 16: Staff Permissions
        ExpandableSettingsSection(
            title = "Role & Staff Permissions",
            subtitle = "Role-based limits for creating, editing, and deleting sales, purchases, and vouchers.",
            icon = Icons.Outlined.Security,
            isExpanded = expandedPermissions,
            onToggleExpand = { expandedPermissions = !expandedPermissions },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Staff Can Create Sales",
                    subtitle = "Permit staff roles to issue new customer invoices.",
                    checked = settings.permissions.canCreateSales,
                    onCheckedChange = { checked ->
                        viewModel.updateTransactionSettings { s ->
                            s.copy(permissions = s.permissions.copy(canCreateSales = checked))
                        }
                    },
                    testTag = "switch_perm_create_sales"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Staff Can Delete Transactions",
                    subtitle = "Permit non-admin staff to delete historical transaction records.",
                    checked = settings.permissions.canDeleteSales,
                    onCheckedChange = { checked ->
                        viewModel.updateTransactionSettings { s ->
                            s.copy(permissions = s.permissions.copy(canDeleteSales = checked))
                        }
                    },
                    testTag = "switch_perm_delete_sales"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Staff Can View Profit",
                    subtitle = "Show gross profit margin and markup on sales detail screens.",
                    checked = settings.permissions.canViewProfit,
                    onCheckedChange = { checked ->
                        viewModel.updateTransactionSettings { s ->
                            s.copy(permissions = s.permissions.copy(canViewProfit = checked))
                        }
                    },
                    testTag = "switch_perm_view_profit"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 17: Advanced Settings
        ExpandableSettingsSection(
            title = "Advanced & Compliance Settings",
            subtitle = "Transaction locking, duplicate prevention, cancellation reasons, and audit trails.",
            icon = Icons.Outlined.Lock,
            isExpanded = expandedAdvanced,
            onToggleExpand = { expandedAdvanced = !expandedAdvanced },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Require Reason Before Cancel",
                    subtitle = "Enforce typing a cancellation note before voiding an invoice.",
                    checked = settings.requireReasonBeforeCancel,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(requireReasonBeforeCancel = it) } },
                    testTag = "switch_require_reason_cancel"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Negative Party Balance",
                    subtitle = "Permit customer balances to exceed zero without strict pre-payment.",
                    checked = settings.allowNegativePartyBalance,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(allowNegativePartyBalance = it) } },
                    testTag = "switch_allow_neg_party_balance"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Enable Transaction Audit Trail",
                    subtitle = "Log user timestamps and devices for every transaction created.",
                    checked = settings.enableTransactionAudit,
                    onCheckedChange = { viewModel.updateTransactionSettings { s -> s.copy(enableTransactionAudit = it) } },
                    testTag = "switch_enable_audit"
                )
            }
        }
    }

    // Preset Selection Dialog
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = {
                Text(
                    text = "Select Business Transaction Preset",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Switching preset updates defaults for forms, VAT, party rules, and stock integration. Existing transaction records remain 100% intact.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    BusinessTransactionPreset.entries.forEach { preset ->
                        val isSelected = selectedPresetToApply == preset
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SkyBlueCardBg else if (isDark) BackgroundDark else Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isSelected) SkyBlueBright else if (isDark) CardBorder else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPresetToApply = preset }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.displayName,
                                            color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF0F172A),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SkyBlueBright,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = preset.description,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyBusinessTransactionPreset(selectedPresetToApply)
                        showPresetDialog = false
                        Toast.makeText(
                            context,
                            "Applied ${selectedPresetToApply.displayName} Preset Defaults",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Apply Preset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Prefix Editing Dialog
    if (showPrefixDialog) {
        var prefixInput by remember { mutableStateOf(currentEditingPrefix) }
        AlertDialog(
            onDismissRequest = { showPrefixDialog = false },
            title = {
                Text(
                    text = prefixDialogTitle,
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter alphanumeric series prefix for new records (e.g. INV-, REC-, PUR-):",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = prefixInput,
                        onValueChange = { prefixInput = it.uppercase() },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (prefixInput.isNotBlank()) {
                            onSavePrefixAction(prefixInput)
                            showPrefixDialog = false
                            Toast.makeText(context, "Prefix updated to $prefixInput", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Save Prefix", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrefixDialog = false }) {
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
                Text(
                    text = "Reset Transaction Settings?",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will restore standard transaction rules and defaults according to your current business profile. All transaction records and ledgers are completely safe.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyBusinessTransactionPreset(BusinessTransactionPreset.GENERAL)
                        showResetDialog = false
                        Toast.makeText(context, "Transaction settings reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InvoiceRose)
                ) {
                    Text("Reset to Defaults", color = Color.White)
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
}

// -----------------------------------------------------------------------------
// SUB-COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun TransactionSettingsTopBar(
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
                    text = "Transaction Settings",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vouchers, invoices, numbering & defaults",
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
private fun TransactionStatusSummaryCard(
    settings: TransactionSettings,
    isDark: Boolean
) {
    val enabledTypesCount = listOf(
        settings.salesEnabled,
        settings.purchaseEnabled,
        settings.paymentInEnabled,
        settings.paymentOutEnabled,
        settings.expenseEnabled
    ).count { it }

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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkyBlueCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CompareArrows,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (settings.enableTransactions) "Transaction Engine Active" else "Transactions Paused",
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settings.businessPreset.displayName} • $enabledTypesCount Active Flows",
                            color = if (settings.enableTransactions) InvoiceEmerald else AmberWarn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (settings.enableTransactions) InvoiceEmerald.copy(alpha = 0.15f) else AmberWarn.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (settings.enableTransactions) "LIVE" else "PAUSED",
                        color = if (settings.enableTransactions) InvoiceEmerald else AmberWarn,
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
                    Text("Payment Mode", fontSize = 11.sp, color = TextMuted)
                    Text(settings.defaultPaymentMode, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) TextWhite else Color(0xFF0F172A))
                }
                Column {
                    Text("Invoice Prefix", fontSize = 11.sp, color = TextMuted)
                    Text("${settings.salesInvoicePrefix}####", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SkyBlueBright)
                }
                Column {
                    Text("Auto Stock Deduction", fontSize = 11.sp, color = TextMuted)
                    Text(if (settings.decreaseStockOnSale) "Enabled" else "Off", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (settings.decreaseStockOnSale) InvoiceEmerald else TextMuted)
                }
            }
        }
    }
}

@Composable
private fun BusinessTransactionPresetBanner(
    currentPreset: BusinessTransactionPreset,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Preset: ${currentPreset.displayName}",
                            color = if (isDark) TextWhite else Color(0xFF1E3A8A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Change", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun ExpandableSettingsSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
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
                    .clickable { onToggleExpand() }
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

                IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
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
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    val isDark = AppTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = if (enabled) {
                    if (isDark) TextWhite else Color(0xFF0F172A)
                } else TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SkyBlue,
                uncheckedThumbColor = SkyBlueBright,
                uncheckedTrackColor = if (isDark) BackgroundDark else Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun PrefixItemRow(
    label: String,
    prefix: String,
    example: String,
    isDark: Boolean,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontSize = 12.sp, color = TextMuted)
            Text("Prefix: $prefix (Sample: $example)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SkyBlueBright)
        }
        Button(
            onClick = onEdit,
            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp).border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
        ) {
            Text("Edit", color = SkyBlueBright, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SelectableFormatChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val isDark = AppTheme.isDark
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SkyBlueCardBg else if (isDark) BackgroundDark else Color(0xFFF1F5F9))
            .border(
                1.dp,
                if (isSelected) SkyBlueBright else if (isDark) CardBorder else Color(0xFFCBD5E1),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF334155),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
