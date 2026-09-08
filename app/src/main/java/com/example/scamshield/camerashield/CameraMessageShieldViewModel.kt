package com.example.scamshield.camerashield

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CameraMessageShieldState(
    val isCapturing: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isTorchEnabled: Boolean = false,
    val extractedText: String? = null,
    val analysisResult: RiskAnalysisResult? = null,
    val errorMessage: String? = null,
    val showRetakeOption: Boolean = false
)

class CameraMessageShieldViewModel(
    private val riskEngine: RiskEngine = RuleBasedRiskEngine(),
    private val ocrManagerProvider: () -> CameraOcrManager = { CameraOcrManager() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraMessageShieldState())
    val uiState: StateFlow<CameraMessageShieldState> = _uiState.asStateFlow()

    fun captureAndAnalyze(context: Context, controller: CameraCaptureController) {
        if (_uiState.value.isCapturing || _uiState.value.isAnalyzing) return

        _uiState.update {
            it.copy(
                isCapturing = true,
                isAnalyzing = true,
                errorMessage = null,
                showRetakeOption = false,
                extractedText = null,
                analysisResult = null
            )
        }

        val executor = ContextCompat.getMainExecutor(context)
        controller.capturePhoto(
            executor = executor,
            onCaptured = { imageProxy ->
                processCapturedFrame(imageProxy)
            },
            onError = { exception ->
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        isAnalyzing = false,
                        errorMessage = "Camera capture failed: ${exception.localizedMessage ?: "Unknown error"}",
                        showRetakeOption = true
                    )
                }
            }
        )
    }

    private fun processCapturedFrame(imageProxy: ImageProxy) {
        val ocrManager = ocrManagerProvider()
        ocrManager.processImageProxy(
            imageProxy = imageProxy,
            onSuccess = { extractedText ->
                evaluateText(extractedText)
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        isAnalyzing = false,
                        errorMessage = error,
                        showRetakeOption = true
                    )
                }
            }
        )
    }

    fun evaluateText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    isCapturing = false,
                    isAnalyzing = false,
                    errorMessage = "No readable text detected. Move closer, improve lighting, and try again.",
                    showRetakeOption = true
                )
            }
            return
        }

        val result = riskEngine.analyze(AnalysisInput.ScreenshotInput(trimmed))

        _uiState.update {
            it.copy(
                extractedText = trimmed,
                analysisResult = result,
                isCapturing = false,
                isAnalyzing = false,
                errorMessage = null,
                showRetakeOption = false
            )
        }
    }

    fun runSyntheticDemoCase() {
        val demoText = "SECURITY ALERT\n\nYour account xx6352 will be blocked today due to unusual activity.\n\nVerify your account immediately.\n\nClick the link below to prevent suspension: https://sbi-netbanking-verify.top/update-kyc\n\nIf you do not verify within 12 hours, your account will be permanently suspended.\n\nNever share your OTP or password."
        evaluateText(demoText)
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchEnabled = !it.isTorchEnabled) }
    }

    fun resetState() {
        _uiState.update {
            CameraMessageShieldState(isTorchEnabled = _uiState.value.isTorchEnabled)
        }
    }
}