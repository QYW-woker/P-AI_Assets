package com.example.smartledger.presentation.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 我的页面ViewModel
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val prefs by lazy {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                // 加载用户名
                val username = prefs.getString("username", "用户") ?: "用户"

                // 加载统计数据
                val transactions = transactionRepository.getAllTransactions()
                val totalCount = transactions.size

                // 计算记账天数
                val daysSinceStart = if (transactions.isNotEmpty()) {
                    val firstTransaction = transactions.minByOrNull { it.date }
                    if (firstTransaction != null) {
                        val daysDiff = (System.currentTimeMillis() - firstTransaction.date) / (1000 * 60 * 60 * 24)
                        (daysDiff + 1).toInt().coerceAtLeast(1)
                    } else {
                        0
                    }
                } else {
                    0
                }

                _uiState.value = ProfileUiState(
                    username = username,
                    totalTransactions = totalCount,
                    daysSinceStart = daysSinceStart,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            prefs.edit().putString("username", newUsername).apply()
            _uiState.value = _uiState.value.copy(username = newUsername)
        }
    }

    fun refresh() {
        loadUserData()
    }
}

/**
 * 我的页面UI状态
 */
data class ProfileUiState(
    val username: String = "用户",
    val totalTransactions: Int = 0,
    val daysSinceStart: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
