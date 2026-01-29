package com.example.smartledger.domain.ai

import android.util.Log
import com.example.smartledger.data.datastore.AiConfig
import com.example.smartledger.data.datastore.AiProvider
import com.example.smartledger.data.datastore.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AiChatService"

/**
 * AI聊天服务 - 支持多种API提供商
 */
@Singleton
class AiChatService @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    /**
     * 发送消息到AI并获取回复
     */
    suspend fun chat(
        messages: List<ChatMessageData>,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ): AiChatResult {
        val config = settingsDataStore.aiConfigFlow.first()

        return when (config.provider) {
            AiProvider.FREE -> handleFreeChat(messages)
            AiProvider.OPENAI -> callOpenAiApi(config, messages, systemPrompt)
            AiProvider.AZURE_OPENAI -> callAzureOpenAiApi(config, messages, systemPrompt)
            AiProvider.ANTHROPIC -> callAnthropicApi(config, messages, systemPrompt)
            AiProvider.CUSTOM -> callCustomApi(config, messages, systemPrompt)
        }
    }

    /**
     * 免费模式 - 使用本地规则处理
     */
    private fun handleFreeChat(messages: List<ChatMessageData>): AiChatResult {
        // 免费模式返回提示信息，让用户知道可以配置API以获得更好的体验
        return AiChatResult.Success(
            "💡 当前使用免费模式，AI能力有限。\n\n" +
            "您可以在「设置 → AI助手配置」中配置API以获得更智能的对话体验。\n\n" +
            "支持的服务商：\n" +
            "• OpenAI (GPT系列)\n" +
            "• Anthropic (Claude系列)\n" +
            "• Azure OpenAI\n" +
            "• 自定义兼容API"
        )
    }

    /**
     * 调用OpenAI API
     */
    private suspend fun callOpenAiApi(
        config: AiConfig,
        messages: List<ChatMessageData>,
        systemPrompt: String
    ): AiChatResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("${config.baseUrl.ifBlank { "https://api.openai.com/v1" }}/chat/completions")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            val requestBody = buildOpenAiRequestBody(config, messages, systemPrompt)

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                AiChatResult.Success(content)
            } else {
                val errorStream = connection.errorStream
                val errorMessage = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "Unknown error"
                }
                Log.e(TAG, "OpenAI API error: $responseCode - $errorMessage")
                AiChatResult.Error("API请求失败: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "OpenAI API exception", e)
            AiChatResult.Error("网络错误: ${e.message}")
        }
    }

    /**
     * 调用Azure OpenAI API
     */
    private suspend fun callAzureOpenAiApi(
        config: AiConfig,
        messages: List<ChatMessageData>,
        systemPrompt: String
    ): AiChatResult = withContext(Dispatchers.IO) {
        try {
            val apiVersion = "2024-02-15-preview"
            val deploymentName = config.modelName.ifBlank { "gpt-35-turbo" }
            val url = URL("${config.baseUrl}/openai/deployments/$deploymentName/chat/completions?api-version=$apiVersion")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("api-key", config.apiKey)
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            val requestBody = buildOpenAiRequestBody(config, messages, systemPrompt, includeModel = false)

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                AiChatResult.Success(content)
            } else {
                val errorStream = connection.errorStream
                val errorMessage = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "Unknown error"
                }
                Log.e(TAG, "Azure OpenAI API error: $responseCode - $errorMessage")
                AiChatResult.Error("API请求失败: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Azure OpenAI API exception", e)
            AiChatResult.Error("网络错误: ${e.message}")
        }
    }

    /**
     * 调用Anthropic API
     */
    private suspend fun callAnthropicApi(
        config: AiConfig,
        messages: List<ChatMessageData>,
        systemPrompt: String
    ): AiChatResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("${config.baseUrl.ifBlank { "https://api.anthropic.com" }}/v1/messages")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-api-key", config.apiKey)
            connection.setRequestProperty("anthropic-version", "2023-06-01")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            val requestBody = buildAnthropicRequestBody(config, messages, systemPrompt)

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                val content = jsonResponse
                    .getJSONArray("content")
                    .getJSONObject(0)
                    .getString("text")

                AiChatResult.Success(content)
            } else {
                val errorStream = connection.errorStream
                val errorMessage = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "Unknown error"
                }
                Log.e(TAG, "Anthropic API error: $responseCode - $errorMessage")
                AiChatResult.Error("API请求失败: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Anthropic API exception", e)
            AiChatResult.Error("网络错误: ${e.message}")
        }
    }

    /**
     * 调用自定义API（兼容OpenAI格式）
     */
    private suspend fun callCustomApi(
        config: AiConfig,
        messages: List<ChatMessageData>,
        systemPrompt: String
    ): AiChatResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("${config.baseUrl}/chat/completions")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            val requestBody = buildOpenAiRequestBody(config, messages, systemPrompt)

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                AiChatResult.Success(content)
            } else {
                val errorStream = connection.errorStream
                val errorMessage = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "Unknown error"
                }
                Log.e(TAG, "Custom API error: $responseCode - $errorMessage")
                AiChatResult.Error("API请求失败: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Custom API exception", e)
            AiChatResult.Error("网络错误: ${e.message}")
        }
    }

    private fun buildOpenAiRequestBody(
        config: AiConfig,
        messages: List<ChatMessageData>,
        systemPrompt: String,
        includeModel: Boolean = true
    ): JSONObject {
        val messagesArray = JSONArray()

        // 添加系统提示
        messagesArray.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })

        // 添加对话历史
        messages.forEach { msg ->
            messagesArray.put(JSONObject().apply {
                put("role", if (msg.isFromUser) "user" else "assistant")
                put("content", msg.content)
            })
        }

        return JSONObject().apply {
            if (includeModel) {
                put("model", config.modelName.ifBlank { config.defaultModel })
            }
            put("messages", messagesArray)
            put("max_tokens", 1000)
            put("temperature", 0.7)
        }
    }

    private fun buildAnthropicRequestBody(
        config: AiConfig,
        messages: List<ChatMessageData>,
        systemPrompt: String
    ): JSONObject {
        val messagesArray = JSONArray()

        // Anthropic的messages不包含system，system是单独的字段
        messages.forEach { msg ->
            messagesArray.put(JSONObject().apply {
                put("role", if (msg.isFromUser) "user" else "assistant")
                put("content", msg.content)
            })
        }

        return JSONObject().apply {
            put("model", config.modelName.ifBlank { config.defaultModel })
            put("max_tokens", 1000)
            put("system", systemPrompt)
            put("messages", messagesArray)
        }
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """你是一个智能记账助手，帮助用户管理财务、记录收支、分析消费习惯。

你的能力包括：
1. 理解用户的消费描述并提取金额、分类、备注等信息
2. 分析用户的消费习惯和财务状况
3. 提供理财建议和省钱技巧
4. 回答关于预算、储蓄目标的问题

回复要求：
- 使用简洁友好的中文回复
- 适当使用emoji让对话更生动
- 提供实用的理财建议
- 如果识别到消费信息，清晰列出金额、分类、备注"""
    }
}

/**
 * AI聊天消息数据
 */
data class ChatMessageData(
    val content: String,
    val isFromUser: Boolean
)

/**
 * AI聊天结果
 */
sealed class AiChatResult {
    data class Success(val content: String) : AiChatResult()
    data class Error(val message: String) : AiChatResult()
}
