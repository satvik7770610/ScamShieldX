package com.example.scamshield.notification

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationShieldIntegrationTest {

    private lateinit var riskEngine: RiskEngine

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
    }

    @Test
    fun testHighRiskNotificationContent_triggersHighRiskAnalysis() {
        val notifText = "URGENT ACCOUNT VERIFICATION Your account will be suspended. Verify immediately: https://github.com.verification.invalid/login Send OTP to complete verification."
        val result = riskEngine.analyze(AnalysisInput.TextInput(notifText))

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 80", result.score >= 80)
        assertTrue(result.signals.any { it.code == "TEXT_ACCOUNT_THREAT" })
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
        assertTrue(result.signals.any { it.code == "TEXT_OTP_HARVEST" })
    }

    @Test
    fun testLegitimateNotificationContent_triggersLowRiskAnalysis() {
        val notifText = "INTERNSHIP APPLICATION Your internship application has been received. You can check your application status on the official portal."
        val result = riskEngine.analyze(AnalysisInput.TextInput(notifText))

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 29", result.score <= 29)
    }

    @Test
    fun testDiagnosticsTracking_updatesStatsCorrectly() {
        NotificationShieldDiagnostics.updateListenerConnected(true)
        NotificationShieldDiagnostics.recordReceived("com.whatsapp")
        NotificationShieldDiagnostics.recordAnalyzed(85, "Phishing Link")

        val data = NotificationShieldDiagnostics.data.value

        assertTrue(data.isListenerConnected)
        assertEquals("com.whatsapp", data.lastSourcePkg)
        assertEquals(85, data.lastAnalysisScore)
        assertEquals("Phishing Link", data.lastAnalysisCategory)
        assertEquals(1, data.totalReceivedCount)
        assertEquals(1, data.totalAnalyzedCount)
    }
}