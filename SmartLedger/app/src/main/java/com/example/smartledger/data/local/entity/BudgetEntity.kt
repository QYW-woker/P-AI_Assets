package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 预算周期枚举
 */
enum class BudgetPeriod(val displayName: String, val days: Int) {
    WEEKLY("每周", 7),         // 周预算
    BIWEEKLY("每两周", 14),    // 双周预算
    MONTHLY("每月", 30),       // 月度预算
    QUARTERLY("每季度", 90),   // 季度预算
    SEMI_ANNUAL("每半年", 180),// 半年预算
    YEARLY("每年", 365);       // 年度预算

    companion object {
        fun fromName(name: String): BudgetPeriod {
            return entries.find { it.name == name } ?: MONTHLY
        }
    }
}

/**
 * 预算实体
 */
@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["period"])
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 分类ID，null表示总预算 */
    val categoryId: Long? = null,

    /** 预算金额 */
    val amount: Double,

    /** 预算周期 */
    val period: BudgetPeriod,

    /** 开始日期（毫秒） */
    val startDate: Long,

    /** 结束日期（毫秒），用于自动计算周期结束 */
    val endDate: Long = 0,

    /** 提醒阈值（默认80%） */
    val alertThreshold: Float = 0.8f,

    /** 预算名称（可选，用于自定义名称） */
    val name: String = "",

    /** 是否自动续期 */
    val autoRenew: Boolean = true,

    /** 是否激活 */
    val isActive: Boolean = true,

    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis()
)
