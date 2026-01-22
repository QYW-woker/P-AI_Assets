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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// iOS风格颜色
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)

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
 * 添加目标对话框 - iOS卡通风格
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
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "创建储蓄目标",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Column {
                // 目标名称
                Text(
                    text = "📝 目标名称",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("例如：买房首付", color = Color(0xFFC7C7CC))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSAccent,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 目标图标选择
                Text(
                    text = "🎨 选择图标",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                // 目标金额
                Text(
                    text = "💵 目标金额",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入目标金额", color = Color(0xFFC7C7CC))
                    },
                    prefix = {
                        Text(
                            "¥",
                            color = iOSAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSAccent,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 目标期限
                Text(
                    text = "⏰ 计划完成时间",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                // 备注
                Text(
                    text = "💬 备注（可选）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("添加备注", color = Color(0xFFC7C7CC))
                    },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSAccent,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
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
                Text(
                    text = "✓ 创建",
                    color = if (name.isNotBlank() && amountText.toDoubleOrNull()?.let { it > 0 } == true)
                        iOSAccent else Color(0xFFC7C7CC),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "取消",
                    color = Color(0xFF8E8E93),
                    fontSize = 16.sp
                )
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
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (selected) iOSAccent.copy(alpha = 0.15f)
                    else Color(0xFFF2F2F7)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) iOSAccent else Color(0xFF8E8E93)
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
            .background(if (selected) iOSAccent else Color(0xFFF2F2F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFF8E8E93)
        )
    }
}

/**
 * 存入金额对话框 - iOS卡通风格
 */
@Composable
fun DepositToGoalDialog(
    goalName: String,
    currentAmount: Double,
    targetAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val remaining = targetAmount - currentAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "存入金额",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Column {
                // 目标信息
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(iOSGreen.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = goalName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 还需存入 ¥${String.format("%.2f", remaining)}",
                            fontSize = 13.sp,
                            color = iOSGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 输入金额
                Text(
                    text = "💵 存入金额",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入存入金额", color = Color(0xFFC7C7CC))
                    },
                    prefix = {
                        Text(
                            "¥",
                            color = iOSGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSGreen,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 快捷金额选择
                Text(
                    text = "⚡ 快捷选择",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(16.dp))

                // 备注
                Text(
                    text = "📝 备注（可选）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("例如：工资存入", color = Color(0xFFC7C7CC))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSGreen,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount, noteText)
                    }
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text(
                    text = "✓ 存入",
                    color = if (amountText.toDoubleOrNull()?.let { it > 0 } == true)
                        iOSGreen else Color(0xFFC7C7CC),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "取消",
                    color = Color(0xFF8E8E93),
                    fontSize = 16.sp
                )
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
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) iOSGreen.copy(alpha = 0.15f)
                else Color(0xFFF2F2F7)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (label == "全部") "🎯 $label" else "¥$label",
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) iOSGreen else Color(0xFF8E8E93)
        )
    }
}

/**
 * 取出金额对话框 - iOS卡通风格
 */
@Composable
fun WithdrawFromGoalDialog(
    goalName: String,
    currentAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💸",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "取出金额",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Column {
                // 目标信息
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(iOSOrange.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = goalName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💰 当前已存 ¥${String.format("%.2f", currentAmount)}",
                            fontSize = 13.sp,
                            color = iOSOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 输入金额
                Text(
                    text = "💵 取出金额",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            val amount = value.toDoubleOrNull() ?: 0.0
                            if (amount <= currentAmount) {
                                amountText = value
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入取出金额", color = Color(0xFFC7C7CC))
                    },
                    prefix = {
                        Text(
                            "¥",
                            color = iOSOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSOrange,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 备注
                Text(
                    text = "📝 备注（可选）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("例如：急用取出", color = Color(0xFFC7C7CC))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSOrange,
                        unfocusedBorderColor = Color(0xFFE5E5EA)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF3CD))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "⚠️ 取出后会减少目标进度",
                        fontSize = 12.sp,
                        color = Color(0xFF856404)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0 && amount <= currentAmount) {
                        onConfirm(amount, noteText)
                    }
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 && it <= currentAmount } == true
            ) {
                Text(
                    text = "✓ 取出",
                    color = if (amountText.toDoubleOrNull()?.let { it > 0 && it <= currentAmount } == true)
                        iOSOrange else Color(0xFFC7C7CC),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "取消",
                    color = Color(0xFF8E8E93),
                    fontSize = 16.sp
                )
            }
        }
    )
}
