package com.example.scamshield.risk.fusion

import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.ScoreBreakdown
import com.example.scamshield.model.VerificationStatus

data class FusionResult(
    val score: Int,
    val level: RiskLevel,
    val confidencePercent: Int,
    val explanation: String,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED_LOCAL,
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown()
)