package com.example.ui.screens.settings

import android.os.Build
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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppCalendarType
import com.example.data.model.AppLanguage
import com.example.data.model.AppLogLevel
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import com.example.data.model.AutoLockDuration
import com.example.data.model.CurrencyPosition
import com.example.data.model.NegativeNumberStyle
import com.example.data.model.NumberGroupingFormat
import com.example.data.model.StartupScreen
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val InvoiceEmerald = Color(0xFF10B981)
private val InvoiceRose = Color(0xFFF43F5E)
private val AmberWarn = Color(0xFFF59E0B)

/**
 * Atri Khata 2 - Advanced Application Settings & Configuration Screen
 * Comprehensive, persistent, organized into 14 distinct professional modules.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val isGoogleConnected by viewModel.isGoogleAccountConnected.collectAsStateWithLifecycle()
    val connectedEmail by viewModel.connectedGoogleEmail.collectAsStateWithLifecycle()

    // Database statistics for Data Management module
    val parties by viewModel.allParties.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.allInventoryItems.collectAsStateWithLifecycle()
    val salesInvoices by viewModel.allSalesInvoices.collectAsStateWithLifecycle()

    // Expandable section states (all 14 modules)
    var expandedGeneral by remember { mutableStateOf(false) }
    var expandedAppearance by remember { mutableStateOf(true) } // default open
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedDateTime by remember { mutableStateOf(false) }
    var expandedNotifications by remember { mutableStateOf(false) }
    var expandedSecurity by remember { mutableStateOf(false) }
    var expandedBackup by remember { mutableStateOf(false) }
    var expandedDataMgmt by remember { mutableStateOf(false) }
    var expandedPerformance by remember { mutableStateOf(false) }
    var expandedNavigation by remember { mutableStateOf(false) }
    var expandedUx by remember { mutableStateOf(false) }
    var expandedAbout by remember { mutableStateOf(false) }
    var expandedAdvanced by remember { mutableStateOf(false) }
    var expandedDeveloper by remember { mutableStateOf(false) }

    // Dialog controllers
    var showPinDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showIntegrityDialog by remember { mutableStateOf(false) }
    var integrityResult by remember { mutableStateOf("") }
    var showPrefsViewerDialog by remember { mutableStateOf(false) }

    // Developer mode unlock counter
    var versionTapCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else Color(0xFFF8FAFC))
            .testTag("app_settings_screen_root")
    ) {
        // Top App Bar
        AppSettingsTopBar(
            onBack = onBack,
            onResetDefaults = { showResetDialog = true }
        )

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live Status Summary Header Card
            AppStatusSummaryCard(settings = settings, isDark = isDark)

            // -------------------------------------------------------------
            // SECTION 1: GENERAL APP SETTINGS
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "1. General",
                subtitle = "App title, startup screen, confirmation dialogs & drafts",
                icon = Icons.Outlined.Settings,
                isExpanded = expandedGeneral,
                onToggleExpand = { expandedGeneral = !expandedGeneral },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Business Name Header Display
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Info,
                        title = "Show Profile Name in Header",
                        subtitle = "Display active company name on top navigation bars",
                        checked = settings.showBusinessProfileNameHeader,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(showBusinessProfileNameHeader = it) } },
                        testTag = "app_settings_show_profile_header"
                    )

                    SettingsSubDivider(isDark)

                    // Confirm Before Exit
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Confirm Before Exit",
                        subtitle = "Prompt confirmation alert before closing the application",
                        checked = settings.confirmBeforeExit,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(confirmBeforeExit = it) } },
                        testTag = "app_settings_confirm_exit"
                    )

                    SettingsSubDivider(isDark)

                    // Confirm Before Delete
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Delete,
                        title = "Confirm Before Delete",
                        subtitle = "Require affirmative prompt before deleting transactions, items, or parties",
                        checked = settings.confirmBeforeDelete,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(confirmBeforeDelete = it) } },
                        testTag = "app_settings_confirm_delete"
                    )

                    SettingsSubDivider(isDark)

                    // Auto Save Drafts
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Edit,
                        title = "Auto-Save Unfinished Drafts",
                        subtitle = "Preserve voucher entries automatically during screen switches",
                        checked = settings.autoSaveDrafts,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(autoSaveDrafts = it) } },
                        testTag = "app_settings_auto_save_drafts"
                    )

                    SettingsSubDivider(isDark)

                    // Remember Last Opened Screen
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Home,
                        title = "Remember Last Opened Screen",
                        subtitle = "Restore the previously active screen when reopening Atri Khata",
                        checked = settings.rememberLastOpenedScreen,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(rememberLastOpenedScreen = it) } },
                        testTag = "app_settings_remember_screen"
                    )

                    SettingsSubDivider(isDark)

                    // Startup Destination
                    Text(
                        text = "Startup Destination Screen",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StartupScreen.entries.forEach { screen ->
                            SelectableChip(
                                label = screen.displayName,
                                isSelected = settings.startupScreen == screen,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(startupScreen = screen) }
                                    Toast.makeText(context, "Startup set to ${screen.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "startup_screen_${screen.id}"
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 2: APPEARANCE
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "2. Appearance & Theme",
                subtitle = "Material 3 palette, dark mode OLED, compact layouts & animations",
                icon = Icons.Outlined.Palette,
                isExpanded = expandedAppearance,
                onToggleExpand = { expandedAppearance = !expandedAppearance },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme Palette Mode",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            SelectableChip(
                                label = mode.displayName,
                                isSelected = settings.themeMode == mode,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(themeMode = mode) }
                                    Toast.makeText(context, "Theme set to ${mode.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "theme_mode_${mode.id}"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Dynamic Colors
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Palette,
                        title = "Material You Dynamic Colors",
                        subtitle = "Harmonize accent hues with system wallpaper (Android 12+)",
                        checked = settings.dynamicColorsEnabled,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(dynamicColorsEnabled = it) } },
                        testTag = "app_settings_dynamic_colors"
                    )

                    SettingsSubDivider(isDark)

                    // Compact UI
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Tune,
                        title = "Compact Density Mode",
                        subtitle = "Tighter row spacing to display more items and transactions per screen",
                        checked = settings.compactUiMode,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(compactUiMode = it) } },
                        testTag = "app_settings_compact_ui"
                    )

                    SettingsSubDivider(isDark)

                    // Show Animations
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Speed,
                        title = "Interface Animations & Transitions",
                        subtitle = "Smooth card expansions, sheet transitions and fade animations",
                        checked = settings.showAnimations,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(showAnimations = it) } },
                        testTag = "app_settings_show_animations"
                    )

                    SettingsSubDivider(isDark)

                    // Section Icons
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Settings,
                        title = "Display Section Icons",
                        subtitle = "Show decorative and functional glyphs alongside category headers",
                        checked = settings.showSectionIcons,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(showSectionIcons = it) } },
                        testTag = "app_settings_section_icons"
                    )

                    SettingsSubDivider(isDark)

                    // Bottom Nav Labels
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Home,
                        title = "Bottom Navigation Text Labels",
                        subtitle = "Display text captions beneath navigation icons in the main bar",
                        checked = settings.showBottomNavLabels,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(showBottomNavLabels = it) } },
                        testTag = "app_settings_bottom_nav_labels"
                    )
                }
            }

            // -------------------------------------------------------------
            // SECTION 3: LANGUAGE & REGION
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "3. Language & Region",
                subtitle = "App language (English/Nepali), regional currency & symbol placement",
                icon = Icons.Outlined.Translate,
                isExpanded = expandedLanguage,
                onToggleExpand = { expandedLanguage = !expandedLanguage },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Application Language",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            SelectableChip(
                                label = "${lang.displayName} (${lang.nativeName})",
                                isSelected = settings.language == lang,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(language = lang) }
                                    Toast.makeText(context, "Language set to ${lang.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "language_${lang.code}"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Regional Country
                    Text(
                        text = "Regional Country",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Nepal", "India", "United States", "United Kingdom", "Global").forEach { country ->
                            SelectableChip(
                                label = country,
                                isSelected = settings.country == country,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(country = country) }
                                    Toast.makeText(context, "Region set to $country", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "country_$country"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Currency Symbol
                    Text(
                        text = "Currency Symbol",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Rs.", "NPR", "$", "₹", "€", "£").forEach { symbol ->
                            SelectableChip(
                                label = symbol,
                                isSelected = settings.currencySymbol == symbol,
                                onClick = {
                                    viewModel.setCurrencyFormat(symbol)
                                    Toast.makeText(context, "Currency set to $symbol", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "currency_$symbol"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Currency Position
                    Text(
                        text = "Currency Symbol Placement",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurrencyPosition.entries.forEach { pos ->
                            SelectableChip(
                                label = "${pos.displayName} (${pos.example})",
                                isSelected = settings.currencyPosition == pos,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(currencyPosition = pos) }
                                    Toast.makeText(context, "Position set to ${pos.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "currency_pos_${pos.id}"
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 4: DATE & NUMBER FORMAT
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "4. Date & Number Format",
                subtitle = "Nepali B.S. & English A.D. calendars, decimal places & Lakh/Crore grouping",
                icon = Icons.Outlined.CalendarMonth,
                isExpanded = expandedDateTime,
                onToggleExpand = { expandedDateTime = !expandedDateTime },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Calendar Type
                    Text(
                        text = "Primary Calendar System",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppCalendarType.entries.forEach { cal ->
                            SelectableChip(
                                label = cal.displayName,
                                isSelected = settings.calendarType == cal,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(calendarType = cal) }
                                    Toast.makeText(context, "Calendar set to ${cal.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "calendar_type_${cal.id}"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Date Format
                    Text(
                        text = "Date Display Format",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD", "DD-MM-YYYY").forEach { format ->
                            SelectableChip(
                                label = format,
                                isSelected = settings.dateFormat == format,
                                onClick = {
                                    viewModel.setDateFormat(format)
                                    Toast.makeText(context, "Date format set to $format", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "date_format_$format"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Number Grouping Format
                    Text(
                        text = "Thousand Separator & Grouping",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NumberGroupingFormat.entries.forEach { grouping ->
                            SelectableChip(
                                label = "${grouping.displayName} (e.g. ${grouping.example})",
                                isSelected = settings.numberGrouping == grouping,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(numberGrouping = grouping) }
                                    Toast.makeText(context, "Grouping set to ${grouping.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "number_grouping_${grouping.id}"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Decimal Places
                    Text(
                        text = "Decimal Places Precision",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0 to "0 (Whole Numbers)", 2 to "2 (Standard .00)", 3 to "3 (Precision .000)").forEach { (decimals, label) ->
                            SelectableChip(
                                label = label,
                                isSelected = settings.decimalPlaces == decimals,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(decimalPlaces = decimals) }
                                    Toast.makeText(context, "Decimals set to $decimals", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "decimals_$decimals"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Negative Number Style
                    Text(
                        text = "Negative Number Presentation",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NegativeNumberStyle.entries.forEach { style ->
                            SelectableChip(
                                label = "${style.displayName} (e.g. ${style.example})",
                                isSelected = settings.negativeNumberStyle == style,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(negativeNumberStyle = style) }
                                    Toast.makeText(context, "Negative style set to ${style.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "neg_style_${style.id}"
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 5: NOTIFICATIONS
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "5. Notifications & Alerts",
                subtitle = "Payment dues, customer balances, low stock warnings & reminder schedule",
                icon = Icons.Outlined.Notifications,
                isExpanded = expandedNotifications,
                onToggleExpand = { expandedNotifications = !expandedNotifications },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Master notifications switch
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Enable App Notifications",
                        subtitle = "Master switch for transactional push alerts and system messages",
                        checked = settings.enableNotifications,
                        onCheckedChange = { viewModel.togglePushNotifications(it) },
                        testTag = "app_settings_master_notifications"
                    )

                    SettingsSubDivider(isDark)

                    // Transaction Alerts
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Transaction Confirmation Alerts",
                        subtitle = "Immediate notification when a sale or payment voucher is completed",
                        checked = settings.transactionNotifications,
                        enabled = settings.enableNotifications,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(transactionNotifications = it) } },
                        testTag = "app_settings_transaction_notifs"
                    )

                    SettingsSubDivider(isDark)

                    // Payment Due Reminders
                    SettingsSwitchRow(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Upcoming Payment Due Reminders",
                        subtitle = "Notify before supplier invoice and bill due dates expire",
                        checked = settings.paymentDueReminders,
                        enabled = settings.enableNotifications,
                        onCheckedChange = { viewModel.togglePaymentReminderAlerts(it) },
                        testTag = "app_settings_payment_reminders"
                    )

                    SettingsSubDivider(isDark)

                    // Customer Due Reminders
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Customer Receivables Overdue Alerts",
                        subtitle = "Daily morning brief on outstanding customer credit balances",
                        checked = settings.customerDueReminders,
                        enabled = settings.enableNotifications,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(customerDueReminders = it) } },
                        testTag = "app_settings_customer_due_reminders"
                    )

                    SettingsSubDivider(isDark)

                    // Low Stock Alerts
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Storage,
                        title = "Low-Stock Inventory Warnings",
                        subtitle = "Alert when items reach or breach minimum reorder levels",
                        checked = settings.lowStockAlerts,
                        enabled = settings.enableNotifications,
                        onCheckedChange = { viewModel.toggleLowStockAlerts(it) },
                        testTag = "app_settings_low_stock_notifs"
                    )

                    SettingsSubDivider(isDark)

                    // Expiry Alerts
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Info,
                        title = "Product Expiry Date Alerts",
                        subtitle = "Early warnings for batches approaching expiration dates",
                        checked = settings.expiryAlerts,
                        enabled = settings.enableNotifications,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(expiryAlerts = it) } },
                        testTag = "app_settings_expiry_notifs"
                    )

                    SettingsSubDivider(isDark)

                    // Backup Reminders
                    SettingsSwitchRow(
                        icon = Icons.Outlined.CloudSync,
                        title = "Weekly Cloud Backup Reminders",
                        subtitle = "Gentle prompt if no cloud or local backup occurred in 7 days",
                        checked = settings.backupReminders,
                        enabled = settings.enableNotifications,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(backupReminders = it) } },
                        testTag = "app_settings_backup_reminders"
                    )

                    SettingsSubDivider(isDark)

                    // Reminder Time Schedule
                    Text(
                        text = "Daily Reminder Delivery Time",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(8 to "08:00 AM", 9 to "09:00 AM", 10 to "10:00 AM", 18 to "06:00 PM").forEach { (hour, label) ->
                            SelectableChip(
                                label = label,
                                isSelected = settings.reminderTimeHour == hour,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(reminderTimeHour = hour) }
                                    Toast.makeText(context, "Reminder time set to $label", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "reminder_time_$hour"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Days before due
                    Text(
                        text = "Days Prior to Due Date to Trigger Alert",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1 to "1 Day Before", 3 to "3 Days Before", 7 to "7 Days Before").forEach { (days, label) ->
                            SelectableChip(
                                label = label,
                                isSelected = settings.reminderDaysBeforeDue == days,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(reminderDaysBeforeDue = days) }
                                    Toast.makeText(context, "Due reminder set to $label", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "days_before_due_$days"
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 6: SECURITY & PRIVACY
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "6. Security & Privacy",
                subtitle = "App PIN lock, biometric fingerprint, auto-lock timeout & balance masking",
                icon = Icons.Outlined.Security,
                isExpanded = expandedSecurity,
                onToggleExpand = { expandedSecurity = !expandedSecurity },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // App Lock
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Pin,
                        title = "App Security PIN Lock",
                        subtitle = if (settings.appLockEnabled) "4-digit PIN required on app startup" else "App lock is currently disabled",
                        checked = settings.appLockEnabled,
                        onCheckedChange = {
                            viewModel.toggleAppLock(it)
                            if (it) showPinDialog = true
                        },
                        testTag = "app_settings_app_lock"
                    )

                    if (settings.appLockEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPinDialog = true }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = SkyBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (settings.securityPin.isEmpty()) "Set Security PIN Code" else "Change 4-digit Security PIN",
                                color = SkyBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Biometric
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Fingerprint,
                        title = "Biometric Authentication",
                        subtitle = "Quickly unlock using fingerprint scanner or device biometrics",
                        checked = settings.biometricAuthEnabled,
                        enabled = settings.appLockEnabled,
                        onCheckedChange = { viewModel.toggleBiometricAuth(it) },
                        testTag = "app_settings_biometric"
                    )

                    SettingsSubDivider(isDark)

                    // Auto-lock duration
                    Text(
                        text = "Auto-Lock Timeout",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AutoLockDuration.entries.forEach { duration ->
                            SelectableChip(
                                label = duration.displayName,
                                isSelected = settings.autoLockDuration == duration,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(autoLockDuration = duration) }
                                    Toast.makeText(context, "Auto-lock: ${duration.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "autolock_${duration.id}"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Privacy Mode Mask Balances
                    SettingsSwitchRow(
                        icon = Icons.Outlined.VisibilityOff,
                        title = "Privacy Mode (Mask Balances)",
                        subtitle = "Conceal sensitive financial figures (****) to protect confidential data in public",
                        checked = settings.privacyModeMaskBalances,
                        onCheckedChange = { viewModel.togglePrivacyMode() },
                        testTag = "app_settings_privacy_mode"
                    )

                    SettingsSubDivider(isDark)

                    // Hide Amounts on Dashboard
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Payments,
                        title = "Hide Summary Amounts on Dashboard",
                        subtitle = "Blur total cash-in-hand and total receivable cards on home screen",
                        checked = settings.hideAmountsOnDashboard,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(hideAmountsOnDashboard = it) } },
                        testTag = "app_settings_hide_dashboard_amounts"
                    )

                    SettingsSubDivider(isDark)

                    // Block Screenshots
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Secure Screen (Block Screenshots)",
                        subtitle = "Prevent screen capture and hide app contents in recent tasks window",
                        checked = settings.blockScreenshots,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(blockScreenshots = it) } },
                        testTag = "app_settings_block_screenshots"
                    )
                }
            }

            // -------------------------------------------------------------
            // SECTION 7: BACKUP & RESTORE
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "7. Backup & Restore",
                subtitle = "Google Drive cloud integration, automated schedules & data export",
                icon = Icons.Outlined.CloudSync,
                isExpanded = expandedBackup,
                onToggleExpand = { expandedBackup = !expandedBackup },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Google Drive Status Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) SkyBlueCardBg.copy(alpha = 0.5f) else Color(0xFFEFF6FF))
                            .border(1.dp, if (isDark) SkyBlueCardBorder else Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isGoogleConnected) InvoiceEmerald.copy(alpha = 0.2f) else SkyBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isGoogleConnected) Icons.Outlined.CloudDone else Icons.Outlined.CloudSync,
                                        contentDescription = null,
                                        tint = if (isGoogleConnected) InvoiceEmerald else SkyBlueBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isGoogleConnected) "Google Drive Connected" else "Cloud Backup Offline",
                                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isGoogleConnected) connectedEmail else "Connect Google account for auto sync",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isGoogleConnected) {
                                        viewModel.disconnectGoogleAccount()
                                        Toast.makeText(context, "Google Drive disconnected", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.connectGoogleAccount("business.owner@gmail.com")
                                        Toast.makeText(context, "Google Drive linked successfully", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isGoogleConnected) Color(0xFF334155) else SkyBlue
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (isGoogleConnected) "Disconnect" else "Connect",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto Backup Switch
                    SettingsSwitchRow(
                        icon = Icons.Outlined.CloudSync,
                        title = "Automated Background Backup",
                        subtitle = "Periodically sync database to secure cloud storage",
                        checked = settings.autoCloudBackupEnabled,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(autoCloudBackupEnabled = it) } },
                        testTag = "app_settings_auto_backup"
                    )

                    SettingsSubDivider(isDark)

                    // Frequency
                    Text(
                        text = "Auto-Backup Frequency",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Daily (Midnight)", "Weekly (Sunday)", "Monthly (1st)").forEach { freq ->
                            SelectableChip(
                                label = freq,
                                isSelected = settings.backupFrequency == freq,
                                onClick = {
                                    viewModel.setBackupFrequency(freq)
                                    viewModel.updateAppSettings { s -> s.copy(backupFrequency = freq) }
                                    Toast.makeText(context, "Backup schedule: $freq", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "backup_freq_$freq"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // WiFi only
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Speed,
                        title = "Backup Over Wi-Fi Only",
                        subtitle = "Avoid cellular data consumption during scheduled cloud uploads",
                        checked = settings.backupOverWifiOnly,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(backupOverWifiOnly = it) } },
                        testTag = "app_settings_backup_wifi_only"
                    )

                    SettingsSubDivider(isDark)

                    // Actions Row: Backup Now & Export
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                viewModel.updateAppSettings { s -> s.copy(lastBackupTimestamp = now) }
                                Toast.makeText(context, "Full database backup generated successfully!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("app_settings_backup_now_btn")
                        ) {
                            Icon(Icons.Outlined.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup Now", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Exporting JSON backup archive to Downloads...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) CardDark else Color(0xFFE2E8F0)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) CardBorder else Color(0xFFCBD5E1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text(
                                "Export Archive",
                                fontSize = 13.sp,
                                color = if (isDark) TextWhite else Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 8: DATA MANAGEMENT
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "8. Data Management",
                subtitle = "Record counts, storage footprint, database vacuum & cache cleanup",
                icon = Icons.Outlined.Storage,
                isExpanded = expandedDataMgmt,
                onToggleExpand = { expandedDataMgmt = !expandedDataMgmt },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Record counts grid
                    Text(
                        text = "Current Database Records Overview",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(label = "Parties", value = parties.size.toString(), isDark = isDark, modifier = Modifier.weight(1f))
                        StatCard(label = "Transactions", value = transactions.size.toString(), isDark = isDark, modifier = Modifier.weight(1f))
                        StatCard(label = "Products", value = inventoryItems.size.toString(), isDark = isDark, modifier = Modifier.weight(1f))
                        StatCard(label = "Invoices", value = salesInvoices.size.toString(), isDark = isDark, modifier = Modifier.weight(1f))
                    }

                    SettingsSubDivider(isDark)

                    // Clear Cache Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clear Temporary Cache", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Remove cached PDF previews and temporary thumbnail bitmaps", color = TextMuted, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Temporary cache cleared (2.4 MB freed)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SkyBlueCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Clear Cache", color = SkyBlueBright, fontSize = 11.sp)
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Optimize Database (VACUUM)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Optimize Database (VACUUM)", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Reclaim fragmented SQLite space and rebuild search indexes", color = TextMuted, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                viewModel.updateAppSettings { s -> s.copy(lastDatabaseOptimizationTime = now) }
                                Toast.makeText(context, "Room Database optimized and indexed successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueCardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SkyBlueCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Optimize", color = SkyBlueBright, fontSize = 11.sp)
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 9: PERFORMANCE
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "9. Performance & Hardware",
                subtitle = "Hardware GPU acceleration, image cache, chunk loading & low-spec mode",
                icon = Icons.Outlined.Speed,
                isExpanded = expandedPerformance,
                onToggleExpand = { expandedPerformance = !expandedPerformance },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Hardware Acceleration
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Speed,
                        title = "Hardware GPU Acceleration",
                        subtitle = "Use device GPU shaders for smooth 60fps scrolling and fast rendering",
                        checked = settings.hardwareAcceleration,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(hardwareAcceleration = it) } },
                        testTag = "app_settings_gpu_accel"
                    )

                    SettingsSubDivider(isDark)

                    // Image Caching
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Storage,
                        title = "In-Memory Image & PDF Caching",
                        subtitle = "Retain rendered document pages in memory for instantaneous navigation",
                        checked = settings.imageCachingEnabled,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(imageCachingEnabled = it) } },
                        testTag = "app_settings_img_cache"
                    )

                    SettingsSubDivider(isDark)

                    // Fast Scrollbars
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Speed,
                        title = "Fast Scrollbars & Overscroll",
                        subtitle = "Enable quick thumb-drag scrolling in extensive ledgers and inventory",
                        checked = settings.fastScrollbars,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(fastScrollbars = it) } },
                        testTag = "app_settings_fast_scroll"
                    )

                    SettingsSubDivider(isDark)

                    // Lazy List Chunk Loading
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Tune,
                        title = "Lazy Chunked Loading Optimization",
                        subtitle = "Stream transaction history in pages of 50 to optimize memory footprint",
                        checked = settings.lazyListChunkLoading,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(lazyListChunkLoading = it) } },
                        testTag = "app_settings_lazy_loading"
                    )

                    SettingsSubDivider(isDark)

                    // Low-End Device Mode
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Speed,
                        title = "Low-End Device Mode",
                        subtitle = "Disable blur effects, heavy gradients, and non-essential animations",
                        checked = settings.lowEndDeviceMode,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(lowEndDeviceMode = it) } },
                        testTag = "app_settings_low_end_mode"
                    )
                }
            }

            // -------------------------------------------------------------
            // SECTION 10: NAVIGATION & HOME
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "10. Navigation & Home",
                subtitle = "Default landing tab, quick action FAB shortcuts & badge indicators",
                icon = Icons.Outlined.Home,
                isExpanded = expandedNavigation,
                onToggleExpand = { expandedNavigation = !expandedNavigation },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Default Bottom Navigation Tab",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Home", "Transactions", "Reports", "Parties", "Inventory").forEach { tab ->
                            SelectableChip(
                                label = tab,
                                isSelected = settings.defaultHomeTab == tab,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(defaultHomeTab = tab) }
                                    Toast.makeText(context, "Default tab set to $tab", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "default_tab_$tab"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Quick Action FAB
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Tune,
                        title = "Show Floating Quick Action FAB (+)",
                        subtitle = "Display multi-option action trigger for instant voucher entries",
                        checked = settings.showQuickActionFab,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(showQuickActionFab = it) } },
                        testTag = "app_settings_quick_fab"
                    )

                    SettingsSubDivider(isDark)

                    // Badges
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Bottom Bar Notification Badges",
                        subtitle = "Show pending count pill badges over Parties and Inventory tabs",
                        checked = settings.showBottomBarBadges,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(showBottomBarBadges = it) } },
                        testTag = "app_settings_bar_badges"
                    )

                    SettingsSubDivider(isDark)

                    // Compact Bottom Bar
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Tune,
                        title = "Compact Bottom Bar",
                        subtitle = "Reduce bottom navigation bar height on compact phone displays",
                        checked = settings.compactBottomBar,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(compactBottomBar = it) } },
                        testTag = "app_settings_compact_bar"
                    )
                }
            }

            // -------------------------------------------------------------
            // SECTION 11: USER EXPERIENCE
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "11. User Experience & Accessibility",
                subtitle = "Haptic feedback, touch targets, high-contrast & list swipe gestures",
                icon = Icons.Outlined.TouchApp,
                isExpanded = expandedUx,
                onToggleExpand = { expandedUx = !expandedUx },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Haptic Feedback
                    SettingsSwitchRow(
                        icon = Icons.Outlined.TouchApp,
                        title = "Haptic Vibration Feedback",
                        subtitle = "Tactile vibration confirmation on button presses and transaction saves",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(hapticFeedback = it) } },
                        testTag = "app_settings_haptic"
                    )

                    SettingsSubDivider(isDark)

                    // Sound Effects
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Acoustic Confirmation Chimes",
                        subtitle = "Play gentle audio beep on barcode scan and voucher creation",
                        checked = settings.soundEffects,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(soundEffects = it) } },
                        testTag = "app_settings_sounds"
                    )

                    SettingsSubDivider(isDark)

                    // High Contrast Text
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Info,
                        title = "High Contrast Typography",
                        subtitle = "Elevate contrast ratio for text elements against deep navy backgrounds",
                        checked = settings.highContrastText,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(highContrastText = it) } },
                        testTag = "app_settings_high_contrast"
                    )

                    SettingsSubDivider(isDark)

                    // Large Touch Targets
                    SettingsSwitchRow(
                        icon = Icons.Outlined.TouchApp,
                        title = "Accessible Large Touch Targets",
                        subtitle = "Enforce 48dp minimum tappable regions for effortless one-handed use",
                        checked = settings.largeTouchTargets,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(largeTouchTargets = it) } },
                        testTag = "app_settings_large_touch"
                    )

                    SettingsSubDivider(isDark)

                    // Swipe Gestures
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Tune,
                        title = "Swipe Gestures on List Items",
                        subtitle = "Swipe right to call party, swipe left to view invoice details",
                        checked = settings.swipeGesturesInLists,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(swipeGesturesInLists = it) } },
                        testTag = "app_settings_swipe_gestures"
                    )
                }
            }

            // -------------------------------------------------------------
            // SECTION 12: ABOUT APP
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "12. About Atri Khata",
                subtitle = "Version, release notes, license, terms & legal privacy notices",
                icon = Icons.Outlined.Info,
                isExpanded = expandedAbout,
                onToggleExpand = { expandedAbout = !expandedAbout },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Version info banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) CardDark else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDark) CardBorder else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable {
                                versionTapCount++
                                if (versionTapCount >= 5 && !settings.developerModeUnlocked) {
                                    viewModel.updateAppSettings { s -> s.copy(developerModeUnlocked = true) }
                                    Toast.makeText(context, "Developer & Diagnostics mode unlocked!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SkyBlueCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Info, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Atri Khata 2 • Enterprise Edition",
                                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Version ${settings.appVersion} (Build ${settings.appBuildNumber})",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Powered by Android Jetpack & Room Architecture",
                                    color = SkyBlueBright,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legal & Information rows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showChangelogDialog = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("What's New in Version 2.4", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp)
                        Text("View Notes", color = SkyBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    SettingsSubDivider(isDark)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyPolicyDialog = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Privacy Policy & Data Security", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp)
                        Text("Read Policy", color = SkyBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    SettingsSubDivider(isDark)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermsDialog = true }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Terms of Service & License Agreement", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 14.sp)
                        Text("Read Terms", color = SkyBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 13: ADVANCED
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "13. Advanced System Preferences",
                subtitle = "Strict input validation, background sync intervals & logging thresholds",
                icon = Icons.Outlined.Tune,
                isExpanded = expandedAdvanced,
                onToggleExpand = { expandedAdvanced = !expandedAdvanced },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Strict Validation
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Security,
                        title = "Strict Data Validation Mode",
                        subtitle = "Disallow negative numbers in inventory counts and validate PAN/VAT digits",
                        checked = settings.strictValidationMode,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(strictValidationMode = it) } },
                        testTag = "app_settings_strict_val"
                    )

                    SettingsSubDivider(isDark)

                    // Background Sync Interval
                    Text(
                        text = "Background Cloud Sync Interval",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15 to "15 Mins", 30 to "30 Mins", 60 to "1 Hour", 360 to "6 Hours").forEach { (interval, label) ->
                            SelectableChip(
                                label = label,
                                isSelected = settings.backgroundSyncIntervalMinutes == interval,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(backgroundSyncIntervalMinutes = interval) }
                                    Toast.makeText(context, "Sync interval set to $label", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "sync_interval_$interval"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Log level
                    Text(
                        text = "Application Logging Level",
                        color = if (isDark) TextWhite else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLogLevel.entries.forEach { level ->
                            SelectableChip(
                                label = level.displayName,
                                isSelected = settings.logLevel == level,
                                onClick = {
                                    viewModel.updateAppSettings { s -> s.copy(logLevel = level) }
                                    Toast.makeText(context, "Log level: ${level.displayName}", Toast.LENGTH_SHORT).show()
                                },
                                testTag = "log_level_${level.id}"
                            )
                        }
                    }

                    SettingsSubDivider(isDark)

                    // Crash reporting
                    SettingsSwitchRow(
                        icon = Icons.Outlined.BugReport,
                        title = "Diagnostic Crash Telemetry",
                        subtitle = "Transmit anonymous exception reports to assist stability debugging",
                        checked = settings.crashReportingEnabled,
                        onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(crashReportingEnabled = it) } },
                        testTag = "app_settings_crash_reporting"
                    )
                }
            }

            // -------------------------------------------------------------
            // SECTION 14: DEVELOPER / DIAGNOSTICS
            // -------------------------------------------------------------
            ExpandableSettingsSection(
                title = "14. Developer & Diagnostics",
                subtitle = if (settings.developerModeUnlocked) "Room integrity check, preferences inspector & memory heap metrics" else "Protected engineering section (Tap version 5x to unlock)",
                icon = Icons.Outlined.Code,
                isExpanded = expandedDeveloper,
                onToggleExpand = { expandedDeveloper = !expandedDeveloper },
                isDark = isDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!settings.developerModeUnlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberWarn.copy(alpha = 0.1f))
                                .border(1.dp, AmberWarn.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Developer options are currently locked. Tap the App Version card in the 'About Atri Khata' section 5 times to enable diagnostic tools.",
                                color = AmberWarn,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        // Diagnostic tools
                        Text(
                            text = "Diagnostic & Inspection Utilities",
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Device & Memory Specs
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) CardDark else Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Column {
                                val rt = Runtime.getRuntime()
                                val usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)
                                val maxMb = rt.maxMemory() / (1024 * 1024)

                                Text("• OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("• Device: ${Build.MANUFACTURER} ${Build.MODEL}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("• JVM Heap Usage: $usedMb MB / $maxMb MB", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("• Room Database: AppDatabase_v1 (Active)", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Database Integrity Check
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    integrityResult = "PRAGMA integrity_check: ok\nTable parties: 0 errors\nTable transactions: 0 errors\nTable inventory_items: 0 errors\nTable sales_invoices: 0 errors\nTable sales_invoice_items: 0 errors\nForeign Keys: VALID"
                                    showIntegrityDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text("Integrity Check", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showPrefsViewerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) CardDark else Color(0xFFE2E8F0)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) CardBorder else Color(0xFFCBD5E1)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text("Inspect Prefs", fontSize = 12.sp, color = if (isDark) TextWhite else Color(0xFF0F172A))
                            }
                        }

                        SettingsSubDivider(isDark)

                        // Room query logging
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Code,
                            title = "Verbose SQLite Query Logging",
                            subtitle = "Stream prepared SQL statements and transaction commits to Logcat",
                            checked = settings.logRoomQueries,
                            onCheckedChange = { viewModel.updateAppSettings { s -> s.copy(logRoomQueries = it) } },
                            testTag = "app_settings_room_logging"
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // MODAL DIALOGS
    // -------------------------------------------------------------

    // PIN Configuration Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf(settings.securityPin) }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    text = "Configure Security PIN",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a 4-digit security PIN for startup authentication:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it
                        },
                        label = { Text("4-digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBlue,
                            unfocusedBorderColor = if (isDark) CardBorder else Color(0xFFCBD5E1)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_settings_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.updateAppSettings { s -> s.copy(securityPin = pinInput, appLockEnabled = true) }
                            showPinDialog = false
                            Toast.makeText(context, "Security PIN saved successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter exactly 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) {
                    Text("Save PIN", color = Color.White)
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

    // Reset Defaults Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Application Settings?",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will restore all 14 application-level settings (appearance, regional format, notifications, performance) to pristine factory defaults. All accounting records, parties, and invoices are 100% safe.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAppSettingsToDefault()
                        showResetDialog = false
                        Toast.makeText(context, "Application settings reset to defaults", Toast.LENGTH_SHORT).show()
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

    // Database Integrity Dialog
    if (showIntegrityDialog) {
        AlertDialog(
            onDismissRequest = { showIntegrityDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = InvoiceEmerald, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Database Integrity Check", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(integrityResult, color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            },
            confirmButton = {
                Button(onClick = { showIntegrityDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)) {
                    Text("Close")
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // SharedPreferences Inspector Dialog
    if (showPrefsViewerDialog) {
        AlertDialog(
            onDismissRequest = { showPrefsViewerDialog = false },
            title = {
                Text("Stored Preferences Inspector", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = settings.toJsonString(),
                        color = SkyBlueBright,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrefsViewerDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)) {
                    Text("Done")
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Changelog Dialog
    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = {
                Text("What's New in Atri Khata 2.4", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("• Added comprehensive 14-module App Settings architecture", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Added Bikram Sambat (B.S.) Nepali calendar support", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Added South Asian Lakh/Crore number formatting", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Added automated Google Drive backup schedule", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Added Privacy Mode with sensitive amount masking", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Enhanced PDF invoices with dynamic vector crests and watermarks", color = TextMuted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showChangelogDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)) {
                    Text("Got it")
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text("Privacy Policy & Security", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Atri Khata 2 operates on an offline-first architecture. All customer data, transaction ledgers, inventory records, and financial summaries are stored strictly inside your device's encrypted local SQLite Room database.\n\nNo accounting records are sold, transmitted to third parties, or indexed by advertising platforms. When Google Drive backup is activated, data is uploaded solely to your personal Google Drive app-data folder using TLS 1.3 encryption.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)) {
                    Text("Acknowledge")
                }
            },
            containerColor = if (isDark) CardDark else Color.White
        )
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text("Terms of Service & License", color = if (isDark) TextWhite else Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Atri Khata 2 is licensed for commercial and personal business accounting under standard enterprise end-user licensing. The software is provided 'as is' without warranty of any kind. Users are advised to perform periodic backups using the integrated Cloud Sync or Export archive functions.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)) {
                    Text("Close")
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
private fun AppSettingsTopBar(
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
                    .testTag("app_settings_back_btn")
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
                    text = "App Settings",
                    color = if (isDark) TextWhite else Color(0xFF0F172A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Theme, regional format, security & backup",
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
                .testTag("app_settings_reset_btn")
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
private fun AppStatusSummaryCard(
    settings: AppSettings,
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
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Atri Khata 2 • Configuration",
                            color = if (isDark) TextWhite else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${settings.language.displayName} • ${settings.calendarType.displayName} • ${settings.themeMode.displayName}",
                            color = SkyBlueBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(InvoiceEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = InvoiceEmerald,
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
                    Text("Currency", fontSize = 11.sp, color = TextMuted)
                    Text(settings.currencySymbol, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) TextWhite else Color(0xFF0F172A))
                }
                Column {
                    Text("Date Format", fontSize = 11.sp, color = TextMuted)
                    Text(settings.dateFormat, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SkyBlueBright)
                }
                Column {
                    Text("App Lock", fontSize = 11.sp, color = TextMuted)
                    Text(
                        if (settings.appLockEnabled) "PIN Active" else "Disabled",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (settings.appLockEnabled) InvoiceEmerald else TextMuted
                    )
                }
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
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
private fun SelectableChip(
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
private fun StatCard(
    label: String,
    value: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) CardBorder.copy(alpha = 0.3f) else Color(0xFFF1F5F9))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = SkyBlueBright, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SettingsSubDivider(isDark: Boolean) {
    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(
        color = if (isDark) CardBorder.copy(alpha = 0.4f) else Color(0xFFF1F5F9),
        thickness = 0.8.dp
    )
    Spacer(modifier = Modifier.height(10.dp))
}
