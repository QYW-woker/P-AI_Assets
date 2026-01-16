package com.example.smartledger.domain.ai

import com.example.smartledger.data.local.entity.BudgetEntity
import com.example.smartledger.data.local.entity.CategoryEntity
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * 财务分析器 - 提供智能财务分析和建议
 */
@Singleton
class FinancialAnalyzer @Inject constructor() {

    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())

    /**
     * 生成月度分析报告
     */
    fun generateMonthlyAnalysis(
        transactions: List<TransactionEntity>,
        categories: Map<Long, CategoryEntity>,
        previousMonthTransactions: List<TransactionEntity>? = null
    ): MonthlyAnalysis {
        val income = transactions.filter { it.type == TransactionType.INCOME }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

        val totalIncome = income.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100) else 0.0

        // 按分类统计支出
        val expenseByCategory = expenses
            .groupBy { it.categoryId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        // 找出最大支出分类
        val topExpenseCategory = expenseByCategory.firstOrNull()?.let { (categoryId, amount) ->
            val category = categories[categoryId]
            CategoryExpense(
                name = category?.name ?: "未分类",
                icon = category?.icon ?: "📦",
                amount = amount,
                percentage = if (totalExpense > 0) (amount / totalExpense * 100) else 0.0
            )
        }

        // 计算环比变化
        val monthOverMonthChange = previousMonthTransactions?.let { prevTxns ->
            val prevExpense = prevTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            if (prevExpense > 0) {
                ((totalExpense - prevExpense) / prevExpense * 100)
            } else null
        }

        // 生成洞察
        val insights = generateInsights(totalIncome, totalExpense, savingsRate, topExpenseCategory, monthOverMonthChange)

        // 生成建议
        val suggestions = generateSuggestions(expenseByCategory, categories, savingsRate, totalExpense)

        return MonthlyAnalysis(
            month = monthFormat.format(Date()),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            savingsRate = savingsRate,
            transactionCount = transactions.size,
            topExpenseCategory = topExpenseCategory,
            monthOverMonthChange = monthOverMonthChange,
            insights = insights,
            suggestions = suggestions
        )
    }

    private fun generateInsights(
        totalIncome: Double,
        totalExpense: Double,
        savingsRate: Double,
        topCategory: CategoryExpense?,
        monthOverMonthChange: Double?
    ): List<String> {
        val insights = mutableListOf<String>()

        // 储蓄率评价
        when {
            savingsRate >= 50 -> insights.add("✅ 储蓄率${String.format("%.1f", savingsRate)}%，非常优秀！继续保持")
            savingsRate >= 30 -> insights.add("👍 储蓄率${String.format("%.1f", savingsRate)}%，表现良好")
            savingsRate >= 10 -> insights.add("📊 储蓄率${String.format("%.1f", savingsRate)}%，还有提升空间")
            savingsRate > 0 -> insights.add("⚠️ 储蓄率较低，建议控制支出")
            else -> insights.add("❗ 本月入不敷出，需要调整消费习惯")
        }

        // 最大支出分类
        topCategory?.let {
            insights.add("📌 最大支出：${it.name}（占比${String.format("%.1f", it.percentage)}%）")
        }

        // 环比变化
        monthOverMonthChange?.let { change ->
            when {
                change < -10 -> insights.add("📉 支出环比减少${String.format("%.1f", abs(change))}%，做得好！")
                change > 10 -> insights.add("📈 支出环比增加${String.format("%.1f", change)}%，注意控制")
                else -> insights.add("➡️ 支出与上月基本持平")
            }
        }

        return insights
    }

    private fun generateSuggestions(
        expenseByCategory: List<Pair<Long, Double>>,
        categories: Map<Long, CategoryEntity>,
        savingsRate: Double,
        totalExpense: Double
    ): List<String> {
        val suggestions = mutableListOf<String>()

        // 基于储蓄率的建议
        if (savingsRate < 30) {
            suggestions.add("💡 建议设置每月储蓄目标，先储蓄再消费")
        }

        // 基于消费结构的建议
        expenseByCategory.take(3).forEach { (categoryId, amount) ->
            val category = categories[categoryId]
            val percentage = if (totalExpense > 0) amount / totalExpense * 100 else 0.0

            when (category?.name) {
                "餐饮美食", "餐饮" -> {
                    if (percentage > 30) {
                        suggestions.add("🍜 餐饮支出较高，建议多在家做饭，减少外卖")
                    }
                }
                "购物消费", "购物" -> {
                    if (percentage > 25) {
                        suggestions.add("🛒 购物支出较多，建议建立购物清单，避免冲动消费")
                    }
                }
                "娱乐休闲", "娱乐" -> {
                    if (percentage > 20) {
                        suggestions.add("🎮 娱乐支出占比较高，可适当控制")
                    }
                }
                "交通出行", "交通" -> {
                    if (percentage > 15) {
                        suggestions.add("🚇 交通支出较多，短途可以考虑骑行或步行")
                    }
                }
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("👏 消费结构合理，继续保持良好的理财习惯！")
        }

        return suggestions
    }

    /**
     * 生成预算分析
     */
    fun generateBudgetAnalysis(
        budgets: List<BudgetEntity>,
        transactions: List<TransactionEntity>,
        categories: Map<Long, CategoryEntity>
    ): BudgetAnalysis {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysRemaining = daysInMonth - currentDay + 1

        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenses.sumOf { it.amount }

        // 总预算
        val totalBudget = budgets.find { it.categoryId == null }
        val totalBudgetAmount = totalBudget?.amount ?: 0.0
        val totalRemaining = totalBudgetAmount - totalExpense
        val dailyAvailable = if (daysRemaining > 0 && totalRemaining > 0) {
            totalRemaining / daysRemaining
        } else 0.0

        // 分类预算
        val categoryBudgetStatus = budgets
            .filter { it.categoryId != null }
            .mapNotNull { budget ->
                val category = categories[budget.categoryId]
                val categoryExpense = expenses
                    .filter { it.categoryId == budget.categoryId }
                    .sumOf { it.amount }
                val usage = if (budget.amount > 0) categoryExpense / budget.amount * 100 else 0.0

                category?.let {
                    CategoryBudgetStatus(
                        name = it.name,
                        icon = it.icon,
                        budgetAmount = budget.amount,
                        usedAmount = categoryExpense,
                        usagePercentage = usage,
                        isOverBudget = categoryExpense > budget.amount
                    )
                }
            }
            .sortedByDescending { it.usagePercentage }

        // 预算警告
        val warnings = categoryBudgetStatus
            .filter { it.usagePercentage > 80 }
            .map {
                if (it.isOverBudget) {
                    "❌ ${it.name}已超支${String.format("%.2f", it.usedAmount - it.budgetAmount)}元"
                } else {
                    "⚠️ ${it.name}预算使用${String.format("%.0f", it.usagePercentage)}%，即将超支"
                }
            }

        return BudgetAnalysis(
            totalBudget = totalBudgetAmount,
            totalUsed = totalExpense,
            totalRemaining = totalRemaining,
            usagePercentage = if (totalBudgetAmount > 0) totalExpense / totalBudgetAmount * 100 else 0.0,
            dailyAvailable = dailyAvailable,
            daysRemaining = daysRemaining,
            categoryBudgets = categoryBudgetStatus,
            warnings = warnings
        )
    }

    /**
     * 生成目标进度分析
     */
    fun generateGoalsAnalysis(goals: List<GoalEntity>): GoalsAnalysis {
        val activeGoals = goals.filter { !it.isCompleted }
        val completedGoals = goals.filter { it.isCompleted }

        val goalStatuses = activeGoals.map { goal ->
            val progress = if (goal.targetAmount > 0) {
                (goal.currentAmount / goal.targetAmount * 100).coerceAtMost(100.0)
            } else 0.0

            // 估算完成时间
            val createdAt = goal.createdAt
            val now = System.currentTimeMillis()
            val daysPassed = ((now - createdAt) / (1000 * 60 * 60 * 24)).toInt()
            val estimatedDays = if (daysPassed > 0 && goal.currentAmount > 0) {
                val dailyRate = goal.currentAmount / daysPassed
                val remaining = goal.targetAmount - goal.currentAmount
                (remaining / dailyRate).toInt()
            } else null

            GoalStatus(
                name = goal.name,
                icon = goal.icon,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                progress = progress,
                estimatedDaysToComplete = estimatedDays,
                isOnTrack = goal.deadline?.let { deadline ->
                    estimatedDays?.let { days ->
                        now + days * 24 * 60 * 60 * 1000L <= deadline
                    } ?: true
                } ?: true
            )
        }

        val suggestions = mutableListOf<String>()
        goalStatuses.forEach { status ->
            when {
                status.progress >= 90 -> suggestions.add("🎉 「${status.name}」即将达成，加油！")
                !status.isOnTrack -> suggestions.add("⏰ 「${status.name}」进度落后，需要加快存钱速度")
                status.progress < 25 -> suggestions.add("💪 「${status.name}」刚刚起步，坚持就是胜利")
            }
        }

        return GoalsAnalysis(
            activeCount = activeGoals.size,
            completedCount = completedGoals.size,
            totalTargetAmount = activeGoals.sumOf { it.targetAmount },
            totalSavedAmount = activeGoals.sumOf { it.currentAmount },
            goalStatuses = goalStatuses,
            suggestions = suggestions
        )
    }
}

