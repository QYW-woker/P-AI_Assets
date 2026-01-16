package com.example.smartledger.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smartledger.R

/**
 * 字体族定义
 * DM Sans - 用于数字显示
 * Noto Sans SC - 用于中文显示
 *
 * 注意：需要添加字体文件到res/font目录
 * 如果字体文件不存在，将使用系统默认字体
 */

// DM Sans字体族 - 数字显示
val DmSansFontFamily = try {
    FontFamily(
        Font(R.font.dm_sans_regular, FontWeight.Normal),
        Font(R.font.dm_sans_medium, FontWeight.Medium),
        Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
        Font(R.font.dm_sans_bold, FontWeight.Bold)
    )
} catch (e: Exception) {
    FontFamily.Default
}

// Noto Sans SC字体族 - 中文显示
val NotoSansScFontFamily = try {
    FontFamily(
        Font(R.font.noto_sans_sc_regular, FontWeight.Normal),
        Font(R.font.noto_sans_sc_medium, FontWeight.Medium),
        Font(R.font.noto_sans_sc_bold, FontWeight.Bold)
    )
} catch (e: Exception) {
    FontFamily.Default
}

/**
 * 应用字体样式定义
 */
object AppTypography {
    // 数字显示样式 - 使用DM Sans
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

    // 中文显示样式 - 使用Noto Sans SC
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
