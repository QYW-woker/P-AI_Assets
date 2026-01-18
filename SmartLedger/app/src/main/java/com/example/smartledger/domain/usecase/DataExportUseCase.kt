package com.example.smartledger.domain.usecase

import android.content.Context
import android.os.Environment
import com.example.smartledger.data.local.entity.TransactionEntity
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.domain.repository.AccountRepository
import com.example.smartledger.domain.repository.CategoryRepository
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 数据导出用例
 * 支持导出为CSV格式
 */
class DataExportUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * 导出所有交易数据到CSV
     * @return 导出的文件路径
     */
    suspend fun exportTransactionsToCsv(): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 获取所有数据
            val transactions = transactionRepository.getAllTransactions()
            val categories = categoryRepository.getAllActiveCategories().first()
            val accounts = accountRepository.getAllActiveAccounts().first()

            // 创建映射
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }

            // 生成CSV内容
            val csvContent = buildString {
                // BOM for UTF-8 Excel compatibility
                append('\uFEFF')

                // 表头
                appendLine("日期,类型,分类,账户,金额,备注,标签")

                // 数据行
                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    val typeLabel = when (transaction.type) {
                        TransactionType.EXPENSE -> "支出"
                        TransactionType.INCOME -> "收入"
                        TransactionType.TRANSFER -> "转账"
                    }
                    val categoryName = categoryMap[transaction.categoryId]?.name ?: "未知"
                    val accountName = accountMap[transaction.accountId]?.name ?: "未知"
                    val dateStr = dateFormat.format(Date(transaction.date))

                    // CSV格式化（处理特殊字符）
                    appendLine(buildCsvRow(
                        dateStr,
                        typeLabel,
                        categoryName,
                        accountName,
                        String.format("%.2f", transaction.amount),
                        transaction.note,
                        transaction.tags
                    ))
                }
            }

            // 保存文件
            val fileName = "SmartLedger_Transactions_${fileNameFormat.format(Date())}.csv"
            val file = saveToFile(fileName, csvContent)

            ExportResult.Success(
                filePath = file.absolutePath,
                fileName = fileName,
                recordCount = transactions.size
            )
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 导出月度汇总数据
     */
    suspend fun exportMonthlySummaryToCsv(
        startDate: Long,
        endDate: Long
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionRepository.getTransactionsByDateRange(startDate, endDate)
            val categories = categoryRepository.getAllActiveCategories().first()
            val accounts = accountRepository.getAllActiveAccounts().first()

            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }

            // 生成CSV内容
            val csvContent = buildString {
                append('\uFEFF')

                // 汇总信息
                val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                appendLine("=== 月度汇总报告 ===")
                appendLine("时间范围,${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}")
                appendLine("总收入,¥${String.format("%.2f", totalIncome)}")
                appendLine("总支出,¥${String.format("%.2f", totalExpense)}")
                appendLine("结余,¥${String.format("%.2f", totalIncome - totalExpense)}")
                appendLine("交易笔数,${transactions.size}")
                appendLine()

                // 分类汇总 - 支出
                appendLine("=== 支出分类汇总 ===")
                appendLine("分类,金额,笔数,占比")

                val expenseByCategory = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        Triple(
                            categoryMap[categoryId]?.name ?: "未知",
                            txns.sumOf { it.amount },
                            txns.size
                        )
                    }
                    .sortedByDescending { it.second }

                val totalExpenseAmount = expenseByCategory.sumOf { it.second }
                expenseByCategory.forEach { (name, amount, count) ->
                    val percent = if (totalExpenseAmount > 0) amount / totalExpenseAmount * 100 else 0.0
                    appendLine(buildCsvRow(
                        name,
                        String.format("%.2f", amount),
                        count.toString(),
                        String.format("%.1f%%", percent)
                    ))
                }
                appendLine()

                // 分类汇总 - 收入
                appendLine("=== 收入分类汇总 ===")
                appendLine("分类,金额,笔数,占比")

                val incomeByCategory = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        Triple(
                            categoryMap[categoryId]?.name ?: "未知",
                            txns.sumOf { it.amount },
                            txns.size
                        )
                    }
                    .sortedByDescending { it.second }

                val totalIncomeAmount = incomeByCategory.sumOf { it.second }
                incomeByCategory.forEach { (name, amount, count) ->
                    val percent = if (totalIncomeAmount > 0) amount / totalIncomeAmount * 100 else 0.0
                    appendLine(buildCsvRow(
                        name,
                        String.format("%.2f", amount),
                        count.toString(),
                        String.format("%.1f%%", percent)
                    ))
                }
                appendLine()

                // 详细交易记录
                appendLine("=== 详细交易记录 ===")
                appendLine("日期,类型,分类,账户,金额,备注")

                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    val typeLabel = when (transaction.type) {
                        TransactionType.EXPENSE -> "支出"
                        TransactionType.INCOME -> "收入"
                        TransactionType.TRANSFER -> "转账"
                    }
                    appendLine(buildCsvRow(
                        dateFormat.format(Date(transaction.date)),
                        typeLabel,
                        categoryMap[transaction.categoryId]?.name ?: "未知",
                        accountMap[transaction.accountId]?.name ?: "未知",
                        String.format("%.2f", transaction.amount),
                        transaction.note
                    ))
                }
            }

            val dateStr = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date(startDate))
            val fileName = "SmartLedger_Monthly_${dateStr}.csv"
            val file = saveToFile(fileName, csvContent)

            ExportResult.Success(
                filePath = file.absolutePath,
                fileName = fileName,
                recordCount = transactions.size
            )
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 导出为Excel格式（实际为带格式的CSV，Excel可直接打开）
     */
    suspend fun exportToExcelFormat(): ExportResult = withContext(Dispatchers.IO) {
        try {
            val transactions = transactionRepository.getAllTransactions()
            val categories = categoryRepository.getAllActiveCategories().first()
            val accounts = accountRepository.getAllActiveAccounts().first()

            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }

            // 使用制表符分隔，Excel更友好
            val content = buildString {
                append('\uFEFF')

                // 表头
                appendLine("日期\t类型\t分类\t账户\t金额\t备注\t标签")

                transactions.sortedByDescending { it.date }.forEach { transaction ->
                    val typeLabel = when (transaction.type) {
                        TransactionType.EXPENSE -> "支出"
                        TransactionType.INCOME -> "收入"
                        TransactionType.TRANSFER -> "转账"
                    }

                    append(dateFormat.format(Date(transaction.date)))
                    append("\t")
                    append(typeLabel)
                    append("\t")
                    append(categoryMap[transaction.categoryId]?.name ?: "未知")
                    append("\t")
                    append(accountMap[transaction.accountId]?.name ?: "未知")
                    append("\t")
                    append(String.format("%.2f", transaction.amount))
                    append("\t")
                    append(transaction.note.replace("\t", " ").replace("\n", " "))
                    append("\t")
                    appendLine(transaction.tags.replace("\t", " ").replace("\n", " "))
                }
            }

            val fileName = "SmartLedger_Export_${fileNameFormat.format(Date())}.xls"
            val file = saveToFile(fileName, content)

            ExportResult.Success(
                filePath = file.absolutePath,
                fileName = fileName,
                recordCount = transactions.size
            )
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 构建CSV行（处理特殊字符）
     */
    private fun buildCsvRow(vararg values: String): String {
        return values.joinToString(",") { value ->
            val escaped = value
                .replace("\"", "\"\"")
                .replace("\n", " ")
                .replace("\r", " ")

            if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
                "\"$escaped\""
            } else {
                escaped
            }
        }
    }

    /**
     * 保存文件到设备存储
     */
    private fun saveToFile(fileName: String, content: String): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "SmartLedger")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, fileName)
        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                writer.write(content)
            }
        }

        return file
    }
}

/**
 * 导出结果
 */
sealed class ExportResult {
    data class Success(
        val filePath: String,
        val fileName: String,
        val recordCount: Int
    ) : ExportResult()

    data class Error(val message: String) : ExportResult()
}
