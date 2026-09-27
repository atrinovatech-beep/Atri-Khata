package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Static Palette Constants (for explicit access)
val StaticDarkBackground = Color(0xFF0A0F1D)
val StaticDarkSurface = Color(0xFF0F172A)
val StaticDarkCard = Color(0xFF141F36)
val StaticDarkCardBorder = Color(0xFF1E2F4D)

val StaticLightBackground = Color(0xFFF8FAFC)
val StaticLightSurface = Color(0xFFFFFFFF)
val StaticLightCard = Color(0xFFFFFFFF)
val StaticLightCardBorder = Color(0xFFE2E8F0)

// Dynamic Composable Color Tokens (adapt automatically to active theme)
val BackgroundDark: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.background

val SurfaceDark: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.surface

val CardDark: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.cardBackground

val CardBorder: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.cardBorder

val CardBorderLight: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.cardBorderLight

// Primary Accents: Bright Sky Blue & Corporate Navy
val SkyBlue: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primary

val SkyBlueBright: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryBright

val SkyBlueLight: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryLight

val SkyBlueDark: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryDark

val SkyBlueGlow = Color(0xFF00A3FF)

val SkyBlueCardBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryCardBg

val SkyBlueCardBorder: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryCardBorder

// Status & Indicators
val UnpaidBadgeBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.unpaidBadgeBg

val UnpaidBadgeText: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.unpaidBadgeText

val SettledBadgeBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.settledBadgeBg

val SettledBadgeText: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.settledBadgeText

// Balance Indicators: "To Give" is Orange
val OrangeAccent: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.orangeAccent

val OrangeCardBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.orangeCardBg

val OrangeCardBorder: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.orangeCardBorder

// Rewards and utility
val GoldCoin: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.goldCoin

val GoldCoinBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.goldCoinBg

val GoldCoinBorder: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.goldCoinBorder

val ShortcutIconBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.shortcutIconBg

val ShortcutIconTint: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.shortcutIconTint

// Typography & Content Colors
val TextWhite: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.textPrimary

val TextGrayLight: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.textSecondary

val TextMuted: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.textMuted

val TextSubtle: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.textSubtle

val RedBadge = Color(0xFFEF4444)
val AmberWarn = Color(0xFFF59E0B)
val AmberWarnBg = Color(0xFFFEF3C7)

// Backwards compatibility aliases for any existing references
val TealAccent: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primary
val TealAccentDark: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryDark
val TealCardBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryCardBg
val TealCardBorder: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryCardBorder
val RoseAccent: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.orangeAccent
val RoseCardBg: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.orangeCardBg
val RoseCardBorder: Color
    @Composable @ReadOnlyComposable get() = AppTheme.colors.orangeCardBorder

// Material 3 mappings
val md_theme_dark_primary = Color(0xFF00A3FF)
val md_theme_dark_onPrimary = Color(0xFF002244)
val md_theme_dark_primaryContainer = Color(0xFF0B213D)
val md_theme_dark_onPrimaryContainer = Color(0xFFBEE3F8)
val md_theme_dark_secondary = Color(0xFF38BDF8)
val md_theme_dark_onSecondary = Color(0xFF001F3F)
val md_theme_dark_background = Color(0xFF0A0F1D)
val md_theme_dark_onBackground = Color(0xFFF8FAFC)
val md_theme_dark_surface = Color(0xFF0F172A)
val md_theme_dark_onSurface = Color(0xFFF8FAFC)
val md_theme_dark_surfaceVariant = Color(0xFF141F36)
val md_theme_dark_onSurfaceVariant = Color(0xFFCBD5E1)
val md_theme_dark_outline = Color(0xFF1E2F4D)


