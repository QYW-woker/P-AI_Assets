package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 账户类型枚举
 */
enum class AccountType {
    CASH,               // 现金
    BANK,               // 银行卡
    ALIPAY,             // 支付宝
    WECHAT,             // 微信
    CREDIT_CARD,        // 信用卡
    INVESTMENT_STOCK,   // 股票账户
    INVESTMENT_FUND,    // 基金账户
    INVESTMENT_DEPOSIT  // 定期存款
}

/**
 * 账户实体
 */
@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["type"])
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val type: AccountType,

    val icon: String,

    val color: String,

    val balance: Double = 0.0,

    val initialBalance: Double = 0.0,

    val currency: String = "CNY",

    val note: String = "",

    val isIncludeInTotal: Boolean = true,

    val sortOrder: Int = 0,

    val isActive: Boolean = true
)
