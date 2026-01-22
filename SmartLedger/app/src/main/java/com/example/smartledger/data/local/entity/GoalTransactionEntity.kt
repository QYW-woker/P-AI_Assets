package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 储蓄目标交易类型
 */
enum class GoalTransactionType {
    DEPOSIT,    // 存入
    WITHDRAW    // 取出
}

/**
 * 储蓄目标交易记录实体
 * 用于记录储蓄目标的全生命周期操作历史
 */
@Entity(
    tableName = "goal_transactions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["createdAt"])
    ]
)
data class GoalTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 关联的目标ID */
    val goalId: Long,

    /** 交易类型：存入或取出 */
    val type: GoalTransactionType,

    /** 交易金额（正数） */
    val amount: Double,

    /** 交易后的余额 */
    val balanceAfter: Double,

    /** 备注 */
    val note: String = "",

    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis()
)
