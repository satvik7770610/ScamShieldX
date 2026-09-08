package com.example.scamshield.model

data class ScreenshotAnalysisResult(
    val extractedText: String,
    val urlResults: List<UrlRiskResult>,
    val overallScore: Int,
    val overallRiskLevel: RiskLevel,
    val overallConfidencePercent: Int,
    val highRiskCount: Int,
    val suspiciousCount: Int,
    val lowRiskCount: Int,
    val explanation: String
)