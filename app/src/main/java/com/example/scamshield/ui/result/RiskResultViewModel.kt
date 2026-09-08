package com.example.scamshield.ui.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamshield.data.ThreatHistoryRepository
import com.example.scamshield.demo.DemoScenarios
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.model.ScreenshotAnalysisResult
import com.example.scamshield.officekit.OfficeKitSyncManager
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import com.example.scamshield.risk.analyzer.MultiUrlScreenshotAnalyzer
import com.example.scamshield.risk.analyzer.PaymentRiskAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RiskResultState(
    val result: RiskAnalysisResult? = null,
    val screenshotResult: ScreenshotAnalysisResult? = null,
    val paymentPayload: PaymentRiskAnalyzer.PaymentPayload? = null,
    val isLoading: Boolean = true,
    val showVerifyDialog: Boolean = false,
    val showReportDialog: Boolean = false,
    val reportSubmitted: Boolean = false
)

class RiskResultViewModel @JvmOverloads constructor(
    application: Application,
    private val riskEngine: RiskEngine = RuleBasedRiskEngine(),
    private val paymentAnalyzer: PaymentRiskAnalyzer = PaymentRiskAnalyzer(),
    private val multiUrlAnalyzer: MultiUrlScreenshotAnalyzer = MultiUrlScreenshotAnalyzer()
) : AndroidViewModel(application) {

    private val repository = ThreatHistoryRepository.getInstance(application)

    private val _uiState = MutableStateFlow(RiskResultState())
    val uiState: StateFlow<RiskResultState> = _uiState.asStateFlow()

    fun evaluatePayload(rawPayload: String) {
        val trimmed = rawPayload.trim()

        val demoItem = DemoScenarios.list.find { it.title.equals(trimmed, ignoreCase = true) }
        val input = if (demoItem != null) {
            demoItem.input
        } else if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
            AnalysisInput.QrPayload(trimmed)
        } else if (isSingleUrl(trimmed)) {
            AnalysisInput.UrlInput(trimmed)
        } else {
            AnalysisInput.ScreenshotInput(trimmed)
        }

        val result = riskEngine.analyze(input)
        val parsedPayment = paymentAnalyzer.parsePaymentPayload(result.rawPayload)

        // Evaluate multi-URL screenshot analysis
        val screenshotResult = if (input is AnalysisInput.ScreenshotInput || trimmed.lines().size > 1 || trimmed.contains("http://") || trimmed.contains("https://") || trimmed.contains("www.")) {
            multiUrlAnalyzer.analyzeScreenshot(trimmed)
        } else {
            null
        }

        // If screenshot result exists, use its overall score and risk level for top-level result consistency
        val finalResult = if (screenshotResult != null && input is AnalysisInput.ScreenshotInput) {
            result.copy(
                score = screenshotResult.overallScore,
                level = screenshotResult.overallRiskLevel,
                threatCategory = "Screenshot OCR",
                recommendedAction = screenshotResult.explanation,
                confidencePercent = screenshotResult.overallConfidencePercent
            )
        } else {
            result
        }

        // Save to Room DB History and Sync to Office Kit Console asynchronously
        val inputTypeString = when (input) {
            is AnalysisInput.QrPayload -> "QR"
            is AnalysisInput.UrlInput -> "URL"
            is AnalysisInput.TextInput -> "TEXT"
            is AnalysisInput.ScreenshotInput -> "SCREENSHOT"
        }

        val sourceName = when (input) {
            is AnalysisInput.QrPayload -> "QR Scanner"
            is AnalysisInput.UrlInput -> "URL Analyzer"
            is AnalysisInput.TextInput -> "Text Analyzer"
            is AnalysisInput.ScreenshotInput -> "Screenshot Shield"
        }

        viewModelScope.launch {
            try {
                repository.saveAnalysisResult(finalResult, inputTypeString)
                OfficeKitSyncManager.getInstance(getApplication())
                    .syncThreatEvent(finalResult, sourceName)
            } catch (_: Exception) {
            }
        }

        _uiState.update {
            it.copy(
                result = finalResult,
                screenshotResult = screenshotResult,
                paymentPayload = parsedPayment,
                isLoading = false
            )
        }
    }

    private fun isSingleUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.contains("\n") || trimmed.contains("\r") || trimmed.contains(" ")) {
            return false
        }
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("www.", ignoreCase = true) ||
            trimmed.startsWith("upi://", ignoreCase = true)) {
            return true
        }
        val hostPart = trimmed.substringBefore("/").substringBefore("?")
        return hostPart.contains(".") && hostPart.split(".").last().length >= 2 && !hostPart.contains("@")
    }

    fun toggleVerifyDialog(show: Boolean) {
        _uiState.update { it.copy(showVerifyDialog = show) }
    }

    fun toggleReportDialog(show: Boolean) {
        _uiState.update { it.copy(showReportDialog = show) }
    }

    fun submitReport() {
        _uiState.update {
            it.copy(
                showReportDialog = false,
                reportSubmitted = true
            )
        }
    }
}