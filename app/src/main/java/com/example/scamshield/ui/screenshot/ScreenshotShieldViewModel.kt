package com.example.scamshield.ui.screenshot

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scamshield.analysis.ScreenshotOcrManager
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScreenshotShieldState(
    val selectedImageUri: Uri? = null,
    val isOcrProcessing: Boolean = false,
    val extractedText: String? = null,
    val analysisResult: RiskAnalysisResult? = null,
    val errorMessage: String? = null
)

class ScreenshotShieldViewModel(
    private val riskEngine: RiskEngine = RuleBasedRiskEngine(),
    private val ocrManager: ScreenshotOcrManager = ScreenshotOcrManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotShieldState())
    val uiState: StateFlow<ScreenshotShieldState> = _uiState.asStateFlow()

    fun onImageSelected(context: Context, imageUri: Uri) {
        _uiState.update {
            it.copy(
                selectedImageUri = imageUri,
                isOcrProcessing = true,
                errorMessage = null,
                extractedText = null,
                analysisResult = null
            )
        }

        ocrManager.processImageUri(
            context = context,
            imageUri = imageUri,
            scope = viewModelScope,
            onSuccess = { text ->
                analyzeExtractedText(text)
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isOcrProcessing = false,
                        errorMessage = error
                    )
                }
            }
        )
    }

    fun analyzeExtractedText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    isOcrProcessing = false,
                    errorMessage = "Extracted text was empty."
                )
            }
            return
        }

        val result = riskEngine.analyze(AnalysisInput.ScreenshotInput(trimmed))

        _uiState.update {
            it.copy(
                extractedText = trimmed,
                analysisResult = result,
                isOcrProcessing = false,
                errorMessage = null
            )
        }
    }

    fun runSyntheticDemoCase() {
        val demoText = "Your bank account will be blocked today. Verify your account immediately. Send ₹5,000 to receive your refund. Do not share this message."
        analyzeExtractedText(demoText)
    }

    fun clearResult() {
        _uiState.update {
            it.copy(
                analysisResult = null,
                isOcrProcessing = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}