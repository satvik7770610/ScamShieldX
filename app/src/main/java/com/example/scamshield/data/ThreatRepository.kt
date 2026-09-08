package com.example.scamshield.data

import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.ThreatEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThreatRepository {

    private val initialEvents = listOf(
        ThreatEvent(
            eventId = "evt_001",
            timestamp = System.currentTimeMillis() - 1200000,
            threatType = "Text / SMS",
            riskScore = 88,
            riskLevel = RiskLevel.HIGH_RISK,
            anonymizedCategory = "Bank Suspension Threat",
            summary = "Blocked bank account coercion detected with urgent threat link."
        ),
        ThreatEvent(
            eventId = "evt_002",
            timestamp = System.currentTimeMillis() - 7200000,
            threatType = "UPI / Payment QR",
            riskScore = 82,
            riskLevel = RiskLevel.HIGH_RISK,
            anonymizedCategory = "Refund Deposit Fraud",
            summary = "Valid UPI handle with advance processing fee payment note."
        ),
        ThreatEvent(
            eventId = "evt_003",
            timestamp = System.currentTimeMillis() - 86400000,
            threatType = "URL / Web Link",
            riskScore = 45,
            riskLevel = RiskLevel.SUSPICIOUS,
            anonymizedCategory = "Unverified Shortener",
            summary = "Bitly URL redirecting to unverified login domain."
        ),
        ThreatEvent(
            eventId = "evt_004",
            timestamp = System.currentTimeMillis() - 172800000,
            threatType = "URL / Web Link",
            riskScore = 10,
            riskLevel = RiskLevel.LOW,
            anonymizedCategory = "Legitimate Domain",
            summary = "Official search domain verified."
        )
    )

    private val _recentEvents = MutableStateFlow(initialEvents)
    val recentEvents: Flow<List<ThreatEvent>> = _recentEvents.asStateFlow()

    fun addThreatEvent(event: ThreatEvent) {
        val current = _recentEvents.value.toMutableList()
        current.add(0, event)
        _recentEvents.value = current
    }

    fun clearEvents() {
        _recentEvents.value = emptyList()
    }
}