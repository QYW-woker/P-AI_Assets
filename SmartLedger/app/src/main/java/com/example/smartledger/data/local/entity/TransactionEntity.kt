package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 交易类型枚举
 */
enum class TransactionType {
    EXPENSE,    // 支出
    INCOME,     // 收入
    TRANSFER    // 转账
}

/**
 * 交易记录实体
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["categoryId"]),
        Index(value = ["accountId"]),
        Index(value = ["type", "date"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: TransactionType,

    val amount: Double,

    val categoryId: Long,

    val accountId: Long,

    val toAccountId: Long? = null,  // 转账目标账户

    val note: String = "",

    val tags: String = "",  // JSON数组格式

    val date: Long,  // 交易日期时间戳

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
