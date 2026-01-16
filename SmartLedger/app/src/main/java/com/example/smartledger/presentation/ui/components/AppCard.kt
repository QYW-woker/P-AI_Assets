package com.example.smartledger.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes

/**
 * 基础卡片组件
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.Large,
    backgroundColor: Color = AppColors.Card,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = AppDimens.CardElevation,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color(0x0A000000),
            spotColor = Color(0x1A000000)
        )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            border = border
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            border = border
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPadding),
                content = content
            )
        }
    }
}

/**
 * 小尺寸卡片
 */
@Composable
fun AppCardSmall(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.Medium,
    backgroundColor: Color = AppColors.Card,
    elevation: Dp = AppDimens.CardElevation,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color(0x0A000000),
            spotColor = Color(0x1A000000)
        )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPaddingSmall),
                content = content
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPaddingSmall),
                content = content
            )
        }
    }
}

/**
 * 渐变背景卡片 - 用于资产展示等
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = AppColors.GradientAssetCard,
    shape: Shape = AppShapes.Large,
    elevation: Dp = AppDimens.CardElevationLarge,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x1A000000),
                spotColor = Color(0x33000000)
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            )
            .then(
                if (onClick != null) {
                    Modifier
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimens.CardPadding),
            content = content
        )
    }
}

/**
 * 边框卡片 - 用于选中状态展示
 */
@Composable
fun OutlinedAppCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.Medium,
    backgroundColor: Color = AppColors.Card,
    borderColor: Color = AppColors.Border,
    borderWidth: Dp = AppDimens.BorderWidth,
    isSelected: Boolean = false,
    selectedBorderColor: Color = AppColors.Accent,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val actualBorderColor = if (isSelected) selectedBorderColor else borderColor
    val actualBorderWidth = if (isSelected) AppDimens.BorderWidthMedium else borderWidth

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(actualBorderWidth, actualBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPaddingSmall),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(actualBorderWidth, actualBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPaddingSmall),
                content = content
            )
        }
    }
}
