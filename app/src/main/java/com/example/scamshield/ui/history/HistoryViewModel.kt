package com.example.scamshield.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamshield.data.ThreatHistoryRepository
import com.example.scamshield.db.ThreatEventEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryState(
    val historyList: List<ThreatEventEntity> = emptyList(),
    val filteredList: List<ThreatEventEntity> = emptyList(),
    val selectedFilter: String = "ALL", // "ALL", "HIGH_RISK", "SUSPICIOUS", "LOW"
    val selectedEvent: ThreatEventEntity? = null,
    val showClearDialog: Boolean = false,
    val isLoading: Boolean = true
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ThreatHistoryRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HistoryState())
    val uiState: StateFlow<HistoryState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.getAllHistory().collectLatest { list ->
                _uiState.update { current ->
                    val filtered = applyFilter(list, current.selectedFilter)
                    current.copy(
                        historyList = list,
                        filteredList = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.historyList, filter)
            current.copy(
                selectedFilter = filter,
                filteredList = filtered
            )
        }
    }

    private fun applyFilter(list: List<ThreatEventEntity>, filter: String): List<ThreatEventEntity> {
        return when (filter) {
            "HIGH_RISK" -> list.filter { it.riskLevel == "HIGH_RISK" }
            "SUSPICIOUS" -> list.filter { it.riskLevel == "SUSPICIOUS" }
            "LOW" -> list.filter { it.riskLevel == "LOW" }
            else -> list
        }
    }

    fun selectEvent(event: ThreatEventEntity?) {
        _uiState.update { it.copy(selectedEvent = event) }
    }

    fun toggleClearDialog(show: Boolean) {
        _uiState.update { it.copy(showClearDialog = show) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.update { it.copy(showClearDialog = false, selectedEvent = null) }
        }
    }
}