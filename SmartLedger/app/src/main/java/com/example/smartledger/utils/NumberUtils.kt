package com.example.smartledger.utils

import java.text.DecimalFormat

/**
 * 数字工具类
 */
object NumberUtils {

    private val amountFormat = DecimalFormat("#,##0.00")
    private val percentFormat = DecimalFormat("0.0%")
    private val integerFormat = DecimalFormat("#,##0")

    /**
     * 格式化金额（带千位分隔符）
     * 例如：1234567.89 -> 1,234,567.89
     */
    fun formatAmount(amount: Double): String {
        return amountFormat.format(amount)
    }

    /**
     * 格式化金额（简洁版）
     * 例如：12345.67 -> 1.23万
     */
    fun formatAmountCompact(amount: Double): String {
        return when {
            amount >= 100000000 -> String.format("%.2f亿", amount / 100000000)
            amount >= 10000 -> String.format("%.2f万", amount / 10000)
            else -> formatAmount(amount)
        }
    }

    /**
     * 格式化百分比
     * 例如：0.125 -> 12.5%
     */
    fun formatPercent(value: Float): String {
        return percentFormat.format(value)
    }

    /**
     * 格式化百分比（不带%符号）
     */
    fun formatPercentValue(value: Float): String {
        return String.format("%.1f", value * 100)
    }

    /**
     * 格式化整数（带千位分隔符）
     */
    fun formatInteger(value: Int): String {
        return integerFormat.format(value)
    }

    /**
     * 格式化整数（带千位分隔符）
     */
    fun formatInteger(value: Long): String {
        return integerFormat.format(value)
    }

    /**
     * 安全解析金额字符串
     */
    fun parseAmount(text: String): Double {
        return try {
            text.replace(",", "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * 格式化带符号的金额变化
     * 正数显示 +，负数显示 -
     */
    fun formatAmountChange(amount: Double): String {
        val prefix = if (amount >= 0) "+" else ""
        return "$prefix${formatAmount(amount)}"
    }

    /**
     * 格式化带符号的百分比变化
     */
    fun formatPercentChange(value: Float): String {
        val prefix = if (value >= 0) "+" else ""
        return "$prefix${formatPercentValue(value)}%"
    }

    /**
     * 计算百分比
     */
    fun calculatePercent(part: Double, total: Double): Float {
        return if (total > 0) (part / total).toFloat() else 0f
    }

    /**
     * 保留小数位数
     */
    fun round(value: Double, decimals: Int = 2): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(value * multiplier) / multiplier
    }

    /**
     * 验证金额字符串格式是否有效
     */
    fun isValidAmount(text: String): Boolean {
        if (text.isEmpty()) return true

        // 检查是否为有效数字格式
        val regex = Regex("^\\d*\\.?\\d{0,2}$")
        return regex.matches(text)
    }

    /**
     * 限制金额输入（最多2位小数）
     */
    fun limitAmountInput(text: String): String {
        if (text.isEmpty()) return text

        // 移除前导零（但保留 "0." 的情况）
        var result = text
        if (result.length > 1 && result.startsWith("0") && !result.startsWith("0.")) {
            result = result.trimStart('0')
            if (result.isEmpty() || result.startsWith(".")) {
                result = "0$result"
            }
        }

        // 确保只有一个小数点
        val parts = result.split(".")
        if (parts.size > 2) {
            result = "${parts[0]}.${parts.drop(1).joinToString("")}"
        }

        // 限制小数位数为2位
        if (parts.size == 2 && parts[1].length > 2) {
            result = "${parts[0]}.${parts[1].take(2)}"
        }

        return result
    }
}
