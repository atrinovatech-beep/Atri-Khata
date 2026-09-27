package com.example.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AltRoute
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BusinessInventoryPreset
import com.example.data.model.DefaultSalePriceType
import com.example.data.model.ExpiredItemAction
import com.example.data.model.InventorySettings
import com.example.data.model.StockValuationMethod
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

/**
 * Atri Khata 2 - Advanced Inventory Settings & Configuration Screen
 * Configurable, persistent, Business-Type aware, theme-safe, and future-ready.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventorySettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val settings by viewModel.inventorySettings.collectAsStateWithLifecycle()

    // Dialog state management
    var showPresetDialog by remember { mutableStateOf(false) }
    var selectedPresetToApply by remember { mutableStateOf(settings.businessPreset) }
    var showMinStockDialog by remember { mutableStateOf(false) }
    var showNearExpiryDialog by remember { mutableStateOf(false) }
    var showCriticalExpiryDialog by remember { mutableStateOf(false) }

    // Section expansion state
    var expandedGeneral by remember { mutableStateOf(true) }
    var expandedItemFields by remember { mutableStateOf(false) }
    var expandedStockControl by remember { mutableStateOf(false) }
    var expandedAdjustment by remember { mutableStateOf(false) }
    var expandedAlerts by remember { mutableStateOf(false) }
    var expandedBatchExpiry by remember { mutableStateOf(false) }
    var expandedValuation by remember { mutableStateOf(false) }
    var expandedSales by remember { mutableStateOf(false) }
    var expandedPurchase by remember { mutableStateOf(false) }
    var expandedPermissions by remember { mutableStateOf(false) }

    val moduleEnabled = settings.inventoryModuleEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp)
            .testTag("inventory_settings_screen")
    ) {
        // Top Header
        InventorySettingsTopBar(onBack = onBack)

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Settings Summary Card (Section 27)
        InventoryStatusSummaryCard(settings = settings, isDark = isDark)

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Business Type Preset Selector Banner (Section 19 & 20)
        BusinessPresetBanner(
            currentPreset = settings.businessPreset,
            isDark = isDark,
            onOpenPresetDialog = {
                selectedPresetToApply = settings.businessPreset
                showPresetDialog = true
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: General Inventory (Section 4)
        ExpandableSettingsSection(
            title = "General",
            subtitle = "Inventory module availability, stock tracking, and item catalog visibility.",
            icon = Icons.Outlined.Inventory2,
            isExpanded = expandedGeneral,
            onToggleExpand = { expandedGeneral = !expandedGeneral },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Inventory Module",
                    subtitle = "Master switch to enable or disable inventory throughout the app.",
                    checked = settings.inventoryModuleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(inventoryModuleEnabled = it) }
                        Toast.makeText(
                            context,
                            if (it) "Inventory Module Enabled" else "Inventory Module Disabled (Data Preserved)",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    testTag = "switch_inventory_module"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Track Stock Automatically",
                    subtitle = "Automatically update stock balance upon sales and purchases.",
                    checked = settings.trackStockAutomatically,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(trackStockAutomatically = it) }
                    },
                    testTag = "switch_track_stock_auto"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Stock Value",
                    subtitle = "Display total catalog valuation (Stock Qty × Purchase Price).",
                    checked = settings.showStockValue,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(showStockValue = it) }
                    },
                    testTag = "switch_show_stock_value"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Show Stock Status",
                    subtitle = "Display In Stock, Low Stock, and Out of Stock indicators.",
                    checked = settings.showStockStatus,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(showStockStatus = it) }
                    },
                    testTag = "switch_show_stock_status"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Enable Categories",
                    subtitle = "Group inventory items by product categories and filters.",
                    checked = settings.enableCategories,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(enableCategories = it) }
                    },
                    testTag = "switch_enable_categories"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Enable SKU / Item Code",
                    subtitle = "Assign unique alphanumeric SKU codes to inventory items.",
                    checked = settings.enableSku,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(enableSku = it) }
                    },
                    testTag = "switch_enable_sku"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Enable Barcode Scanner",
                    subtitle = "Support camera barcode scanning during item lookup and billing.",
                    checked = settings.enableBarcode,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(enableBarcode = it) }
                    },
                    testTag = "switch_enable_barcode"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Item / Product Settings (Section 6 & 7)
        ExpandableSettingsSection(
            title = "Item / Product",
            subtitle = "Control which item attributes appear in the catalog and item editor.",
            icon = Icons.Outlined.Category,
            isExpanded = expandedItemFields,
            onToggleExpand = { expandedItemFields = !expandedItemFields },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Item Name (Mandatory)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Item Name",
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Primary product title. Required for all catalog entries.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(SkyBlueCardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Mandatory", color = SkyBlueBright, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "SKU / Item Code Field",
                    subtitle = "Capture SKU field when creating or editing items.",
                    checked = settings.skuEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(skuEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Category Field",
                    subtitle = "Allow assigning items to specific product categories.",
                    checked = settings.categoryEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(categoryEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Measurement Unit",
                    subtitle = "Enable units of measure (Pcs, Box, Kg, Ltr, Mtr).",
                    checked = settings.unitEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(unitEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Purchase Price",
                    subtitle = "Record procurement cost and compute profit margins.",
                    checked = settings.purchasePriceEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(purchasePriceEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Sale Price",
                    subtitle = "Record default selling price for billing and invoicing.",
                    checked = settings.salePriceEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(salePriceEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "VAT / Tax Support",
                    subtitle = "Include VAT calculation on applicable product lines.",
                    checked = settings.vatEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(vatEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Barcode Support",
                    subtitle = "Link barcodes to products for instant scanning.",
                    checked = settings.barcodeEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(barcodeEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Brand & Manufacturer",
                    subtitle = "Catalog brand names (Configurable / Future-ready).",
                    checked = settings.brandEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(brandEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Product Images",
                    subtitle = "Attach product thumbnail photos (Configurable / Future-ready).",
                    checked = settings.itemImageEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(itemImageEnabled = it) }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Default Sale Price Type Selection (Section 7)
                Text(
                    text = "Default Sale Price Model",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Select pricing policy used when adding item lines to invoices.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DefaultSalePriceType.entries.forEach { priceType ->
                        val isSelected = settings.defaultSalePriceType == priceType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) SkyBlueCardBg else if (isDark) CardDark else Color(0xFFF1F5F9)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SkyBlue else if (isDark) CardBorder else Color(0xFFCBD5E1),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = moduleEnabled) {
                                    viewModel.updateInventorySettings { s -> s.copy(defaultSalePriceType = priceType) }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = priceType.displayName,
                                color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF334155),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Stock Control (Section 8)
        ExpandableSettingsSection(
            title = "Stock Control",
            subtitle = "Manage inventory movement entries, transfers, and ledger visibility.",
            icon = Icons.Outlined.Tune,
            isExpanded = expandedStockControl,
            onToggleExpand = { expandedStockControl = !expandedStockControl },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Opening Stock",
                    subtitle = "Allow specifying opening inventory balance when creating an item.",
                    checked = settings.openingStockEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(openingStockEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Stock Adjustment",
                    subtitle = "Permit manual stock additions, reductions, and audit sets.",
                    checked = settings.stockAdjustmentEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(stockAdjustmentEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Stock In Tracking",
                    subtitle = "Track inbound items from purchases and supplier returns.",
                    checked = settings.stockInEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(stockInEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Stock Out Tracking",
                    subtitle = "Track outbound items from sales and customer dispatches.",
                    checked = settings.stockOutEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(stockOutEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Stock Ledger Movement",
                    subtitle = "Maintain immutable chronological movement logs (Prepared architecture).",
                    checked = settings.stockLedgerEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(stockLedgerEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Stock Transfer Between Godowns",
                    subtitle = "Multi-warehouse stock transfers (Future-ready).",
                    checked = settings.stockTransferEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(stockTransferEnabled = it) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 4: Manual Stock Adjustment (Section 9)
        ExpandableSettingsSection(
            title = "Manual Stock Adjustment",
            subtitle = "Rules for physical inventory counts, damage write-offs, and audits.",
            icon = Icons.Outlined.SwapHoriz,
            isExpanded = expandedAdjustment,
            onToggleExpand = { expandedAdjustment = !expandedAdjustment },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Allow Manual Stock Adjustment",
                    subtitle = "Permit staff to manually adjust on-hand item counts.",
                    checked = settings.allowManualStockAdjustment,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(allowManualStockAdjustment = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Require Adjustment Reason",
                    subtitle = "Mandate selecting reason (Audit, Damaged, Expired, Returned).",
                    checked = settings.requireAdjustmentReason,
                    enabled = moduleEnabled && settings.allowManualStockAdjustment,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(requireAdjustmentReason = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Require Manager Approval",
                    subtitle = "Hold stock adjustments until approved by an admin or manager.",
                    checked = settings.requireApprovalForAdjustment,
                    enabled = moduleEnabled && settings.allowManualStockAdjustment,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(requireApprovalForAdjustment = it) }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Standard reasons chips overview
                Text(
                    text = "Supported Adjustment Reasons",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Physical Audit", "Stock In / Restock", "Damaged / Expired", "Returned Goods", "Transfer / Other").forEach { r ->
                        Box(
                            modifier = Modifier
                                .background(if (isDark) BackgroundDark else Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(r, fontSize = 11.sp, color = if (isDark) TextWhite.copy(alpha = 0.8f) else Color(0xFF475569))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 5: Low Stock & Alerts (Section 10 & 11)
        ExpandableSettingsSection(
            title = "Low Stock & Alerts",
            subtitle = "Re-order warnings, out-of-stock badges, and global threshold defaults.",
            icon = Icons.Outlined.NotificationsActive,
            isExpanded = expandedAlerts,
            onToggleExpand = { expandedAlerts = !expandedAlerts },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Low Stock Alert",
                    subtitle = "Trigger warnings when stock drops to or below threshold level.",
                    checked = settings.lowStockAlertEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(lowStockAlertEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Out of Stock Alert",
                    subtitle = "Highlight items when inventory reaches zero.",
                    checked = settings.outOfStockAlertEnabled,
                    enabled = moduleEnabled && settings.lowStockAlertEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(outOfStockAlertEnabled = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                // Default Minimum Stock Threshold (Section 11)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default Minimum Stock Threshold",
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Default for newly created items: ${settings.defaultLowStockThreshold} ${settings.defaultMeasurementUnit} (Existing items unaffected).",
                            color = SkyBlueBright,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showMinStockDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                            .testTag("btn_edit_min_stock")
                    ) {
                        Text("Edit", color = SkyBlueBright, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                // Default Measurement Unit Chips
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Default Measurement Unit",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Pre-selected unit applied when creating a new inventory item.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Pcs", "Box", "Kg", "Ltr", "Mtr", "Pack", "Dozen").forEach { unit ->
                        val isSelected = settings.defaultMeasurementUnit.equals(unit, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) SkyBlueCardBg else if (isDark) CardDark else Color(0xFFF1F5F9)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SkyBlue else if (isDark) CardBorder else Color(0xFFCBD5E1),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.updateInventorySettings { s -> s.copy(defaultMeasurementUnit = unit) }
                                    Toast.makeText(context, "Default unit set to $unit", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = unit,
                                color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF334155),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 6: Batch & Expiry (Section 12, 13 & 14)
        ExpandableSettingsSection(
            title = "Batch & Expiry",
            subtitle = "Track pharmaceuticals, food expiry dates, and lot numbers.",
            icon = Icons.Outlined.Event,
            isExpanded = expandedBatchExpiry,
            onToggleExpand = { expandedBatchExpiry = !expandedBatchExpiry },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Batch Tracking",
                    subtitle = "Track product lots and unique batch numbers (Recommended for Pharmacy).",
                    checked = settings.batchTrackingEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(batchTrackingEnabled = it) }
                    },
                    testTag = "switch_batch_tracking"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Expiry Tracking",
                    subtitle = "Monitor product expiration dates and generate near-expiry alerts.",
                    checked = settings.expiryTrackingEnabled,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(expiryTrackingEnabled = it) }
                    },
                    testTag = "switch_expiry_tracking"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Require Batch on Purchase",
                    subtitle = "Mandate entering batch number when receiving stock.",
                    checked = settings.requireBatchOnPurchase,
                    enabled = moduleEnabled && settings.batchTrackingEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(requireBatchOnPurchase = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Require Batch on Sale",
                    subtitle = "Prompt salesperson to pick batch number when invoicing.",
                    checked = settings.requireBatchOnSale,
                    enabled = moduleEnabled && settings.batchTrackingEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(requireBatchOnSale = it) }
                    }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Multiple Batches",
                    subtitle = "Permit holding multiple batch lots with different expiry dates for one SKU.",
                    checked = settings.allowMultipleBatches,
                    enabled = moduleEnabled && settings.batchTrackingEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(allowMultipleBatches = it) }
                    }
                )

                // Expiry Alerts (Section 10 & 12) - Visible when Expiry Tracking is ON
                if (settings.expiryTrackingEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "EXPIRY WARNING ALERTS",
                        color = SkyBlueBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSwitchRow(
                        title = "Expiry Alert",
                        subtitle = "Alert cashier when an item has passed its expiration date.",
                        checked = settings.expiryAlertEnabled,
                        enabled = moduleEnabled,
                        onCheckedChange = {
                            viewModel.updateInventorySettings { s -> s.copy(expiryAlertEnabled = it) }
                        }
                    )

                    HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                    SettingsSwitchRow(
                        title = "Near Expiry Alert",
                        subtitle = "Early warning before product reaches expiry deadline.",
                        checked = settings.nearExpiryAlertEnabled,
                        enabled = moduleEnabled,
                        onCheckedChange = {
                            viewModel.updateInventorySettings { s -> s.copy(nearExpiryAlertEnabled = it) }
                        }
                    )

                    HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                    // Near Expiry Warning Days
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Near Expiry Threshold",
                                color = if (isDark) TextWhite else Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Notify within ${settings.nearExpiryWarningDays} days of expiration",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { showNearExpiryDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text("${settings.nearExpiryWarningDays} d", color = SkyBlueBright, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                    // Critical Expiry Warning Days
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Critical Expiry Threshold",
                                color = if (isDark) TextWhite else Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Urgent highlight within ${settings.criticalExpiryWarningDays} days",
                                color = InvoiceRose,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { showCriticalExpiryDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text("${settings.criticalExpiryWarningDays} d", color = SkyBlueBright, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                    // Expired Item Sale Action (Section 14)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "When Item is Expired (Sale Rule)",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ExpiredItemAction.entries.forEach { action ->
                            val isSelected = settings.expiredItemSaleAction == action
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.updateInventorySettings { s -> s.copy(expiredItemSaleAction = action) }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateInventorySettings { s -> s.copy(expiredItemSaleAction = action) }
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        action.displayName,
                                        color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Text(
                                        action.description,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 7: Stock Valuation (Section 15)
        ExpandableSettingsSection(
            title = "Stock Valuation",
            subtitle = "Determine how the asset value of in-hand inventory is calculated.",
            icon = Icons.Outlined.AttachMoney,
            isExpanded = expandedValuation,
            onToggleExpand = { expandedValuation = !expandedValuation },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Stock Valuation Principle",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Controls valuation formulas on balance sheet and inventory summary cards.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                StockValuationMethod.entries.forEach { method ->
                    val isSelected = settings.stockValuationMethod == method
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SkyBlueCardBg else if (isDark) CardDark else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                1.dp,
                                if (isSelected) SkyBlue else if (isDark) CardBorder else Color(0xFFE2E8F0),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (method.isAvailable) {
                                    viewModel.updateInventorySettings { s -> s.copy(stockValuationMethod = method) }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "${method.displayName} architecture is configured and ready for the upcoming Stock Ledger update.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (method.isAvailable) {
                                        viewModel.updateInventorySettings { s -> s.copy(stockValuationMethod = method) }
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = method.displayName,
                                        color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (method.isAvailable) {
                                        Box(
                                            modifier = Modifier
                                                .background(InvoiceEmerald.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Active", color = InvoiceEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Prepared", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = method.formulaDescription,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 8: Sales Integration (Section 5 & 16)
        ExpandableSettingsSection(
            title = "Sales Integration",
            subtitle = "Link sales invoices with real-time stock deduction and negative stock guards.",
            icon = Icons.Outlined.PointOfSale,
            isExpanded = expandedSales,
            onToggleExpand = { expandedSales = !expandedSales },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Decrease Stock on Sale",
                    subtitle = "Automatically deduct sold quantities from inventory when creating invoices.",
                    checked = settings.decreaseStockOnSale,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(decreaseStockOnSale = it) }
                    },
                    testTag = "switch_decrease_stock_sale"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Negative Stock",
                    subtitle = "Permit selling items when physical stock reaches zero (Default: OFF).",
                    checked = settings.allowNegativeStock,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(allowNegativeStock = it) }
                    },
                    testTag = "switch_allow_negative_stock"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    title = "Allow Sale Without Stock",
                    subtitle = "Allow cashiers to add items to invoice even if inventory count is zero.",
                    checked = settings.allowSaleWithoutStock,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(allowSaleWithoutStock = it) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 9: Purchase Integration (Section 17)
        ExpandableSettingsSection(
            title = "Purchase Integration",
            subtitle = "Replenish inventory automatically upon recording purchase vouchers.",
            icon = Icons.Outlined.ShoppingCart,
            isExpanded = expandedPurchase,
            onToggleExpand = { expandedPurchase = !expandedPurchase },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Increase Stock on Purchase",
                    subtitle = "Automatically credit inward stock quantities upon recording purchase entries.",
                    checked = settings.increaseStockOnPurchase,
                    enabled = moduleEnabled,
                    onCheckedChange = {
                        viewModel.updateInventorySettings { s -> s.copy(increaseStockOnPurchase = it) }
                    },
                    testTag = "switch_increase_stock_purchase"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 10: Inventory Permissions (Section 18)
        ExpandableSettingsSection(
            title = "Permissions",
            subtitle = "Role-based inventory actions integrated with staff management.",
            icon = Icons.Outlined.Security,
            isExpanded = expandedPermissions,
            onToggleExpand = { expandedPermissions = !expandedPermissions },
            isDark = isDark
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Inventory Permissions Matrix",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Active staff roles (Admin, Partner, Manager, Stock Manager) inherit these rules.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val p = settings.permissions
                val permItems = listOf(
                    Triple("View Inventory Catalog", "Allow browsing items list and search", p.viewInventory) to { v: Boolean -> p.copy(viewInventory = v) },
                    Triple("Add New Items", "Allow adding new products to catalog", p.addItem) to { v: Boolean -> p.copy(addItem = v) },
                    Triple("Edit Item Details", "Allow editing names, categories, and prices", p.editItem) to { v: Boolean -> p.copy(editItem = v) },
                    Triple("Delete Inventory Items", "Allow archiving or removing items", p.deleteItem) to { v: Boolean -> p.copy(deleteItem = v) },
                    Triple("View Purchase Price", "Reveal procurement cost on product details", p.viewPurchasePrice) to { v: Boolean -> p.copy(viewPurchasePrice = v) },
                    Triple("View Stock Valuation", "Reveal total inventory monetary asset value", p.viewStockValue) to { v: Boolean -> p.copy(viewStockValue = v) },
                    Triple("Adjust Physical Stock", "Permit adjusting inventory quantities", p.adjustStock) to { v: Boolean -> p.copy(adjustStock = v) },
                    Triple("View Stock Ledger", "Access chronological ledger history", p.viewStockLedger) to { v: Boolean -> p.copy(viewStockLedger = v) },
                    Triple("Transfer Stock", "Permit transferring stock between stores", p.transferStock) to { v: Boolean -> p.copy(transferStock = v) }
                )

                permItems.forEachIndexed { index, (item, updater) ->
                    val (title, sub, checked) = item
                    SettingsSwitchRow(
                        title = title,
                        subtitle = sub,
                        checked = checked,
                        enabled = moduleEnabled,
                        onCheckedChange = { v ->
                            viewModel.updateInventorySettings { s -> s.copy(permissions = updater(v)) }
                        }
                    )
                    if (index < permItems.size - 1) {
                        HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }

    // ----------------- DIALOGS -----------------

    // 1. Business Type Preset Confirmation Dialog
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = {
                Text(
                    text = "Apply Business Preset",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose your business type. This sets smart initial defaults for inventory, batch tracking, expiry, and low-stock rules. Your existing items and transactions remain completely safe.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    BusinessInventoryPreset.entries.forEach { preset ->
                        val isSelected = selectedPresetToApply == preset
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SkyBlueCardBg else if (isDark) BackgroundDark else Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    1.dp,
                                    if (isSelected) SkyBlue else if (isDark) CardBorder else Color(0xFFE2E8F0),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPresetToApply = preset }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPresetToApply = preset },
                                    colors = RadioButtonDefaults.colors(selectedColor = SkyBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        preset.displayName,
                                        color = if (isSelected) SkyBlueBright else if (isDark) TextWhite else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        preset.description,
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
                        viewModel.applyBusinessInventoryPreset(selectedPresetToApply)
                        showPresetDialog = false
                        Toast.makeText(
                            context,
                            "Applied preset defaults for ${selectedPresetToApply.displayName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Apply Preset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // 2. Default Minimum Stock Dialog
    if (showMinStockDialog) {
        var inputVal by remember { mutableStateOf(settings.defaultLowStockThreshold.toString()) }
        AlertDialog(
            onDismissRequest = { showMinStockDialog = false },
            title = {
                Text(
                    "Default Minimum Stock",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Default re-order threshold for newly created items. Changing this does NOT modify existing items.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { if (it.all { c -> c.isDigit() }) inputVal = it },
                        label = { Text("Quantity (${settings.defaultMeasurementUnit})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputVal.toIntOrNull() ?: 10
                        viewModel.updateInventorySettings { s -> s.copy(defaultLowStockThreshold = num) }
                        showMinStockDialog = false
                        Toast.makeText(context, "Default minimum stock set to $num units", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showMinStockDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // 3. Near Expiry Warning Dialog
    if (showNearExpiryDialog) {
        var inputVal by remember { mutableStateOf(settings.nearExpiryWarningDays.toString()) }
        AlertDialog(
            onDismissRequest = { showNearExpiryDialog = false },
            title = { Text("Near Expiry Warning", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { if (it.all { c -> c.isDigit() }) inputVal = it },
                    label = { Text("Days before expiration") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputVal.toIntOrNull() ?: 30
                        viewModel.updateInventorySettings { s -> s.copy(nearExpiryWarningDays = num) }
                        showNearExpiryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNearExpiryDialog = false }) { Text("Cancel", color = TextMuted) }
            }
        )
    }

    // 4. Critical Expiry Warning Dialog
    if (showCriticalExpiryDialog) {
        var inputVal by remember { mutableStateOf(settings.criticalExpiryWarningDays.toString()) }
        AlertDialog(
            onDismissRequest = { showCriticalExpiryDialog = false },
            title = { Text("Critical Expiry Warning", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { if (it.all { c -> c.isDigit() }) inputVal = it },
                    label = { Text("Days before expiration (Urgent)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputVal.toIntOrNull() ?: 7
                        viewModel.updateInventorySettings { s -> s.copy(criticalExpiryWarningDays = num) }
                        showCriticalExpiryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCriticalExpiryDialog = false }) { Text("Cancel", color = TextMuted) }
            }
        )
    }
}

// ----------------- SUB-COMPONENTS -----------------

@Composable
private fun InventorySettingsTopBar(onBack: () -> Unit) {
    val isDark = AppTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("inventory_settings_back")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (isDark) TextWhite else Color(0xFF0F172A)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Inventory Settings",
                color = if (isDark) TextWhite else Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage inventory and stock behavior",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Top Inventory Status Summary Card (Section 27)
 */
