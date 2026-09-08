package com.example.scamshield.ui.text

import androidx.lifecycle.ViewModel
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TextAnalyzerState(
    val inputText: String = "",
    val analysisResult: RiskAnalysisResult? = null,
    val errorMessage: String? = null
)

class TextAnalyzerViewModel(
    private val riskEngine: RiskEngine = RuleBasedRiskEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextAnalyzerState())
    val uiState: StateFlow<TextAnalyzerState> = _uiState.asStateFlow()

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun analyzeInput() {
        val trimmed = _uiState.value.inputText.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter or paste text or URL to analyze.") }
            return
        }

        val input = if (isSingleUrl(trimmed)) {
            AnalysisInput.UrlInput(trimmed)
        } else {
            AnalysisInput.TextInput(trimmed)
        }

        val result = riskEngine.analyze(input)
        _uiState.update {
            it.copy(
                analysisResult = result,
                errorMessage = null
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

    fun setDemoText(text: String) {
        _uiState.update { it.copy(inputText = text) }
        analyzeInput()
    }

    fun clearResult() {
        _uiState.update { it.copy(analysisResult = null) }
    }
}