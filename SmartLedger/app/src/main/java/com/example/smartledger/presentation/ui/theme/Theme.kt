package com.example.smartledger.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 浅色主题配色方案
 */
private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.TextOnPrimary,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.TextOnPrimary,

    secondary = AppColors.Accent,
    onSecondary = AppColors.TextOnAccent,
    secondaryContainer = AppColors.AccentLight,
    onSecondaryContainer = AppColors.Accent,

    tertiary = AppColors.Success,
    onTertiary = AppColors.TextOnPrimary,
    tertiaryContainer = AppColors.SuccessLight,
    onTertiaryContainer = AppColors.Success,

    error = AppColors.Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,

    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.Card,
    onSurfaceVariant = AppColors.TextSecondary,

    outline = AppColors.Border,
    outlineVariant = AppColors.Divider,

    inverseSurface = AppColors.Primary,
    inverseOnSurface = AppColors.TextOnPrimary,
    inversePrimary = AppColors.AccentLight
)

/**
 * 深色主题配色方案
 */
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Accent,
    onPrimary = AppColors.TextOnPrimary,
    primaryContainer = DarkColors.PrimaryLight,
    onPrimaryContainer = AppColors.TextOnPrimary,

    secondary = AppColors.Accent,
    onSecondary = AppColors.TextOnAccent,
    secondaryContainer = AppColors.AccentDark,
    onSecondaryContainer = AppColors.AccentLight,

    tertiary = AppColors.Success,
    onTertiary = AppColors.TextOnPrimary,
    tertiaryContainer = Color(0xFF004D40),
    onTertiaryContainer = AppColors.SuccessLight,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = DarkColors.Background,
    onBackground = DarkColors.TextPrimary,

    surface = DarkColors.Surface,
    onSurface = DarkColors.TextPrimary,
    surfaceVariant = DarkColors.Card,
    onSurfaceVariant = DarkColors.TextSecondary,

    outline = DarkColors.Border,
    outlineVariant = DarkColors.Divider,

    inverseSurface = AppColors.Background,
    inverseOnSurface = AppColors.TextPrimary,
    inversePrimary = AppColors.Accent
)

/**
 * 扩展颜色 - 用于自定义颜色访问
 */
data class ExtendedColors(
    val success: Color,
    val successLight: Color,
    val warning: Color,
    val warningLight: Color,
    val info: Color,
    val infoLight: Color,
    val accent: Color,
    val accentLight: Color,
    val textMuted: Color,
    val divider: Color,
    val chartColors: List<Color>,
    val categoryColors: List<Color>
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = AppColors.Success,
        successLight = AppColors.SuccessLight,
        warning = AppColors.Warning,
        warningLight = AppColors.WarningLight,
        info = AppColors.Info,
        infoLight = AppColors.InfoLight,
        accent = AppColors.Accent,
        accentLight = AppColors.AccentLight,
        textMuted = AppColors.TextMuted,
        divider = AppColors.Divider,
        chartColors = AppColors.ChartColors,
        categoryColors = AppColors.CategoryColors
    )
}

/**
 * 扩展颜色实例
 */
private val LightExtendedColors = ExtendedColors(
    success = AppColors.Success,
    successLight = AppColors.SuccessLight,
    warning = AppColors.Warning,
    warningLight = AppColors.WarningLight,
    info = AppColors.Info,
    infoLight = AppColors.InfoLight,
    accent = AppColors.Accent,
    accentLight = AppColors.AccentLight,
    textMuted = AppColors.TextMuted,
    divider = AppColors.Divider,
    chartColors = AppColors.ChartColors,
    categoryColors = AppColors.CategoryColors
)

private val DarkExtendedColors = ExtendedColors(
    success = AppColors.Success,
    successLight = Color(0xFF1A3D35),
    warning = AppColors.Warning,
    warningLight = Color(0xFF3D3520),
    info = AppColors.Info,
    infoLight = Color(0xFF1A1A3D),
    accent = AppColors.Accent,
    accentLight = Color(0xFF3D1A25),
    textMuted = DarkColors.TextMuted,
    divider = DarkColors.Divider,
    chartColors = AppColors.ChartColors,
    categoryColors = AppColors.CategoryColors.map { it.copy(alpha = 0.3f) }
)

/**
 * 智能记账主题
 */
@Composable
fun SmartLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SmartLedgerTypography,
            shapes = SmartLedgerShapes,
            content = content
        )
    }
}

/**
 * 扩展颜色访问器
 */
object SmartLedgerTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
