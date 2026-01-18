package com.example.smartledger.presentation.navigation

/**
 * 应用页面路由定义
 */
sealed class Screen(val route: String) {
    // 主要Tab页面
    data object Home : Screen("home")
    data object Stats : Screen("stats")
    data object Record : Screen("record")
    data object Assets : Screen("assets")
    data object Profile : Screen("profile")

    // 记账相关
    data object RecordExpense : Screen("record/expense")
    data object RecordIncome : Screen("record/income")
    data object RecordTransfer : Screen("record/transfer")
    data object TransactionDetail : Screen("transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction/$transactionId"
    }
    data object TransactionEdit : Screen("transaction/{transactionId}/edit") {
        fun createRoute(transactionId: Long) = "transaction/$transactionId/edit"
    }
    data object TransactionList : Screen("transactions")

    // 分类管理
    data object CategoryManage : Screen("category/manage")
    data object CategoryAdd : Screen("category/add/{type}") {
        fun createRoute(type: String) = "category/add/$type"
    }
    data object CategoryEdit : Screen("category/{categoryId}/edit") {
        fun createRoute(categoryId: Long) = "category/$categoryId/edit"
    }

    // 账户管理
    data object AccountManage : Screen("account/manage")
    data object AccountAdd : Screen("account/add")
    data object AccountEdit : Screen("account/{accountId}/edit") {
        fun createRoute(accountId: Long) = "account/$accountId/edit"
    }
    data object AccountDetail : Screen("account/{accountId}") {
        fun createRoute(accountId: Long) = "account/$accountId"
    }

    // 预算
    data object Budget : Screen("budget")
    data object BudgetAdd : Screen("budget/add")
    data object BudgetEdit : Screen("budget/{budgetId}/edit") {
        fun createRoute(budgetId: Long) = "budget/$budgetId/edit"
    }

    // 目标
    data object Goals : Screen("goals")
    data object GoalAdd : Screen("goal/add")
    data object GoalDetail : Screen("goal/{goalId}") {
        fun createRoute(goalId: Long) = "goal/$goalId"
    }
    data object GoalEdit : Screen("goal/{goalId}/edit") {
        fun createRoute(goalId: Long) = "goal/$goalId/edit"
    }

    // AI助手
    data object AiChat : Screen("ai/chat")

    // 财务健康
    data object FinancialHealth : Screen("financial-health")

    // 报告
    data object Report : Screen("report")

    // 固定收支
    data object RecurringTransaction : Screen("recurring-transaction")

    // 历史资产记录
    data object AssetHistory : Screen("asset-history")

    // 投资明细
    data object InvestmentHolding : Screen("investment-holding")

    // 设置
    data object Settings : Screen("settings")
    data object SettingsCurrency : Screen("settings/currency")
    data object SettingsReminder : Screen("settings/reminder")
    data object SettingsTheme : Screen("settings/theme")
    data object SettingsAbout : Screen("settings/about")

    // 备份
    data object Backup : Screen("backup")

    // 搜索
    data object Search : Screen("search")
}

/**
 * 导航参数Keys
 */
object NavArgs {
    const val TRANSACTION_ID = "transactionId"
    const val CATEGORY_ID = "categoryId"
    const val ACCOUNT_ID = "accountId"
    const val BUDGET_ID = "budgetId"
    const val GOAL_ID = "goalId"
    const val TRANSACTION_TYPE = "type"
}

/**
 * 主页Tab页面列表
 */
val mainTabRoutes = listOf(
    Screen.Home.route,
    Screen.Stats.route,
    Screen.Record.route,
    Screen.Assets.route,
    Screen.Profile.route
)

/**
 * 判断是否为主Tab页面
 */
fun isMainTabRoute(route: String?): Boolean {
    return route in mainTabRoutes
}
