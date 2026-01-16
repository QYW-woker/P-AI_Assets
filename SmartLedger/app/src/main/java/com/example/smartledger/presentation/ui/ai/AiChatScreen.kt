package com.example.smartledger.presentation.ui.ai

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartledger.presentation.ui.components.AppTopBarWithBack
import com.example.smartledger.presentation.ui.theme.AppColors
import com.example.smartledger.presentation.ui.theme.AppDimens
import com.example.smartledger.presentation.ui.theme.AppShapes
import com.example.smartledger.presentation.ui.theme.AppTypography

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
                onVoiceClick = { /* TODO: 语音输入 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Card)
                    .padding(AppDimens.PaddingL)
            )
        }
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "说点什么，如\"午餐花了35元\"",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextMuted
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppColors.Background,
                unfocusedContainerColor = AppColors.Background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AppColors.Accent
            ),
            shape = AppShapes.Full,
            textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(AppDimens.SpacingS))

        IconButton(onClick = onVoiceClick) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "语音",
                tint = AppColors.TextSecondary
            )
        }

        IconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "发送",
                tint = if (value.isNotBlank()) AppColors.Accent else AppColors.TextMuted
            )
        }
    }
}
