package com.example.smartledger.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 圆形加载指示器
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 3.dp,
    color: Color = AppColors.Accent
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
        strokeCap = StrokeCap.Round
    )
}

/**
 * 带文字的加载指示器
 */
@Composable
fun LoadingIndicatorWithText(
    text: String = "加载中...",
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = AppColors.Accent
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoadingIndicator(size = size, color = color)
        Spacer(modifier = Modifier.height(AppDimens.SpacingM))
        Text(
            text = text,
            style = AppTypography.BodyMedium,
            color = AppColors.TextSecondary
        )
    }
}

/**
 * 全屏加载
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    text: String = "加载中...",
    backgroundColor: Color = AppColors.Background.copy(alpha = 0.9f)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicatorWithText(text = text)
    }
}

/**
 * 覆盖式加载（半透明遮罩）
 */
@Composable
fun OverlayLoading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    text: String = "加载中..."
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(AppShapes.Large)
                    .background(AppColors.Card)
                    .padding(AppDimens.PaddingXXL),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicatorWithText(text = text)
            }
        }
    }
}

/**
 * 线性进度指示器
 */
@Composable
fun LinearLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = AppColors.Accent,
    trackColor: Color = AppColors.Border
) {
    LinearProgressIndicator(
        modifier = modifier
            .height(AppDimens.ProgressBarHeightSmall)
            .clip(AppShapes.Full),
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

/**
 * 带进度的线性指示器
 */
@Composable
fun LinearProgressIndicatorWithProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = AppColors.Accent,
    trackColor: Color = AppColors.Border
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .height(AppDimens.ProgressBarHeight)
            .clip(AppShapes.Full),
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

/**
 * 脉冲点加载动画
 */
@Composable
fun PulsingDotsIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = AppColors.Accent,
    dotCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingDots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 100,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotScale$index"
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

/**
 * 骨架屏加载效果
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .clip(AppShapes.Small)
            .background(AppColors.Border.copy(alpha = alpha))
    )
}

/**
 * 列表项骨架屏
 */
@Composable
fun ListItemSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(AppDimens.PaddingL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像骨架
        ShimmerBox(
            modifier = Modifier
                .size(AppDimens.AvatarMedium)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(AppDimens.SpacingM))

        Column(modifier = Modifier.weight(1f)) {
            // 标题骨架
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 副标题骨架
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
            )
        }

        // 金额骨架
        ShimmerBox(
            modifier = Modifier
                .width(60.dp)
                .height(18.dp)
        )
    }
}

/**
 * 卡片骨架屏
 */
@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(AppShapes.Large)
            .background(AppColors.Card)
            .padding(AppDimens.CardPadding)
    ) {
        Column {
            ShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingM))
            ShimmerBox(
                modifier = Modifier
                    .width(150.dp)
                    .height(28.dp)
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
            )
        }
    }
}
