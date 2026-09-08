package com.example.scamshield.ui.dev

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.scamshield.ml.MlPrediction
import com.example.scamshield.ml.ModelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MlTestLabState(
    val inputText: String = "Company Name: Infosys\nPost Name: Data Process\nSalary: Up to 4.3 LPA\nApply: https://job4freshers.co.in/infosys-data",
    val prediction: MlPrediction? = null,
    val inferenceTimeMs: Double = 0.0,
    val isModelLoaded: Boolean = false,
    val modelStatusMessage: String = "",
    val isAnalyzing: Boolean = false
)

class MlTestLabViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MlTestLabState())
    val uiState: StateFlow<MlTestLabState> = _uiState.asStateFlow()

    init {
        checkModelStatus()
    }

    private fun checkModelStatus() {
        val classifier = ModelManager.getClassifier(getApplication())
        _uiState.update {
            it.copy(
                isModelLoaded = classifier.isModelLoaded(),
                modelStatusMessage = ModelManager.modelStatusMessage
            )
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun runMlAnalysis() {
        val text = _uiState.value.inputText
        if (text.isBlank()) return

        _uiState.update { it.copy(isAnalyzing = true) }

        val classifier = ModelManager.getClassifier(getApplication())

        val startTime = System.nanoTime()
        val prediction = classifier.classify(text)
        val endTime = System.nanoTime()

        val latencyMs = (endTime - startTime) / 1_000_000.0

        _uiState.update {
            it.copy(
                prediction = prediction,
                inferenceTimeMs = latencyMs,
                isModelLoaded = classifier.isModelLoaded(),
                modelStatusMessage = ModelManager.modelStatusMessage,
                isAnalyzing = false
            )
        }
    }
}