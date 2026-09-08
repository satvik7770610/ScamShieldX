package com.example.scamshield.shieldscan

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.RuleBasedRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShieldScanIntegrationTest {

    private lateinit var riskEngine: RuleBasedRiskEngine

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
    }

    @Test
    fun testShieldScanExtractedPhishing_returnsHighRisk() {
        val extractedOcrText = """
            URGENT: Your Axis Bank account xx8912 will be suspended today.
            Verify immediately at https://axis-secure-update.com
            Do not share your OTP with anyone.
        """.trimIndent()

        val input = AnalysisInput.ScreenshotInput(extractedOcrText)
        val result = riskEngine.analyze(input)

        println("ShieldScan Phishing Test - Score: ${result.score}, Level: ${result.level}")

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score should be >= 85", result.score >= 85)
        assertTrue("Category should be Screenshot OCR", result.threatCategory == "Screenshot OCR")
    }

    @Test
    fun testShieldScanExtractedLegitimateText_returnsLowRisk() {
        val extractedOcrText = "Your package has been shipped. Track delivery in official app."

        val input = AnalysisInput.ScreenshotInput(extractedOcrText)
        val result = riskEngine.analyze(input)

        println("ShieldScan Legitimate Test - Score: ${result.score}, Level: ${result.level}")

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score should be <= 25", result.score <= 25)
    }
}
