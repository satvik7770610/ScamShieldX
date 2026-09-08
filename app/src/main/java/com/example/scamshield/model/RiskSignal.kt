package com.example.scamshield.model

data class RiskSignal(
    val code: String,
    val title: String,
    val description: String,
    val severity: SignalSeverity,
    val weight: Int = when (severity) {
        SignalSeverity.INFO -> 10
        SignalSeverity.WARNING -> 20
        SignalSeverity.CRITICAL -> 30
    }
)