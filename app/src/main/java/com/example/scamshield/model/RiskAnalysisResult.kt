package com.example.scamshield.model

enum class VerificationStatus {
    VERIFIED,
    UNVERIFIED_LOCAL
}

data class ScoreBreakdown(
    val baseScore: Int = 0,
    val textSignalsScore: Int = 0,
    val urlSignalsScore: Int = 0,
    val domainSignalsScore: Int = 0,
    val behaviorSignalsScore: Int = 0,
    val mlScore: Int = 0,
    val mlConfidencePercent: Int = 80,
    val finalScore: Int = 0
)

data class RiskAnalysisResult(
    val score: Int,
    val level: RiskLevel,
    val recipient: String? = null,
    val signals: List<RiskSignal> = emptyList(),
    val threatCategory: String = "General",
    val recommendedAction: String,
    val rawPayload: String,
    val confidencePercent: Int = 85,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED_LOCAL,
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown()
)