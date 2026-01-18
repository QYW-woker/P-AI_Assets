package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 月度资产快照实体
 * 每月末自动记录各账户余额状态
 */
@Entity(tableName = "monthly_snapshots")
data class MonthlySnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 年份
     */
    val year: Int,

    /**
     * 月份 (1-12)
     */
    val month: Int,

    /**
     * 快照日期时间戳
     */
    val snapshotDate: Long,

    /**
     * 总资产（所有计入总资产的账户余额之和）
     */
    val totalAssets: Double,

    /**
     * 总负债（信用卡等负债账户）
     */
    val totalLiabilities: Double,

    /**
     * 净资产（总资产 - 总负债）
     */
    val netWorth: Double,

    /**
     * 现金类资产（现金+储蓄卡+支付宝+微信）
     */
    val cashAssets: Double,

    /**
     * 投资类资产总额（股票+基金+定期）
     */
    val investmentAssets: Double,

    /**
     * 投资本金总额
     */
    val investmentPrincipal: Double,

    /**
     * 投资收益
     */
    val investmentReturn: Double,

    /**
     * 本月收入
     */
    val monthlyIncome: Double,

    /**
     * 本月支出
     */
    val monthlyExpense: Double,

    /**
     * 本月结余
     */
    val monthlyBalance: Double,

    /**
     * 储蓄率 (%)
     */
    val savingsRate: Double,

    /**
     * 账户快照JSON（各账户余额明细）
     * 格式: [{"accountId":1,"name":"现金","type":"CASH","balance":1000.0},...]
     */
    val accountsJson: String,

    /**
     * 创建时间
     */
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 账户快照数据类（用于JSON序列化）
 */
data class AccountSnapshot(
    val accountId: Long,
    val name: String,
    val type: String,
    val balance: Double,
    val initialBalance: Double
)
