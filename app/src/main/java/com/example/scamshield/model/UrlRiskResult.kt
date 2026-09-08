package com.example.scamshield.model

import com.example.scamshield.risk.analyzer.UrlRole
import com.example.scamshield.risk.intent.MessageIntent

data class UrlRiskResult(
    val url: String,
    val normalizedUrl: String,
    val domain: String,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val confidencePercent: Int,
    val urlRole: UrlRole,
    val intent: MessageIntent,
    val signals: List<RiskSignal>,
    val explanation: String,
    val contextSnippet: String,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED_LOCAL,
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown()
)