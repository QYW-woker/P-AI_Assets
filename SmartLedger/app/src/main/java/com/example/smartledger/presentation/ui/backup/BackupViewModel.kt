package com.example.smartledger.presentation.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.backup.BackupResult
import com.example.smartledger.data.backup.DataBackupManager
import com.example.smartledger.data.backup.ExportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 备份页面ViewModel
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: DataBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        loadBackupInfo()
    }

    private fun loadBackupInfo() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val stats = backupManager.getBackupStats()

                val dataSize = when {
                    stats.estimatedSize < 1024 -> "${stats.estimatedSize} B"
                    stats.estimatedSize < 1024 * 1024 -> "${stats.estimatedSize / 1024} KB"
                    else -> String.format("%.1f MB", stats.estimatedSize / (1024.0 * 1024.0))
                }

                val lastBackupTime = stats.lastBackupTime?.let {
                    dateFormat.format(Date(it))
                }

                _uiState.update {
                    it.copy(
                        recordCount = stats.totalRecords,
                        transactionCount = stats.transactionCount,
                        categoryCount = stats.categoryCount,
                        accountCount = stats.accountCount,
                        budgetCount = stats.budgetCount,
                        goalCount = stats.goalCount,
                        dataSize = dataSize,
                        lastBackupTime = lastBackupTime,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun backup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackingUp = true, errorMessage = null) }

            val result = backupManager.createBackup()

            when (result) {
                is BackupResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isBackingUp = false,
                            lastBackupTime = dateFormat.format(Date()),
                            successMessage = "备份成功"
                        )
                    }
                }
                is BackupResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isBackingUp = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }

            val result = backupManager.exportToJson()

            when (result) {
                is ExportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            successMessage = "导出成功: ${result.filePath}",
                            lastExportPath = result.filePath
                        )
                    }
                }
                is ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, errorMessage = null) }

            val result = backupManager.exportToCsv()

            when (result) {
                is ExportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            successMessage = "导出成功: ${result.filePath}",
                            lastExportPath = result.filePath
                        )
                    }
                }
                is ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun restore() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, errorMessage = null) }

            val result = backupManager.restoreBackup()

            when (result) {
                is BackupResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            successMessage = "数据恢复成功"
                        )
                    }
                    loadBackupInfo()
                }
                is BackupResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                backupManager.clearAllData()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recordCount = 0,
                        transactionCount = 0,
                        categoryCount = 0,
                        accountCount = 0,
                        budgetCount = 0,
                        goalCount = 0,
                        dataSize = "0 KB",
                        successMessage = "所有数据已清除"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "清除失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refresh() {
        loadBackupInfo()
    }
}

/**
 * 备份页面UI状态
 */
data class BackupUiState(
    val recordCount: Int = 0,
    val transactionCount: Int = 0,
    val categoryCount: Int = 0,
    val accountCount: Int = 0,
    val budgetCount: Int = 0,
    val goalCount: Int = 0,
    val dataSize: String = "0 KB",
    val lastBackupTime: String? = null,
    val lastExportPath: String? = null,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isExporting: Boolean = false,
    val isLoading: Boolean = true,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
