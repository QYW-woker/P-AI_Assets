package com.example.smartledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 投资持仓实体
 * 用于记录具体的投资项目明细
 */
@Entity(
    tableName = "investment_holdings",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId")]
)
data class InvestmentHoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 所属投资账户ID
     */
    val accountId: Long,

    /**
     * 投资标的名称（如：贵州茅台、沪深300ETF）
     */
    val name: String,

    /**
     * 投资标的代码（如：600519、510300）
     */
    val code: String = "",

    /**
     * 持仓类型
     */
    val holdingType: HoldingType,

    /**
     * 持有数量/份额
     */
    val quantity: Double,

    /**
     * 买入均价/成本价
     */
    val costPrice: Double,

    /**
     * 当前价格
     */
    val currentPrice: Double,

    /**
     * 投入本金（数量 × 成本价）
     */
    val principal: Double,

    /**
     * 当前市值（数量 × 当前价）
     */
    val marketValue: Double,

    /**
     * 浮动盈亏（市值 - 本金）
     */
    val profitLoss: Double,

    /**
     * 收益率 (%)
     */
    val returnRate: Double,

    /**
     * 首次买入日期
     */
    val firstBuyDate: Long,

    /**
     * 最近更新日期
     */
    val lastUpdateDate: Long = System.currentTimeMillis(),

    /**
     * 备注
     */
    val note: String = "",

    /**
     * 是否启用（隐藏已清仓的持仓）
     */
    val isActive: Boolean = true,

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
 * 持仓类型枚举
 */
enum class HoldingType(val label: String) {
    STOCK("股票"),
    FUND("基金"),
    BOND("债券"),
    DEPOSIT("定期存款"),
    MONEY_FUND("货币基金"),
    FINANCIAL_PRODUCT("理财产品"),
    OTHER("其他")
}
