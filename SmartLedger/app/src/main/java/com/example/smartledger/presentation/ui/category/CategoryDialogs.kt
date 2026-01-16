package com.example.smartledger.presentation.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 支出分类图标选项
 */
private val expenseIcons = listOf(
    "🍜", "🍔", "☕", "🍰",  // 餐饮
    "🚗", "🚌", "✈️", "⛽",  // 交通
    "🛒", "👗", "👟", "💄",  // 购物
    "🏠", "💡", "💧", "🔧",  // 居住
    "🎮", "🎬", "🎵", "📚",  // 娱乐教育
    "💊", "🏥", "🎁", "📱",  // 医疗通讯
    "👶", "🐕", "💈", "📦"   // 其他
)

/**
 * 收入分类图标选项
 */
private val incomeIcons = listOf(
    "💰", "💵", "💳", "🏆",  // 工资奖金
    "💼", "📈", "📊", "🏦",  // 副业投资
    "🧧", "🎁", "💎", "🏠",  // 礼金其他
    "💻", "📱", "🎯", "⭐"
)

/**
 * 颜色选项
 */
private val colorOptions = listOf(
    "#FF6B6B", "#FF8E72", "#FFA94D", "#FFD43B",
    "#69DB7C", "#4ECDC4", "#45B7D1", "#4DABF7",
    "#748FFC", "#9775FA", "#DA77F2", "#F783AC",
    "#AEB6BF", "#5D6D7E", "#795548", "#37474F"
)

/**
 * 添加分类对话框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryDialog(
    categoryType: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember {
        mutableStateOf(
            if (categoryType == TransactionType.EXPENSE) "📦" else "💰"
        )
    }
    var selectedColor by remember { mutableStateOf("#4ECDC4") }

    val icons = if (categoryType == TransactionType.EXPENSE) expenseIcons else incomeIcons

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (categoryType == TransactionType.EXPENSE) "添加支出分类" else "添加收入分类",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                // 分类名称
                Text(
                    text = "分类名称",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("输入分类名称", color = AppColors.TextMuted)
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 选择图标
                Text(
                    text = "选择图标",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    icons.forEach { icon ->
                        IconOption(
                            icon = icon,
                            color = selectedColor,
                            selected = selectedIcon == icon,
                            onClick = { selectedIcon = icon }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 选择颜色
                Text(
                    text = "选择颜色",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    colorOptions.forEach { color ->
                        ColorOption(
                            color = color,
                            selected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 预览
                Text(
                    text = "预览",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewColor = try {
                        Color(android.graphics.Color.parseColor(selectedColor))
                    } catch (e: Exception) {
                        AppColors.Primary
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(previewColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedIcon,
                            style = AppTypography.TitleSmall
                        )
                    }

                    Spacer(modifier = Modifier.padding(AppDimens.SpacingM))

                    Text(
                        text = name.ifBlank { "分类名称" },
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon, selectedColor)
                    }
                },
                enabled = name.isNotBlank()
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
private fun IconOption(
    icon: String,
    color: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = try {
        if (selected) Color(android.graphics.Color.parseColor(color)) else AppColors.Card
    } catch (e: Exception) {
        if (selected) AppColors.Primary else AppColors.Card
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = AppTypography.BodyMedium
        )
    }
}

@Composable
private fun ColorOption(
    color: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        AppColors.Primary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (selected) {
                    Modifier.border(3.dp, Color.White, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    )
}

/**
 * 编辑分类对话框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditCategoryDialog(
    category: CategoryUiModel,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var selectedIcon by remember { mutableStateOf(category.icon) }
    var selectedColor by remember { mutableStateOf(category.color) }

    val icons = if (category.type == TransactionType.EXPENSE) expenseIcons else incomeIcons

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑分类",
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column {
                // 分类名称
                Text(
                    text = "分类名称",
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

                // 选择图标
                Text(
                    text = "选择图标",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    icons.forEach { icon ->
                        IconOption(
                            icon = icon,
                            color = selectedColor,
                            selected = selectedIcon == icon,
                            onClick = { selectedIcon = icon }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 选择颜色
                Text(
                    text = "选择颜色",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
                ) {
                    colorOptions.forEach { color ->
                        ColorOption(
                            color = color,
                            selected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingL))

                // 预览
                Text(
                    text = "预览",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(AppDimens.SpacingS))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewColor = try {
                        Color(android.graphics.Color.parseColor(selectedColor))
                    } catch (e: Exception) {
                        AppColors.Primary
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(previewColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedIcon,
                            style = AppTypography.TitleSmall
                        )
                    }

                    Spacer(modifier = Modifier.padding(AppDimens.SpacingM))

                    Text(
                        text = name.ifBlank { "分类名称" },
                        style = AppTypography.BodyMedium,
                        color = AppColors.TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon, selectedColor)
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
