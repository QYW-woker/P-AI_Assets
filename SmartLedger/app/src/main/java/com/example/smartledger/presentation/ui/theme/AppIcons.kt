package com.example.smartledger.presentation.ui.theme

/**
 * 统一图标系统 - 使用SF Symbols风格的emoji图标
 * 所有图标集中管理，确保整个应用视觉一致性
 */
object AppIcons {

    // ============ 导航图标 ============
    object Nav {
        const val HOME = "🏠"
        const val STATS = "📊"
        const val BUDGET = "💰"
        const val PROFILE = "👤"
        const val BACK = "←"
        const val CLOSE = "✕"
        const val MORE = "⋯"
    }

    // ============ 操作图标 ============
    object Action {
        const val ADD = "＋"
        const val EDIT = "✏️"
        const val DELETE = "🗑️"
        const val SAVE = "✓"
        const val CANCEL = "✕"
        const val SEARCH = "🔍"
        const val FILTER = "🔽"
        const val REFRESH = "🔄"
        const val SHARE = "📤"
        const val DOWNLOAD = "📥"
    }

    // ============ 交易类型图标 ============
    object Transaction {
        const val EXPENSE = "💸"
        const val INCOME = "💵"
        const val TRANSFER = "🔄"
    }

    // ============ 支出分类图标 ============
    object ExpenseCategory {
        // 餐饮
        const val FOOD = "🍽️"
        const val FOOD_NOODLE = "🍜"
        const val FOOD_BURGER = "🍔"
        const val FOOD_COFFEE = "☕"
        const val FOOD_CAKE = "🍰"

        // 交通
        const val TRANSPORT = "🚗"
        const val TRANSPORT_BUS = "🚌"
        const val TRANSPORT_PLANE = "✈️"
        const val TRANSPORT_GAS = "⛽"
        const val TRANSPORT_TAXI = "🚕"

        // 购物
        const val SHOPPING = "🛒"
        const val SHOPPING_CLOTHES = "👔"
        const val SHOPPING_SHOES = "👟"
        const val SHOPPING_COSMETICS = "💄"
        const val SHOPPING_GIFT = "🎁"

        // 居住
        const val HOUSING = "🏡"
        const val HOUSING_ELECTRIC = "💡"
        const val HOUSING_WATER = "💧"
        const val HOUSING_REPAIR = "🔧"
        const val HOUSING_FURNITURE = "🛋️"

        // 娱乐
        const val ENTERTAINMENT = "🎮"
        const val ENTERTAINMENT_MOVIE = "🎬"
        const val ENTERTAINMENT_MUSIC = "🎵"
        const val ENTERTAINMENT_TRAVEL = "🏖️"
        const val ENTERTAINMENT_SPORT = "⚽"

        // 教育
        const val EDUCATION = "📚"
        const val EDUCATION_BOOK = "📖"
        const val EDUCATION_COURSE = "🎓"

        // 医疗
        const val MEDICAL = "💊"
        const val MEDICAL_HOSPITAL = "🏥"

        // 通讯
        const val PHONE = "📱"
        const val INTERNET = "📶"

        // 其他
        const val PET = "🐕"
        const val BABY = "👶"
        const val HAIRCUT = "💈"
        const val OTHER = "📦"
    }

    // ============ 收入分类图标 ============
    object IncomeCategory {
        const val SALARY = "💰"
        const val BONUS = "🏆"
        const val INVESTMENT = "📈"
        const val SIDEJOB = "💼"
        const val INTEREST = "🏦"
        const val REFUND = "💳"
        const val GIFT = "🧧"
        const val RENTAL = "🏠"
        const val DIVIDEND = "📊"
        const val FREELANCE = "💻"
        const val PRIZE = "🎯"
        const val OTHER = "💵"
    }

    // ============ 预算图标 ============
    object Budget {
        const val TOTAL = "🎯"
        const val CATEGORY = "📂"
        const val WEEKLY = "📆"
        const val BIWEEKLY = "📅"
        const val MONTHLY = "🗓️"
        const val QUARTERLY = "📊"
        const val SEMI_ANNUAL = "📈"
        const val YEARLY = "🎯"
        const val ALERT = "⚠️"
        const val OVER = "🔴"
        const val SAFE = "🟢"
    }

    // ============ 储蓄目标图标 ============
    object Goal {
        const val SAVINGS = "🎯"
        const val DEPOSIT = "💰"
        const val WITHDRAW = "💸"
        const val PROGRESS = "📈"
        const val COMPLETE = "🏆"
        const val TRAVEL = "✈️"
        const val CAR = "🚗"
        const val HOUSE = "🏠"
        const val EDUCATION = "🎓"
        const val WEDDING = "💒"
        const val EMERGENCY = "🆘"
        const val RETIREMENT = "🏖️"
        const val OTHER = "⭐"
    }

