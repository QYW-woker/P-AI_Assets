package com.example.smartledger.data.backup

import android.content.Context
import com.example.smartledger.data.local.entity.BudgetEntity
import com.example.smartledger.data.local.entity.BudgetPeriod
import com.example.smartledger.data.local.entity.GoalEntity
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.BudgetRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.GoalRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据备份管理器
 */
@Singleton
class DataBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository
) {
    private val backupDir: File
        get() = File(context.filesDir, "backups").apply { mkdirs() }

    /**
     * 创建完整备份
     */
    suspend fun createBackup(): BackupResult {
        return try {
            val transactions = transactionRepository.getAllTransactions()
            val categories = categoryRepository.getAllCategories()
            val accounts = accountRepository.getAllAccounts()
            val budgets = budgetRepository.getAllBudgets()
            val goals = goalRepository.getAllGoalsForBackup()

            val backupJson = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("timestamp", System.currentTimeMillis())
                put("transactions", JSONArray().apply {
                    transactions.forEach { tx ->
                        put(JSONObject().apply {
                            put("id", tx.id)
                            put("type", tx.type.name)
                            put("amount", tx.amount)
                            put("categoryId", tx.categoryId)
                            put("accountId", tx.accountId)
                            put("toAccountId", tx.toAccountId)
                            put("note", tx.note)
                            put("tags", tx.tags)
                            put("date", tx.date)
                            put("createdAt", tx.createdAt)
                            put("updatedAt", tx.updatedAt)
                        })
                    }
                })
                put("categories", JSONArray().apply {
                    categories.forEach { cat ->
                        put(JSONObject().apply {
                            put("id", cat.id)
                            put("name", cat.name)
                            put("icon", cat.icon)
                            put("color", cat.color)
                            put("type", cat.type.name)
                            put("parentId", cat.parentId)
                            put("sortOrder", cat.sortOrder)
                            put("isSystem", cat.isSystem)
                            put("isActive", cat.isActive)
                        })
                    }
                })
                put("accounts", JSONArray().apply {
                    accounts.forEach { acc ->
                        put(JSONObject().apply {
                            put("id", acc.id)
                            put("name", acc.name)
                            put("type", acc.type.name)
                            put("icon", acc.icon)
                            put("color", acc.color)
                            put("balance", acc.balance)
                            put("initialBalance", acc.initialBalance)
                            put("currency", acc.currency)
                            put("note", acc.note)
                            put("isIncludeInTotal", acc.isIncludeInTotal)
                            put("sortOrder", acc.sortOrder)
                            put("isActive", acc.isActive)
                        })
                    }
                })
                put("budgets", JSONArray().apply {
                    budgets.forEach { budget ->
                        put(JSONObject().apply {
                            put("id", budget.id)
                            put("categoryId", budget.categoryId)
                            put("amount", budget.amount)
                            put("period", budget.period.name)
                            put("startDate", budget.startDate)
                            put("alertThreshold", budget.alertThreshold)
                            put("isActive", budget.isActive)
                        })
                    }
                })
                put("goals", JSONArray().apply {
                    goals.forEach { goal ->
                        put(JSONObject().apply {
                            put("id", goal.id)
                            put("name", goal.name)
                            put("icon", goal.icon)
                            put("targetAmount", goal.targetAmount)
                            put("currentAmount", goal.currentAmount)
                            put("deadline", goal.deadline)
                            put("note", goal.note)
                            put("createdAt", goal.createdAt)
                            put("isCompleted", goal.isCompleted)
                        })
                    }
                })
            }

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "backup_${dateFormat.format(Date())}.json"
            val file = File(backupDir, fileName)

            file.writeText(backupJson.toString(2))

            BackupResult.Success(file.absolutePath, file.length())
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "备份失败")
        }
    }

    /**
     * 导出为JSON
     */
    suspend fun exportToJson(): ExportResult {
        return try {
            val result = createBackup()
            when (result) {
                is BackupResult.Success -> {
                    val exportDir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
                    val sourceFile = File(result.filePath)
                    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val fileName = "smartledger_export_${dateFormat.format(Date())}.json"
                    val destFile = File(exportDir, fileName)

                    sourceFile.copyTo(destFile, overwrite = true)
                    ExportResult.Success(destFile.absolutePath)
                }
                is BackupResult.Error -> ExportResult.Error(result.message)
            }
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 导出为CSV
     */
    suspend fun exportToCsv(): ExportResult {
        return try {
            val transactions = transactionRepository.getAllTransactions()
            val categories = categoryRepository.getAllCategories()
            val accounts = accountRepository.getAllAccounts()

            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val csvContent = buildString {
                // BOM for Excel UTF-8 support
                append("\uFEFF")
                // Header
                appendLine("日期,类型,分类,账户,金额,备注")

                // Data rows
                transactions.forEach { tx ->
                    val date = dateFormat.format(Date(tx.date))
                    val type = when (tx.type.name) {
                        "EXPENSE" -> "支出"
                        "INCOME" -> "收入"
                        else -> "转账"
                    }
                    val category = categoryMap[tx.categoryId]?.name ?: "未分类"
                    val account = accountMap[tx.accountId]?.name ?: "未知账户"
                    val amount = String.format("%.2f", tx.amount)
                    val note = "\"${tx.note.replace("\"", "\"\"")}\""

                    appendLine("$date,$type,$category,$account,$amount,$note")
                }
            }

            val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "smartledger_export_${fileDateFormat.format(Date())}.csv"
            val exportDir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)

            file.writeText(csvContent)

            ExportResult.Success(file.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 获取备份统计信息
     */
    suspend fun getBackupStats(): BackupStats {
        val transactions = transactionRepository.getAllTransactions()
        val categories = categoryRepository.getAllCategories()
        val accounts = accountRepository.getAllAccounts()
        val budgets = budgetRepository.getAllBudgets()
        val goals = goalRepository.getAllGoalsForBackup()

        val totalRecords = transactions.size + categories.size + accounts.size + budgets.size + goals.size

        // 估算数据大小
        val estimatedSize = (totalRecords * 200L) // 每条记录约200字节

        // 获取最新备份时间
        val latestBackup = backupDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.maxByOrNull { it.lastModified() }

        return BackupStats(
            totalRecords = totalRecords,
            transactionCount = transactions.size,
            categoryCount = categories.size,
            accountCount = accounts.size,
            budgetCount = budgets.size,
            goalCount = goals.size,
            estimatedSize = estimatedSize,
            lastBackupTime = latestBackup?.lastModified()
        )
    }

    /**
     * 获取备份文件列表
     */
    fun getBackupFiles(): List<BackupFileInfo> {
        return backupDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                BackupFileInfo(
                    path = file.absolutePath,
                    name = file.name,
                    size = file.length(),
                    timestamp = file.lastModified()
                )
            } ?: emptyList()
    }

    /**
     * 删除备份文件
     */
    fun deleteBackup(filePath: String): Boolean {
        return File(filePath).delete()
    }

    /**
     * 从最新备份恢复数据
     */
    suspend fun restoreBackup(): BackupResult {
        return try {
            val latestBackup = backupDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.maxByOrNull { it.lastModified() }
                ?: return BackupResult.Error("没有找到备份文件")

            restoreFromFile(latestBackup.absolutePath)
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "恢复失败")
        }
    }

    /**
     * 从指定文件恢复数据
     */
    suspend fun restoreFromFile(filePath: String): BackupResult {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return BackupResult.Error("备份文件不存在")
            }

            // 检查文件大小，防止OOM（限制50MB）
            if (file.length() > 50 * 1024 * 1024) {
                return BackupResult.Error("备份文件过大，无法恢复")
            }

            val jsonContent = file.readText()
            val json = JSONObject(jsonContent)

            // 验证版本
            val version = json.optInt("version", 0)
            if (version > BACKUP_VERSION) {
                return BackupResult.Error("备份版本过高，请更新应用后再恢复")
            }

            // 清除现有数据
            clearAllData()

            // 恢复交易记录
            val transactionsArray = json.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until transactionsArray.length()) {
                val txJson = transactionsArray.getJSONObject(i)
                val transaction = TransactionEntity(
                    id = txJson.optLong("id", 0),
                    type = TransactionType.valueOf(txJson.optString("type", "EXPENSE")),
                    amount = txJson.optDouble("amount", 0.0),
                    categoryId = txJson.optLong("categoryId", 0),
                    accountId = txJson.optLong("accountId", 0),
                    toAccountId = if (txJson.has("toAccountId") && !txJson.isNull("toAccountId"))
                        txJson.optLong("toAccountId") else null,
                    note = txJson.optString("note", ""),
                    tags = txJson.optString("tags", ""),
                    date = txJson.optLong("date", System.currentTimeMillis()),
                    createdAt = txJson.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = txJson.optLong("updatedAt", System.currentTimeMillis())
                )
                transactionRepository.insertTransaction(transaction)
            }

            // 恢复预算
            val budgetsArray = json.optJSONArray("budgets") ?: JSONArray()
            for (i in 0 until budgetsArray.length()) {
                val budgetJson = budgetsArray.getJSONObject(i)
                val budget = BudgetEntity(
                    id = budgetJson.optLong("id", 0),
                    categoryId = if (budgetJson.has("categoryId") && !budgetJson.isNull("categoryId"))
                        budgetJson.optLong("categoryId") else null,
                    amount = budgetJson.optDouble("amount", 0.0),
                    period = BudgetPeriod.valueOf(budgetJson.optString("period", "MONTHLY")),
                    startDate = budgetJson.optLong("startDate", System.currentTimeMillis()),
                    alertThreshold = budgetJson.optDouble("alertThreshold", 0.8).toFloat(),
                    isActive = budgetJson.optBoolean("isActive", true)
                )
                budgetRepository.insertBudget(budget)
            }

            // 恢复目标
            val goalsArray = json.optJSONArray("goals") ?: JSONArray()
            for (i in 0 until goalsArray.length()) {
                val goalJson = goalsArray.getJSONObject(i)
                val goal = GoalEntity(
                    id = goalJson.optLong("id", 0),
                    name = goalJson.optString("name", ""),
                    icon = goalJson.optString("icon", "🎯"),
                    targetAmount = goalJson.optDouble("targetAmount", 0.0),
                    currentAmount = goalJson.optDouble("currentAmount", 0.0),
                    deadline = if (goalJson.has("deadline") && !goalJson.isNull("deadline"))
                        goalJson.optLong("deadline") else null,
                    note = goalJson.optString("note", ""),
                    createdAt = goalJson.optLong("createdAt", System.currentTimeMillis()),
                    isCompleted = goalJson.optBoolean("isCompleted", false)
                )
                goalRepository.insertGoal(goal)
            }

            BackupResult.Success(filePath, file.length())
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "恢复失败")
        }
    }

    /**
     * 清除所有数据
     */
    suspend fun clearAllData() {
        transactionRepository.deleteAllTransactions()
        budgetRepository.deleteAllBudgets()
        goalRepository.deleteAllGoals()
        // 保留系统预设的分类和账户
    }

    companion object {
        const val BACKUP_VERSION = 1
    }
}

sealed class BackupResult {
    data class Success(val filePath: String, val size: Long) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(
        val transactionCount: Int,
        val categoryCount: Int,
        val accountCount: Int,
        val budgetCount: Int,
        val goalCount: Int
    ) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

sealed class ExportResult {
    data class Success(val filePath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

data class BackupStats(
    val totalRecords: Int,
    val transactionCount: Int,
    val categoryCount: Int,
    val accountCount: Int,
    val budgetCount: Int,
    val goalCount: Int,
    val estimatedSize: Long,
    val lastBackupTime: Long?
)

data class BackupFileInfo(
    val path: String,
    val name: String,
    val size: Long,
    val timestamp: Long
)
