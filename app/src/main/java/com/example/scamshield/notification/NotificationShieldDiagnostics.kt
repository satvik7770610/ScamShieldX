package com.example.scamshield.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationDiagnosticData(
    val isListenerConnected: Boolean = false,
    val lastReceivedTime: Long = 0L,
    val lastSourcePkg: String = "None",
    val lastAnalysisScore: Int = 0,
    val lastAnalysisCategory: String = "None",
    val totalReceivedCount: Int = 0,
    val totalAnalyzedCount: Int = 0,
    val totalIgnoredCount: Int = 0
)

object NotificationShieldDiagnostics {

    private val _data = MutableStateFlow(NotificationDiagnosticData())
    val data: StateFlow<NotificationDiagnosticData> = _data.asStateFlow()

    fun updateListenerConnected(connected: Boolean) {
        _data.update { it.copy(isListenerConnected = connected) }
    }

    fun recordReceived(pkgName: String) {
        _data.update {
            it.copy(
                lastReceivedTime = System.currentTimeMillis(),
                lastSourcePkg = pkgName,
                totalReceivedCount = it.totalReceivedCount + 1
            )
        }
    }

    fun recordIgnored() {
        _data.update { it.copy(totalIgnoredCount = it.totalIgnoredCount + 1) }
    }

    fun recordAnalyzed(score: Int, category: String) {
        _data.update {
            it.copy(
                lastAnalysisScore = score,
                lastAnalysisCategory = category,
                totalAnalyzedCount = it.totalAnalyzedCount + 1
            )
        }
    }
}