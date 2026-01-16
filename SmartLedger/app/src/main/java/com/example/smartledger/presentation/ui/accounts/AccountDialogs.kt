package com.example.smartledger.presentation.ui.accounts

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
import com.example.smartledger.data.local.entity.AccountType
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 账户类型选项
 */
private data class AccountTypeOption(
    val type: AccountType,
    val name: String,
    val icon: String,
    val color: String,
    val isInvestment: Boolean
)

private val accountTypeOptions = listOf(
    AccountTypeOption(AccountType.CASH, "现金", "💵", "#4CAF50", false),
    AccountTypeOption(AccountType.BANK, "银行卡", "🏦", "#2196F3", false),
    AccountTypeOption(AccountType.ALIPAY, "支付宝", "📱", "#1677FF", false),
    AccountTypeOption(AccountType.WECHAT, "微信", "💬", "#07C160", false),
    AccountTypeOption(AccountType.CREDIT_CARD, "信用卡", "💳", "#FF5722", false),
    AccountTypeOption(AccountType.INVESTMENT_STOCK, "股票", "📈", "#9C27B0", true),
    AccountTypeOption(AccountType.INVESTMENT_FUND, "基金", "📊", "#673AB7", true),
    AccountTypeOption(AccountType.INVESTMENT_DEPOSIT, "定期", "🏛️", "#795548", true)
)

/**
 * 添加账户对话框
 */
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, icon: String, color: String, balance: Double, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<AccountTypeOption?>(null) }
    var balanceText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加账户",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                // 账户类型选择
                Text(
                    text = "选择账户类型",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                // 资产账户
                Text(
                    text = "资产账户",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingXS))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    items(accountTypeOptions.filter { !it.isInvestment }) { option ->
                        AccountTypeChip(
                            option = option,
                            selected = selectedType == option,
                            onClick = {
                                selectedType = option
                                if (name.isBlank()) name = option.name
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                // 投资账户
                Text(
                    text = "投资账户",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingXS))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    items(accountTypeOptions.filter { it.isInvestment }) { option ->
                        AccountTypeChip(
                            option = option,
                            selected = selectedType == option,
                            onClick = {
                                selectedType = option
                                if (name.isBlank()) name = option.name
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 账户名称
                Text(
                    text = "账户名称",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入账户名称", color = AppColors.TextMuted)
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 初始余额
                Text(
                    text = "初始余额",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^-?\\d*\\.?\\d{0,2}$"))) {
                            balanceText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("0.00", color = AppColors.TextMuted)
                    },
                    prefix = {
                        Text("¥", color = AppColors.TextSecondary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

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
                    selectedType?.let { type ->
                        val balance = balanceText.toDoubleOrNull() ?: 0.0
                        onConfirm(name, type.type, type.icon, type.color, balance, note)
                    }
                },
                enabled = name.isNotBlank() && selectedType != null
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
private fun AccountTypeChip(
    option: AccountTypeOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = try {
        Color(android.graphics.Color.parseColor(option.color))
    } catch (e: Exception) {
        AppColors.Primary
    }

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
                .background(if (selected) chipColor else AppColors.Card),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = option.icon,
                style = AppTypography.TitleSmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = option.name,
            style = AppTypography.Caption,
            color = if (selected) chipColor else AppColors.TextMuted
        )
    }
}

/**
 * 编辑账户对话框
 */
@Composable
fun EditAccountDialog(
    account: AccountUiModel,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var note by remember { mutableStateOf(account.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑账户",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.icon,
                        style = AppTypography.TitleMedium
                    )
                    Spacer(modifier = Modifier.padding(AppDimens.SpacingS))
                    Text(
                        text = account.typeName,
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 账户名称
                Text(
                    text = "账户名称",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 备注
                Text(
                    text = "备注",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, account.icon, account.color, note)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存", color = AppColors.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextMuted)
            }
        }
    )
}

/**
 * 更新余额对话框
 */
@Composable
fun UpdateBalanceDialog(
    accountName: String,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (newBalance: Double) -> Unit
) {
    var balanceText by remember { mutableStateOf(String.format("%.2f", currentBalance)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "更新余额",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = accountName,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Text(
                    text = "当前余额: ¥${String.format("%.2f", currentBalance)}",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                Text(
                    text = "新余额",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^-?\\d*\\.?\\d{0,2}$"))) {
                            balanceText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
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
                    val newBalance = balanceText.toDoubleOrNull()
                    if (newBalance != null) {
                        onConfirm(newBalance)
                    }
                },
                enabled = balanceText.toDoubleOrNull() != null
            ) {
                Text("更新", color = AppColors.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextMuted)
            }
        }
    )
}
