package com.example.scamshield.model

sealed class AnalysisInput {
    data class QrPayload(val rawContent: String) : AnalysisInput()
    data class UrlInput(val url: String) : AnalysisInput()
    data class TextInput(val text: String) : AnalysisInput()
    data class ScreenshotInput(val extractedText: String) : AnalysisInput()
}