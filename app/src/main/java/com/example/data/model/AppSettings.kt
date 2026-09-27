package com.example.data.model

import org.json.JSONObject

/**
 * Atri Khata 2 - App-level Theme Modes
 */
enum class AppThemeMode(val id: String, val displayName: String) {
    SYSTEM("system", "System Default"),
    LIGHT("light", "Corporate Light"),
    DARK("dark", "Corporate Dark (Navy OLED)");

    companion object {
        fun fromId(id: String?): AppThemeMode = entries.firstOrNull { it.id == id } ?: DARK
    }
}

/**
 * Atri Khata 2 - Startup Navigation Destinations
 */
enum class StartupScreen(val id: String, val displayName: String) {
    LAST_OPENED("last_opened", "Last Opened Screen"),
    HOME("home", "Dashboard / Home"),
    TRANSACTIONS("transactions", "Transactions Ledger"),
    REPORTS("reports", "Reports & Analytics"),
    PARTIES("parties", "Parties & Customers"),
    INVENTORY("inventory", "Inventory & Stock");

    companion object {
        fun fromId(id: String?): StartupScreen = entries.firstOrNull { it.id == id } ?: HOME
    }
}

/**
 * Atri Khata 2 - App Languages
 */
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English (US)"),
    NEPALI("ne", "Nepali", "नेपाली (Nepal)");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

/**
 * Atri Khata 2 - Calendar System
 */
enum class AppCalendarType(val id: String, val displayName: String, val description: String) {
    GREGORIAN_AD("ad", "English (A.D.)", "Gregorian Calendar"),
    BIKRAM_SAMBAT_BS("bs", "Nepali (B.S.)", "Bikram Sambat Calendar");

    companion object {
        fun fromId(id: String?): AppCalendarType = entries.firstOrNull { it.id == id } ?: GREGORIAN_AD
    }
}

/**
 * Atri Khata 2 - Currency Symbol Position
 */
enum class CurrencyPosition(val id: String, val displayName: String, val example: String) {
    PREFIX("prefix", "Before Amount", "Rs. 1,25,000"),
    SUFFIX("suffix", "After Amount", "1,25,000 Rs.");

    companion object {
        fun fromId(id: String?): CurrencyPosition = entries.firstOrNull { it.id == id } ?: PREFIX
    }
}

/**
 * Atri Khata 2 - Number Grouping Formats
 */
enum class NumberGroupingFormat(val id: String, val displayName: String, val example: String) {
    SOUTH_ASIAN("south_asian", "Lakh / Crore (South Asian)", "1,25,000.00"),
    INTERNATIONAL("international", "Million / Billion (Standard)", "125,000.00");

    companion object {
        fun fromId(id: String?): NumberGroupingFormat = entries.firstOrNull { it.id == id } ?: SOUTH_ASIAN
    }
}

/**
 * Atri Khata 2 - Negative Number Display Formats
 */
enum class NegativeNumberStyle(val id: String, val displayName: String, val example: String) {
    MINUS_PREFIX("minus_prefix", "-Rs. 100", "-Rs. 100.00"),
    PARENTHESES("parentheses", "(Rs. 100)", "(Rs. 100.00)"),
    MINUS_SUFFIX("minus_suffix", "Rs. 100-", "Rs. 100.00-");

    companion object {
        fun fromId(id: String?): NegativeNumberStyle = entries.firstOrNull { it.id == id } ?: MINUS_PREFIX
    }
}

/**
 * Atri Khata 2 - Auto Lock Durations
 */
enum class AutoLockDuration(val id: String, val displayName: String, val minutes: Int) {
    IMMEDIATELY("immediately", "Immediately on exit", 0),
    ONE_MINUTE("1_min", "1 minute in background", 1),
    FIVE_MINUTES("5_min", "5 minutes in background", 5),
    FIFTEEN_MINUTES("15_min", "15 minutes in background", 15),
    THIRTY_MINUTES("30_min", "30 minutes in background", 30);

    companion object {
        fun fromId(id: String?): AutoLockDuration = entries.firstOrNull { it.id == id } ?: ONE_MINUTE
    }
}

/**
 * Atri Khata 2 - Logging Levels
 */
enum class AppLogLevel(val id: String, val displayName: String) {
    ERROR("error", "Errors Only"),
    WARN("warn", "Warnings & Errors"),
    INFO("info", "Standard Info"),
    DEBUG("debug", "Verbose Debug");

