package com.example.smartledger.presentation.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.AccountEntity
import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.data.datastore.AiProvider
import com.example.smartledger.data.datastore.SettingsDataStore
import com.example.smartledger.domain.ai.AiChatResult
import com.example.smartledger.domain.ai.AiChatService
import com.example.smartledger.domain.ai.ChatMessageData
import com.example.smartledger.domain.ai.FinancialAnalyzer
import com.example.smartledger.domain.ai.ParseResult
import com.example.smartledger.domain.ai.SmartTransactionParser
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.BudgetRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.GoalRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * AI聊天ViewModel - 增强版
 */
@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    private val transactionParser: SmartTransactionParser,
    private val financialAnalyzer: FinancialAnalyzer,
    private val settingsDataStore: SettingsDataStore,
    private val aiChatService: AiChatService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var messageIdCounter = 0L
    private var categories: List<CategoryEntity> = emptyList()
    private var accounts: List<AccountEntity> = emptyList()
    private var pendingTransaction: PendingTransaction? = null
    private var pendingBatchTransactions: List<PendingTransaction> = emptyList()

    init {
        loadInitialData()
        loadAiConfigAndShowWelcome()
    }

    private fun loadAiConfigAndShowWelcome() {
        viewModelScope.launch {
            val aiConfig = settingsDataStore.aiConfigFlow.first()
            val aiModeText = if (aiConfig.provider != AiProvider.FREE && aiConfig.isConfigured) {
                "🔌 已连接: ${aiConfig.provider.displayName}"
            } else {
                "💡 提示: 可在设置中配置AI API以获得更智能的对话"
            }

            addMessage(
                content = "你好！我是你的AI记账助手 🤖\n\n" +
                        "$aiModeText\n\n" +
                        "你可以直接告诉我消费内容，比如：\n" +
                        "• 「午餐花了35元」\n" +
                        "• 「打车15块」\n" +
                        "• 「收到工资8000」\n\n" +
                        "也可以问我：\n" +
                        "• 「本月分析」- 查看财务状况\n" +
                        "• 「预算情况」- 查看预算使用\n" +
                        "• 「目标进度」- 查看储蓄目标\n" +
                        "• 「省钱建议」- 获取理财建议",
                isFromUser = false
            )
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            categories = categoryRepository.getAllActiveCategories().first()
            accounts = accountRepository.getAllActiveAccounts().first()
        }
    }

    fun sendMessage(content: String) {
        addMessage(content = content, isFromUser = true)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(300) // 模拟思考

            val response = processMessage(content)
            if (response.isNotEmpty()) {
                addMessage(content = response, isFromUser = false)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun confirmTransaction() {
        pendingTransaction?.let { pending ->
            viewModelScope.launch {
                val account = accounts.firstOrNull()
                if (account != null) {
                    val transaction = TransactionEntity(
                        amount = pending.amount,
                        type = pending.type,
                        categoryId = pending.categoryId ?: 0L,
                        accountId = account.id,
                        date = pending.timestamp,
                        note = pending.note,
                        tags = ""
                    )
                    transactionRepository.insertTransaction(transaction)

                    // 使用 incrementBalance 更新账户余额，避免使用可能已过期的 account.balance
                    val balanceChange = if (pending.type == TransactionType.EXPENSE) {
                        -pending.amount
                    } else {
                        pending.amount
                    }
                    accountRepository.incrementBalance(account.id, balanceChange)

                    addMessage(
                        content = "✅ 记录成功！\n\n" +
                                "金额：¥${String.format("%.2f", pending.amount)}\n" +
                                "分类：${pending.categoryName}\n" +
                                "备注：${pending.note}\n\n" +
                                "继续记录下一笔，或查看「本月分析」。",
                        isFromUser = false
                    )
                    pendingTransaction = null
                    _uiState.update { it.copy(showConfirmation = false) }
                }
            }
        }
    }

    fun cancelTransaction() {
        pendingTransaction = null
        _uiState.update { it.copy(showConfirmation = false) }
        addMessage(
            content = "已取消记录。有什么其他需要帮助的吗？",
            isFromUser = false
        )
    }

    /**
     * 处理语音识别结果 - 直接解析并显示确认对话框
     */
    fun handleVoiceInput(voiceText: String) {
        val result = transactionParser.parse(voiceText, categories)
        when (result) {
            is ParseResult.Success -> {
                val data = result.data
                _uiState.update {
                    it.copy(
                        showVoiceConfirmDialog = true,
                        voiceParsedTransaction = VoiceParsedTransaction(
                            amount = data.amount,
                            type = data.type,
                            categoryId = data.categoryId,
                            categoryName = data.categoryName,
                            note = data.note,
                            timestamp = data.timestamp
                        )
                    )
                }
            }
            is ParseResult.Failure -> {
                // 解析失败，回退到聊天模式
                sendMessage(voiceText)
            }
        }
    }

    /**
     * 更新语音解析的交易信息
     */
    fun updateVoiceParsedTransaction(transaction: VoiceParsedTransaction) {
        _uiState.update { it.copy(voiceParsedTransaction = transaction) }
    }

    /**
     * 确认语音识别的交易
     */
    fun confirmVoiceTransaction() {
        val parsed = _uiState.value.voiceParsedTransaction ?: return
        viewModelScope.launch {
            val account = accounts.firstOrNull()
            if (account != null) {
                val transaction = TransactionEntity(
                    amount = parsed.amount,
                    type = parsed.type,
                    categoryId = parsed.categoryId ?: 0L,
                    accountId = account.id,
                    date = parsed.timestamp,
                    note = parsed.note,
                    tags = ""
                )
                transactionRepository.insertTransaction(transaction)

                // 使用 incrementBalance 更新账户余额
                val balanceChange = if (parsed.type == TransactionType.EXPENSE) {
                    -parsed.amount
                } else {
                    parsed.amount
                }
                accountRepository.incrementBalance(account.id, balanceChange)

                _uiState.update {
                    it.copy(
                        showVoiceConfirmDialog = false,
                        voiceParsedTransaction = null
                    )
                }

                addMessage(
                    content = "✅ 语音记账成功！\n\n" +
                            "金额：¥${String.format("%.2f", parsed.amount)}\n" +
                            "分类：${parsed.categoryName}\n" +
                            "备注：${parsed.note}",
                    isFromUser = false
                )
            }
        }
    }

    /**
     * 取消语音识别的交易
     */
    fun cancelVoiceTransaction() {
        _uiState.update {
            it.copy(
                showVoiceConfirmDialog = false,
                voiceParsedTransaction = null
            )
        }
    }

    /**
     * 获取所有分类
     */
    fun getCategories(): List<CategoryEntity> = categories

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

    private suspend fun processMessage(content: String): String {
        val lowerContent = content.lowercase()

        // 首先处理确认/取消等特殊指令
        when {
            // 确认记账
            pendingTransaction != null && (lowerContent.contains("确认") || lowerContent.contains("是") || lowerContent == "好" || lowerContent == "ok") -> {
                confirmTransaction()
                return "" // 由confirmTransaction处理回复
            }

            // 取消记账
            pendingTransaction != null && (lowerContent.contains("取消") || lowerContent.contains("不") || lowerContent.contains("算了")) -> {
                cancelTransaction()
                return "" // 由cancelTransaction处理回复
            }

            // 确认批量记账
            pendingBatchTransactions.isNotEmpty() && (lowerContent.contains("全部确认") || lowerContent.contains("确认全部")) -> {
                confirmBatchTransactions()
                return ""
            }
        }

        // 检查是否配置了外部AI API
        val aiConfig = settingsDataStore.aiConfigFlow.first()
        if (aiConfig.provider != AiProvider.FREE && aiConfig.isConfigured) {
            // 使用外部AI API处理
            return processWithExternalAi(content)
        }

        // 使用本地处理逻辑
        return when {
            // 本月分析
            lowerContent.contains("本月分析") || lowerContent.contains("分析") || lowerContent.contains("报告") -> {
                generateMonthlyAnalysisResponse()
            }

            // 预算情况
            lowerContent.contains("预算") -> {
                generateBudgetAnalysisResponse()
            }

            // 目标进度
            lowerContent.contains("目标") || lowerContent.contains("储蓄目标") -> {
                generateGoalsAnalysisResponse()
            }

            // 省钱建议
            lowerContent.contains("省钱") || lowerContent.contains("建议") || lowerContent.contains("理财") -> {
                generateSavingsSuggestionsResponse()
            }

            // 最近消费
            lowerContent.contains("最近") || lowerContent.contains("今天") || lowerContent.contains("昨天") -> {
                generateRecentTransactionsResponse()
            }

            // 批量导入 - 检测多行或多条记录
            content.contains("\n") || content.count { it == '，' || it == ',' || it == '；' || it == ';' } >= 2 -> {
                tryParseBatchTransactions(content)
            }

            // 尝试解析记账
            else -> {
                tryParseAndRecordTransaction(content)
            }
        }
    }

    /**
     * 使用外部AI API处理消息
     */
    private suspend fun processWithExternalAi(content: String): String {
        // 构建对话历史
        val recentMessages = _uiState.value.messages.takeLast(10).map { msg ->
            ChatMessageData(
                content = msg.content,
                isFromUser = msg.isFromUser
            )
        }

        // 添加当前消息
        val messages = recentMessages + ChatMessageData(content, true)

        // 添加财务上下文
        val financialContext = buildFinancialContext()

        val systemPrompt = """${AiChatService.DEFAULT_SYSTEM_PROMPT}

当前用户的财务数据摘要：
$financialContext

如果用户想要记账，请提取以下信息并按格式回复：
- 金额
- 类型（支出/收入）
- 分类建议
- 备注

如果用户询问财务问题，基于上述数据给出建议。"""

        return when (val result = aiChatService.chat(messages, systemPrompt)) {
            is AiChatResult.Success -> result.content
            is AiChatResult.Error -> {
                // AI服务出错，回退到本地处理
                "⚠️ AI服务暂时不可用: ${result.message}\n\n正在使用本地模式...\n\n" +
                        tryParseAndRecordTransaction(content)
            }
        }
    }

    /**
     * 构建财务上下文信息
     */
    private suspend fun buildFinancialContext(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val monthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        val transactions = transactionRepository.getTransactionsByDateRange(monthStart, monthEnd)
        val budgets = budgetRepository.getAllBudgets()
        val goals = goalRepository.getAllGoals().first()

        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        return buildString {
            appendLine("本月支出: ¥${String.format("%.2f", totalExpense)}")
            appendLine("本月收入: ¥${String.format("%.2f", totalIncome)}")
            appendLine("本月结余: ¥${String.format("%.2f", totalIncome - totalExpense)}")

            if (budgets.isNotEmpty()) {
                val totalBudget = budgets.sumOf { it.amount }
                appendLine("本月预算: ¥${String.format("%.2f", totalBudget)}")
                appendLine("预算使用: ${String.format("%.1f", totalExpense / totalBudget * 100)}%")
            }

            if (goals.isNotEmpty()) {
                appendLine("储蓄目标: ${goals.size}个")
                val totalGoalAmount = goals.sumOf { it.targetAmount }
                val totalSaved = goals.sumOf { it.currentAmount }
                appendLine("目标总额: ¥${String.format("%.2f", totalGoalAmount)}")
                appendLine("已存入: ¥${String.format("%.2f", totalSaved)}")
            }

            // 添加分类列表
            appendLine("\n可用的支出分类: ${categories.filter { it.type == TransactionType.EXPENSE }.joinToString(", ") { it.name }}")
            appendLine("可用的收入分类: ${categories.filter { it.type == TransactionType.INCOME }.joinToString(", ") { it.name }}")
        }
    }

    /**
     * 批量解析多条交易记录
     */
    private fun tryParseBatchTransactions(content: String): String {
        // 按换行、中文分号、英文分号分割
        val lines = content.split(Regex("[\\n；;]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 2 }

        if (lines.size < 2) {
            return tryParseAndRecordTransaction(content)
        }

        val successList = mutableListOf<PendingTransaction>()
        val failureList = mutableListOf<String>()

        lines.forEach { line ->
            val result = transactionParser.parse(line, categories)
            when (result) {
                is ParseResult.Success -> {
                    val data = result.data
                    successList.add(PendingTransaction(
                        amount = data.amount,
                        type = data.type,
                        categoryId = data.categoryId,
                        categoryName = data.categoryName,
                        note = data.note,
                        timestamp = data.timestamp
                    ))
                }
                is ParseResult.Failure -> {
                    failureList.add(line)
                }
            }
        }

        if (successList.isEmpty()) {
            return "抱歉，未能识别出有效的记录。\n\n" +
                    "批量导入格式示例：\n" +
                    "午餐35元\n" +
                    "打车15元\n" +
                    "买水果28元"
        }

        pendingBatchTransactions = successList
        _uiState.update { it.copy(showBatchConfirmation = true) }

        return buildString {
            appendLine("📋 **批量识别结果**")
            appendLine()
            appendLine("成功识别 ${successList.size} 条记录：")
            appendLine()
            successList.forEachIndexed { index, txn ->
                val typeIcon = if (txn.type == TransactionType.EXPENSE) "💸" else "💰"
                appendLine("${index + 1}. $typeIcon ¥${String.format("%.2f", txn.amount)} - ${txn.categoryName}")
            }
            if (failureList.isNotEmpty()) {
                appendLine()
                appendLine("⚠️ 未能识别 ${failureList.size} 条：")
                failureList.forEach { appendLine("• $it") }
            }
            appendLine()
            append("回复「全部确认」保存所有记录，或「取消」放弃")
        }
    }

    /**
     * 确认批量记账
     */
    private fun confirmBatchTransactions() {
        val transactions = pendingBatchTransactions
        if (transactions.isEmpty()) return

        viewModelScope.launch {
            val account = accounts.firstOrNull()
            if (account != null) {
                var successCount = 0
                transactions.forEach { pending ->
                    try {
                        val transaction = TransactionEntity(
                            amount = pending.amount,
                            type = pending.type,
                            categoryId = pending.categoryId ?: 0L,
                            accountId = account.id,
                            date = pending.timestamp,
                            note = pending.note
                        )
                        transactionRepository.insertTransaction(transaction)

                        // 使用 incrementBalance 更新账户余额
                        val balanceChange = if (pending.type == TransactionType.EXPENSE) -pending.amount else pending.amount
                        accountRepository.incrementBalance(account.id, balanceChange)

                        successCount++
                    } catch (e: Exception) {
                        // 忽略单条错误
                    }
                }

                pendingBatchTransactions = emptyList()
                _uiState.update { it.copy(showBatchConfirmation = false) }
                addMessage(
                    content = "✅ 批量记账完成！\n\n成功保存 $successCount 条记录。",
                    isFromUser = false
                )
            }
        }
    }

    private suspend fun generateMonthlyAnalysisResponse(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val monthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        val transactions = transactionRepository.getTransactionsByDateRange(monthStart, monthEnd)
        val categoryMap = categories.associateBy { it.id }

        // 获取上月数据用于对比
        calendar.add(Calendar.MONTH, -2)
        val prevMonthStart = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val prevMonthEnd = calendar.timeInMillis
        val prevTransactions = transactionRepository.getTransactionsByDateRange(prevMonthStart, prevMonthEnd)

        val analysis = financialAnalyzer.generateMonthlyAnalysis(transactions, categoryMap, prevTransactions)

        return buildString {
            appendLine("📊 **${analysis.month} 财务分析**")
            appendLine()
            appendLine("💰 总收入：¥${String.format("%.2f", analysis.totalIncome)}")
            appendLine("💸 总支出：¥${String.format("%.2f", analysis.totalExpense)}")
            appendLine("💵 结余：¥${String.format("%.2f", analysis.balance)}")
            appendLine("📈 储蓄率：${String.format("%.1f", analysis.savingsRate)}%")
            appendLine()
            appendLine("🔍 **洞察发现：**")
            analysis.insights.forEach { appendLine(it) }
            appendLine()
            appendLine("💡 **理财建议：**")
            analysis.suggestions.forEach { appendLine("• $it") }
        }
    }

    private suspend fun generateBudgetAnalysisResponse(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val monthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        val transactions = transactionRepository.getTransactionsByDateRange(monthStart, monthEnd)
        val budgets = budgetRepository.getAllBudgets()
        val categoryMap = categories.associateBy { it.id }

        if (budgets.isEmpty()) {
            return "📊 **预算概览**\n\n" +
                    "您还没有设置预算哦！\n\n" +
                    "建议在「预算管理」中设置月度预算，更好地控制支出。"
        }

        val analysis = financialAnalyzer.generateBudgetAnalysis(budgets, transactions, categoryMap)

        return buildString {
            appendLine("📊 **预算概览**")
            appendLine()
            if (analysis.totalBudget > 0) {
                appendLine("本月总预算：¥${String.format("%.2f", analysis.totalBudget)}")
                appendLine("已使用：¥${String.format("%.2f", analysis.totalUsed)} (${String.format("%.1f", analysis.usagePercentage)}%)")
                appendLine("剩余：¥${String.format("%.2f", analysis.totalRemaining)}")
                appendLine("日均可用：¥${String.format("%.2f", analysis.dailyAvailable)}")
                appendLine("剩余天数：${analysis.daysRemaining}天")
            }

            if (analysis.categoryBudgets.isNotEmpty()) {
                appendLine()
                appendLine("**分类预算使用情况：**")
                analysis.categoryBudgets.take(5).forEach { budget ->
                    val status = when {
                        budget.isOverBudget -> "❌"
                        budget.usagePercentage > 80 -> "⚠️"
                        else -> "✅"
                    }
                    appendLine("$status ${budget.icon} ${budget.name}：¥${String.format("%.0f", budget.usedAmount)}/¥${String.format("%.0f", budget.budgetAmount)} (${String.format("%.0f", budget.usagePercentage)}%)")
                }
            }

            if (analysis.warnings.isNotEmpty()) {
                appendLine()
                appendLine("**预算警告：**")
                analysis.warnings.forEach { appendLine(it) }
            }
        }
    }

    private suspend fun generateGoalsAnalysisResponse(): String {
        val goals = goalRepository.getAllGoals().first()

        if (goals.isEmpty()) {
            return "🎯 **储蓄目标**\n\n" +
                    "您还没有设置储蓄目标哦！\n\n" +
                    "建议在「储蓄目标」中创建目标，让存钱更有动力！"
        }

        val analysis = financialAnalyzer.generateGoalsAnalysis(goals)

        return buildString {
            appendLine("🎯 **储蓄目标进度**")
            appendLine()
            appendLine("进行中：${analysis.activeCount}个 | 已完成：${analysis.completedCount}个")
            appendLine("目标总额：¥${String.format("%.2f", analysis.totalTargetAmount)}")
            appendLine("已存入：¥${String.format("%.2f", analysis.totalSavedAmount)}")
            appendLine()

            analysis.goalStatuses.forEach { goal ->
                val progressBar = buildProgressBar(goal.progress.toInt())
                appendLine("${goal.icon} **${goal.name}**")
                appendLine("$progressBar ${String.format("%.1f", goal.progress)}%")
                appendLine("¥${String.format("%.2f", goal.currentAmount)} / ¥${String.format("%.2f", goal.targetAmount)}")
                goal.estimatedDaysToComplete?.let {
                    appendLine("预计${it}天后达成")
                }
                appendLine()
            }

            if (analysis.suggestions.isNotEmpty()) {
                appendLine("**建议：**")
                analysis.suggestions.forEach { appendLine(it) }
            }
        }
    }

    private fun buildProgressBar(percentage: Int): String {
        val filled = percentage / 10
        val empty = 10 - filled
        return "[${"█".repeat(filled)}${"░".repeat(empty)}]"
    }

    private suspend fun generateSavingsSuggestionsResponse(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val monthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        val transactions = transactionRepository.getTransactionsByDateRange(monthStart, monthEnd)
        val categoryMap = categories.associateBy { it.id }

        val analysis = financialAnalyzer.generateMonthlyAnalysis(transactions, categoryMap, emptyList())

        return buildString {
            appendLine("💡 **省钱建议**")
            appendLine()

            if (analysis.suggestions.isNotEmpty()) {
                analysis.suggestions.forEach { appendLine("• $it") }
            }

            appendLine()
            appendLine("**通用理财技巧：**")
            appendLine("1. 📝 记录每笔消费，培养理财意识")
            appendLine("2. 💰 先储蓄后消费，每月固定存入一定比例")
            appendLine("3. 🛒 购物前列清单，避免冲动消费")
            appendLine("4. 📱 利用优惠券和返利平台")
            appendLine("5. 🍳 减少外卖，多自己做饭")
            appendLine("6. ☕ 减少非必要的订阅服务")
        }
    }

    private suspend fun generateRecentTransactionsResponse(): String {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        val startOfDay = today.timeInMillis

        today.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = today.timeInMillis

        val todayTransactions = transactionRepository.getTransactionsByDateRange(startOfDay, endOfDay)
        val categoryMap = categories.associateBy { it.id }

        if (todayTransactions.isEmpty()) {
            return "📋 **今日消费**\n\n今天还没有记录哦！\n\n有什么消费需要记录吗？直接告诉我就行。"
        }

        val totalExpense = todayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalIncome = todayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        return buildString {
            appendLine("📋 **今日消费记录**")
            appendLine()
            appendLine("支出：¥${String.format("%.2f", totalExpense)} | 收入：¥${String.format("%.2f", totalIncome)}")
            appendLine()

            todayTransactions.take(10).forEach { txn ->
                val category = categoryMap[txn.categoryId]
                val icon = category?.icon ?: "📦"
                val name = category?.name ?: "未分类"
                val sign = if (txn.type == TransactionType.EXPENSE) "-" else "+"
                appendLine("$icon $name ${sign}¥${String.format("%.2f", txn.amount)}")
            }

            if (todayTransactions.size > 10) {
                appendLine("...")
                appendLine("共${todayTransactions.size}笔记录")
            }
        }
    }

    private fun tryParseAndRecordTransaction(content: String): String {
        val result = transactionParser.parse(content, categories)

        return when (result) {
            is ParseResult.Success -> {
                val data = result.data
                pendingTransaction = PendingTransaction(
                    amount = data.amount,
                    type = data.type,
                    categoryId = data.categoryId,
                    categoryName = data.categoryName,
                    note = data.note,
                    timestamp = data.timestamp
                )

                val typeText = if (data.type == TransactionType.EXPENSE) "支出" else "收入"
                val confidenceText = when {
                    data.confidence >= 0.8 -> "（高置信度）"
                    data.confidence >= 0.5 -> "（中置信度）"
                    else -> "（低置信度，请确认）"
                }

                _uiState.update { it.copy(showConfirmation = true) }

                "📝 识别到一笔$typeText$confidenceText\n\n" +
                        "• 金额：¥${String.format("%.2f", data.amount)}\n" +
                        "• 分类：${data.categoryName}\n" +
                        "• 备注：${data.note}\n\n" +
                        "确认记录吗？回复「确认」或「取消」"
            }

            is ParseResult.Failure -> {
                "抱歉，${result.message}\n\n" +
                        "你可以试试：\n" +
                        "• 直接说消费内容，如「午餐35元」\n" +
                        "• 「本月分析」查看财务状况\n" +
                        "• 「省钱建议」获取理财建议"
            }
        }
    }
}

/**
 * AI聊天UI状态
 */
data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val showConfirmation: Boolean = false,
    val showBatchConfirmation: Boolean = false,
    val showVoiceConfirmDialog: Boolean = false,
    val voiceParsedTransaction: VoiceParsedTransaction? = null
)

/**
 * 语音识别解析出的交易（可编辑）
 */
data class VoiceParsedTransaction(
    var amount: Double,
    var type: TransactionType,
    var categoryId: Long?,
    var categoryName: String,
    var note: String,
    var timestamp: Long
)

/**
 * 聊天消息
 */
data class ChatMessage(
    val id: Long,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long
)

/**
 * 待确认交易
 */
data class PendingTransaction(
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long?,
    val categoryName: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
