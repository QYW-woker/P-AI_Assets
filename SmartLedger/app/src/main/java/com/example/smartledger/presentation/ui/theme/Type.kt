package com.example.smartledger.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 字体族定义
 * 使用系统默认字体，确保在所有设备上正常显示
 *
 * 如需自定义字体，可以：
 * 1. 将字体文件放到 res/font/ 目录
 * 2. 使用 Font(R.font.xxx) 加载
 */

// 数字显示字体族 - 使用系统默认
val DmSansFontFamily = FontFamily.Default

// 中文显示字体族 - 使用系统默认
val NotoSansScFontFamily = FontFamily.Default

/**
 * 应用字体样式定义
 */
object AppTypography {
    // 数字显示样式
    val NumberLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )

    val NumberMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.25).sp
    )

    val NumberSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )

    val NumberTiny = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    // 中文显示样式
    val TitleLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )

    val TitleMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )

    val TitleSmall = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    )

    val BodyLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    )

    val BodyMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )

    val BodySmall = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )

    val LabelLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    )

    val LabelMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    )

    val LabelSmall = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp
    )

    val Caption = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        color = AppColors.TextMuted
    )
}

/**
 * Material3 Typography配置
 */
val SmartLedgerTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = 57.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = 45.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansScFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    )
)
