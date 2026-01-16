package com.example.smartledger.presentation.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI聊天ViewModel
 */
@HiltViewModel
class AiChatViewModel @Inject constructor(
    // TODO: 注入TransactionParser, FinancialAnalyzer等
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var messageIdCounter = 0L

    init {
        // 添加欢迎消息
        addMessage(
            content = "你好！我是你的AI记账助手。你可以直接告诉我你的消费，比如「午餐花了35元」，我会帮你自动记录。也可以问我「本月分析」来了解你的财务状况。",
            isFromUser = false
        )
    }

    fun sendMessage(content: String) {
        // 添加用户消息
        addMessage(content = content, isFromUser = true)

        // 处理消息并生成回复
        viewModelScope.launch {
            delay(500) // 模拟思考时间

            val response = processMessage(content)
            addMessage(content = response, isFromUser = false)
        }
    }

    private fun addMessage(content: String, isFromUser: Boolean) {
        val message = ChatMessage(
            id = messageIdCounter++,
            content = content,
            isFromUser = isFromUser,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }

    private fun processMessage(content: String): String {
        // 简单的关键词匹配处理
        return when {
            content.contains("记一笔") || content.contains("记账") -> {
                "好的，请告诉我具体的消费内容，比如「午餐35元」或「打车15元」。"
            }

            content.contains("本月分析") || content.contains("分析") -> {
                """
                📊 **本月财务分析**

                💰 总收入：¥15,000.00
                💸 总支出：¥5,320.00
                📈 储蓄率：64.5%

                🔍 **洞察发现：**
                ✅ 储蓄率64.5%，非常优秀！
                📌 最大支出：餐饮美食（占比29.7%）
                📉 支出环比减少8.5%，做得好！

                💡 **建议：**
                可以考虑将部分储蓄转入投资账户，让钱生钱。
                """.trimIndent()
            }

            content.contains("省钱") || content.contains("建议") -> {
                """
                💡 **省钱建议**

                1. **餐饮优化**：本月餐饮支出较高，建议：
                   - 多在家做饭，减少外卖
                   - 使用团购和优惠券

                2. **购物控制**：
                   - 建立购物清单，避免冲动消费
                   - 等待促销活动再购买

                3. **交通省钱**：
                   - 短途可以骑共享单车
                   - 办理地铁月票更划算
                """.trimIndent()
            }

            content.contains("预算") -> {
                """
                📊 **预算概览**

                本月总预算：¥8,000.00
                已使用：¥5,320.00 (66.5%)
                剩余：¥2,680.00

                日均可用：¥89.33

                各类别预算使用情况：
                🍜 餐饮：¥1,580/¥2,000 (79%)
                🛒 购物：¥1,200/¥1,500 (80%)
                🚇 交通：¥850/¥1,000 (85%)

                ⚠️ 交通预算即将超支，请注意控制。
                """.trimIndent()
            }

            // 尝试解析记账内容
            containsAmount(content) -> {
                val parsed = parseTransaction(content)
                """
                ✅ 已记录：

                类型：支出
                分类：${parsed.category}
                金额：¥${parsed.amount}
                备注：${parsed.note}

                记录成功！继续记录下一笔或查看「本月分析」。
                """.trimIndent()
            }

            else -> {
                "我理解了你的问题。你可以试试：\n• 直接说消费内容，如「午餐35元」\n• 「本月分析」查看财务状况\n• 「省钱建议」获取省钱技巧\n• 「预算概览」查看预算使用情况"
            }
        }
    }

    private fun containsAmount(text: String): Boolean {
        val amountPatterns = listOf(
            Regex("\\d+\\.?\\d*块"),
            Regex("\\d+\\.?\\d*元"),
            Regex("¥\\d+\\.?\\d*"),
            Regex("花了\\d+"),
            Regex("\\d+\\.?\\d*rmb", RegexOption.IGNORE_CASE)
        )
        return amountPatterns.any { it.containsMatchIn(text) }
    }

    private fun parseTransaction(text: String): ParsedTransaction {
        // 简单的解析逻辑
        val amountPattern = Regex("(\\d+\\.?\\d*)")
        val amountMatch = amountPattern.find(text)
        val amount = amountMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

        val category = when {
            text.contains("午餐") || text.contains("晚餐") || text.contains("早餐") ||
                    text.contains("吃饭") || text.contains("外卖") -> "餐饮美食"

            text.contains("打车") || text.contains("地铁") || text.contains("公交") ||
                    text.contains("滴滴") -> "交通出行"

            text.contains("购物") || text.contains("买") || text.contains("淘宝") ||
                    text.contains("京东") -> "购物消费"

            text.contains("电影") || text.contains("游戏") || text.contains("娱乐") -> "娱乐休闲"
            else -> "其他支出"
        }

        return ParsedTransaction(
            amount = amount,
            category = category,
            note = text
        )
    }
}

/**
 * AI聊天UI状态
 */
data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * 解析的交易
 */
data class ParsedTransaction(
    val amount: Double,
    val category: String,
    val note: String
)
