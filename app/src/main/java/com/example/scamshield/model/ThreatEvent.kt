package com.example.scamshield.model

data class ThreatEvent(
    val eventId: String,
    val timestamp: Long,
    val threatType: String,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val anonymizedCategory: String,
    val summary: String
)