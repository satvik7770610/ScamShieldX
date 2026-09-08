package com.example.scamshield.data

import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThreatHistoryRepositoryTest {

    @Test
    fun testRiskAnalysisResultToThreatEventEntityMapping() {
        val result = RiskAnalysisResult(
            score = 85,
            level = RiskLevel.HIGH_RISK,
            recipient = "paypa1-security.example.com",
            signals = listOf(
                RiskSignal("URL_LOOKALIKE_BRAND", "Look-Alike Brand", "Brand impersonation", SignalSeverity.CRITICAL, 30)
            ),
            threatCategory = "Account Security",
            recommendedAction = "HIGH RISK. Do not enter credentials.",
            rawPayload = "https://paypa1-security.example.com/login"
        )

        assertNotNull(result)
        assertEquals(85, result.score)
        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertEquals("Account Security", result.threatCategory)
        assertEquals("paypa1-security.example.com", result.recipient)
        assertEquals(1, result.signals.size)
    }
}