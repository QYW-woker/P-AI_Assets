package com.example.smartledger.presentation.ui.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppCard
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.components.DangerButton
import com.example.smartledger.presentation.ui.components.OutlinedAppButton
import com.example.smartledger.presentation.ui.components.PrimaryButton
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppTypography

/**
 * 备份页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "备份与恢复",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingL),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(AppDimens.PaddingL)
        ) {
            // 备份状态卡片
            item {
                BackupStatusCard(
                    recordCount = uiState.recordCount,
                    dataSize = uiState.dataSize,
                    lastBackupTime = uiState.lastBackupTime
                )
            }

            // 备份操作
            item {
                BackupActionsCard(
                    onBackupClick = { viewModel.backup() },
                    onRestoreClick = { viewModel.restore() },
                    isBackingUp = uiState.isBackingUp
                )
            }

            // 导出选项
            item {
                ExportOptionsCard(
                    onExportJson = { viewModel.exportJson() },
                    onExportCsv = { viewModel.exportCsv() }
                )
            }

            // 危险操作
            item {
                DangerZoneCard(
                    onClearData = { viewModel.clearAllData() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(AppDimens.SpacingXXL))
            }
        }
    }
}

/**
 * 备份状态卡片
 */
@Composable
private fun BackupStatusCard(
    recordCount: Int,
    dataSize: String,
    lastBackupTime: String?
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AppColors.Success
            )

            Spacer(modifier = Modifier.padding(AppDimens.SpacingL))

            Column {
                Text(
                    text = "数据安全",
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(AppDimens.SpacingS))
                Text(
                    text = "共 $recordCount 条记录",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "数据大小: $dataSize",
                    style = AppTypography.Caption,
                    color = AppColors.TextMuted
                )
                if (lastBackupTime != null) {
                    Text(
                        text = "上次备份: $lastBackupTime",
                        style = AppTypography.Caption,
                        color = AppColors.TextMuted
                    )
                }
            }
        }
    }
}

/**
 * 备份操作卡片
 */
@Composable
private fun BackupActionsCard(
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    isBackingUp: Boolean
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "备份与恢复",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                PrimaryButton(
                    text = "立即备份",
                    onClick = onBackupClick,
                    icon = Icons.Filled.Upload,
                    isLoading = isBackingUp,
                    modifier = Modifier.weight(1f)
                )

                OutlinedAppButton(
                    text = "恢复数据",
                    onClick = onRestoreClick,
                    icon = Icons.Filled.Download,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 导出选项卡片
 */
@Composable
private fun ExportOptionsCard(
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "导出数据",
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            ExportOption(
                icon = Icons.Filled.FileDownload,
                title = "导出JSON",
                description = "完整数据备份，可用于恢复",
                onClick = onExportJson
            )

            HorizontalDivider(color = AppColors.Divider)

            ExportOption(
                icon = Icons.Filled.FileDownload,
                title = "导出CSV",
                description = "可用Excel打开查看",
                onClick = onExportCsv
            )
        }
    }
}

/**
 * 导出选项项
 */
@Composable
private fun ExportOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.PaddingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppColors.Info
        )

        Spacer(modifier = Modifier.padding(AppDimens.SpacingM))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.BodyMedium,
                color = AppColors.TextPrimary
            )
            Text(
                text = description,
                style = AppTypography.Caption,
                color = AppColors.TextMuted
            )
        }

        OutlinedAppButton(
            text = "导出",
            onClick = onClick,
            borderColor = AppColors.Info,
            contentColor = AppColors.Info
        )
    }
}

/**
 * 危险操作卡片
 */
@Composable
private fun DangerZoneCard(
    onClearData: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "危险操作",
                style = AppTypography.TitleSmall,
                color = AppColors.Error
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            Text(
                text = "清除所有数据将删除所有记账记录、分类、账户等信息，且无法恢复。请谨慎操作。",
                style = AppTypography.BodySmall,
                color = AppColors.TextMuted
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            DangerButton(
                text = "清除所有数据",
                onClick = onClearData,
                icon = Icons.Filled.Delete,
                fullWidth = true
            )
        }
    }
}
