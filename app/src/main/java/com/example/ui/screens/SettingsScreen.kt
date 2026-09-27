package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.*

/**
 * Redesigned Settings Screen & Sheet matching the uploaded interface specification:
 * - App Settings: General preferences, dark/light mode toggle, security PIN, and notifications.
 * - Transaction: Configurations (default payment modes, receipt printing formats, prefixes).
 * - Invoice: Tax settings, terms & conditions, logo branding, header/footer text.
 * - Party: Customer/supplier rules (credit limits, grouping, sorting preferences).
 * - Inventory: Stock management (low-stock thresholds, barcode scanner defaults, unit settings).
 */
enum class SettingsSubSection(val title: String) {
    APP_SETTINGS("App Settings"),
    TRANSACTION("Transaction"),
    INVOICE("Invoice"),
    PARTY("Party"),
    INVENTORY("Inventory")
}

@Composable
fun SettingsSheet(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    SettingsContent(
        viewModel = viewModel,
        onBack = onClose,
        isSheet = true
    )
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    SettingsContent(
        viewModel = viewModel,
        onBack = onBack,
        isSheet = false
    )
}

@Composable
private fun SettingsContent(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    isSheet: Boolean
) {
    var currentSubSection by remember { mutableStateOf<SettingsSubSection?>(null) }

    AnimatedContent(
        targetState = currentSubSection,
        transitionSpec = {
            if (targetState != null && initialState == null) {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width / 2 } + fadeOut()
                )
            } else {
                (slideInHorizontally { width -> -width / 2 } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut()
                )
            }
        },
        label = "SettingsNavAnimation"
    ) { targetSection ->
        when (targetSection) {
            null -> {
                // Main Settings Menu matching Screenshot_20260917_003007
                MainSettingsMenu(
                    onBack = onBack,
                    isSheet = isSheet,
                    onSelectSection = { currentSubSection = it }
                )
            }
            SettingsSubSection.APP_SETTINGS -> {
                com.example.ui.screens.settings.AppSettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubSection = null }
                )
            }
            SettingsSubSection.TRANSACTION -> {
                com.example.ui.screens.settings.TransactionSettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubSection = null }
                )
            }
            SettingsSubSection.INVOICE -> {
                com.example.ui.screens.settings.InvoiceSettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubSection = null }
                )
            }
            SettingsSubSection.PARTY -> {
                com.example.ui.screens.settings.PartySettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubSection = null }
                )
            }
            SettingsSubSection.INVENTORY -> {
                com.example.ui.screens.settings.InventorySettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentSubSection = null }
                )
            }
        }
    }
}

/**
 * Main Settings Menu view:
 * Top bar with Back arrow and "Settings" title.
 * Dark rounded Card container holding the 5 primary options with outline icons and chevron arrows.
 */
@Composable
private fun MainSettingsMenu(
    onBack: () -> Unit,
    isSheet: Boolean,
    onSelectSection: (SettingsSubSection) -> Unit
) {
    val isDark = AppTheme.isDark

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .testTag("settings_screen_root")
    ) {
        // Top App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = if (isSheet) Icons.Outlined.Close else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) TextWhite else Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Settings",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Single Rounded Card with the 5 Options
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) CardDark else Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    1.dp,
                    if (isDark) CardBorder else Color(0xFFE2E8F0),
                    RoundedCornerShape(18.dp)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. App Settings
                SettingsMenuItemRow(
                    icon = Icons.Outlined.Settings,
                    title = "App Settings",
                    onClick = { onSelectSection(SettingsSubSection.APP_SETTINGS) },
                    testTag = "settings_menu_app_settings"
                )

                SettingsItemDivider(isDark)

                // 2. Transaction
                SettingsMenuItemRow(
                    icon = Icons.Outlined.Payments,
                    title = "Transaction",
                    onClick = { onSelectSection(SettingsSubSection.TRANSACTION) },
                    testTag = "settings_menu_transaction"
                )

                SettingsItemDivider(isDark)

                // 3. Invoice
                SettingsMenuItemRow(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "Invoice",
                    onClick = { onSelectSection(SettingsSubSection.INVOICE) },
                    testTag = "settings_menu_invoice"
                )

                SettingsItemDivider(isDark)

                // 4. Party
                SettingsMenuItemRow(
                    icon = Icons.Outlined.Group,
                    title = "Party",
                    onClick = { onSelectSection(SettingsSubSection.PARTY) },
                    testTag = "settings_menu_party"
                )

                SettingsItemDivider(isDark)

                // 5. Inventory
                SettingsMenuItemRow(
                    icon = Icons.Outlined.Inventory2,
                    title = "Inventory",
                    onClick = { onSelectSection(SettingsSubSection.INVENTORY) },
                    testTag = "settings_menu_inventory"
                )
            }
        }
    }
}

