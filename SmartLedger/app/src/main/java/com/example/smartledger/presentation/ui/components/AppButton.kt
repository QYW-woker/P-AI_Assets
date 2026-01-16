package com.example.smartledger.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 主要按钮 - 强调色背景
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppDimens.ButtonHeight)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier),
        enabled = enabled && !isLoading,
        shape = AppShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Accent,
            contentColor = AppColors.TextOnAccent,
            disabledContainerColor = AppColors.Accent.copy(alpha = 0.5f),
            disabledContentColor = AppColors.TextOnAccent.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppColors.TextOnAccent,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = AppTypography.LabelLarge
                )
            }
        }
    }
}

/**
 * 次要按钮 - 深色背景
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppDimens.ButtonHeight)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier),
        enabled = enabled && !isLoading,
        shape = AppShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = AppColors.TextOnPrimary,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f),
            disabledContentColor = AppColors.TextOnPrimary.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppColors.TextOnPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = AppTypography.LabelLarge
                )
            }
        }
    }
}

/**
 * 边框按钮
 */
@Composable
fun OutlinedAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    borderColor: Color = AppColors.Accent,
    contentColor: Color = AppColors.Accent,
    fullWidth: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(AppDimens.ButtonHeight)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier),
        enabled = enabled && !isLoading,
        shape = AppShapes.Medium,
        border = BorderStroke(
            width = AppDimens.BorderWidthMedium,
            color = if (enabled) borderColor else borderColor.copy(alpha = 0.5f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = AppTypography.LabelLarge
                )
            }
        }
    }
}

/**
 * 文本按钮
 */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color = AppColors.Accent
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = AppTypography.LabelMedium
            )
        }
    }
}

/**
 * 成功按钮 - 绿色
 */
@Composable
fun SuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppDimens.ButtonHeight)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier),
        enabled = enabled && !isLoading,
        shape = AppShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Success,
            contentColor = Color.White,
            disabledContainerColor = AppColors.Success.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = AppTypography.LabelLarge
                )
            }
        }
    }
}

/**
 * 危险按钮 - 红色
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppDimens.ButtonHeight)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier),
        enabled = enabled && !isLoading,
        shape = AppShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Error,
            contentColor = Color.White,
            disabledContainerColor = AppColors.Error.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = AppTypography.LabelLarge
                )
            }
        }
    }
}

/**
 * 小按钮
 */
@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = AppColors.Accent,
    contentColor: Color = AppColors.TextOnAccent
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(AppDimens.ButtonHeightSmall),
        enabled = enabled,
        shape = AppShapes.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = AppTypography.LabelSmall
        )
    }
}
