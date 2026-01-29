package com.example.smartledger.domain.ai

import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.TransactionType
import java.util.Calendar
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 智能交易解析器 - 从自然语言中解析交易信息
 * 支持多种输入格式：文字、语音转文字、图片OCR结果
 */
@Singleton
class SmartTransactionParser @Inject constructor() {

    /**
     * 解析自然语言输入，提取交易信息
     */
    fun parse(input: String, categories: List<CategoryEntity>): ParseResult {
        val cleanInput = input.trim()
        val lowerInput = cleanInput.lowercase()

        // 1. 解析金额（支持中文数字）
        val amount = parseAmount(lowerInput)
        if (amount == null || amount <= 0) {
            return ParseResult.Failure("无法识别金额，请使用格式如「35元」或「花了35」")
        }

        // 2. 判断收支类型
        val transactionType = parseTransactionType(lowerInput)

        // 3. 匹配分类
        val matchedCategory = matchCategory(lowerInput, categories, transactionType)

        // 4. 提取时间（增强版）
        val parsedTime = parseTimeEnhanced(lowerInput)

        // 5. 提取备注
        val note = extractNote(cleanInput, matchedCategory?.name)

        // 6. 计算置信度
        val confidence = calculateConfidence(amount, matchedCategory, parsedTime != System.currentTimeMillis())

        return ParseResult.Success(
            ParsedTransactionData(
                amount = amount,
                type = transactionType,
                categoryId = matchedCategory?.id,
                categoryName = matchedCategory?.name ?: getDefaultCategoryName(transactionType),
                note = note,
                timestamp = parsedTime,
                confidence = confidence
            )
        )
    }

