package com.example.smartledger.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * 扩展函数集合
 */

// ================= Context扩展 =================

/**
 * 显示短Toast
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 显示长Toast
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ================= String扩展 =================

/**
 * 将十六进制颜色字符串转换为Compose Color
 */
fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * 安全截取字符串
 */
fun String.safeSubstring(start: Int, end: Int): String {
    val safeStart = start.coerceAtLeast(0).coerceAtMost(length)
    val safeEnd = end.coerceAtLeast(safeStart).coerceAtMost(length)
    return substring(safeStart, safeEnd)
}

/**
 * 省略过长的字符串
 */
fun String.ellipsize(maxLength: Int, suffix: String = "..."): String {
    return if (length <= maxLength) this
    else safeSubstring(0, maxLength - suffix.length) + suffix
}

// ================= Double扩展 =================

/**
 * 格式化为金额字符串
 */
fun Double.toAmountString(): String = NumberUtils.formatAmount(this)

/**
 * 格式化为简洁金额字符串
 */
fun Double.toCompactAmountString(): String = NumberUtils.formatAmountCompact(this)

/**
 * 格式化为带符号的金额变化字符串
 */
fun Double.toAmountChangeString(): String = NumberUtils.formatAmountChange(this)

// ================= Float扩展 =================

/**
 * 格式化为百分比字符串
 */
fun Float.toPercentString(): String = NumberUtils.formatPercent(this)

/**
 * 格式化为百分比变化字符串
 */
fun Float.toPercentChangeString(): String = NumberUtils.formatPercentChange(this)

// ================= Long扩展 =================

/**
 * 时间戳转日期字符串
 */
fun Long.toDateString(): String = DateUtils.formatDate(this)

/**
 * 时间戳转日期时间字符串
 */
fun Long.toDateTimeString(): String = DateUtils.formatDateTime(this)

/**
 * 时间戳转月份字符串
 */
fun Long.toMonthString(): String = DateUtils.formatMonth(this)

/**
 * 时间戳转相对时间描述
 */
fun Long.toRelativeTimeString(): String = DateUtils.getRelativeTimeDescription(this)

// ================= Flow扩展 =================

/**
 * Flow安全收集，捕获异常
 */
fun <T> Flow<T>.catchAndReturn(defaultValue: T): Flow<T> {
    return this.catch { emit(defaultValue) }
}

/**
 * Flow映射并处理空值
 */
fun <T, R> Flow<T?>.mapNotNull(transform: (T) -> R): Flow<R?> {
    return this.map { value -> value?.let(transform) }
}

// ================= List扩展 =================

/**
 * 安全获取列表元素
 */
fun <T> List<T>.getOrNull(index: Int): T? {
    return if (index in 0 until size) get(index) else null
}

/**
 * 列表转换为以ID为key的Map
 */
fun <T, K> List<T>.toMapById(keySelector: (T) -> K): Map<K, T> {
    return associateBy(keySelector)
}

// ================= Collection扩展 =================

/**
 * 计算集合元素的总和
 */
fun <T> Collection<T>.sumByDouble(selector: (T) -> Double): Double {
    return fold(0.0) { acc, element -> acc + selector(element) }
}
