package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 账户类型枚举
 */
enum class AccountType {
    // 资产账户
    CASH,               // 现金
    BANK,               // 银行卡（储蓄卡）
    ALIPAY,             // 支付宝
    WECHAT,             // 微信
    // 信用/负债账户
    CREDIT_CARD,        // 信用卡
    HUABEI,             // 花呗
    BAITIAO,            // 白条
    LOAN,               // 贷款
    MORTGAGE,           // 房贷
    CAR_LOAN,           // 车贷
    // 投资账户
    INVESTMENT_STOCK,   // 股票账户
    INVESTMENT_FUND,    // 基金账户
    INVESTMENT_DEPOSIT  // 定期存款
}

/**
 * 银行枚举
 */
enum class BankType(val bankName: String, val icon: String) {
    ICBC("工商银行", "🏦"),
    CCB("建设银行", "🏦"),
    ABC("农业银行", "🏦"),
    BOC("中国银行", "🏦"),
    BOCOM("交通银行", "🏦"),
    CMB("招商银行", "🏦"),
    CITIC("中信银行", "🏦"),
    CEB("光大银行", "🏦"),
    CMBC("民生银行", "🏦"),
    PAB("平安银行", "🏦"),
    SPDB("浦发银行", "🏦"),
    CIB("兴业银行", "🏦"),
    HXB("华夏银行", "🏦"),
    GDB("广发银行", "🏦"),
    PSBC("邮储银行", "🏦"),
    OTHER("其他银行", "🏦")
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

    // 银行相关字段
    val bankType: BankType? = null,

    val cardNumber: String = "",  // 卡号（支持后4位）

    // 信贷账户相关字段
    val creditLimit: Double = 0.0,  // 信用额度/贷款总额

    val billingDay: Int = 1,  // 账单日

    val dueDay: Int = 20,  // 还款日

    val isIncludeInTotal: Boolean = true,

    val sortOrder: Int = 0,

    val isActive: Boolean = true
)