// 数据类定义

data class MonthlyAnalysis(
    val month: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val savingsRate: Double,
    val transactionCount: Int,
    val topExpenseCategory: CategoryExpense?,
    val monthOverMonthChange: Double?,
    val insights: List<String>,
    val suggestions: List<String>
)

data class CategoryExpense(
    val name: String,
    val icon: String,
    val amount: Double,
    val percentage: Double
)

data class BudgetAnalysis(
    val totalBudget: Double,
    val totalUsed: Double,
    val totalRemaining: Double,
    val usagePercentage: Double,
    val dailyAvailable: Double,
    val daysRemaining: Int,
    val categoryBudgets: List<CategoryBudgetStatus>,
    val warnings: List<String>
)

data class CategoryBudgetStatus(
    val name: String,
    val icon: String,
    val budgetAmount: Double,
    val usedAmount: Double,
    val usagePercentage: Double,
    val isOverBudget: Boolean
)

data class GoalsAnalysis(
    val activeCount: Int,
    val completedCount: Int,
    val totalTargetAmount: Double,
    val totalSavedAmount: Double,
    val goalStatuses: List<GoalStatus>,
    val suggestions: List<String>
)

data class GoalStatus(
    val name: String,
    val icon: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val progress: Double,
    val estimatedDaysToComplete: Int?,
    val isOnTrack: Boolean
)
