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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.example.smartledger.data.local.entity.BankType
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 账户类别
 */
enum class AccountCategory {
    ASSET,      // 资产账户
    CREDIT,     // 信贷账户
    INVESTMENT  // 投资账户
}

/**
 * 账户类型选项
 */
private data class AccountTypeOption(
    val type: AccountType,
    val name: String,
    val icon: String,
    val color: String,
    val category: AccountCategory,
    val needsBank: Boolean = false,
    val needsCardNumber: Boolean = false,
    val needsCreditLimit: Boolean = false
)

private val accountTypeOptions = listOf(
    // 资产账户
    AccountTypeOption(AccountType.CASH, "现金", "💵", "#4CAF50", AccountCategory.ASSET),
    AccountTypeOption(AccountType.BANK, "储蓄卡", "🏦", "#2196F3", AccountCategory.ASSET, needsBank = true, needsCardNumber = true),
    AccountTypeOption(AccountType.ALIPAY, "支付宝", "📱", "#1677FF", AccountCategory.ASSET),
    AccountTypeOption(AccountType.WECHAT, "微信", "💬", "#07C160", AccountCategory.ASSET),
    // 信贷账户
    AccountTypeOption(AccountType.CREDIT_CARD, "信用卡", "💳", "#FF5722", AccountCategory.CREDIT, needsBank = true, needsCardNumber = true, needsCreditLimit = true),
    AccountTypeOption(AccountType.HUABEI, "花呗", "🌸", "#FF6B35", AccountCategory.CREDIT, needsCreditLimit = true),
    AccountTypeOption(AccountType.BAITIAO, "白条", "📋", "#E53935", AccountCategory.CREDIT, needsCreditLimit = true),
    AccountTypeOption(AccountType.LOAN, "贷款", "💰", "#795548", AccountCategory.CREDIT, needsCreditLimit = true),
    AccountTypeOption(AccountType.MORTGAGE, "房贷", "🏠", "#607D8B", AccountCategory.CREDIT, needsCreditLimit = true),
    AccountTypeOption(AccountType.CAR_LOAN, "车贷", "🚗", "#455A64", AccountCategory.CREDIT, needsCreditLimit = true),
    // 投资账户
    AccountTypeOption(AccountType.INVESTMENT_STOCK, "股票", "📈", "#9C27B0", AccountCategory.INVESTMENT),
    AccountTypeOption(AccountType.INVESTMENT_FUND, "基金", "📊", "#673AB7", AccountCategory.INVESTMENT),
    AccountTypeOption(AccountType.INVESTMENT_DEPOSIT, "定期", "🏛️", "#795548", AccountCategory.INVESTMENT)
)

/**
 * 添加账户对话框
 */
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, icon: String, color: String, balance: Double, note: String, bankType: BankType?, cardNumber: String, creditLimit: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<AccountTypeOption?>(null) }
    var balanceText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf<BankType?>(null) }
    var cardNumber by remember { mutableStateOf("") }
    var creditLimitText by remember { mutableStateOf("") }
    var showBankDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
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
                    items(accountTypeOptions.filter { it.category == AccountCategory.ASSET }) { option ->
                        AccountTypeChip(
                            option = option,
                            selected = selectedType == option,
                            onClick = {
                                selectedType = option
                                if (name.isBlank()) name = option.name
                                // 重置银行和卡号
                                if (!option.needsBank) selectedBank = null
                                if (!option.needsCardNumber) cardNumber = ""
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingM))

                // 信贷账户
                Text(
                    text = "信贷账户",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingXS))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    items(accountTypeOptions.filter { it.category == AccountCategory.CREDIT }) { option ->
                        AccountTypeChip(
                            option = option,
                            selected = selectedType == option,
                            onClick = {
                                selectedType = option
                                if (name.isBlank()) name = option.name
                                if (!option.needsBank) selectedBank = null
                                if (!option.needsCardNumber) cardNumber = ""
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
                    items(accountTypeOptions.filter { it.category == AccountCategory.INVESTMENT }) { option ->
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

                // 银行选择（仅银行卡/信用卡显示）
                if (selectedType?.needsBank == true) {
                    Text(
                        text = "选择银行",
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                    Box {
                        OutlinedTextField(
                            value = selectedBank?.bankName ?: "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBankDropdown = true },
                            placeholder = {
                                Text("请选择银行", color = AppColors.TextMuted)
                            },
                            readOnly = true,
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppColors.TextMuted
                                )
                            },
                            singleLine = true
                        )

                        // 点击区域
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showBankDropdown = true }
                        )

                        DropdownMenu(
                            expanded = showBankDropdown,
                            onDismissRequest = { showBankDropdown = false }
                        ) {
                            BankType.entries.forEach { bank ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(bank.icon)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(bank.bankName)
                                        }
                                    },
                                    onClick = {
                                        selectedBank = bank
                                        // 自动更新账户名称
                                        if (selectedType?.type == AccountType.BANK) {
                                            name = "${bank.bankName}储蓄卡"
                                        } else if (selectedType?.type == AccountType.CREDIT_CARD) {
                                            name = "${bank.bankName}信用卡"
                                        }
                                        showBankDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppDimens.SpacingL))
                }

                // 卡号（仅银行卡/信用卡显示）
                if (selectedType?.needsCardNumber == true) {
                    Text(
                        text = "卡号后四位（可选）",
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { value ->
                            if (value.length <= 4 && value.all { it.isDigit() }) {
                                cardNumber = value
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("输入后4位数字", color = AppColors.TextMuted)
                        },
                        prefix = {
                            Text("**** **** **** ", color = AppColors.TextMuted)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(AppDimens.SpacingL))
                }

                // 信用额度（仅信贷账户显示）
                if (selectedType?.needsCreditLimit == true) {
                    Text(
                        text = if (selectedType?.type in listOf(AccountType.MORTGAGE, AccountType.CAR_LOAN, AccountType.LOAN))
                            "贷款总额" else "信用额度",
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { value ->
                            if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                creditLimitText = value
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
                }

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

                // 初始余额/已用额度
                Text(
                    text = if (selectedType?.category == AccountCategory.CREDIT) "当前欠款" else "初始余额",
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
                        val creditLimit = creditLimitText.toDoubleOrNull() ?: 0.0
                        // 信贷账户余额应为负数表示欠款
                        val finalBalance = if (type.category == AccountCategory.CREDIT && balance > 0) -balance else balance
                        onConfirm(name, type.type, type.icon, type.color, finalBalance, note, selectedBank, cardNumber, creditLimit)
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
