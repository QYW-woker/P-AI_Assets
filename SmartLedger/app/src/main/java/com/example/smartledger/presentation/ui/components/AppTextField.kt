package com.example.smartledger.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 基础输入框
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            label = label?.let { { Text(it, style = AppTypography.BodySmall) } },
            placeholder = placeholder?.let { { Text(it, style = AppTypography.BodyMedium, color = AppColors.TextMuted) } },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = AppColors.TextSecondary
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = AppColors.TextSecondary
                        )
                    }
                }
            },
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction?.invoke() },
                onSearch = { onImeAction?.invoke() },
                onGo = { onImeAction?.invoke() }
            ),
            shape = AppShapes.Medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Accent,
                unfocusedBorderColor = AppColors.Border,
                errorBorderColor = AppColors.Error,
                focusedLabelColor = AppColors.Accent,
                unfocusedLabelColor = AppColors.TextSecondary,
                cursorColor = AppColors.Accent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent
            ),
            textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary)
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                style = AppTypography.Caption.copy(color = AppColors.Error),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 密码输入框
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = label?.let { { Text(it, style = AppTypography.BodySmall) } },
            placeholder = placeholder?.let { { Text(it, style = AppTypography.BodyMedium, color = AppColors.TextMuted) } },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = AppColors.TextSecondary
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction?.invoke() }
            ),
            shape = AppShapes.Medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Accent,
                unfocusedBorderColor = AppColors.Border,
                errorBorderColor = AppColors.Error,
                focusedLabelColor = AppColors.Accent,
                unfocusedLabelColor = AppColors.TextSecondary,
                cursorColor = AppColors.Accent
            ),
            textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary)
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                style = AppTypography.Caption.copy(color = AppColors.Error),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 数字输入框 - 用于金额输入
 */
@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "0.00",
    prefix: String = "¥",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // 只允许数字和小数点
                val filtered = newValue.filter { it.isDigit() || it == '.' }
                // 确保只有一个小数点
                val parts = filtered.split(".")
                val result = when {
                    parts.size <= 1 -> filtered
                    else -> "${parts[0]}.${parts.drop(1).joinToString("").take(2)}"
                }
                onValueChange(result)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = label?.let { { Text(it, style = AppTypography.BodySmall) } },
            placeholder = { Text(placeholder, style = AppTypography.BodyMedium, color = AppColors.TextMuted) },
            prefix = { Text(prefix, style = AppTypography.BodyMedium, color = AppColors.TextSecondary) },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            shape = AppShapes.Medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Accent,
                unfocusedBorderColor = AppColors.Border,
                errorBorderColor = AppColors.Error,
                focusedLabelColor = AppColors.Accent,
                unfocusedLabelColor = AppColors.TextSecondary,
                cursorColor = AppColors.Accent
            ),
            textStyle = AppTypography.NumberMedium.copy(color = AppColors.TextPrimary)
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                style = AppTypography.Caption.copy(color = AppColors.Error),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 搜索输入框
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    leadingIcon: ImageVector? = null,
    onSearch: (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = AppTypography.BodyMedium, color = AppColors.TextMuted) },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = AppColors.TextSecondary
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch?.invoke() }
        ),
        shape = AppShapes.Full,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppColors.Card,
            unfocusedContainerColor = AppColors.Card,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = AppColors.Accent
        ),
        textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary)
    )
}

/**
 * 多行文本输入框
 */
@Composable
fun MultilineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    minLines: Int = 3,
    maxLines: Int = 5,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = label?.let { { Text(it, style = AppTypography.BodySmall) } },
        placeholder = placeholder?.let { { Text(it, style = AppTypography.BodyMedium, color = AppColors.TextMuted) } },
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        shape = AppShapes.Medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.Accent,
            unfocusedBorderColor = AppColors.Border,
            focusedLabelColor = AppColors.Accent,
            unfocusedLabelColor = AppColors.TextSecondary,
            cursorColor = AppColors.Accent
        ),
        textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary)
    )
}
