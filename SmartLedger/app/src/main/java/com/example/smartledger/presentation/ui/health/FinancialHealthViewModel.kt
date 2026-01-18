package com.example.smartledger.presentation.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.domain.usecase.FinancialHealthReport
import com.example.smartledger.domain.usecase.FinancialHealthUseCase
import com.example.smartledger.domain.usecase.HealthLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 财务健康ViewModel
 */
@HiltViewModel
class FinancialHealthViewModel @Inject constructor(
    private val financialHealthUseCase: FinancialHealthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialHealthUiState())
    val uiState: StateFlow<FinancialHealthUiState> = _uiState.asStateFlow()

    init {
        loadHealthReport()
    }

    fun loadHealthReport() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val report = financialHealthUseCase.calculateFinancialHealth()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun refresh() {
        loadHealthReport()
    }
}

/**
 * 财务健康UI状态
 */
data class FinancialHealthUiState(
    val isLoading: Boolean = true,
    val report: FinancialHealthReport? = null,
    val error: String? = null
)
