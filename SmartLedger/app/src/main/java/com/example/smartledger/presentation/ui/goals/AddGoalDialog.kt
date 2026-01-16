package com.example.smartledger.presentation.ui.goals

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 目标图标选项
 */
private val goalIcons = listOf(
    "🏠" to "买房",
    "🚗" to "买车",
    "✈️" to "旅行",
    "📱" to "数码",
    "💍" to "婚礼",
    "🎓" to "教育",
    "🏥" to "医疗",
    "💰" to "储蓄",
    "🎯" to "其他"
)

/**
 * 添加目标对话框
 */
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, targetAmount: Double, deadline: Long?, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("💰") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDeadlineMonths by remember { mutableStateOf<Int?>(null) }

    val deadlineOptions = listOf(
        3 to "3个月",
        6 to "6个月",
        12 to "1年",
        24 to "2年",
        36 to "3年",
        60 to "5年"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加储蓄目标",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                // 目标名称
                Text(
                    text = "目标名称",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("例如：买房首付", color = AppColors.TextMuted)
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 目标图标选择
                Text(
                    text = "选择图标",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    items(goalIcons) { (icon, label) ->
                        GoalIconChip(
                            icon = icon,
                            label = label,
                            selected = selectedIcon == icon,
                            onClick = { selectedIcon = icon }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 目标金额
                Text(
                    text = "目标金额",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入目标金额", color = AppColors.TextMuted)
                    },
                    prefix = {
                        Text("¥", color = AppColors.TextSecondary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 目标期限
                Text(
                    text = "计划完成时间",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    items(deadlineOptions) { (months, label) ->
                        DeadlineChip(
                            label = label,
                            selected = selectedDeadlineMonths == months,
                            onClick = {
                                selectedDeadlineMonths = if (selectedDeadlineMonths == months) null else months
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 备注
                Text(
                    text = "备注（可选）",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("添加备注", color = AppColors.TextMuted)
                    },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (name.isNotBlank() && amount != null && amount > 0) {
                        val deadline = selectedDeadlineMonths?.let {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.MONTH, it)
                            calendar.timeInMillis
                        }
                        onConfirm(name, selectedIcon, amount, deadline, note)
                    }
                },
                enabled = name.isNotBlank() && amountText.toDoubleOrNull()?.let { it > 0 } == true
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
private fun GoalIconChip(
    icon: String,
    label: String,
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
                .background(if (selected) AppColors.AccentLight else AppColors.Card),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = AppTypography.TitleSmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = AppTypography.Caption,
            color = if (selected) AppColors.Accent else AppColors.TextMuted
        )
    }
}

@Composable
private fun DeadlineChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) AppColors.Accent else AppColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.LabelMedium,
            color = if (selected) Color.White else AppColors.TextSecondary
        )
    }
}

/**
 * 存入金额对话框
 */
@Composable
fun DepositToGoalDialog(
    goalName: String,
    currentAmount: Double,
    targetAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val remaining = targetAmount - currentAmount
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "存入金额",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = goalName,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Text(
                    text = "还需存入 ¥${String.format("%.2f", remaining)}",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入存入金额", color = AppColors.TextMuted)
                    },
                    prefix = {
                        Text("¥", color = AppColors.TextSecondary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                // 快捷金额选择
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    QuickAmountChip("100", amountText) { amountText = "100" }
                    QuickAmountChip("500", amountText) { amountText = "500" }
                    QuickAmountChip("1000", amountText) { amountText = "1000" }
                    if (remaining > 0 && remaining < 10000) {
                        QuickAmountChip("全部", amountText) {
                            amountText = String.format("%.2f", remaining)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                    }
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("确定存入", color = AppColors.Accent)
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
private fun QuickAmountChip(
    label: String,
    currentValue: String,
    onClick: () -> Unit
) {
    val isSelected = currentValue == label || (label != "全部" && currentValue == "$label.00")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AppColors.AccentLight else AppColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (label == "全部") label else "¥$label",
            style = AppTypography.Caption,
            color = if (isSelected) AppColors.Accent else AppColors.TextSecondary
        )
    }
}
