package com.example.smartledger.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartledger.presentation.ui.components.AppBottomNav
import com.example.smartledger.presentation.ui.home.HomeScreen
import com.example.smartledger.presentation.ui.stats.StatsScreen
import com.example.smartledger.presentation.ui.record.RecordScreen
import com.example.smartledger.presentation.ui.assets.AssetsScreen
import com.example.smartledger.presentation.ui.profile.ProfileScreen
import com.example.smartledger.presentation.ui.ai.AiChatScreen
import com.example.smartledger.presentation.ui.budget.BudgetScreen
import com.example.smartledger.presentation.ui.goals.GoalDetailScreen
import com.example.smartledger.presentation.ui.goals.GoalsScreen
import com.example.smartledger.presentation.ui.settings.SettingsScreen
import com.example.smartledger.presentation.ui.backup.BackupScreen
import com.example.smartledger.presentation.ui.accounts.AccountDetailScreen
import com.example.smartledger.presentation.ui.accounts.AccountManagementScreen
import com.example.smartledger.presentation.ui.category.CategoryManagementScreen
import com.example.smartledger.presentation.ui.search.SearchScreen
import com.example.smartledger.presentation.ui.transactions.TransactionDetailScreen
import com.example.smartledger.presentation.ui.transactions.TransactionListScreen
import com.example.smartledger.presentation.ui.health.FinancialHealthScreen
import com.example.smartledger.presentation.ui.report.ReportScreen
import com.example.smartledger.presentation.ui.recurring.RecurringTransactionScreen

/**
 * 应用主导航Host
 */
@Composable
fun SmartLedgerNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 判断是否显示底部导航
    val showBottomNav = isMainTabRoute(currentRoute)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomNav) {
                AppBottomNav(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // 弹出到起始目的地，避免堆栈积累
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            // 避免重复创建同一页面
                            launchSingleTop = true
                            // 恢复状态
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
            }
        ) {
            // 主Tab页面
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToRecord = { navController.navigate(Screen.Record.route) },
                    onNavigateToAiChat = { navController.navigate(Screen.AiChat.route) },
                    onNavigateToAssets = { navController.navigate(Screen.Assets.route) },
                    onNavigateToTransactionDetail = { id ->
                        navController.navigate(Screen.TransactionDetail.createRoute(id))
                    },
                    onNavigateToTransactionList = { navController.navigate(Screen.TransactionList.route) }
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTransactionDetail = { id ->
                        navController.navigate(Screen.TransactionDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Record.route) {
                RecordScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable(Screen.Assets.route) {
                AssetsScreen(
                    onNavigateToAccountDetail = { id ->
                        navController.navigate(Screen.AccountDetail.createRoute(id))
                    },
                    onNavigateToAccountManage = { navController.navigate(Screen.AccountManage.route) },
                    onNavigateToAccountAdd = { navController.navigate(Screen.AccountManage.route) }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                    onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                    onNavigateToBackup = { navController.navigate(Screen.Backup.route) },
                    onNavigateToAiChat = { navController.navigate(Screen.AiChat.route) },
                    onNavigateToCategoryManage = { navController.navigate(Screen.CategoryManage.route) },
                    onNavigateToFinancialHealth = { navController.navigate(Screen.FinancialHealth.route) },
                    onNavigateToReport = { navController.navigate(Screen.Report.route) },
                    onNavigateToRecurring = { navController.navigate(Screen.RecurringTransaction.route) }
                )
            }

            // AI聊天
            composable(Screen.AiChat.route) {
                AiChatScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 预算
            composable(Screen.Budget.route) {
                BudgetScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddBudget = { navController.navigate(Screen.BudgetAdd.route) }
                )
            }

            // 目标
            composable(Screen.Goals.route) {
                GoalsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddGoal = { navController.navigate(Screen.GoalAdd.route) },
                    onNavigateToGoalDetail = { id ->
                        navController.navigate(Screen.GoalDetail.createRoute(id))
                    }
                )
            }

            // 设置
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 备份
            composable(Screen.Backup.route) {
                BackupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 交易详情
            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(
                    navArgument(NavArgs.TRANSACTION_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong(NavArgs.TRANSACTION_ID) ?: 0L
                TransactionDetailScreen(
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 交易列表
            composable(Screen.TransactionList.route) {
                TransactionListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToTransactionDetail = { id ->
                        navController.navigate(Screen.TransactionDetail.createRoute(id))
                    }
                )
            }

            // 账户详情
            composable(
                route = Screen.AccountDetail.route,
                arguments = listOf(
                    navArgument(NavArgs.ACCOUNT_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getLong(NavArgs.ACCOUNT_ID) ?: 0L
                AccountDetailScreen(
                    accountId = accountId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 账户管理
            composable(Screen.AccountManage.route) {
                AccountManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 分类管理
            composable(Screen.CategoryManage.route) {
                CategoryManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 添加预算
            composable(Screen.BudgetAdd.route) {
                // BudgetAddScreen(...)
                PlaceholderScreen(title = "添加预算")
            }

            // 添加目标
            composable(Screen.GoalAdd.route) {
                // GoalAddScreen(...)
                PlaceholderScreen(title = "添加目标")
            }

            // 目标详情
            composable(
                route = Screen.GoalDetail.route,
                arguments = listOf(
                    navArgument(NavArgs.GOAL_ID) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getLong(NavArgs.GOAL_ID) ?: 0L
                GoalDetailScreen(
                    goalId = goalId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 搜索
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTransactionDetail = { id ->
                        navController.navigate(Screen.TransactionDetail.createRoute(id))
                    }
                )
            }

            // 财务健康诊断
            composable(Screen.FinancialHealth.route) {
                FinancialHealthScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 报告
            composable(Screen.Report.route) {
                ReportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 固定收支
            composable(Screen.RecurringTransaction.route) {
                RecurringTransactionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * 占位符页面 - 用于尚未实现的页面
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}
