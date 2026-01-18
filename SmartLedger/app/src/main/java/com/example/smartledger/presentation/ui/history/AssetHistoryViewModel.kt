package com.example.smartledger.presentation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartledger.data.local.entity.AccountSnapshot
import com.example.smartledger.data.local.entity.MonthlySnapshotEntity
import com.example.smartledger.domain.repository.MonthlySnapshotRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AssetHistoryViewModel @Inject constructor(
    private val snapshotRepository: MonthlySnapshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetHistoryUiState())
    val uiState: StateFlow<AssetHistoryUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        loadSnapshots()
    }

    private fun loadSnapshots() {
        viewModelScope.launch {
            snapshotRepository.getRecent(24).collect { snapshots ->
                val previous = if (snapshots.size >= 2) snapshots[1] else null
                _uiState.value = _uiState.value.copy(
                    snapshots = snapshots,
                    selectedSnapshot = snapshots.firstOrNull(),
                    previousSnapshot = previous,
                    isLoading = false
                )
            }
        }
    }

    fun selectSnapshot(snapshot: MonthlySnapshotEntity) {
        viewModelScope.launch {
            val previous = snapshotRepository.getPreviousMonth(snapshot.year, snapshot.month)
            _uiState.value = _uiState.value.copy(
                selectedSnapshot = snapshot,
                previousSnapshot = previous
            )
        }
    }

    fun createCurrentSnapshot() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)
            try {
                snapshotRepository.createCurrentMonthSnapshot()
            } finally {
                _uiState.value = _uiState.value.copy(isCreating = false)
            }
        }
    }

    fun parseAccountSnapshots(json: String): List<AccountSnapshot> {
        return try {
            val type = object : TypeToken<List<AccountSnapshot>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAvailableYears(): List<Int> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = _uiState.value.snapshots.map { it.year }.distinct()
        return if (years.isEmpty()) listOf(currentYear) else years.sorted().reversed()
    }

    fun filterByYear(year: Int?) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }
}

data class AssetHistoryUiState(
    val snapshots: List<MonthlySnapshotEntity> = emptyList(),
    val selectedSnapshot: MonthlySnapshotEntity? = null,
    val previousSnapshot: MonthlySnapshotEntity? = null,
    val selectedYear: Int? = null,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false
) {
    val filteredSnapshots: List<MonthlySnapshotEntity>
        get() = if (selectedYear != null) {
            snapshots.filter { it.year == selectedYear }
        } else {
            snapshots
        }
}
