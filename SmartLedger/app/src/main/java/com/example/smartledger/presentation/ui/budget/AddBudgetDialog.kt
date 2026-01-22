package com.example.smartledger.presentation.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.example.smartledger.data.local.entity.BudgetPeriod

// iOS风格颜色
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)
private val iOSOrange = Color(0xFFFF9500)
private val iOSPurple = Color(0xFFAF52DE)

/**
 * 添加预算对话框 - iOS卡通风格
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBudgetDialog(
    categories: List<BudgetCategoryItem>,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Long?, amount: Double, period: BudgetPeriod) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var isTotal by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💰", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "添加预算",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // 预算类型选择
                Text(
                    text = "📋 预算类型",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BudgetTypeChip(
                        text = "总预算",
                        icon = "🎯",
                        selected = isTotal,
                        onClick = {
                            isTotal = true
                            selectedCategoryId = null
                        }
                    )
                    BudgetTypeChip(
                        text = "分类预算",
                        icon = "📂",
                        selected = !isTotal,
                        onClick = { isTotal = false }
                    )
                }

                // 分类选择（仅当选择分类预算时显示）
                if (!isTotal) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "📁 选择分类",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E8E93)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                // 预算周期选择
                Text(
                    text = "📅 预算周期",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetPeriod.entries.forEach { period ->
                        PeriodChip(
                            period = period,
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 金额输入
                Text(
                    text = "💵 预算金额",
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
                        Text("输入预算金额", color = Color(0xFFC7C7CC))
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
                    QuickAmountChip("1000", amountText) { amountText = "1000" }
                    QuickAmountChip("3000", amountText) { amountText = "3000" }
                    QuickAmountChip("5000", amountText) { amountText = "5000" }
                    QuickAmountChip("10000", amountText) { amountText = "10000" }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 预算周期提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(iOSAccent.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "选择「${selectedPeriod.displayName}」预算，系统将自动在每个${selectedPeriod.displayName.replace("每", "")}开始时重置统计",
                            fontSize = 12.sp,
                            color = iOSAccent,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        if (isTotal) {
                            onConfirm(null, amount, selectedPeriod)
                        } else if (selectedCategoryId != null) {
                            onConfirm(selectedCategoryId, amount, selectedPeriod)
                        }
                    }
                },
                enabled = amountText.toDoubleOrNull()?.let { it > 0 } == true &&
                        (isTotal || selectedCategoryId != null)
            ) {
                Text(
                    text = "✓ 确定",
                    color = if (amountText.toDoubleOrNull()?.let { it > 0 } == true &&
                        (isTotal || selectedCategoryId != null)) iOSAccent else Color(0xFFC7C7CC),
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

/**
 * 预算类型选择芯片
 */
@Composable
private fun BudgetTypeChip(
    text: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) iOSAccent else Color(0xFFF2F2F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else Color(0xFF8E8E93)
            )
        }
    }
}

/**
 * 预算周期选择芯片
 */
@Composable
private fun PeriodChip(
    period: BudgetPeriod,
    selected: Boolean,
    onClick: () -> Unit
) {
    val periodIcon = when (period) {
        BudgetPeriod.WEEKLY -> "📆"
        BudgetPeriod.BIWEEKLY -> "📅"
        BudgetPeriod.MONTHLY -> "🗓️"
        BudgetPeriod.QUARTERLY -> "📊"
        BudgetPeriod.SEMI_ANNUAL -> "📈"
        BudgetPeriod.YEARLY -> "🎯"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) iOSPurple else Color(0xFFF2F2F7)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = periodIcon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = period.displayName,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else Color(0xFF8E8E93)
            )
        }
    }
}

/**
 * 快捷金额选择芯片
 */
@Composable
private fun QuickAmountChip(
    label: String,
    currentValue: String,
    onClick: () -> Unit
) {
    val isSelected = currentValue == label

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) iOSGreen.copy(alpha = 0.15f)
                else Color(0xFFF2F2F7)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "¥$label",
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) iOSGreen else Color(0xFF8E8E93)
        )
    }
}

/**
 * 分类选择芯片
 */
@Composable
private fun CategoryChip(
    category: BudgetCategoryItem,
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
                    if (selected) {
                        try {
                            Color(android.graphics.Color.parseColor(category.color))
                        } catch (e: Exception) {
                            iOSAccent
                        }
                    } else {
                        Color(0xFFF2F2F7)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Color(0xFF1C1C1E) else Color(0xFF8E8E93)
        )
    }
}
