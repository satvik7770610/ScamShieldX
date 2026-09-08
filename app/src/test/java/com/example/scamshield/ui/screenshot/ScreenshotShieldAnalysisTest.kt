package com.example.scamshield.ui.screenshot

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import com.example.scamshield.risk.analyzer.MultiUrlScreenshotAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScreenshotShieldAnalysisTest {

    private lateinit var riskEngine: RiskEngine
    private lateinit var multiUrlAnalyzer: MultiUrlScreenshotAnalyzer

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
        multiUrlAnalyzer = MultiUrlScreenshotAnalyzer()
    }

    @Test
    fun testScreenshotWithNoUrls_evaluatesTextThreatsAndMlIntent() {
        val ocrText = "Security Alert: Your bank account will be blocked today due to unusual activity. Never share your OTP or password."
        val result = riskEngine.analyze(AnalysisInput.ScreenshotInput(ocrText))

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 70", result.score >= 70)
        assertEquals("Screenshot OCR", result.threatCategory)
        assertTrue(result.signals.any { it.code == "TEXT_ACCOUNT_THREAT" })
        assertTrue(result.signals.any { it.code == "TEXT_OTP_HARVEST" })
    }

    @Test
    fun testScreenshotWithOneUrl_evaluatesSingleUrlContextAndThreats() {
        val ocrText = "Pay ₹1,000 registration fee to confirm your interview slot: https://danger-example.com/pay"
        val screenshotResult = multiUrlAnalyzer.analyzeScreenshot(ocrText)

        assertEquals(RiskLevel.HIGH_RISK, screenshotResult.overallRiskLevel)
        assertTrue(screenshotResult.overallScore >= 60)
        assertEquals(1, screenshotResult.urlResults.size)
        assertEquals(1, screenshotResult.highRiskCount)
        assertEquals("danger-example.com", screenshotResult.urlResults[0].domain)
    }

    @Test
    fun testScreenshotWithMultipleUrls_evaluatesIndividuallyAndOverallHighRisk() {
        val ocrText = """
            Apply for internship here:
            https://legitimate-example.com/apply

            Pay ₹999 verification fee:
            https://danger-example.com/pay
        """.trimIndent()

        val screenshotResult = multiUrlAnalyzer.analyzeScreenshot(ocrText)

        assertEquals(2, screenshotResult.urlResults.size)
        assertEquals(1, screenshotResult.lowRiskCount)
        assertEquals(1, screenshotResult.highRiskCount)

        val linkA = screenshotResult.urlResults.find { it.normalizedUrl.contains("legitimate-example") }
        val linkB = screenshotResult.urlResults.find { it.normalizedUrl.contains("danger-example") }

        assertNotNull(linkA)
        assertNotNull(linkB)

        assertEquals(RiskLevel.LOW, linkA?.riskLevel)
        assertEquals(RiskLevel.HIGH_RISK, linkB?.riskLevel)

        assertEquals(RiskLevel.HIGH_RISK, screenshotResult.overallRiskLevel)
    }

    @Test
    fun testLegitimateRecruitmentScreenshot_returnsLowRiskOverall() {
        val ocrText = """
            Company Name: Infosys
            Post Name: Data Process
            Salary: Up to 4.3 LPA
            Experience: Freshers
            Batch: 2023-25
            Job Location: Mangalore
            Walk-in Date: 5th September 2026

            Apply Link: https://job4freshers.co.in/infosys-role
            YouTube: https://yt.openinapp.co/success-story
            WhatsApp: https://whatsapp.com/channel/xyz
        """.trimIndent()

        val screenshotResult = multiUrlAnalyzer.analyzeScreenshot(ocrText)

        assertEquals(RiskLevel.LOW, screenshotResult.overallRiskLevel)
        assertTrue("Overall score ${screenshotResult.overallScore} should be <= 29", screenshotResult.overallScore <= 29)
        assertEquals(0, screenshotResult.highRiskCount)
    }
}