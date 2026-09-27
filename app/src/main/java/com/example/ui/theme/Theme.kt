package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Comprehensive Dual-Theme Colors Data Class
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val cardBorderLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val primary: Color,
    val primaryBright: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val primaryCardBg: Color,
    val primaryCardBorder: Color,
    val unpaidBadgeBg: Color,
    val unpaidBadgeText: Color,
    val settledBadgeBg: Color,
    val settledBadgeText: Color,
    val orangeAccent: Color,
    val orangeCardBg: Color,
    val orangeCardBorder: Color,
    val goldCoin: Color,
    val goldCoinBg: Color,
    val goldCoinBorder: Color,
    val shortcutIconBg: Color,
    val shortcutIconTint: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val divider: Color,
    val error: Color = Color(0xFFEF4444)
)

// Dark Palette (Corporate Navy Theme)
val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0A0F1D),
    surface = Color(0xFF0F172A),
    cardBackground = Color(0xFF141F36),
    cardBorder = Color(0xFF1E2F4D),
    cardBorderLight = Color(0xFF2A3F66),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF94A3B8),
    textSubtle = Color(0xFF64748B),
    primary = Color(0xFF00A3FF),
    primaryBright = Color(0xFF38BDF8),
    primaryLight = Color(0xFF7DD3FC),
    primaryDark = Color(0xFF0284C7),
    primaryCardBg = Color(0xFF0B213D),
    primaryCardBorder = Color(0xFF1B426E),
    unpaidBadgeBg = Color(0xFF0E2847),
    unpaidBadgeText = Color(0xFF38BDF8),
    settledBadgeBg = Color(0xFF1E293B),
    settledBadgeText = Color(0xFF94A3B8),
    orangeAccent = Color(0xFFFF8A00),
    orangeCardBg = Color(0xFF2E1909),
    orangeCardBorder = Color(0xFF593012),
    goldCoin = Color(0xFFFFB800),
    goldCoinBg = Color(0xFF2B210C),
    goldCoinBorder = Color(0xFF594314),
    shortcutIconBg = Color(0xFF0F2644),
    shortcutIconTint = Color(0xFF00A3FF),
    inputBackground = Color(0xFF0F172A),
    inputBorder = Color(0xFF1E2F4D),
    divider = Color(0xFF1E2F4D)
)

// Light Palette (Corporate Light Slate & Sky Blue Theme)
val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF8FAFC),       // Clean off-white
    surface = Color(0xFFFFFFFF),          // Crisp white
    cardBackground = Color(0xFFFFFFFF),   // Crisp white cards
    cardBorder = Color(0xFFE2E8F0),       // Soft clean border
    cardBorderLight = Color(0xFFCBD5E1),
    textPrimary = Color(0xFF0F172A),      // Deep charcoal/navy for high contrast
    textSecondary = Color(0xFF334155),    // Slate secondary
    textMuted = Color(0xFF64748B),        // Muted slate
    textSubtle = Color(0xFF94A3B8),
    primary = Color(0xFF0284C7),          // Bright Sky Blue (high-contrast for light)
    primaryBright = Color(0xFF0284C7),
    primaryLight = Color(0xFF0369A1),
    primaryDark = Color(0xFF075985),
    primaryCardBg = Color(0xFFE0F2FE),    // Soft icy blue card
    primaryCardBorder = Color(0xFFBAE6FD),
    unpaidBadgeBg = Color(0xFFE0F2FE),
    unpaidBadgeText = Color(0xFF0284C7),
    settledBadgeBg = Color(0xFFF1F5F9),
    settledBadgeText = Color(0xFF475569),
    orangeAccent = Color(0xFFEA580C),     // Vibrant amber-orange
    orangeCardBg = Color(0xFFFFF7ED),     // Soft warm container
    orangeCardBorder = Color(0xFFFED7AA),
    goldCoin = Color(0xFFD97706),
    goldCoinBg = Color(0xFFFEF3C7),
    goldCoinBorder = Color(0xFFFDE68A),
    shortcutIconBg = Color(0xFFF0F9FF),
    shortcutIconTint = Color(0xFF0284C7),
    inputBackground = Color(0xFFF8FAFC),
    inputBorder = Color(0xFFCBD5E1),
    divider = Color(0xFFE2E8F0)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.isDark
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00A3FF),
    onPrimary = Color(0xFF002244),
    primaryContainer = Color(0xFF0B213D),
    onPrimaryContainer = Color(0xFFBEE3F8),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF001F3F),
    background = Color(0xFF0A0F1D),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF141F36),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF1E2F4D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0086E6),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = appColors.background.toArgb()
                window.navigationBarColor = appColors.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