    // ============ 设置图标 ============
    object Settings {
        const val MAIN = "⚙️"
        const val CURRENCY = "💱"
        const val DATE = "📅"
        const val WEEK = "📆"
        const val THEME = "🎨"
        const val DARK_MODE = "🌙"
        const val NOTIFICATION = "🔔"
        const val REMINDER = "⏰"
        const val EXPORT = "📤"
        const val IMPORT = "📥"
        const val CLEAR = "🗑️"
        const val VERSION = "📱"
        const val RATE = "⭐"
        const val FEEDBACK = "💬"
        const val ABOUT = "ℹ️"
        const val SECURITY = "🔒"
        const val LANGUAGE = "🌐"
    }

    // ============ 统计图标 ============
    object Stats {
        const val CHART = "📊"
        const val TREND = "📈"
        const val PIE = "🥧"
        const val CALENDAR = "📅"
        const val SUMMARY = "📋"
        const val COMPARE = "⚖️"
    }

    // ============ 状态图标 ============
    object Status {
        const val SUCCESS = "✅"
        const val WARNING = "⚠️"
        const val ERROR = "❌"
        const val INFO = "ℹ️"
        const val LOADING = "⏳"
        const val EMPTY = "📭"
        const val TIP = "💡"
    }

    // ============ 时间图标 ============
    object Time {
        const val TODAY = "📅"
        const val WEEK = "📆"
        const val MONTH = "🗓️"
        const val YEAR = "📊"
        const val CUSTOM = "✏️"
        const val HISTORY = "🕐"
    }

    // ============ 账户图标 ============
    object Account {
        const val CASH = "💵"
        const val BANK = "🏦"
        const val CREDIT = "💳"
        const val ALIPAY = "📱"
        const val WECHAT = "💬"
        const val INVESTMENT = "📈"
        const val OTHER = "💰"
    }

    /**
     * 支出分类图标列表（用于选择器）
     */
    val expenseIconList = listOf(
        ExpenseCategory.FOOD,
        ExpenseCategory.FOOD_NOODLE,
        ExpenseCategory.FOOD_BURGER,
        ExpenseCategory.FOOD_COFFEE,
        ExpenseCategory.FOOD_CAKE,
        ExpenseCategory.TRANSPORT,
        ExpenseCategory.TRANSPORT_BUS,
        ExpenseCategory.TRANSPORT_PLANE,
        ExpenseCategory.TRANSPORT_GAS,
        ExpenseCategory.SHOPPING,
        ExpenseCategory.SHOPPING_CLOTHES,
        ExpenseCategory.SHOPPING_SHOES,
        ExpenseCategory.SHOPPING_COSMETICS,
        ExpenseCategory.HOUSING,
        ExpenseCategory.HOUSING_ELECTRIC,
        ExpenseCategory.HOUSING_WATER,
        ExpenseCategory.HOUSING_REPAIR,
        ExpenseCategory.ENTERTAINMENT,
        ExpenseCategory.ENTERTAINMENT_MOVIE,
        ExpenseCategory.ENTERTAINMENT_MUSIC,
        ExpenseCategory.EDUCATION,
        ExpenseCategory.EDUCATION_BOOK,
        ExpenseCategory.MEDICAL,
        ExpenseCategory.MEDICAL_HOSPITAL,
        ExpenseCategory.PHONE,
        ExpenseCategory.PET,
        ExpenseCategory.BABY,
        ExpenseCategory.OTHER
    )

    /**
     * 收入分类图标列表（用于选择器）
     */
    val incomeIconList = listOf(
        IncomeCategory.SALARY,
        IncomeCategory.BONUS,
        IncomeCategory.INVESTMENT,
        IncomeCategory.SIDEJOB,
        IncomeCategory.INTEREST,
        IncomeCategory.REFUND,
        IncomeCategory.GIFT,
        IncomeCategory.RENTAL,
        IncomeCategory.DIVIDEND,
        IncomeCategory.FREELANCE,
        IncomeCategory.PRIZE,
        IncomeCategory.OTHER
    )

    /**
     * 储蓄目标图标列表（用于选择器）
     */
    val goalIconList = listOf(
        Goal.SAVINGS,
        Goal.TRAVEL,
        Goal.CAR,
        Goal.HOUSE,
        Goal.EDUCATION,
        Goal.WEDDING,
        Goal.EMERGENCY,
        Goal.RETIREMENT,
        Goal.OTHER
    )
}
