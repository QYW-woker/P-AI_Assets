package com.example.smartledger.presentation.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 分类数据用于预算选择
 */
data class BudgetCategoryItem(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String
)

/**
 * 添加预算对话框
 */
@Composable
fun AddBudgetDialog(
    categories: List<BudgetCategoryItem>,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Long?, amount: Double) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var isTotal by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加预算",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                // 预算类型选择
                Text(
                    text = "预算类型",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
                ) {
                    BudgetTypeChip(
                        text = "总预算",
                        selected = isTotal,
                        onClick = {
                            isTotal = true
                            selectedCategoryId = null
                        }
                    )
                    BudgetTypeChip(
                        text = "分类预算",
                        selected = !isTotal,
                        onClick = { isTotal = false }
                    )
                }

                // 分类选择（仅当选择分类预算时显示）
                if (!isTotal) {
                    Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                    Text(
                        text = "选择分类",
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                    ) {
                        items(categories) { category ->
                            CategoryChip(
                                category = category,
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 金额输入
                Text(
                    text = "预算金额",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        // 只允许数字和小数点
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入金额", color = AppColors.TextMuted)
                    },
                    prefix = {
                        Text("¥", color = AppColors.TextSecondary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        if (isTotal) {
                            onConfirm(null, amount)
                        } else if (selectedCategoryId != null) {
                            onConfirm(selectedCategoryId, amount)
                        }
                    }
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true &&
                        (isTotal || selectedCategoryId != null)
            ) {
                Text("确定", color = AppColors.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextMuted)
            }
        }
    )
}

@Composable
private fun BudgetTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(AppColors.run { androidx.compose.foundation.shape.RoundedCornerShape(20.dp) })
            .background(if (selected) AppColors.Accent else AppColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = AppTypography.LabelMedium,
            color = if (selected) Color.White else AppColors.TextSecondary
        )
    }
}

@Composable
private fun CategoryChip(
    category: BudgetCategoryItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(AppDimens.SpacingXS),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        try {
                            Color(android.graphics.Color.parseColor(category.color))
                        } catch (e: Exception) {
                            AppColors.Primary
                        }
                    } else {
                        AppColors.Card
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                style = AppTypography.TitleSmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            style = AppTypography.Caption,
            color = if (selected) AppColors.TextPrimary else AppColors.TextMuted
        )
    }
}
