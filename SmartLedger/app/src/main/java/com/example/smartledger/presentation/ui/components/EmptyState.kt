package com.example.smartledger.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 空状态组件
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    iconTint: Color = AppColors.TextMuted,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppDimens.PaddingXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = iconTint
        )

        Spacer(modifier = Modifier.height(AppDimens.SpacingL))

        Text(
            text = title,
            style = AppTypography.TitleMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            Text(
                text = description,
                style = AppTypography.BodyMedium,
                color = AppColors.TextMuted,
                textAlign = TextAlign.Center
            )
        }

        if (action != null) {
            Spacer(modifier = Modifier.height(AppDimens.SpacingXL))
            action()
        }
    }
}

/**
 * 全屏空状态
 */
@Composable
fun FullScreenEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    iconTint: Color = AppColors.TextMuted,
    action: @Composable (() -> Unit)? = null
) {
    EmptyState(
        icon = icon,
        title = title,
        modifier = modifier.fillMaxSize(),
        description = description,
        iconTint = iconTint,
        action = action
    )
}

/**
 * 无数据状态
 */
@Composable
fun NoDataState(
    modifier: Modifier = Modifier,
    title: String = "暂无数据",
    description: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Inbox,
        title = title,
        modifier = modifier,
        description = description,
        action = action
    )
}

/**
 * 无搜索结果状态
 */
@Composable
fun NoSearchResultState(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    action: @Composable (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.SearchOff,
        title = "未找到结果",
        modifier = modifier,
        description = if (searchQuery.isNotEmpty()) "没有找到与\"$searchQuery\"相关的内容" else null,
        action = action
    )
}

/**
 * 网络错误状态
 */
@Composable
fun NetworkErrorState(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.SignalWifiOff,
        title = "网络连接失败",
        modifier = modifier,
        description = "请检查网络连接后重试",
        iconTint = AppColors.Warning,
        action = if (onRetry != null) {
            {
                PrimaryButton(
                    text = "重试",
                    onClick = onRetry
                )
            }
        } else null
    )
}

/**
 * 服务器错误状态
 */
@Composable
fun ServerErrorState(
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.CloudOff,
        title = "服务异常",
        modifier = modifier,
        description = errorMessage ?: "服务器开小差了，请稍后再试",
        iconTint = AppColors.Error,
        action = if (onRetry != null) {
            {
                PrimaryButton(
                    text = "重试",
                    onClick = onRetry
                )
            }
        } else null
    )
}

/**
 * 通用错误状态
 */
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    title: String = "出错了",
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Error,
        title = title,
        modifier = modifier,
        description = errorMessage,
        iconTint = AppColors.Error,
        action = if (onRetry != null) {
            {
                PrimaryButton(
                    text = "重试",
                    onClick = onRetry
                )
            }
        } else null
    )
}

/**
 * 无交易记录状态
 */
@Composable
fun NoTransactionsState(
    modifier: Modifier = Modifier,
    onAddTransaction: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Inbox,
        title = "还没有记账",
        modifier = modifier,
        description = "点击下方按钮开始记录第一笔账",
        action = if (onAddTransaction != null) {
            {
                PrimaryButton(
                    text = "开始记账",
                    onClick = onAddTransaction
                )
            }
        } else null
    )
}

/**
 * 无预算状态
 */
@Composable
fun NoBudgetState(
    modifier: Modifier = Modifier,
    onAddBudget: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Inbox,
        title = "还没有设置预算",
        modifier = modifier,
        description = "设置预算帮助你更好地管理支出",
        action = if (onAddBudget != null) {
            {
                PrimaryButton(
                    text = "设置预算",
                    onClick = onAddBudget
                )
            }
        } else null
    )
}

/**
 * 无目标状态
 */
@Composable
fun NoGoalsState(
    modifier: Modifier = Modifier,
    onAddGoal: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Inbox,
        title = "还没有设置目标",
        modifier = modifier,
        description = "设置储蓄目标，一步步实现你的梦想",
        action = if (onAddGoal != null) {
            {
                PrimaryButton(
                    text = "创建目标",
                    onClick = onAddGoal
                )
            }
        } else null
    )
}
