package com.example.smartledger.presentation.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 智能记账应用色彩系统
 */
object AppColors {
    // 主色
    val Primary = Color(0xFF1A1A2E)        // 深蓝黑 - 主背景
    val PrimaryLight = Color(0xFF16213E)   // 浅深蓝 - 卡片背景
    val PrimaryVariant = Color(0xFF0F3460) // 深蓝 - 变体

    // 强调色
    val Accent = Color(0xFFE94560)         // 珊瑚红 - 主强调/支出
    val AccentLight = Color(0xFFFFF0F3)    // 淡红 - 强调背景
    val AccentDark = Color(0xFFD63354)     // 深红 - 强调深色

    // 功能色
    val Success = Color(0xFF00D9A5)        // 青绿色 - 收入/正向
    val SuccessLight = Color(0xFFE6FFF7)   // 淡绿 - 成功背景
    val Warning = Color(0xFFFFB020)        // 琥珀色 - 警告
    val WarningLight = Color(0xFFFFF8E6)   // 淡黄 - 警告背景
    val Info = Color(0xFF667EEA)           // 靛蓝色 - 信息
    val InfoLight = Color(0xFFEEF0FF)      // 淡蓝 - 信息背景
    val Error = Color(0xFFFF4444)          // 红色 - 错误

    // 中性色
    val Background = Color(0xFFFAF7F2)     // 奶油白 - 页面背景
    val Surface = Color(0xFFFFFFFF)        // 纯白 - 卡片/表面
    val Card = Color(0xFFFFFFFF)           // 纯白 - 卡片
    val Border = Color(0xFFEEEBE6)         // 浅灰 - 边框
    val Divider = Color(0xFFF0EDE8)        // 分割线

    // 文字色
    val TextPrimary = Color(0xFF1A1A2E)    // 主文字
    val TextSecondary = Color(0xFF6B7280)  // 次要文字
    val TextMuted = Color(0xFF9CA3AF)      // 提示文字
    val TextOnPrimary = Color(0xFFFFFFFF)  // 主色上的文字
    val TextOnAccent = Color(0xFFFFFFFF)   // 强调色上的文字

    // 渐变色
    val GradientPrimary = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF16213E)
    )
    val GradientAccent = listOf(
        Color(0xFFE94560),
        Color(0xFFD63354)
    )
    val GradientSuccess = listOf(
        Color(0xFF00D9A5),
        Color(0xFF00C896)
    )
    val GradientAssetCard = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF2D2D44),
        Color(0xFF3D3D5C)
    )

    // 图表颜色
    val ChartColors = listOf(
        Color(0xFFE94560),  // 珊瑚红
        Color(0xFF667EEA),  // 靛蓝
        Color(0xFF00D9A5),  // 青绿
        Color(0xFFFFB020),  // 琥珀
        Color(0xFF8B5CF6),  // 紫色
        Color(0xFF06B6D4),  // 青色
        Color(0xFFF97316),  // 橙色
        Color(0xFF84CC16),  // 黄绿
        Color(0xFFEC4899),  // 粉色
        Color(0xFF14B8A6)   // 青绿深
    )

    // 分类背景色
    val CategoryColors = listOf(
        Color(0xFFFFF3E0),  // 淡橙
        Color(0xFFE3F2FD),  // 淡蓝
        Color(0xFFFCE4EC),  // 淡粉
        Color(0xFFF3E5F5),  // 淡紫
        Color(0xFFE8F5E9),  // 淡绿
        Color(0xFFFFF8E1),  // 淡黄
        Color(0xFFE0F7FA),  // 淡青
        Color(0xFFFFEBEE),  // 淡红
        Color(0xFFE8EAF6),  // 淡靛蓝
        Color(0xFFECEFF1)   // 淡灰
    )
}

// 深色主题颜色
object DarkColors {
    val Primary = Color(0xFF1A1A2E)
    val PrimaryLight = Color(0xFF16213E)
    val Background = Color(0xFF0D0D14)
    val Surface = Color(0xFF1A1A2E)
    val Card = Color(0xFF1F1F33)
    val Border = Color(0xFF2D2D44)
    val Divider = Color(0xFF2D2D44)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B0B0)
    val TextMuted = Color(0xFF808080)
}
