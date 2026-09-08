package com.example.scamshield.scanner

import androidx.lifecycle.ViewModel
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class QrScannerState(
    val isScanningActive: Boolean = true,
    val isTorchEnabled: Boolean = false,
    val scannedPayload: String? = null,
    val analysisResult: RiskAnalysisResult? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val showManualInputDialog: Boolean = false
)

class QrScannerViewModel(
    private val riskEngine: RiskEngine = RuleBasedRiskEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrScannerState())
    val uiState: StateFlow<QrScannerState> = _uiState.asStateFlow()

    fun onQrCodeScanned(payload: String) {
        val current = _uiState.value
        if (!current.isScanningActive || current.isProcessing) return

        val trimmed = payload.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Scanned QR code contained empty data.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isScanningActive = false,
                isProcessing = true,
                scannedPayload = trimmed,
                errorMessage = null
            )
        }

        try {
            val result = riskEngine.analyze(AnalysisInput.QrPayload(trimmed))
            _uiState.update {
                it.copy(
                    analysisResult = result,
                    isProcessing = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    errorMessage = "Analysis failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchEnabled = !it.isTorchEnabled) }
    }

    fun toggleManualInputDialog(show: Boolean) {
        _uiState.update { it.copy(showManualInputDialog = show) }
    }

    fun resetScanner() {
        _uiState.update {
            QrScannerState(
                isScanningActive = true,
                isTorchEnabled = _uiState.value.isTorchEnabled
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}