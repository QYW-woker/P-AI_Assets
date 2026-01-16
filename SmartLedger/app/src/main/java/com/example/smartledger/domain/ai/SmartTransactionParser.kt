package com.example.smartledger.domain.ai

import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 智能交易解析器 - 从自然语言中解析交易信息
 */
@Singleton
class SmartTransactionParser @Inject constructor() {

    /**
     * 解析自然语言输入，提取交易信息
     */
    fun parse(input: String, categories: List<CategoryEntity>): ParseResult {
        val cleanInput = input.trim().lowercase()

        // 1. 解析金额
        val amount = parseAmount(cleanInput)
        if (amount == null || amount <= 0) {
            return ParseResult.Failure("无法识别金额，请使用格式如「35元」或「花了35」")
        }

        // 2. 判断收支类型
        val transactionType = parseTransactionType(cleanInput)

        // 3. 匹配分类
        val matchedCategory = matchCategory(cleanInput, categories, transactionType)

        // 4. 提取备注
        val note = extractNote(cleanInput)

        return ParseResult.Success(
            ParsedTransactionData(
                amount = amount,
                type = transactionType,
                categoryId = matchedCategory?.id,
                categoryName = matchedCategory?.name ?: getDefaultCategoryName(transactionType),
                note = note,
                confidence = calculateConfidence(amount, matchedCategory)
            )
        )
    }

    private fun parseAmount(input: String): Double? {
        val patterns = listOf(
            // 匹配 "35.5元" "35元" "35.50块"
            Regex("(\\d+\\.?\\d*)\\s*[元块]"),
            // 匹配 "¥35.5" "￥35"
            Regex("[¥￥](\\d+\\.?\\d*)"),
            // 匹配 "花了35" "花了35.5"
            Regex("花了\\s*(\\d+\\.?\\d*)"),
            // 匹配 "35rmb" "35RMB"
            Regex("(\\d+\\.?\\d*)\\s*rmb", RegexOption.IGNORE_CASE),
            // 匹配 "收入5000" "入账5000"
            Regex("(?:收入|入账|到账|工资|薪资)\\s*(\\d+\\.?\\d*)"),
            // 匹配末尾的数字
            Regex("(\\d+\\.?\\d*)$")
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull()
            }
        }
        return null
    }

    private fun parseTransactionType(input: String): TransactionType {
        val incomeKeywords = listOf(
            "收入", "入账", "到账", "工资", "薪资", "奖金", "红包", "收到",
            "转入", "报销", "利息", "分红", "退款", "返现", "兼职", "副业"
        )

        return if (incomeKeywords.any { input.contains(it) }) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }
    }

    private fun matchCategory(
        input: String,
        categories: List<CategoryEntity>,
        type: TransactionType
    ): CategoryEntity? {
        // 分类关键词映射
        val categoryKeywords = mapOf(
            // 餐饮
            "餐饮" to listOf("午餐", "晚餐", "早餐", "吃饭", "外卖", "饭", "餐", "火锅", "烧烤", "奶茶", "咖啡", "零食", "水果", "菜", "肉", "米", "面", "饮料", "美团", "饿了么", "小吃", "夜宵", "快餐", "食堂"),
            // 交通
            "交通" to listOf("打车", "地铁", "公交", "滴滴", "高铁", "火车", "飞机", "机票", "车票", "加油", "停车", "过路费", "出租车", "单车", "骑车", "uber", "曹操", "嘀嗒"),
            // 购物
            "购物" to listOf("淘宝", "京东", "拼多多", "买", "购物", "网购", "超市", "商场", "衣服", "鞋", "包", "化妆品", "护肤", "日用品", "生活用品"),
            // 娱乐
            "娱乐" to listOf("电影", "游戏", "KTV", "酒吧", "演唱会", "音乐", "视频", "会员", "Netflix", "爱奇艺", "腾讯", "B站", "抖音", "直播"),
            // 居住
            "居住" to listOf("房租", "水费", "电费", "燃气", "物业", "网费", "宽带", "暖气", "维修", "装修", "家具", "家电"),
            // 医疗
            "医疗" to listOf("医院", "药", "看病", "挂号", "体检", "牙", "眼镜", "保健", "医保"),
            // 教育
            "教育" to listOf("学费", "书", "课程", "培训", "考试", "学习", "教材", "网课", "补习"),
            // 通讯
            "通讯" to listOf("话费", "流量", "手机", "电话", "短信"),
            // 工资
            "工资" to listOf("工资", "薪资", "月薪", "发工资"),
            // 奖金
            "奖金" to listOf("奖金", "年终奖", "提成", "绩效"),
            // 红包
            "红包" to listOf("红包", "转账", "微信", "支付宝收到"),
            // 投资
            "投资" to listOf("理财", "基金", "股票", "利息", "分红", "收益")
        )

        // 按类型筛选分类
        val filteredCategories = categories.filter { it.type == type }

        // 尝试匹配关键词
        for (category in filteredCategories) {
            val keywords = categoryKeywords[category.name] ?: continue
            if (keywords.any { input.contains(it) }) {
                return category
            }
        }

        // 尝试直接匹配分类名
        for (category in filteredCategories) {
            if (input.contains(category.name.lowercase())) {
                return category
            }
        }

        // 返回默认分类或第一个匹配类型的分类
        return filteredCategories.firstOrNull { it.name.contains("其他") }
            ?: filteredCategories.firstOrNull()
    }

    private fun extractNote(input: String): String {
        // 移除金额相关的文字，保留主要内容
        var note = input
            .replace(Regex("\\d+\\.?\\d*\\s*[元块]"), "")
            .replace(Regex("[¥￥]\\d+\\.?\\d*"), "")
            .replace(Regex("花了\\s*\\d+\\.?\\d*"), "")
            .replace(Regex("\\d+\\.?\\d*\\s*rmb", RegexOption.IGNORE_CASE), "")
            .replace(Regex("收入|入账|到账|支出|花费|消费"), "")
            .trim()

        // 如果备注为空或太短，使用原始输入
        if (note.length < 2) {
            note = input.take(50)
        }

        return note.take(100)
    }

    private fun getDefaultCategoryName(type: TransactionType): String {
        return if (type == TransactionType.EXPENSE) "其他支出" else "其他收入"
    }

    private fun calculateConfidence(amount: Double?, category: CategoryEntity?): Float {
        var confidence = 0.5f
        if (amount != null && amount > 0) confidence += 0.3f
        if (category != null) confidence += 0.2f
        return confidence.coerceAtMost(1f)
    }
}

/**
 * 解析结果
 */
sealed class ParseResult {
    data class Success(val data: ParsedTransactionData) : ParseResult()
    data class Failure(val message: String) : ParseResult()
}

/**
 * 解析出的交易数据
 */
data class ParsedTransactionData(
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long?,
    val categoryName: String,
    val note: String,
    val confidence: Float
)
