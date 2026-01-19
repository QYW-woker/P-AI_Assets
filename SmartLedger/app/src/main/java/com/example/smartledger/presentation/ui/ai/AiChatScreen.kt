package com.example.smartledger.presentation.ui.ai

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.data.local.entity.TransactionType
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography
import java.util.Locale

/**
 * AI聊天页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    // 语音识别结果处理 - 直接解析并显示确认对话框
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        val data = result.data
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!results.isNullOrEmpty()) {
            // 直接调用语音处理，解析后显示确认对话框
            viewModel.handleVoiceInput(results[0])
        }
    }

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition(context, speechLauncher) { isListening = true }
        } else {
            Toast.makeText(context, "需要录音权限才能使用语音输入", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            AppTopBarWithBack(
                title = "AI助手",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
                .imePadding()
        ) {
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.PaddingL),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                item {
                    Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                }

                items(uiState.messages) { message ->
                    ChatMessageItem(message = message)
                }

                item {
                    Spacer(modifier = Modifier.height(AppDimens.SpacingM))
                }
            }

            // 快捷建议
            QuickSuggestions(
                suggestions = listOf("记一笔", "本月分析", "省钱建议", "预算概览"),
                onSuggestionClick = { suggestion ->
                    viewModel.sendMessage(suggestion)
                },
                modifier = Modifier.padding(horizontal = AppDimens.PaddingL)
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // 输入区域
            ChatInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                onVoiceClick = {
                    // 直接请求权限，不检查 isRecognitionAvailable
                    // 因为 Intent 方式的语音识别可能在 isRecognitionAvailable 返回 false 时仍然可用
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                isListening = isListening,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Card)
                    .padding(AppDimens.PaddingL)
            )
        }
    }

    // 语音记账确认对话框
    if (uiState.showVoiceConfirmDialog && uiState.voiceParsedTransaction != null) {
        VoiceConfirmDialog(
            transaction = uiState.voiceParsedTransaction!!,
            categories = viewModel.getCategories(),
            onConfirm = { viewModel.confirmVoiceTransaction() },
            onCancel = { viewModel.cancelVoiceTransaction() },
            onUpdate = { viewModel.updateVoiceParsedTransaction(it) }
        )
    }
}

/**
 * 启动语音识别
 */
private fun startSpeechRecognition(
    context: android.content.Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onStart: () -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        // 优先使用中文
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出您要记录的内容...")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    // 直接尝试启动语音识别，不检查 queryIntentActivities
    // 因为 Android 11+ 的包可见性限制可能导致查询返回空，但实际 Intent 可以正常工作
    try {
        onStart()
        launcher.launch(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        Toast.makeText(context, "请安装 Google 应用或其他语音识别应用", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "语音识别启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 聊天消息项
 */
@Composable
private fun ChatMessageItem(
    message: ChatMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            // AI头像
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.AccentLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    style = AppTypography.LabelSmall,
                    color = AppColors.Accent
                )
            }
            Spacer(modifier = Modifier.width(AppDimens.SpacingS))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isFromUser) 16.dp else 4.dp,
                        topEnd = if (message.isFromUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(
                    if (message.isFromUser) AppColors.Accent else AppColors.Card
                )
                .padding(AppDimens.PaddingM)
        ) {
            Text(
                text = message.content,
                style = AppTypography.BodyMedium,
                color = if (message.isFromUser) Color.White else AppColors.TextPrimary
            )
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(AppDimens.SpacingS))
            // 用户头像
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = AppTypography.LabelSmall,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 快捷建议
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
    ) {
        items(suggestions) { suggestion ->
            FilterChip(
                selected = false,
                onClick = { onSuggestionClick(suggestion) },
                label = {
                    Text(
                        text = suggestion,
                        style = AppTypography.LabelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = AppColors.Card,
                    labelColor = AppColors.TextSecondary
                )
            )
        }
    }
}

/**
 * 聊天输入栏
 */
@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f),
            placeholder = {
                Text(
                    text = if (isListening) "正在聆听..." else "说点什么，如\"午餐花了35元\"",
                    style = AppTypography.BodyMedium,
                    color = if (isListening) AppColors.Accent else AppColors.TextMuted
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.Background,
                unfocusedContainerColor = AppColors.Background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AppColors.Accent
            ),
            shape = AppShapes.Large,
            textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary),
            maxLines = 3,
            singleLine = false
        )

        Spacer(modifier = Modifier.width(AppDimens.SpacingXS))

        // 语音按钮
        IconButton(
            onClick = onVoiceClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "语音输入",
                tint = if (isListening) AppColors.Accent else AppColors.TextMuted
            )
        }

        // 发送按钮
        IconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank(),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "发送",
                tint = if (value.isNotBlank()) AppColors.Accent else AppColors.TextMuted
            )
        }
    }
}

/**
 * 语音记账确认对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceConfirmDialog(
    transaction: VoiceParsedTransaction,
    categories: List<com.example.smartledger.data.local.entity.CategoryEntity>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onUpdate: (VoiceParsedTransaction) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amount.toString()) }
    var noteText by remember { mutableStateOf(transaction.note) }
    var selectedType by remember { mutableStateOf(transaction.type) }
    var selectedCategoryId by remember { mutableStateOf(transaction.categoryId) }

    // 根据类型筛选分类
    val filteredCategories = categories.filter { it.type == selectedType }

    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Card)
                .padding(AppDimens.PaddingL)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "确认记账",
                    style = AppTypography.TitleMedium,
                    color = AppColors.TextPrimary
                )
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "关闭",
                        tint = AppColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // 类型选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                listOf(
                    TransactionType.EXPENSE to "支出",
                    TransactionType.INCOME to "收入"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            // 切换类型时重置分类
                            val newCategories = categories.filter { it.type == type }
                            selectedCategoryId = newCategories.firstOrNull()?.id
                            onUpdate(transaction.copy(
                                type = type,
                                categoryId = selectedCategoryId,
                                categoryName = newCategories.firstOrNull()?.name ?: ""
                            ))
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == TransactionType.EXPENSE)
                                AppColors.Accent.copy(alpha = 0.2f) else AppColors.Success.copy(alpha = 0.2f),
                            selectedLabelColor = if (type == TransactionType.EXPENSE)
                                AppColors.Accent else AppColors.Success
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // 金额输入
            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    amountText = newValue
                    newValue.toDoubleOrNull()?.let { amount ->
                        onUpdate(transaction.copy(amount = amount))
                    }
                },
                label = { Text("金额") },
                prefix = { Text("¥") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Accent,
                    cursorColor = AppColors.Accent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // 分类选择
            Text(
                text = "分类",
                style = AppTypography.LabelMedium,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(AppDimens.SpacingS))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingS)
            ) {
                items(filteredCategories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = {
                            selectedCategoryId = category.id
                            onUpdate(transaction.copy(
                                categoryId = category.id,
                                categoryName = category.name
                            ))
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(category.icon)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(category.name)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Accent.copy(alpha = 0.2f),
                            selectedLabelColor = AppColors.Accent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingM))

            // 备注输入
            OutlinedTextField(
                value = noteText,
                onValueChange = { newValue ->
                    noteText = newValue
                    onUpdate(transaction.copy(note = newValue))
                },
                label = { Text("备注") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Accent,
                    cursorColor = AppColors.Accent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(AppDimens.SpacingL))

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingM)
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消", color = AppColors.TextSecondary)
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Accent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("确认记账")
                }
            }
        }
    }
}
