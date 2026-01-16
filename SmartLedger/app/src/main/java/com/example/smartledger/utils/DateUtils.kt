package com.example.smartledger.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期工具类
 */
object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val monthFormat = SimpleDateFormat("yyyy年M月", Locale.CHINA)
    private val dayFormat = SimpleDateFormat("M月d日", Locale.CHINA)
    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.CHINA)

    /**
     * 获取当前时间戳
     */
    fun now(): Long = System.currentTimeMillis()

    /**
     * 格式化日期为 yyyy-MM-dd
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * 格式化日期时间为 yyyy-MM-dd HH:mm:ss
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /**
     * 格式化为 yyyy年M月
     */
    fun formatMonth(timestamp: Long): String {
        return monthFormat.format(Date(timestamp))
    }

    /**
     * 格式化为 M月d日
     */
    fun formatDay(timestamp: Long): String {
        return dayFormat.format(Date(timestamp))
    }

    /**
     * 格式化为 yyyy-MM
     */
    fun formatYearMonth(timestamp: Long): String {
        return yearMonthFormat.format(Date(timestamp))
    }

    /**
     * 获取今天开始时间戳（00:00:00）
     */
    fun getTodayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取今天结束时间戳（23:59:59）
     */
    fun getTodayEnd(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * 获取本月开始时间戳
     */
    fun getMonthStart(monthOffset: Int = 0): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthOffset)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取本月结束时间戳
     */
    fun getMonthEnd(monthOffset: Int = 0): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthOffset + 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MILLISECOND, -1)
        return calendar.timeInMillis
    }

    /**
     * 获取本周开始时间戳
     */
    fun getWeekStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取本周结束时间戳
     */
    fun getWeekEnd(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * 获取本年开始时间戳
     */
    fun getYearStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取本年结束时间戳
     */
    fun getYearEnd(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * 获取本月剩余天数
     */
    fun getRemainingDaysInMonth(): Int {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return lastDay - currentDay
    }

    /**
     * 判断是否是今天
     */
    fun isToday(timestamp: Long): Boolean {
        return formatDate(timestamp) == formatDate(now())
    }

    /**
     * 判断是否是昨天
     */
    fun isYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return formatDate(timestamp) == formatDate(calendar.timeInMillis)
    }

    /**
     * 获取相对时间描述
     */
    fun getRelativeTimeDescription(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "今天"
            isYesterday(timestamp) -> "昨天"
            else -> formatDay(timestamp)
        }
    }
}
