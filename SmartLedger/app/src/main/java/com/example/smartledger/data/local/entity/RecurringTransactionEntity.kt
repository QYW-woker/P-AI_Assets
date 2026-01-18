package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 固定收支实体
 * 用于管理定期自动记账的交易模板
 */
@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 交易金额
     */
    val amount: Double,

    /**
     * 交易类型
     */
    val type: TransactionType,

    /**
     * 分类ID
     */
    val categoryId: Long,

    /**
     * 账户ID
     */
    val accountId: Long,

    /**
     * 名称/描述
     */
    val name: String,

    /**
     * 备注
     */
    val note: String = "",

    /**
     * 重复频率
     */
    val frequency: RecurringFrequency,

    /**
     * 每月的日期（1-31）或每周的星期几（1-7）
     * 对于MONTHLY: 1-31表示每月几号
     * 对于WEEKLY: 1-7表示周一到周日
     * 对于DAILY: 不使用此字段
     * 对于YEARLY: 使用monthDay和month字段
     */
    val dayOfPeriod: Int = 1,

    /**
     * 年度重复的月份（1-12）
     */
    val month: Int = 1,

    /**
     * 开始日期
     */
    val startDate: Long,

    /**
     * 结束日期（null表示无限期）
     */
    val endDate: Long? = null,

    /**
     * 下次执行日期
     */
    val nextExecutionDate: Long,

    /**
     * 上次执行日期
     */
    val lastExecutionDate: Long? = null,

    /**
     * 是否启用
     */
    val isActive: Boolean = true,

    /**
     * 是否自动执行（否则只是提醒）
     */
    val autoExecute: Boolean = true,

    /**
     * 是否提前提醒
     */
    val remindBefore: Boolean = false,

    /**
     * 提前提醒天数
     */
    val remindDaysBefore: Int = 1,

    /**
     * 已执行次数
     */
    val executionCount: Int = 0,

    /**
     * 创建时间
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * 更新时间
     */
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 重复频率枚举
 */
enum class RecurringFrequency(val label: String) {
    DAILY("每天"),
    WEEKLY("每周"),
    MONTHLY("每月"),
    YEARLY("每年")
}
