package com.example.smartledger.domain.usecase

import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.BudgetRepository
import com.example.smartledger.domain.repository.GoalRepository
import com.example.smartledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

/**
 * 财务健康评估用例
 * 基于多维度数据分析用户的财务健康状况
 */
class FinancialHealthUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository
) {
    /**
     * 计算财务健康评分和诊断报告
     */
    suspend fun calculateFinancialHealth(): FinancialHealthReport {
        val calendar = Calendar.getInstance()

        // 获取本月数据
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis

        // 获取上月数据
        calendar.add(Calendar.MONTH, -2)
        val lastMonthStart = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val lastMonthEnd = calendar.timeInMillis

        // 获取近3个月数据
        calendar.add(Calendar.MONTH, -3)
        val threeMonthStart = calendar.timeInMillis

        // 基础数据
        val monthIncome = transactionRepository.getTotalByDateRange(TransactionType.INCOME, monthStart, monthEnd)
        val monthExpense = transactionRepository.getTotalByDateRange(TransactionType.EXPENSE, monthStart, monthEnd)
        val lastMonthIncome = transactionRepository.getTotalByDateRange(TransactionType.INCOME, lastMonthStart, lastMonthEnd)
        val lastMonthExpense = transactionRepository.getTotalByDateRange(TransactionType.EXPENSE, lastMonthStart, lastMonthEnd)

        // 账户总资产
        val accounts = accountRepository.getAllActiveAccounts().first()
        val totalAssets = accounts.sumOf { it.balance }

        // 预算数据
        val budgets = budgetRepository.getAllActiveBudgets().first()
        val budgetCompliance = if (budgets.isNotEmpty()) {
            budgets.count { it.usedAmount <= it.amount }.toFloat() / budgets.size
        } else 1f

        // 目标数据
        val goals = goalRepository.getActiveGoals().first()
        val goalProgress = if (goals.isNotEmpty()) {
            goals.sumOf { it.currentAmount } / goals.sumOf { it.targetAmount }.coerceAtLeast(1.0)
        } else 0.0

        // 计算各维度评分 (每项满分100)
        val savingsScore = calculateSavingsScore(monthIncome, monthExpense)
        val stabilityScore = calculateStabilityScore(monthIncome, lastMonthIncome, monthExpense, lastMonthExpense)
        val budgetScore = calculateBudgetScore(budgetCompliance)
        val goalScore = calculateGoalScore(goalProgress)
        val diversityScore = calculateDiversityScore(accounts.size)
        val emergencyFundScore = calculateEmergencyFundScore(totalAssets, monthExpense)

        // 综合评分 (加权平均)
        val overallScore = (
            savingsScore * 0.25 +
            stabilityScore * 0.20 +
            budgetScore * 0.15 +
            goalScore * 0.15 +
            diversityScore * 0.10 +
            emergencyFundScore * 0.15
        ).toInt()

        // 生成诊断建议
        val suggestions = generateSuggestions(
            savingsScore, stabilityScore, budgetScore, goalScore,
            diversityScore, emergencyFundScore, monthIncome, monthExpense, totalAssets
        )

        // 确定健康等级
        val healthLevel = when {
            overallScore >= 90 -> HealthLevel.EXCELLENT
            overallScore >= 75 -> HealthLevel.GOOD
            overallScore >= 60 -> HealthLevel.FAIR
            overallScore >= 40 -> HealthLevel.POOR
            else -> HealthLevel.CRITICAL
        }

        return FinancialHealthReport(
            overallScore = overallScore,
            healthLevel = healthLevel,
            savingsScore = savingsScore,
            stabilityScore = stabilityScore,
            budgetScore = budgetScore,
            goalScore = goalScore,
            diversityScore = diversityScore,
            emergencyFundScore = emergencyFundScore,
            monthlyIncome = monthIncome,
            monthlyExpense = monthExpense,
            savingsRate = if (monthIncome > 0) ((monthIncome - monthExpense) / monthIncome * 100) else 0.0,
            totalAssets = totalAssets,
            emergencyFundMonths = if (monthExpense > 0) (totalAssets / monthExpense) else 0.0,
            suggestions = suggestions,
            strengths = identifyStrengths(savingsScore, stabilityScore, budgetScore, goalScore, diversityScore, emergencyFundScore),
            weaknesses = identifyWeaknesses(savingsScore, stabilityScore, budgetScore, goalScore, diversityScore, emergencyFundScore)
        )
    }

    /**
     * 计算储蓄率评分
     * 储蓄率 > 30% 得满分，逐步递减
     */
    private fun calculateSavingsScore(income: Double, expense: Double): Int {
        if (income <= 0) return 0
        val savingsRate = (income - expense) / income
        return when {
            savingsRate >= 0.30 -> 100
            savingsRate >= 0.20 -> 80 + ((savingsRate - 0.20) / 0.10 * 20).toInt()
            savingsRate >= 0.10 -> 60 + ((savingsRate - 0.10) / 0.10 * 20).toInt()
            savingsRate >= 0 -> 40 + ((savingsRate) / 0.10 * 20).toInt()
            else -> (40 + savingsRate * 100).toInt().coerceAtLeast(0)
        }
    }

    /**
     * 计算收支稳定性评分
     * 基于月度环比波动
     */
    private fun calculateStabilityScore(
        currentIncome: Double,
        lastIncome: Double,
        currentExpense: Double,
        lastExpense: Double
    ): Int {
        if (lastIncome <= 0 && lastExpense <= 0) return 70 // 无历史数据默认中等

        val incomeChange = if (lastIncome > 0) {
            kotlin.math.abs(currentIncome - lastIncome) / lastIncome
        } else 0.0

        val expenseChange = if (lastExpense > 0) {
            kotlin.math.abs(currentExpense - lastExpense) / lastExpense
        } else 0.0

        val avgChange = (incomeChange + expenseChange) / 2

        return when {
            avgChange <= 0.10 -> 100
            avgChange <= 0.20 -> 85
            avgChange <= 0.30 -> 70
            avgChange <= 0.50 -> 55
            else -> 40
        }
    }

    /**
     * 计算预算执行评分
     */
    private fun calculateBudgetScore(compliance: Float): Int {
        return (compliance * 100).toInt()
    }

    /**
     * 计算目标完成评分
     */
    private fun calculateGoalScore(progress: Double): Int {
        return (progress * 100).toInt().coerceIn(0, 100)
    }

    /**
     * 计算账户多样性评分
     */
    private fun calculateDiversityScore(accountCount: Int): Int {
        return when {
            accountCount >= 4 -> 100
            accountCount == 3 -> 85
            accountCount == 2 -> 70
            accountCount == 1 -> 50
            else -> 30
        }
    }

    /**
     * 计算应急资金评分
     * 建议保留3-6个月的生活费
     */
    private fun calculateEmergencyFundScore(totalAssets: Double, monthlyExpense: Double): Int {
        if (monthlyExpense <= 0) return 70
        val months = totalAssets / monthlyExpense
        return when {
            months >= 6 -> 100
            months >= 3 -> 70 + ((months - 3) / 3 * 30).toInt()
            months >= 1 -> 40 + ((months - 1) / 2 * 30).toInt()
            else -> (months * 40).toInt()
        }
    }

    /**
     * 生成诊断建议
     */
    private fun generateSuggestions(
        savingsScore: Int,
        stabilityScore: Int,
        budgetScore: Int,
        goalScore: Int,
        diversityScore: Int,
        emergencyFundScore: Int,
        monthlyIncome: Double,
        monthlyExpense: Double,
        totalAssets: Double
    ): List<FinancialSuggestion> {
        val suggestions = mutableListOf<FinancialSuggestion>()

        // 储蓄建议
        if (savingsScore < 60) {
            suggestions.add(
                FinancialSuggestion(
                    category = "储蓄",
                    priority = SuggestionPriority.HIGH,
                    title = "提高储蓄率",
                    description = "您的储蓄率偏低，建议每月将收入的20-30%用于储蓄",
                    actionItems = listOf(
                        "制定每月固定储蓄计划",
                        "减少非必要支出",
                        "考虑开源增收"
                    )
                )
            )
        }

        // 预算建议
        if (budgetScore < 70) {
            suggestions.add(
                FinancialSuggestion(
                    category = "预算",
                    priority = SuggestionPriority.MEDIUM,
                    title = "优化预算执行",
                    description = "部分预算超支，建议重新评估各类支出预算",
                    actionItems = listOf(
                        "分析超支原因",
                        "调整不合理预算",
                        "设置支出提醒"
                    )
                )
            )
        }

        // 应急资金建议
        if (emergencyFundScore < 70) {
            val monthsOfExpense = if (monthlyExpense > 0) totalAssets / monthlyExpense else 0.0
            suggestions.add(
                FinancialSuggestion(
                    category = "应急资金",
                    priority = if (monthsOfExpense < 1) SuggestionPriority.HIGH else SuggestionPriority.MEDIUM,
                    title = "建立应急资金",
                    description = "建议保留3-6个月生活费作为应急资金，目前约有${String.format("%.1f", monthsOfExpense)}个月",
                    actionItems = listOf(
                        "设立专门的应急资金账户",
                        "每月固定存入应急资金",
                        "达标前避免动用此资金"
                    )
                )
            )
        }

        // 账户多样性建议
        if (diversityScore < 70) {
            suggestions.add(
                FinancialSuggestion(
                    category = "资产配置",
                    priority = SuggestionPriority.LOW,
                    title = "丰富账户配置",
                    description = "建议根据用途设置不同账户，便于资金管理",
                    actionItems = listOf(
                        "设立日常消费账户",
                        "设立储蓄账户",
                        "设立投资账户"
                    )
                )
            )
        }

        // 目标建议
        if (goalScore < 50) {
            suggestions.add(
                FinancialSuggestion(
                    category = "储蓄目标",
                    priority = SuggestionPriority.MEDIUM,
                    title = "加快目标进度",
                    description = "储蓄目标进度较慢，建议增加定期存款",
                    actionItems = listOf(
                        "评估目标可行性",
                        "设置自动转账",
                        "寻找额外收入来源"
                    )
                )
            )
        }

        // 稳定性建议
        if (stabilityScore < 60) {
            suggestions.add(
                FinancialSuggestion(
                    category = "收支稳定",
                    priority = SuggestionPriority.MEDIUM,
                    title = "保持收支稳定",
                    description = "收支波动较大，建议保持稳定的消费习惯",
                    actionItems = listOf(
                        "避免冲动消费",
                        "大额支出提前规划",
                        "建立稳定的记账习惯"
                    )
                )
            )
        }

        return suggestions.sortedByDescending { it.priority.ordinal }
    }

    /**
     * 识别财务优势
     */
    private fun identifyStrengths(
        savingsScore: Int,
        stabilityScore: Int,
        budgetScore: Int,
        goalScore: Int,
        diversityScore: Int,
        emergencyFundScore: Int
    ): List<String> {
        val strengths = mutableListOf<String>()

        if (savingsScore >= 80) strengths.add("储蓄习惯良好")
        if (stabilityScore >= 80) strengths.add("收支保持稳定")
        if (budgetScore >= 80) strengths.add("预算执行优秀")
        if (goalScore >= 80) strengths.add("目标进展顺利")
        if (diversityScore >= 80) strengths.add("账户配置合理")
        if (emergencyFundScore >= 80) strengths.add("应急资金充足")

        if (strengths.isEmpty()) {
            strengths.add("正在建立良好的财务习惯")
        }

        return strengths
    }

    /**
     * 识别财务短板
     */
    private fun identifyWeaknesses(
        savingsScore: Int,
        stabilityScore: Int,
        budgetScore: Int,
        goalScore: Int,
        diversityScore: Int,
        emergencyFundScore: Int
    ): List<String> {
        val weaknesses = mutableListOf<String>()

        if (savingsScore < 60) weaknesses.add("储蓄率偏低")
        if (stabilityScore < 60) weaknesses.add("收支波动较大")
        if (budgetScore < 60) weaknesses.add("预算执行不佳")
        if (goalScore < 60) weaknesses.add("目标进度滞后")
        if (diversityScore < 60) weaknesses.add("账户单一")
        if (emergencyFundScore < 60) weaknesses.add("应急资金不足")

        return weaknesses
    }
}

/**
 * 财务健康报告
 */
data class FinancialHealthReport(
    val overallScore: Int,
    val healthLevel: HealthLevel,
    val savingsScore: Int,
    val stabilityScore: Int,
    val budgetScore: Int,
    val goalScore: Int,
    val diversityScore: Int,
    val emergencyFundScore: Int,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val savingsRate: Double,
    val totalAssets: Double,
    val emergencyFundMonths: Double,
    val suggestions: List<FinancialSuggestion>,
    val strengths: List<String>,
    val weaknesses: List<String>
)

/**
 * 健康等级
 */
enum class HealthLevel(val label: String, val description: String) {
    EXCELLENT("优秀", "财务状况非常健康"),
    GOOD("良好", "财务状况较为健康"),
    FAIR("一般", "财务状况尚可，有改善空间"),
    POOR("较差", "财务状况需要改善"),
    CRITICAL("危险", "财务状况亟需改善")
}

/**
 * 财务建议
 */
data class FinancialSuggestion(
    val category: String,
    val priority: SuggestionPriority,
    val title: String,
    val description: String,
    val actionItems: List<String>
)

/**
 * 建议优先级
 */
enum class SuggestionPriority {
    LOW, MEDIUM, HIGH
}