@Composable
private fun SettingsMenuItemRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    val isDark = AppTheme.isDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isDark) SkyBlueBright else SkyBlue,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = title,
            color = if (isDark) TextWhite else Color(0xFF0F172A),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open",
            tint = if (isDark) TextMuted else Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsItemDivider(isDark: Boolean) {
    HorizontalDivider(
        color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9),
        thickness = 0.8.dp,
        modifier = Modifier.padding(start = 62.dp, end = 16.dp)
    )
}

// -----------------------------------------------------------------------------
// SUB-SCREEN 1: APP SETTINGS
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppSettingsSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currencyFormat by viewModel.currencyFormat.collectAsStateWithLifecycle()
    val dateFormat by viewModel.dateFormat.collectAsStateWithLifecycle()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle()
    val biometricAuthEnabled by viewModel.biometricAuthEnabled.collectAsStateWithLifecycle()
    val privacyMode by viewModel.privacyMode.collectAsStateWithLifecycle()
    val pushNotificationsEnabled by viewModel.pushNotificationsEnabled.collectAsStateWithLifecycle()
    val paymentReminderAlerts by viewModel.paymentReminderAlerts.collectAsStateWithLifecycle()
    val lowStockAlerts by viewModel.lowStockAlerts.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SubSettingsHeader(title = "App Settings", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: App Theme Mode
        SettingsSectionHeader(title = "THEME & APPEARANCE")
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SkyBlueCardBg)
                                .border(1.dp, SkyBlueCardBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = null,
                                tint = SkyBlueBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Dark Theme",
                                color = if (isDark) TextWhite else Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDarkMode) "Corporate Navy Blue (OLED)" else "Corporate Light Slate",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            viewModel.toggleDarkMode(it)
                            Toast.makeText(
                                context,
                                if (it) "Switched to Corporate Dark Mode" else "Switched to Corporate Light Mode",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SkyBlue,
                            uncheckedThumbColor = SkyBlueBright,
                            uncheckedTrackColor = SurfaceDark
                        ),
                        modifier = Modifier.testTag("app_settings_theme_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Regional & Currency
        SettingsSectionHeader(title = "CURRENCY & REGIONAL")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Currency Symbol",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Rs.", "NPR", "$", "₹", "€", "£").forEach { option ->
                        SelectableFormatChip(
                            label = option,
                            isSelected = currencyFormat == option,
                            onClick = {
                                viewModel.setCurrencyFormat(option)
                                Toast.makeText(context, "Currency set to $option", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "currency_chip_$option"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Date Format",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD").forEach { option ->
                        SelectableFormatChip(
                            label = option,
                            isSelected = dateFormat == option,
                            onClick = {
                                viewModel.setDateFormat(option)
                                Toast.makeText(context, "Date format set to $option", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "date_chip_$option"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Security & PIN
        SettingsSectionHeader(title = "SECURITY & PRIVACY")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Pin,
                    title = "App Security PIN Lock",
                    subtitle = if (appLockEnabled) "4-digit PIN required on app startup" else "App lock currently disabled",
                    checked = appLockEnabled,
                    onCheckedChange = {
                        viewModel.toggleAppLock(it)
                        if (it) showPinDialog = true
                    },
                    testTag = "settings_app_lock_switch"
                )

                if (appLockEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPinDialog = true }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(36.dp))
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = SkyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Security PIN", color = SkyBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Fingerprint,
                    title = "Biometric Authentication",
                    subtitle = "Unlock using fingerprint or device biometrics",
                    checked = biometricAuthEnabled,
                    enabled = appLockEnabled,
                    onCheckedChange = { viewModel.toggleBiometricAuth(it) },
                    testTag = "settings_biometric_switch"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    icon = Icons.Outlined.VisibilityOff,
                    title = "Privacy Mode (Mask Balances)",
                    subtitle = "Hide sensitive figures and amounts in public view",
                    checked = privacyMode,
                    onCheckedChange = { viewModel.togglePrivacyMode() },
                    testTag = "settings_privacy_mode_switch"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Notifications
        SettingsSectionHeader(title = "NOTIFICATIONS")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive timely transactional notifications and system alerts",
                    checked = pushNotificationsEnabled,
                    onCheckedChange = { viewModel.togglePushNotifications(it) },
                    testTag = "settings_notifications_switch"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Payments,
                    title = "Payment Due Reminders",
                    subtitle = "Alerts for upcoming customer payments & supplier dues",
                    checked = paymentReminderAlerts,
                    enabled = pushNotificationsEnabled,
                    onCheckedChange = { viewModel.togglePaymentReminderAlerts(it) },
                    testTag = "settings_reminders_switch"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Inventory2,
                    title = "Low-Stock Inventory Alerts",
                    subtitle = "Warnings when product quantities reach reorder levels",
                    checked = lowStockAlerts,
                    enabled = pushNotificationsEnabled,
                    onCheckedChange = { viewModel.toggleLowStockAlerts(it) },
                    testTag = "settings_low_stock_switch"
                )
            }
        }
    }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure Security PIN", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                Column {
                    Text("Enter a 4-digit security PIN for app startup authentication:", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text("4-digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = CardBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("pin_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            showPinDialog = false
                            Toast.makeText(context, "Security PIN updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid 4-digit PIN", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// SUB-SCREEN 2: TRANSACTION SETTINGS
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionSettingsSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val defaultPaymentMode by viewModel.defaultPaymentMode.collectAsStateWithLifecycle()
    val receiptPaperSize by viewModel.receiptPaperSize.collectAsStateWithLifecycle()
    val salesPrefix by viewModel.salesPrefix.collectAsStateWithLifecycle()
    val autoPrintOnSave by viewModel.autoPrintOnSave.collectAsStateWithLifecycle()
    val autoRoundOff by viewModel.autoRoundOff.collectAsStateWithLifecycle()

    var showPrefixDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SubSettingsHeader(title = "Transaction", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Payment Modes
        SettingsSectionHeader(title = "DEFAULT PAYMENT MODE")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Default Payment Mode for New Transactions",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "Bank Transfer", "Cheque", "Digital Wallet").forEach { mode ->
                        SelectableFormatChip(
                            label = mode,
                            isSelected = defaultPaymentMode == mode,
                            onClick = {
                                viewModel.setDefaultPaymentMode(mode)
                                Toast.makeText(context, "Default payment mode set to $mode", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "paymode_chip_$mode"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Receipt Printing Formats
        SettingsSectionHeader(title = "RECEIPT & PRINTING FORMATS")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Paper Size Configuration",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("58mm Thermal", "80mm Thermal", "A4 Standard").forEach { size ->
                        SelectableFormatChip(
                            label = size,
                            isSelected = receiptPaperSize == size,
                            onClick = {
                                viewModel.setReceiptPaperSize(size)
                                Toast.makeText(context, "Paper format set to $size", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "paper_size_$size"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(4.dp))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Print,
                    title = "Auto-Print on Save",
                    subtitle = "Automatically trigger receipt printing when saving a transaction",
                    checked = autoPrintOnSave,
                    onCheckedChange = { viewModel.toggleAutoPrintOnSave(it) },
                    testTag = "transaction_auto_print_switch"
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Check,
                    title = "Auto-Round Off Amounts",
                    subtitle = "Round transaction totals to nearest whole currency unit",
                    checked = autoRoundOff,
                    onCheckedChange = { viewModel.toggleAutoRoundOff(it) },
                    testTag = "transaction_round_off_switch"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Transaction Prefixes
        SettingsSectionHeader(title = "TRANSACTION PREFIXES & SERIES")
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
                    Column {
                        Text("Sales Invoice Prefix", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Current: $salesPrefix####", color = SkyBlueBright, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showPrefixDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Text("Edit", color = SkyBlueBright, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showPrefixDialog) {
        var prefixText by remember { mutableStateOf(salesPrefix) }
        AlertDialog(
            onDismissRequest = { showPrefixDialog = false },
            title = { Text("Invoice Prefix", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = prefixText,
                    onValueChange = { prefixText = it },
                    label = { Text("Prefix (e.g. INV-, BILL-)") },
                    singleLine = true,
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
                        viewModel.setSalesPrefix(prefixText.trim())
                        showPrefixDialog = false
                        Toast.makeText(context, "Prefix updated to ${prefixText.trim()}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrefixDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// SUB-SCREEN 3: INVOICE SETTINGS
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InvoiceSettingsSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val taxEnabled by viewModel.taxEnabled.collectAsStateWithLifecycle()
    val taxRate by viewModel.taxRate.collectAsStateWithLifecycle()
    val panVatNumber by viewModel.panVatNumber.collectAsStateWithLifecycle()
    val invoiceTerms by viewModel.invoiceTerms.collectAsStateWithLifecycle()
    val invoiceHeader by viewModel.invoiceHeader.collectAsStateWithLifecycle()
    val invoiceFooter by viewModel.invoiceFooter.collectAsStateWithLifecycle()
    val showLogoOnInvoice by viewModel.showLogoOnInvoice.collectAsStateWithLifecycle()
    val showSignatureLine by viewModel.showSignatureLine.collectAsStateWithLifecycle()

    var showPanDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showHeaderFooterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SubSettingsHeader(title = "Invoice", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Tax Settings
        SettingsSectionHeader(title = "TAX & VAT CONFIGURATION")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Percent,
                    title = "Enable VAT / Tax on Invoices",
                    subtitle = "Calculate tax automatically on taxable products",
                    checked = taxEnabled,
                    onCheckedChange = { viewModel.toggleTaxEnabled(it) },
                    testTag = "invoice_tax_switch"
                )

                if (taxEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Default Tax Rate", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.0 to "0% (Exempt)", 5.0 to "5%", 13.0 to "13% (Standard VAT)", 18.0 to "18%").forEach { (rate, label) ->
                            SelectableFormatChip(
                                label = label,
                                isSelected = taxRate == rate,
                                onClick = {
                                    viewModel.setTaxRate(rate)
                                    Toast.makeText(context, "Tax rate set to $label", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Business PAN / VAT Number", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp)
                            Text(panVatNumber, color = SkyBlueBright, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { showPanDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text("Edit PAN", color = SkyBlueBright, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Terms and Conditions
        SettingsSectionHeader(title = "TERMS & CONDITIONS")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Standard Invoice Terms",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) SurfaceDark else Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Text(
                        text = invoiceTerms,
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showTermsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Edit Terms & Conditions", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Logo Branding & Header/Footer
        SettingsSectionHeader(title = "BRANDING, HEADER & FOOTER")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Description,
                    title = "Show Company Logo on Invoice",
                    subtitle = "Display Atri Nova corporate emblem on invoice header",
                    checked = showLogoOnInvoice,
                    onCheckedChange = { viewModel.toggleShowLogoOnInvoice(it) }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Check,
                    title = "Authorized Signature Line",
                    subtitle = "Include signature & date box at bottom of invoice",
                    checked = showSignatureLine,
                    onCheckedChange = { viewModel.toggleShowSignatureLine(it) }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHeaderFooterDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Invoice Header & Footer Text", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Header: \"$invoiceHeader\" • Footer greetings", color = TextSubtle, fontSize = 12.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                }
            }
        }
    }

    if (showPanDialog) {
        var panInput by remember { mutableStateOf(panVatNumber) }
        AlertDialog(
            onDismissRequest = { showPanDialog = false },
            title = { Text("Business PAN / VAT Number", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = panInput,
                    onValueChange = { panInput = it },
                    label = { Text("PAN Number (e.g. 609823415)") },
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
                        viewModel.setPanVatNumber(panInput.trim())
                        showPanDialog = false
                        Toast.makeText(context, "PAN saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showPanDialog = false }) { Text("Cancel", color = TextMuted) } },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    if (showTermsDialog) {
        var termsInput by remember { mutableStateOf(invoiceTerms) }
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Edit Terms and Conditions", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = termsInput,
                    onValueChange = { termsInput = it },
                    label = { Text("Terms & Conditions") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setInvoiceTerms(termsInput)
                        showTermsDialog = false
                        Toast.makeText(context, "Terms updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showTermsDialog = false }) { Text("Cancel", color = TextMuted) } },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    if (showHeaderFooterDialog) {
        var headerInput by remember { mutableStateOf(invoiceHeader) }
        var footerInput by remember { mutableStateOf(invoiceFooter) }
        AlertDialog(
            onDismissRequest = { showHeaderFooterDialog = false },
            title = { Text("Header & Footer Customization", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = headerInput,
                        onValueChange = { headerInput = it },
                        label = { Text("Invoice Header (e.g. TAX INVOICE)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = footerInput,
                        onValueChange = { footerInput = it },
                        label = { Text("Footer Greeting Note") },
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
                        viewModel.setInvoiceHeader(headerInput)
                        viewModel.setInvoiceFooter(footerInput)
                        showHeaderFooterDialog = false
                        Toast.makeText(context, "Header & Footer saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showHeaderFooterDialog = false }) { Text("Cancel", color = TextMuted) } },
            containerColor = if (isDark) CardDark else Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// SUB-SCREEN 4: PARTY SETTINGS
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PartySettingsSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val defaultCreditLimit by viewModel.defaultCreditLimit.collectAsStateWithLifecycle()
    val warnCreditLimitExceeded by viewModel.warnCreditLimitExceeded.collectAsStateWithLifecycle()
    val defaultPartyCategory by viewModel.defaultPartyCategory.collectAsStateWithLifecycle()
    val partySortPreference by viewModel.partySortPreference.collectAsStateWithLifecycle()
    val defaultPaymentTermDays by viewModel.defaultPaymentTermDays.collectAsStateWithLifecycle()

    var showCreditLimitDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SubSettingsHeader(title = "Party", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Credit Limits
        SettingsSectionHeader(title = "CREDIT LIMIT & SAFETY RULES")
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
                    Column {
                        Text("Default Credit Limit for Parties", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Rs. ${String.format(java.util.Locale.US, "%,.0f", defaultCreditLimit)}", color = SkyBlueBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(4.dp))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Security,
                    title = "Warn on Credit Limit Overrun",
                    subtitle = "Alert cashier when outstanding balance crosses limit",
                    checked = warnCreditLimitExceeded,
                    onCheckedChange = { viewModel.toggleWarnCreditLimitExceeded(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Grouping & Categories
        SettingsSectionHeader(title = "PARTY CATEGORY & GROUPING")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Default Category for New Parties", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Retailer", "Wholesaler", "Distributor", "End Customer").forEach { cat ->
                        SelectableFormatChip(
                            label = cat,
                            isSelected = defaultPartyCategory == cat,
                            onClick = {
                                viewModel.setDefaultPartyCategory(cat)
                                Toast.makeText(context, "Default party category set to $cat", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Sorting Preferences & Due Days
        SettingsSectionHeader(title = "SORTING & PAYMENT TERMS")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sort Parties By", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Name (A-Z)", "Highest Balance", "Recent Activity").forEach { sort ->
                        SelectableFormatChip(
                            label = sort,
                            isSelected = partySortPreference == sort,
                            onClick = {
                                viewModel.setPartySortPreference(sort)
                                Toast.makeText(context, "Parties sorted by $sort", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Default Payment Term Duration", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7 to "7 Days", 15 to "15 Days", 30 to "30 Days", 60 to "60 Days").forEach { (days, label) ->
                        SelectableFormatChip(
                            label = label,
                            isSelected = defaultPaymentTermDays == days,
                            onClick = {
                                viewModel.setDefaultPaymentTermDays(days)
                                Toast.makeText(context, "Payment term set to $label", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreditLimitDialog) {
        var limitInput by remember { mutableStateOf(defaultCreditLimit.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showCreditLimitDialog = false },
            title = { Text("Default Party Credit Limit", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) limitInput = it },
                    label = { Text("Credit Limit (Rs.)") },
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
                        val limit = limitInput.toDoubleOrNull() ?: 50000.0
                        viewModel.setDefaultCreditLimit(limit)
                        showCreditLimitDialog = false
                        Toast.makeText(context, "Credit limit set to Rs. $limit", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showCreditLimitDialog = false }) { Text("Cancel", color = TextMuted) } },
            containerColor = if (isDark) CardDark else Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// SUB-SCREEN 5: INVENTORY SETTINGS
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InventorySettingsSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val defaultLowStockThreshold by viewModel.defaultLowStockThreshold.collectAsStateWithLifecycle()
    val barcodeBeepSound by viewModel.barcodeBeepSound.collectAsStateWithLifecycle()
    val defaultMeasurementUnit by viewModel.defaultMeasurementUnit.collectAsStateWithLifecycle()
    val allowNegativeStock by viewModel.allowNegativeStock.collectAsStateWithLifecycle()
    val continuousScanMode by viewModel.continuousScanMode.collectAsStateWithLifecycle()

    var showThresholdDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        SubSettingsHeader(title = "Inventory", onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Stock Warnings
        SettingsSectionHeader(title = "LOW-STOCK THRESHOLD & ALERTS")
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
                    Column {
                        Text("Default Low-Stock Warning Threshold", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Trigger alert when stock drops below $defaultLowStockThreshold units", color = SkyBlueBright, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showThresholdDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, SkyBlueCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Text("Edit", color = SkyBlueBright, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(4.dp))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Security,
                    title = "Allow Negative Stock",
                    subtitle = "Permit selling items when physical stock reaches zero",
                    checked = allowNegativeStock,
                    onCheckedChange = { viewModel.toggleAllowNegativeStock(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Barcode Scanner
        SettingsSectionHeader(title = "BARCODE SCANNER CONFIGURATION")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.QrCode,
                    title = "Audio Feedback on Scan",
                    subtitle = "Play confirmation beep upon successful barcode recognition",
                    checked = barcodeBeepSound,
                    onCheckedChange = { viewModel.toggleBarcodeBeepSound(it) }
                )

                HorizontalDivider(color = if (isDark) CardBorder.copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                SettingsSwitchRow(
                    icon = Icons.Outlined.Inventory2,
                    title = "Continuous Batch Scan Mode",
                    subtitle = "Keep camera active to scan multiple items in sequence",
                    checked = continuousScanMode,
                    onCheckedChange = { viewModel.toggleContinuousScanMode(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Measurement Units
        SettingsSectionHeader(title = "DEFAULT MEASUREMENT UNIT")
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDark else Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Default Item Unit", color = TextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Pcs", "Kg", "Box", "Litre", "Meter", "Dozen", "Pack").forEach { unit ->
                        SelectableFormatChip(
                            label = unit,
                            isSelected = defaultMeasurementUnit == unit,
                            onClick = {
                                viewModel.setDefaultMeasurementUnit(unit)
                                Toast.makeText(context, "Default unit set to $unit", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showThresholdDialog) {
        var thresholdInput by remember { mutableStateOf(defaultLowStockThreshold.toString()) }
        AlertDialog(
            onDismissRequest = { showThresholdDialog = false },
            title = { Text("Low-Stock Threshold", color = if (isDark) TextWhite else Color(0xFF0F172A)) },
            text = {
                OutlinedTextField(
                    value = thresholdInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) thresholdInput = it },
                    label = { Text("Threshold Quantity (units)") },
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
                        val num = thresholdInput.toIntOrNull() ?: 5
                        viewModel.setDefaultLowStockThreshold(num)
                        showThresholdDialog = false
                        Toast.makeText(context, "Low-stock threshold set to $num units", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showThresholdDialog = false }) { Text("Cancel", color = TextMuted) } },
            containerColor = if (isDark) CardDark else Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// REUSABLE SUB-COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun SubSettingsHeader(
    title: String,
    onBack: () -> Unit
) {
    val isDark = AppTheme.isDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) TextWhite else Color(0xFF0F172A),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = if (isDark) TextWhite else Color(0xFF0F172A),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = TextSubtle,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    val isDark = AppTheme.isDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) (if (isDark) SkyBlueBright else SkyBlue) else TextSubtle,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) (if (isDark) TextWhite else Color(0xFF0F172A)) else TextSubtle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SkyBlue,
                uncheckedThumbColor = if (isDark) TextGrayLight else Color(0xFF94A3B8),
                uncheckedTrackColor = if (isDark) SurfaceDark else Color(0xFFCBD5E1),
                disabledCheckedTrackColor = SkyBlue.copy(alpha = 0.4f),
                disabledUncheckedTrackColor = if (isDark) SurfaceDark.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
            ),
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
private fun SelectableFormatChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String? = null
) {
    val isDark = AppTheme.isDark

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) SkyBlueCardBg else (if (isDark) SurfaceDark else Color(0xFFF1F5F9))
            )
            .border(
                1.5.dp,
                if (isSelected) SkyBlue else (if (isDark) CardBorderLight else Color(0xFFCBD5E1)),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SkyBlueBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = if (isSelected) (if (isDark) SkyBlueLight else SkyBlue) else (if (isDark) TextGrayLight else Color(0xFF334155)),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