@Composable
private fun InventoryStatusSummaryCard(
    settings: InventorySettings,
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
                            .size(32.dp)
                            .background(SkyBlueCardBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Assessment,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Inventory Status",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!settings.inventoryModuleEnabled) {
                    Box(
                        modifier = Modifier
                            .background(InvoiceRose.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Module Paused", color = InvoiceRose, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // 6-item Status Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    SummaryStatusRow(label = "Inventory", active = settings.inventoryModuleEnabled)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryStatusRow(label = "Stock Tracking", active = settings.trackStockAutomatically)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryStatusRow(label = "Low Stock Alerts", active = settings.lowStockAlertEnabled)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SummaryStatusRow(label = "Batch Tracking", active = settings.batchTrackingEnabled)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryStatusRow(label = "Expiry Tracking", active = settings.expiryTrackingEnabled)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryStatusRow(label = "Negative Stock", active = settings.allowNegativeStock)
                }
            }
        }
    }
}

@Composable
private fun SummaryStatusRow(label: String, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .background(
                    if (active) InvoiceEmerald.copy(alpha = 0.15f) else Color(0xFF94A3B8).copy(alpha = 0.15f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (active) "ON" else "OFF",
                color = if (active) InvoiceEmerald else Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Business Preset Banner (Section 19 & 20)
 */
@Composable
private fun BusinessPresetBanner(
    currentPreset: BusinessInventoryPreset,
    isDark: Boolean,
    onOpenPresetDialog: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) SkyBlueCardBg.copy(alpha = 0.5f) else Color(0xFFF0FDF4)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                1.dp,
                if (isDark) SkyBlueCardBorder else Color(0xFFBBF7D0),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SkyBlue.copy(alpha = 0.2f), CircleShape),
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
                    Text(
                        text = "Preset: ${currentPreset.displayName}",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentPreset.description,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onOpenPresetDialog,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_change_business_preset")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Preset", fontSize = 11.sp)
            }
        }
    }
}

/**
 * Modern Expandable Settings Section Card
 */
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
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
    ) {
        Column {
            // Expandable Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isDark) BackgroundDark else Color(0xFFF1F5F9),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextMuted
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

/**
 * Standard Switch row matching existing Atri Khata 2 design
 */
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
    val alpha = if (enabled) 1f else 0.45f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isDark) TextWhite else Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        val switchModifier = if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SkyBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
            ),
            modifier = switchModifier
        )
    }
}
