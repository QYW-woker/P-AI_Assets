package com.example.smartledger.presentation.ui.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartledger.presentation.ui.theme.AppIcons

// iOS风格颜色
private val iOSCardBackground = Color.White
private val iOSAccent = Color(0xFF007AFF)
private val iOSGreen = Color(0xFF34C759)

/**
 * 货币选项
 */
private val currencyOptions = listOf(
    "CNY ¥" to "人民币",
    "USD $" to "美元",
    "EUR €" to "欧元",
    "GBP £" to "英镑",
    "JPY ¥" to "日元",
    "KRW ₩" to "韩元",
    "HKD $" to "港币",
    "TWD $" to "台币"
)

/**
 * 星期选项
 */
private val weekDayOptions = listOf(
    "周一", "周二", "周三", "周四", "周五", "周六", "周日"
)

/**
 * 货币选择对话框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = AppIcons.Settings.CURRENCY, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "选择货币单位",
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
                currencyOptions.forEach { (currency, name) ->
                    CurrencyOptionItem(
                        currency = currency,
                        name = name,
                        selected = selectedCurrency == currency,
                        onClick = { selectedCurrency = currency }
                    )
                    if (currency != currencyOptions.last().first) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedCurrency) }) {
                Text(
                    text = "${AppIcons.Action.SAVE} 确定",
                    color = iOSAccent,
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
private fun CurrencyOptionItem(
    currency: String,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) iOSAccent.copy(alpha = 0.1f) else Color(0xFFF2F2F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currency,
                    fontSize = 16.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) iOSAccent else Color(0xFF1C1C1E)
                )
                Text(
                    text = name,
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
            }
            if (selected) {
                Text(
                    text = AppIcons.Status.SUCCESS,
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * 月份起始日选择对话框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthStartDayDialog(
    currentDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(currentDay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = AppIcons.Settings.DATE, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "选择月份起始日",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "选择每月账单统计的起始日期",
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..28).forEach { day ->
                        DayChip(
                            day = day,
                            selected = selectedDay == day,
                            onClick = { selectedDay = day }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(iOSAccent.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = AppIcons.Status.TIP, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "每月${selectedDay}日开始统计当月账单",
                            fontSize = 13.sp,
                            color = iOSAccent
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDay) }) {
                Text(
                    text = "${AppIcons.Action.SAVE} 确定",
                    color = iOSAccent,
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
private fun DayChip(
    day: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) iOSAccent else Color(0xFFF2F2F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF8E8E93)
        )
    }
}

/**
 * 星期起始日选择对话框
 */
@Composable
fun WeekStartDayDialog(
    currentDay: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedDay by remember { mutableStateOf(currentDay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = AppIcons.Settings.WEEK, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "选择每周起始日",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Column {
                weekDayOptions.forEach { day ->
                    WeekDayOptionItem(
                        day = day,
                        selected = selectedDay == day,
                        onClick = { selectedDay = day }
                    )
                    if (day != weekDayOptions.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDay) }) {
                Text(
                    text = "${AppIcons.Action.SAVE} 确定",
                    color = iOSAccent,
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
private fun WeekDayOptionItem(
    day: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) iOSAccent.copy(alpha = 0.1f) else Color(0xFFF2F2F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) iOSAccent else Color(0xFF1C1C1E)
            )
            if (selected) {
                Text(
                    text = AppIcons.Status.SUCCESS,
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * 提醒时间选择对话框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderTimeDialog(
    currentTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val timeOptions = listOf(
        "08:00", "09:00", "10:00", "12:00",
        "14:00", "16:00", "18:00", "19:00",
        "20:00", "21:00", "22:00", "23:00"
    )

    var selectedTime by remember { mutableStateOf(currentTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = AppIcons.Settings.REMINDER, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "选择提醒时间",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "每天在选定时间提醒你记账",
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeOptions.forEach { time ->
                        TimeChip(
                            time = time,
                            selected = selectedTime == time,
                            onClick = { selectedTime = time }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTime) }) {
                Text(
                    text = "${AppIcons.Action.SAVE} 确定",
                    color = iOSAccent,
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
private fun TimeChip(
    time: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) iOSGreen else Color(0xFFF2F2F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF8E8E93)
        )
    }
}

/**
 * 确认清除数据对话框
 */
@Composable
fun ConfirmClearDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = iOSCardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = AppIcons.Status.WARNING, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "确认清除数据",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF3B30)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "此操作将删除所有记账数据，包括：",
                    fontSize = 15.sp,
                    color = Color(0xFF1C1C1E)
                )

                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    "所有交易记录",
                    "所有预算设置",
                    "所有储蓄目标",
                    "所有自定义分类"
                ).forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "•", color = Color(0xFFFF3B30), fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF3B30).copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "此操作不可撤销！",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3B30)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "确认删除",
                    color = Color(0xFFFF3B30),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "取消",
                    color = iOSAccent,
                    fontSize = 16.sp
                )
            }
        }
    )
}
