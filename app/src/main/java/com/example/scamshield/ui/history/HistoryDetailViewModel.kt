package com.example.scamshield.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamshield.data.ThreatHistoryRepository
import com.example.scamshield.db.ThreatEventEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryDetailState(
    val storedEvent: ThreatEventEntity? = null,
    val isLoading: Boolean = true
)

class HistoryDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ThreatHistoryRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HistoryDetailState())
    val uiState: StateFlow<HistoryDetailState> = _uiState.asStateFlow()

    fun loadHistoryById(historyId: Long) {
        println("Opening history detail: READ ONLY, historyId=$historyId")
        viewModelScope.launch {
            val stored = repository.getHistoryById(historyId)
            if (stored != null) {
                println("Loaded history: id=${stored.id}, score=${stored.riskScore}, level=${stored.riskLevel}")
            } else {
                println("Loaded history null for historyId=$historyId")
            }
            _uiState.update {
                it.copy(
                    storedEvent = stored,
                    isLoading = false
                )
            }
        }
    }
}