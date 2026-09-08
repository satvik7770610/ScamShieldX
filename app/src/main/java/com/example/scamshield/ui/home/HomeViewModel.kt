package com.example.scamshield.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamshield.data.ThreatHistoryRepository
import com.example.scamshield.model.CategoryFormatter
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.ThreatEvent
import com.example.scamshield.notification.ScamShieldNotificationListener
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isProtectionActive: Boolean = true,
    val isNotificationShieldActive: Boolean = false,
    val recentThreats: List<ThreatEvent> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ThreatHistoryRepository.getInstance(application)

    val uiState: StateFlow<HomeUiState> = repository.getRecentHistory(5)
        .map { list ->
            val isNotifActive = ScamShieldNotificationListener.isNotificationAccessGranted(getApplication())
            val threatEvents = list.map { entity ->
                val level = when (entity.riskLevel) {
                    "HIGH_RISK" -> RiskLevel.HIGH_RISK
                    "SUSPICIOUS" -> RiskLevel.SUSPICIOUS
                    else -> RiskLevel.LOW
                }
                val formattedCat = CategoryFormatter.formatCategory(entity.threatCategory)
                ThreatEvent(
                    eventId = entity.id.toString(),
                    timestamp = entity.timestamp,
                    threatType = entity.inputType,
                    riskScore = entity.riskScore,
                    riskLevel = level,
                    anonymizedCategory = formattedCat,
                    summary = entity.shortExplanation
                )
            }
            HomeUiState(
                isProtectionActive = true,
                isNotificationShieldActive = isNotifActive,
                recentThreats = threatEvents
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(
                isNotificationShieldActive = ScamShieldNotificationListener.isNotificationAccessGranted(application)
            )
        )
}
