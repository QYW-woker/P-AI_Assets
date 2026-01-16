package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 预算周期枚举
 */
enum class BudgetPeriod {
    MONTHLY,   // 月度预算
    YEARLY     // 年度预算
}

/**
 * 预算实体
 */
@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["categoryId"])
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val categoryId: Long? = null,  // null表示总预算

    val amount: Double,

    val period: BudgetPeriod,

    val startDate: Long,

    val alertThreshold: Float = 0.8f,  // 80%提醒

    val isActive: Boolean = true
)