    /**
     * 批量解析多条记录
     */
    fun parseBatch(input: String, categories: List<CategoryEntity>): List<ParseResult> {
        // 按换行、中文分号、句号分割
        val lines = input.split(Regex("[\\n；;。]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 2 }

        return lines.map { parse(it, categories) }
    }

    /**
     * 从图片OCR结果解析交易（支持小票格式）
     */
    fun parseReceipt(ocrText: String, categories: List<CategoryEntity>): ParseResult {
        val lines = ocrText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        // 尝试提取总金额
        var totalAmount: Double? = null
        var merchantName = ""
        var items = mutableListOf<String>()

        for (line in lines) {
            val lower = line.lowercase()

            // 寻找总金额关键词
            if (lower.contains("合计") || lower.contains("总计") || lower.contains("实付") ||
                lower.contains("应付") || lower.contains("total") || lower.contains("amount")) {
                val amount = parseAmount(lower)
                if (amount != null && amount > 0) {
                    totalAmount = amount
                }
            }

            // 提取商家名称（通常在前几行）
            if (merchantName.isEmpty() && lines.indexOf(line) < 3) {
                if (line.length in 2..20 && !line.contains(Regex("\\d{4}"))) {
                    merchantName = line
                }
            }

            // 收集商品项
            if (line.contains(Regex("\\d+\\.?\\d*")) && line.length > 3) {
                items.add(line)
            }
        }

        // 如果没找到总金额，尝试找最大的金额
        if (totalAmount == null) {
            totalAmount = lines.mapNotNull { parseAmount(it.lowercase()) }
                .filter { it > 0 }
                .maxOrNull()
        }

        if (totalAmount == null || totalAmount <= 0) {
            return ParseResult.Failure("无法从小票中识别金额")
        }

        // 根据商家名或商品匹配分类
        val combinedText = (merchantName + " " + items.joinToString(" ")).lowercase()
        val matchedCategory = matchCategory(combinedText, categories, TransactionType.EXPENSE)

        val note = if (merchantName.isNotEmpty()) merchantName else items.firstOrNull() ?: "小票消费"

        return ParseResult.Success(
            ParsedTransactionData(
                amount = totalAmount,
                type = TransactionType.EXPENSE,
                categoryId = matchedCategory?.id,
                categoryName = matchedCategory?.name ?: "购物",
                note = note.take(50),
                timestamp = System.currentTimeMillis(),
                confidence = 0.7f
            )
        )
    }

    /**
     * 增强版时间解析
     */
    private fun parseTimeEnhanced(input: String): Long {
        val calendar = Calendar.getInstance()

        // 1. 解析具体日期 "3号" "15号" "1日"
        val dayPattern = Regex("(\\d{1,2})[号日]")
        dayPattern.find(input)?.let { match ->
            val day = match.groupValues[1].toIntOrNull()
            if (day != null && day in 1..31) {
                calendar.set(Calendar.DAY_OF_MONTH, day)
            }
        }

        // 2. 解析星期 "周一" "星期五" "上周三"
        val weekdayMap = mapOf(
            "一" to Calendar.MONDAY, "二" to Calendar.TUESDAY,
            "三" to Calendar.WEDNESDAY, "四" to Calendar.THURSDAY,
            "五" to Calendar.FRIDAY, "六" to Calendar.SATURDAY,
            "日" to Calendar.SUNDAY, "天" to Calendar.SUNDAY
        )

        val weekPattern = Regex("(上周|上个星期|这周|这个星期|本周)?[周星期]([一二三四五六日天])")
        weekPattern.find(input)?.let { match ->
            val prefix = match.groupValues[1]
            val dayChar = match.groupValues[2]
            val targetDay = weekdayMap[dayChar] ?: return@let

            // 计算目标日期
            val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
            var diff = targetDay - currentDay

            when {
                prefix.contains("上") -> {
                    // 上周
                    diff -= 7
                }
                diff > 0 -> {
                    // 如果目标日期在今天之后，默认认为是上周的
                    diff -= 7
                }
            }

            calendar.add(Calendar.DAY_OF_MONTH, diff)
        }

        // 3. 解析相对日期
        when {
            input.contains("昨天") || input.contains("昨日") -> {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            input.contains("前天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, -2)
            }
            input.contains("大前天") -> {
                calendar.add(Calendar.DAY_OF_MONTH, -3)
            }
            input.contains("上个月") -> {
                calendar.add(Calendar.MONTH, -1)
            }
        }

        // 4. 解析时段
        when {
            input.contains("凌晨") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 2)
            }
            input.contains("早上") || input.contains("早餐") || input.contains("早饭") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 8)
            }
            input.contains("上午") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 10)
            }
            input.contains("中午") || input.contains("午餐") || input.contains("午饭") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 12)
            }
            input.contains("下午") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 15)
            }
            input.contains("傍晚") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 17)
            }
            input.contains("晚上") || input.contains("晚餐") || input.contains("晚饭") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 19)
            }
            input.contains("夜宵") || input.contains("宵夜") || input.contains("深夜") -> {
                calendar.set(Calendar.HOUR_OF_DAY, 22)
            }
        }

        // 5. 解析具体时间 "8点" "下午3点" "14:30"
        val timePattern1 = Regex("(\\d{1,2})[点时](?:(\\d{1,2})分?)?")
        val timePattern2 = Regex("(\\d{1,2}):(\\d{2})")

        timePattern1.find(input)?.let { match ->
            var hour = match.groupValues[1].toIntOrNull() ?: return@let
            val minute = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0

            // 根据上下文调整12小时制
            if (hour < 12 && (input.contains("下午") || input.contains("晚上"))) {
                hour += 12
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        }

        timePattern2.find(input)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return@let
            val minute = match.groupValues[2].toIntOrNull() ?: return@let
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        }

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * 解析金额（支持中文数字）
     */
    private fun parseAmount(input: String): Double? {
        // 先尝试转换中文数字
        val convertedInput = convertChineseNumber(input)

        val patterns = listOf(
            // 匹配 "35.5元" "35元" "35.50块" "35块钱"
            Regex("(\\d+\\.?\\d*)\\s*[元块]钱?"),
            // 匹配 "¥35.5" "￥35"
            Regex("[¥￥](\\d+\\.?\\d*)"),
            // 匹配 "花了35" "花了35.5" "花费35"
            Regex("(?:花了|花费|消费|支出|付了|付款)\\s*(\\d+\\.?\\d*)"),
            // 匹配 "35rmb" "35RMB"
            Regex("(\\d+\\.?\\d*)\\s*rmb", RegexOption.IGNORE_CASE),
            // 匹配 "收入5000" "入账5000" "到账5000"
            Regex("(?:收入|入账|到账|工资|薪资|收到|转入)\\s*(\\d+\\.?\\d*)"),
            // 匹配 "合计35" "总计35" "共35"
            Regex("(?:合计|总计|总共|一共|共)\\s*(\\d+\\.?\\d*)"),
            // 匹配中间的金额 如 "午餐35元很好吃"
            Regex("(?<=\\D)(\\d+\\.?\\d*)(?=\\s*[元块])"),
            // 匹配末尾的数字
            Regex("(\\d+\\.?\\d*)$")
        )

        for (pattern in patterns) {
            val match = pattern.find(convertedInput)
            if (match != null) {
                val amount = match.groupValues[1].toDoubleOrNull()
                if (amount != null && amount > 0) {
                    return amount
                }
            }
        }
        return null
    }

    /**
     * 转换中文数字
     */
    private fun convertChineseNumber(input: String): String {
        val chineseDigits = mapOf(
            '零' to 0, '一' to 1, '二' to 2, '两' to 2, '三' to 3, '四' to 4,
            '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9
        )

        val chineseUnits = mapOf(
            '十' to 10, '百' to 100, '千' to 1000, '万' to 10000
        )

        var result = input

        // 简单的中文数字模式
        val patterns = listOf(
            // "三十五" -> 35
            Regex("([一二两三四五六七八九])?十([一二三四五六七八九])?"),
            // "一百二十" -> 120
            Regex("([一二两三四五六七八九])百([一二两三四五六七八九])?十?([一二三四五六七八九])?"),
            // "五块" -> 5
            Regex("([零一二两三四五六七八九十百千万]+)[块元]")
        )

        // 转换 "三十五" 等模式
        val simplePattern = Regex("([一二两三四五六七八九])?十([一二三四五六七八九])?")
        result = simplePattern.replace(result) { match ->
            val tens = chineseDigits[match.groupValues[1].firstOrNull()] ?: 1
            val ones = chineseDigits[match.groupValues[2].firstOrNull()] ?: 0
            (tens * 10 + ones).toString()
        }

        // 转换单个中文数字
        for ((char, digit) in chineseDigits) {
            result = result.replace(char.toString(), digit.toString())
        }

        return result
    }

    /**
     * 判断收支类型
     */
    private fun parseTransactionType(input: String): TransactionType {
        val incomeKeywords = listOf(
            "收入", "入账", "到账", "工资", "薪资", "奖金", "红包", "收到",
            "转入", "报销", "利息", "分红", "退款", "返现", "兼职", "副业",
            "卖", "卖出", "收款", "进账", "收益", "提成", "佣金", "稿费",
            "版税", "租金收入", "中奖", "赔偿", "补贴", "津贴"
        )

        val expenseKeywords = listOf(
            "花", "买", "购", "消费", "支出", "付", "交", "还", "缴",
            "打车", "吃", "喝", "玩", "看", "充", "订", "送", "寄"
        )

        // 优先检查收入关键词
        if (incomeKeywords.any { input.contains(it) }) {
            return TransactionType.INCOME
        }

        // 检查支出关键词（增加权重）
        val expenseScore = expenseKeywords.count { input.contains(it) }
        if (expenseScore > 0) {
            return TransactionType.EXPENSE
        }

        // 默认为支出
        return TransactionType.EXPENSE
    }

    /**
     * 匹配分类（增强版）
     */
    private fun matchCategory(
        input: String,
        categories: List<CategoryEntity>,
        type: TransactionType
    ): CategoryEntity? {
        // 扩展的分类关键词映射
        val categoryKeywords = mapOf(
            // 餐饮相关
            "餐饮" to listOf(
                "午餐", "晚餐", "早餐", "吃饭", "外卖", "饭", "餐", "火锅", "烧烤",
                "奶茶", "咖啡", "零食", "水果", "菜", "肉", "米", "面", "饮料",
                "美团", "饿了么", "小吃", "夜宵", "快餐", "食堂", "便当", "盒饭",
                "麦当劳", "肯德基", "星巴克", "瑞幸", "喜茶", "蜜雪冰城", "海底捞",
                "烤肉", "寿司", "披萨", "汉堡", "炸鸡", "烤串", "麻辣烫", "米线",
                "面条", "饺子", "包子", "馒头", "粥", "豆浆", "油条", "蛋糕",
                "面包", "甜点", "冰淇淋", "水", "茶", "酒", "啤酒", "可乐", "果汁"
            ),
            // 交通相关
            "交通" to listOf(
                "打车", "地铁", "公交", "滴滴", "高铁", "火车", "飞机", "机票",
                "车票", "加油", "停车", "过路费", "出租车", "单车", "骑车", "uber",
                "曹操", "嘀嗒", "首汽", "神州", "一号专车", "高速", "ETC", "罚单",
                "违章", "保养", "洗车", "修车", "汽车", "摩托", "电动车", "共享单车",
                "哈啰", "美团单车", "青桔", "顺风车", "拼车", "代驾", "租车"
            ),
            // 购物相关
            "购物" to listOf(
                "淘宝", "京东", "拼多多", "买", "购物", "网购", "超市", "商场",
                "衣服", "鞋", "包", "化妆品", "护肤", "日用品", "生活用品",
                "天猫", "苏宁", "唯品会", "小红书", "得物", "闲鱼", "万达", "沃尔玛",
                "家乐福", "永辉", "盒马", "711", "全家", "罗森", "便利店",
                "数码", "电子", "手机", "电脑", "配件", "耳机", "充电器"
            ),
            // 娱乐相关
            "娱乐" to listOf(
                "电影", "游戏", "KTV", "酒吧", "演唱会", "音乐", "视频", "会员",
                "Netflix", "爱奇艺", "腾讯", "B站", "抖音", "直播", "打赏",
                "steam", "switch", "ps5", "xbox", "王者", "吃鸡", "原神",
                "网易云", "QQ音乐", "酷狗", "喜马拉雅", "樊登", "得到", "知乎",
                "健身", "游泳", "瑜伽", "跑步", "球", "运动", "按摩", "spa",
                "旅游", "景点", "门票", "酒店", "民宿", "度假", "签证"
            ),
            // 居住相关
            "居住" to listOf(
                "房租", "水费", "电费", "燃气", "物业", "网费", "宽带", "暖气",
                "维修", "装修", "家具", "家电", "床", "沙发", "桌子", "椅子",
                "窗帘", "地毯", "空调", "冰箱", "洗衣机", "电视", "热水器",
                "煤气", "天然气", "取暖", "清洁", "保洁", "钟点工"
            ),
            // 医疗相关
            "医疗" to listOf(
                "医院", "药", "看病", "挂号", "体检", "牙", "眼镜", "保健",
                "医保", "诊所", "急诊", "门诊", "住院", "手术", "检查", "化验",
                "CT", "核磁", "B超", "验血", "拍片", "感冒", "发烧", "咳嗽",
                "疫苗", "核酸", "口罩", "消毒", "维生素", "保健品", "中药", "西药"
            ),
            // 教育相关
            "教育" to listOf(
                "学费", "书", "课程", "培训", "考试", "学习", "教材", "网课",
                "补习", "家教", "辅导", "托福", "雅思", "考研", "公务员",
                "驾照", "驾校", "证书", "认证", "知识付费", "电子书", "kindle"
            ),
            // 通讯相关
            "通讯" to listOf(
                "话费", "流量", "手机", "电话", "短信", "充值", "移动", "联通",
                "电信", "宽带", "wifi", "套餐", "通话"
            ),
            // 宠物相关
            "宠物" to listOf(
                "猫", "狗", "宠物", "猫粮", "狗粮", "宠物医院", "宠物店",
                "疫苗", "驱虫", "洗澡", "美容", "寄养", "遛狗"
            ),
            // 人情相关
            "人情" to listOf(
                "红包", "礼物", "礼金", "份子钱", "请客", "送礼", "婚礼",
                "生日", "满月", "乔迁", "升学", "慰问", "探望"
            ),
            // 工资收入
            "工资" to listOf("工资", "薪资", "月薪", "发工资", "薪水", "底薪", "基本工资"),
            // 奖金收入
            "奖金" to listOf("奖金", "年终奖", "提成", "绩效", "季度奖", "全勤奖"),
            // 红包收入
            "红包" to listOf("红包", "转账收入", "微信收款", "支付宝收款"),
            // 投资收入
            "投资收益" to listOf("理财", "基金", "股票", "利息", "分红", "收益", "定期", "余额宝")
        )

        // 按类型筛选分类
        val filteredCategories = categories.filter { it.type == type }

        // 计算每个分类的匹配分数
        val categoryScores = mutableMapOf<CategoryEntity, Int>()

        for (category in filteredCategories) {
            var score = 0

            // 检查扩展关键词
            val keywords = categoryKeywords[category.name]
            if (keywords != null) {
                score += keywords.count { input.contains(it) } * 2
            }

            // 检查分类名称
            if (input.contains(category.name.lowercase())) {
                score += 5
            }

            if (score > 0) {
                categoryScores[category] = score
            }
        }

        // 返回最高分的分类
        val bestMatch = categoryScores.maxByOrNull { it.value }?.key
        if (bestMatch != null) {
            return bestMatch
        }

        // 返回默认分类
        return filteredCategories.firstOrNull { it.name.contains("其他") }
            ?: filteredCategories.firstOrNull()
    }

    /**
     * 提取备注
     */
    private fun extractNote(input: String, categoryName: String? = null): String {
        var note = input
            // 移除金额
            .replace(Regex("\\d+\\.?\\d*\\s*[元块]钱?"), "")
            .replace(Regex("[¥￥]\\d+\\.?\\d*"), "")
            .replace(Regex("(?:花了|花费|消费|支出|付了|付款)\\s*\\d+\\.?\\d*"), "")
            .replace(Regex("\\d+\\.?\\d*\\s*rmb", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(?:收入|入账|到账)\\s*\\d+\\.?\\d*"), "")
            // 移除通用动词
            .replace(Regex("收入|入账|到账|支出|花费|消费|花了|买了|付了"), "")
            // 移除时间词
            .replace(Regex("昨天|昨日|前天|大前天|今天|今日|上周|这周|本周"), "")
            .replace(Regex("早上|上午|中午|下午|晚上|凌晨|傍晚|深夜"), "")
            .replace(Regex("[周星期][一二三四五六日天]"), "")
            .replace(Regex("\\d{1,2}[点时号日](?:\\d{1,2}分?)?"), "")
            .replace(Regex("早餐|午餐|晚餐|早饭|午饭|晚饭|夜宵|宵夜"), "")
            .trim()

        // 清理多余空格
        note = note.replace(Regex("\\s+"), " ").trim()

        // 如果备注基本为空，使用分类名作为备注
        if (note.length < 2) {
            note = categoryName ?: input.take(20)
        }

        return note.take(100)
    }

    private fun getDefaultCategoryName(type: TransactionType): String {
        return if (type == TransactionType.EXPENSE) "其他支出" else "其他收入"
    }

    private fun calculateConfidence(amount: Double?, category: CategoryEntity?, hasTimeInfo: Boolean): Float {
        var confidence = 0.4f
        if (amount != null && amount > 0) confidence += 0.3f
        if (category != null) confidence += 0.2f
        if (hasTimeInfo) confidence += 0.1f
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
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Float
)