    companion object {
        fun fromId(id: String?): AppLogLevel = entries.firstOrNull { it.id == id } ?: INFO
    }
}

/**
 * Comprehensive App-level settings data class for Atri Khata 2.
 * Contains purely application-wide settings organized cleanly into 14 distinct modules.
 */
data class AppSettings(
    // -------------------------------------------------------------
    // 1. GENERAL APP SETTINGS
    // -------------------------------------------------------------
    val appName: String = "Atri Khata 2",
    val showBusinessProfileNameHeader: Boolean = true,
    val confirmBeforeExit: Boolean = true,
    val confirmBeforeDelete: Boolean = true,
    val showWelcomeOnboarding: Boolean = false,
    val autoSaveDrafts: Boolean = true,
    val rememberLastOpenedScreen: Boolean = true,
    val keepLastSelectedTab: Boolean = true,
    val startupScreen: StartupScreen = StartupScreen.HOME,

    // -------------------------------------------------------------
    // 2. APPEARANCE
    // -------------------------------------------------------------
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val dynamicColorsEnabled: Boolean = false,
    val compactUiMode: Boolean = false,
    val showAnimations: Boolean = true,
    val showSectionIcons: Boolean = true,
    val showBottomNavLabels: Boolean = true,

    // -------------------------------------------------------------
    // 3. LANGUAGE & REGION
    // -------------------------------------------------------------
    val language: AppLanguage = AppLanguage.ENGLISH,
    val country: String = "Nepal",
    val currencySymbol: String = "Rs.",
    val currencyPosition: CurrencyPosition = CurrencyPosition.PREFIX,

    // -------------------------------------------------------------
    // 4. DATE & NUMBER FORMAT
    // -------------------------------------------------------------
    val dateFormat: String = "DD/MM/YYYY",
    val calendarType: AppCalendarType = AppCalendarType.GREGORIAN_AD,
    val decimalPlaces: Int = 2,
    val numberGrouping: NumberGroupingFormat = NumberGroupingFormat.SOUTH_ASIAN,
    val showTrailingZeros: Boolean = true,
    val negativeNumberStyle: NegativeNumberStyle = NegativeNumberStyle.MINUS_PREFIX,

    // -------------------------------------------------------------
    // 5. NOTIFICATIONS
    // -------------------------------------------------------------
    val enableNotifications: Boolean = true,
    val transactionNotifications: Boolean = true,
    val paymentDueReminders: Boolean = true,
    val customerDueReminders: Boolean = true,
    val lowStockAlerts: Boolean = true,
    val expiryAlerts: Boolean = true,
    val backupReminders: Boolean = true,
    val reminderTimeHour: Int = 9,
    val reminderTimeMinute: Int = 0,
    val reminderDaysBeforeDue: Int = 3,
    val reminderDaysBeforeExpiry: Int = 15,

    // -------------------------------------------------------------
    // 6. SECURITY & PRIVACY
    // -------------------------------------------------------------
    val appLockEnabled: Boolean = false,
    val securityPin: String = "",
    val biometricAuthEnabled: Boolean = true,
    val autoLockDuration: AutoLockDuration = AutoLockDuration.ONE_MINUTE,
    val privacyModeMaskBalances: Boolean = false,
    val hideAmountsOnDashboard: Boolean = false,
    val blockScreenshots: Boolean = false,

    // -------------------------------------------------------------
    // 7. BACKUP & RESTORE
    // -------------------------------------------------------------
    val autoCloudBackupEnabled: Boolean = false,
    val backupFrequency: String = "Daily (Midnight)",
    val lastBackupTimestamp: Long = 0L,
    val backupOverWifiOnly: Boolean = true,
    val googleDriveAccountEmail: String = "",

    // -------------------------------------------------------------
    // 8. DATA MANAGEMENT
    // -------------------------------------------------------------
    val autoCleanTempFilesOnExit: Boolean = true,
    val lastDatabaseOptimizationTime: Long = 0L,

    // -------------------------------------------------------------
    // 9. PERFORMANCE
    // -------------------------------------------------------------
    val hardwareAcceleration: Boolean = true,
    val imageCachingEnabled: Boolean = true,
    val fastScrollbars: Boolean = true,
    val lazyListChunkLoading: Boolean = true,
    val lowEndDeviceMode: Boolean = false,

    // -------------------------------------------------------------
    // 10. NAVIGATION & HOME
    // -------------------------------------------------------------
    val defaultHomeTab: String = "Home",
    val showQuickActionFab: Boolean = true,
    val showBottomBarBadges: Boolean = true,
    val compactBottomBar: Boolean = false,

    // -------------------------------------------------------------
    // 11. USER EXPERIENCE
    // -------------------------------------------------------------
    val hapticFeedback: Boolean = true,
    val soundEffects: Boolean = false,
    val highContrastText: Boolean = false,
    val largeTouchTargets: Boolean = false,
    val swipeGesturesInLists: Boolean = true,

    // -------------------------------------------------------------
    // 12. ABOUT APP
    // -------------------------------------------------------------
    val appVersion: String = "2.4.0",
    val appBuildNumber: String = "2026.09.25",
    val developerName: String = "Atri Solutions",
    val supportEmail: String = "support@atrikhata.com",
    val websiteUrl: String = "https://atrikhata.com",

    // -------------------------------------------------------------
    // 13. ADVANCED
    // -------------------------------------------------------------
    val strictValidationMode: Boolean = true,
    val backgroundSyncIntervalMinutes: Int = 30,
    val logLevel: AppLogLevel = AppLogLevel.INFO,
    val experimentalFeaturesEnabled: Boolean = false,
    val crashReportingEnabled: Boolean = true,

    // -------------------------------------------------------------
    // 14. DEVELOPER / DIAGNOSTICS
    // -------------------------------------------------------------
    val developerModeUnlocked: Boolean = false,
    val showDebugMetricsOverlay: Boolean = false,
    val logRoomQueries: Boolean = false
) {
    /**
     * Serializes this AppSettings configuration into a structured JSON string.
     */
    fun toJsonString(): String {
        val json = JSONObject()

        // 1. General
        json.put("appName", appName)
        json.put("showBusinessProfileNameHeader", showBusinessProfileNameHeader)
        json.put("confirmBeforeExit", confirmBeforeExit)
        json.put("confirmBeforeDelete", confirmBeforeDelete)
        json.put("showWelcomeOnboarding", showWelcomeOnboarding)
        json.put("autoSaveDrafts", autoSaveDrafts)
        json.put("rememberLastOpenedScreen", rememberLastOpenedScreen)
        json.put("keepLastSelectedTab", keepLastSelectedTab)
        json.put("startupScreen", startupScreen.id)

        // 2. Appearance
        json.put("themeMode", themeMode.id)
        json.put("dynamicColorsEnabled", dynamicColorsEnabled)
        json.put("compactUiMode", compactUiMode)
        json.put("showAnimations", showAnimations)
        json.put("showSectionIcons", showSectionIcons)
        json.put("showBottomNavLabels", showBottomNavLabels)

        // 3. Language & Region
        json.put("language", language.code)
        json.put("country", country)
        json.put("currencySymbol", currencySymbol)
        json.put("currencyPosition", currencyPosition.id)

        // 4. Date & Number Format
        json.put("dateFormat", dateFormat)
        json.put("calendarType", calendarType.id)
        json.put("decimalPlaces", decimalPlaces)
        json.put("numberGrouping", numberGrouping.id)
        json.put("showTrailingZeros", showTrailingZeros)
        json.put("negativeNumberStyle", negativeNumberStyle.id)

        // 5. Notifications
        json.put("enableNotifications", enableNotifications)
        json.put("transactionNotifications", transactionNotifications)
        json.put("paymentDueReminders", paymentDueReminders)
        json.put("customerDueReminders", customerDueReminders)
        json.put("lowStockAlerts", lowStockAlerts)
        json.put("expiryAlerts", expiryAlerts)
        json.put("backupReminders", backupReminders)
        json.put("reminderTimeHour", reminderTimeHour)
        json.put("reminderTimeMinute", reminderTimeMinute)
        json.put("reminderDaysBeforeDue", reminderDaysBeforeDue)
        json.put("reminderDaysBeforeExpiry", reminderDaysBeforeExpiry)

        // 6. Security & Privacy
        json.put("appLockEnabled", appLockEnabled)
        json.put("securityPin", securityPin)
        json.put("biometricAuthEnabled", biometricAuthEnabled)
        json.put("autoLockDuration", autoLockDuration.id)
        json.put("privacyModeMaskBalances", privacyModeMaskBalances)
        json.put("hideAmountsOnDashboard", hideAmountsOnDashboard)
        json.put("blockScreenshots", blockScreenshots)

        // 7. Backup & Restore
        json.put("autoCloudBackupEnabled", autoCloudBackupEnabled)
        json.put("backupFrequency", backupFrequency)
        json.put("lastBackupTimestamp", lastBackupTimestamp)
        json.put("backupOverWifiOnly", backupOverWifiOnly)
        json.put("googleDriveAccountEmail", googleDriveAccountEmail)

        // 8. Data Management
        json.put("autoCleanTempFilesOnExit", autoCleanTempFilesOnExit)
        json.put("lastDatabaseOptimizationTime", lastDatabaseOptimizationTime)

        // 9. Performance
        json.put("hardwareAcceleration", hardwareAcceleration)
        json.put("imageCachingEnabled", imageCachingEnabled)
        json.put("fastScrollbars", fastScrollbars)
        json.put("lazyListChunkLoading", lazyListChunkLoading)
        json.put("lowEndDeviceMode", lowEndDeviceMode)

        // 10. Navigation & Home
        json.put("defaultHomeTab", defaultHomeTab)
        json.put("showQuickActionFab", showQuickActionFab)
        json.put("showBottomBarBadges", showBottomBarBadges)
        json.put("compactBottomBar", compactBottomBar)

        // 11. User Experience
        json.put("hapticFeedback", hapticFeedback)
        json.put("soundEffects", soundEffects)
        json.put("highContrastText", highContrastText)
        json.put("largeTouchTargets", largeTouchTargets)
        json.put("swipeGesturesInLists", swipeGesturesInLists)

        // 12. About App
        json.put("appVersion", appVersion)
        json.put("appBuildNumber", appBuildNumber)
        json.put("developerName", developerName)
        json.put("supportEmail", supportEmail)
        json.put("websiteUrl", websiteUrl)

        // 13. Advanced
        json.put("strictValidationMode", strictValidationMode)
        json.put("backgroundSyncIntervalMinutes", backgroundSyncIntervalMinutes)
        json.put("logLevel", logLevel.id)
        json.put("experimentalFeaturesEnabled", experimentalFeaturesEnabled)
        json.put("crashReportingEnabled", crashReportingEnabled)

        // 14. Developer / Diagnostics
        json.put("developerModeUnlocked", developerModeUnlocked)
        json.put("showDebugMetricsOverlay", showDebugMetricsOverlay)
        json.put("logRoomQueries", logRoomQueries)

        return json.toString()
    }

    companion object {
        /**
         * Reconstructs an AppSettings object from a JSON string.
         * Falls back safely to default values for missing or corrupted keys.
         */
        fun fromJsonString(jsonString: String?): AppSettings? {
            if (jsonString.isNullOrBlank()) return null
            return try {
                val json = JSONObject(jsonString)
                AppSettings(
                    appName = json.optString("appName", "Atri Khata 2"),
                    showBusinessProfileNameHeader = json.optBoolean("showBusinessProfileNameHeader", true),
                    confirmBeforeExit = json.optBoolean("confirmBeforeExit", true),
                    confirmBeforeDelete = json.optBoolean("confirmBeforeDelete", true),
                    showWelcomeOnboarding = json.optBoolean("showWelcomeOnboarding", false),
                    autoSaveDrafts = json.optBoolean("autoSaveDrafts", true),
                    rememberLastOpenedScreen = json.optBoolean("rememberLastOpenedScreen", true),
                    keepLastSelectedTab = json.optBoolean("keepLastSelectedTab", true),
                    startupScreen = StartupScreen.fromId(json.optString("startupScreen", "home")),

                    themeMode = AppThemeMode.fromId(json.optString("themeMode", "dark")),
                    dynamicColorsEnabled = json.optBoolean("dynamicColorsEnabled", false),
                    compactUiMode = json.optBoolean("compactUiMode", false),
                    showAnimations = json.optBoolean("showAnimations", true),
                    showSectionIcons = json.optBoolean("showSectionIcons", true),
                    showBottomNavLabels = json.optBoolean("showBottomNavLabels", true),

                    language = AppLanguage.fromCode(json.optString("language", "en")),
                    country = json.optString("country", "Nepal"),
                    currencySymbol = json.optString("currencySymbol", "Rs."),
                    currencyPosition = CurrencyPosition.fromId(json.optString("currencyPosition", "prefix")),

                    dateFormat = json.optString("dateFormat", "DD/MM/YYYY"),
                    calendarType = AppCalendarType.fromId(json.optString("calendarType", "ad")),
                    decimalPlaces = json.optInt("decimalPlaces", 2),
                    numberGrouping = NumberGroupingFormat.fromId(json.optString("numberGrouping", "south_asian")),
                    showTrailingZeros = json.optBoolean("showTrailingZeros", true),
                    negativeNumberStyle = NegativeNumberStyle.fromId(json.optString("negativeNumberStyle", "minus_prefix")),

                    enableNotifications = json.optBoolean("enableNotifications", true),
                    transactionNotifications = json.optBoolean("transactionNotifications", true),
                    paymentDueReminders = json.optBoolean("paymentDueReminders", true),
                    customerDueReminders = json.optBoolean("customerDueReminders", true),
                    lowStockAlerts = json.optBoolean("lowStockAlerts", true),
                    expiryAlerts = json.optBoolean("expiryAlerts", true),
                    backupReminders = json.optBoolean("backupReminders", true),
                    reminderTimeHour = json.optInt("reminderTimeHour", 9),
                    reminderTimeMinute = json.optInt("reminderTimeMinute", 0),
                    reminderDaysBeforeDue = json.optInt("reminderDaysBeforeDue", 3),
                    reminderDaysBeforeExpiry = json.optInt("reminderDaysBeforeExpiry", 15),

                    appLockEnabled = json.optBoolean("appLockEnabled", false),
                    securityPin = json.optString("securityPin", ""),
                    biometricAuthEnabled = json.optBoolean("biometricAuthEnabled", true),
                    autoLockDuration = AutoLockDuration.fromId(json.optString("autoLockDuration", "1_min")),
                    privacyModeMaskBalances = json.optBoolean("privacyModeMaskBalances", false),
                    hideAmountsOnDashboard = json.optBoolean("hideAmountsOnDashboard", false),
                    blockScreenshots = json.optBoolean("blockScreenshots", false),

                    autoCloudBackupEnabled = json.optBoolean("autoCloudBackupEnabled", false),
                    backupFrequency = json.optString("backupFrequency", "Daily (Midnight)"),
                    lastBackupTimestamp = json.optLong("lastBackupTimestamp", 0L),
                    backupOverWifiOnly = json.optBoolean("backupOverWifiOnly", true),
                    googleDriveAccountEmail = json.optString("googleDriveAccountEmail", ""),

                    autoCleanTempFilesOnExit = json.optBoolean("autoCleanTempFilesOnExit", true),
                    lastDatabaseOptimizationTime = json.optLong("lastDatabaseOptimizationTime", 0L),

                    hardwareAcceleration = json.optBoolean("hardwareAcceleration", true),
                    imageCachingEnabled = json.optBoolean("imageCachingEnabled", true),
                    fastScrollbars = json.optBoolean("fastScrollbars", true),
                    lazyListChunkLoading = json.optBoolean("lazyListChunkLoading", true),
                    lowEndDeviceMode = json.optBoolean("lowEndDeviceMode", false),

                    defaultHomeTab = json.optString("defaultHomeTab", "Home"),
                    showQuickActionFab = json.optBoolean("showQuickActionFab", true),
                    showBottomBarBadges = json.optBoolean("showBottomBarBadges", true),
                    compactBottomBar = json.optBoolean("compactBottomBar", false),

                    hapticFeedback = json.optBoolean("hapticFeedback", true),
                    soundEffects = json.optBoolean("soundEffects", false),
                    highContrastText = json.optBoolean("highContrastText", false),
                    largeTouchTargets = json.optBoolean("largeTouchTargets", false),
                    swipeGesturesInLists = json.optBoolean("swipeGesturesInLists", true),

                    appVersion = json.optString("appVersion", "2.4.0"),
                    appBuildNumber = json.optString("appBuildNumber", "2026.09.25"),
                    developerName = json.optString("developerName", "Atri Solutions"),
                    supportEmail = json.optString("supportEmail", "support@atrikhata.com"),
                    websiteUrl = json.optString("websiteUrl", "https://atrikhata.com"),

                    strictValidationMode = json.optBoolean("strictValidationMode", true),
                    backgroundSyncIntervalMinutes = json.optInt("backgroundSyncIntervalMinutes", 30),
                    logLevel = AppLogLevel.fromId(json.optString("logLevel", "info")),
                    experimentalFeaturesEnabled = json.optBoolean("experimentalFeaturesEnabled", false),
                    crashReportingEnabled = json.optBoolean("crashReportingEnabled", true),

                    developerModeUnlocked = json.optBoolean("developerModeUnlocked", false),
                    showDebugMetricsOverlay = json.optBoolean("showDebugMetricsOverlay", false),
                    logRoomQueries = json.optBoolean("logRoomQueries", false)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
