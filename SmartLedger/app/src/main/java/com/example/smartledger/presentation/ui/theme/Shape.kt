package com.example.smartledger.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 应用形状/圆角系统
 */
object AppShapes {
    // 圆角
    val RadiusNone = 0.dp
    val RadiusXSmall = 4.dp
    val RadiusSmall = 8.dp
    val RadiusMedium = 12.dp
    val RadiusLarge = 16.dp
    val RadiusXLarge = 24.dp
    val RadiusXXLarge = 32.dp
    val RadiusFull = 100.dp

    // 形状
    val None = RoundedCornerShape(RadiusNone)
    val XSmall = RoundedCornerShape(RadiusXSmall)
    val Small = RoundedCornerShape(RadiusSmall)
    val Medium = RoundedCornerShape(RadiusMedium)
    val Large = RoundedCornerShape(RadiusLarge)
    val XLarge = RoundedCornerShape(RadiusXLarge)
    val XXLarge = RoundedCornerShape(RadiusXXLarge)
    val Full = RoundedCornerShape(RadiusFull)

    // 特殊形状 - 顶部圆角
    val TopLarge = RoundedCornerShape(
        topStart = RadiusLarge,
        topEnd = RadiusLarge,
        bottomStart = RadiusNone,
        bottomEnd = RadiusNone
    )

    val TopXLarge = RoundedCornerShape(
        topStart = RadiusXLarge,
        topEnd = RadiusXLarge,
        bottomStart = RadiusNone,
        bottomEnd = RadiusNone
    )

    // 底部圆角
    val BottomLarge = RoundedCornerShape(
        topStart = RadiusNone,
        topEnd = RadiusNone,
        bottomStart = RadiusLarge,
        bottomEnd = RadiusLarge
    )

    val BottomXLarge = RoundedCornerShape(
        topStart = RadiusNone,
        topEnd = RadiusNone,
        bottomStart = RadiusXLarge,
        bottomEnd = RadiusXLarge
    )
}

/**
 * 应用尺寸规范
 */
object AppDimens {
    // 间距
    val SpacingXXS = 2.dp
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 12.dp
    val SpacingL = 16.dp
    val SpacingXL = 24.dp
    val SpacingXXL = 32.dp
    val SpacingXXXL = 48.dp

    // 内边距
    val PaddingXS = 4.dp
    val PaddingS = 8.dp
    val PaddingM = 12.dp
    val PaddingL = 16.dp
    val PaddingXL = 20.dp
    val PaddingXXL = 24.dp

    // 卡片
    val CardPadding = 20.dp
    val CardPaddingSmall = 16.dp
    val CardElevation = 2.dp
    val CardElevationLarge = 4.dp

    // 图标尺寸
    val IconXSmall = 16.dp
    val IconSmall = 20.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val IconXLarge = 48.dp
    val IconXXLarge = 64.dp

    // 按钮
    val ButtonHeight = 48.dp
    val ButtonHeightSmall = 36.dp
    val ButtonHeightLarge = 56.dp
    val ButtonMinWidth = 64.dp

    // 输入框
    val TextFieldHeight = 56.dp
    val TextFieldHeightSmall = 48.dp

    // 底部导航
    val BottomNavHeight = 80.dp
    val BottomNavIconSize = 24.dp

    // 顶部栏
    val TopBarHeight = 56.dp

    // 分割线
    val DividerThickness = 1.dp

    // 头像
    val AvatarSmall = 32.dp
    val AvatarMedium = 48.dp
    val AvatarLarge = 64.dp

    // 列表项
    val ListItemHeight = 56.dp
    val ListItemHeightLarge = 72.dp

    // 边框宽度
    val BorderWidth = 1.dp
    val BorderWidthMedium = 1.5.dp
    val BorderWidthThick = 2.dp

    // 进度条
    val ProgressBarHeight = 8.dp
    val ProgressBarHeightSmall = 4.dp
    val ProgressBarHeightLarge = 12.dp
}

/**
 * Material3 Shapes配置
 */
val SmartLedgerShapes = Shapes(
    extraSmall = AppShapes.XSmall,
    small = AppShapes.Small,
    medium = AppShapes.Medium,
    large = AppShapes.Large,
    extraLarge = AppShapes.XLarge
)
