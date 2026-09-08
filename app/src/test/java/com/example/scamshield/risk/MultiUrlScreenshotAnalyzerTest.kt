package com.example.scamshield.risk

import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.analyzer.MultiUrlScreenshotAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MultiUrlScreenshotAnalyzerTest {

    private lateinit var analyzer: MultiUrlScreenshotAnalyzer

    @Before
    fun setUp() {
        analyzer = MultiUrlScreenshotAnalyzer()
    }

    @Test
    fun testIqooReskilllDashboardUrl_returnsLowRisk() {
        val text = "Event Dashboard: https://iqoo.reskilll.com/dashboard/iqoo-chennai-event"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.LOW, result.overallRiskLevel)
        assertTrue("Score ${result.overallScore} should be in 0..29 range", result.overallScore in 0..29)
        assertEquals(1, result.urlResults.size)
        assertEquals("iqoo.reskilll.com", result.urlResults[0].domain)
    }

    @Test
    fun testExampleCareersUrl_returnsLowRisk() {
        val text = "Careers portal: https://example.com/careers"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.LOW, result.overallRiskLevel)
        assertTrue(result.overallScore in 0..29)
    }

    @Test
    fun testExampleDashboardUrl_returnsLowRisk() {
        val text = "Dashboard link: https://example.com/dashboard"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.LOW, result.overallRiskLevel)
        assertTrue(result.overallScore in 0..29)
    }

    @Test
    fun testPaypa1SecurityLookalikeUrl_returnsHighRisk() {
        val text = "Security update required at https://paypa1-security.example.com/login"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.HIGH_RISK, result.overallRiskLevel)
        assertTrue("Score ${result.overallScore} should be >= 60", result.overallScore >= 60)
        assertTrue(result.highRiskCount >= 1)
    }

    @Test
    fun testUnknownDomainApplyUrl_returnsLowRisk() {
        val text = "Apply at https://unknown-example.com/apply"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.LOW, result.overallRiskLevel)
        assertTrue(result.overallScore in 0..29)
    }

    @Test
    fun testUnknownDomainWithPaymentRequest_returnsHighRisk() {
        val text = "Pay ₹500 registration fee to confirm interview at https://unknown-example.com/pay"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.HIGH_RISK, result.overallRiskLevel)
        assertTrue(result.highRiskCount >= 1)
    }

    @Test
    fun testUnknownDomainWithOtpRequest_returnsHighRisk() {
        val text = "Enter 6-digit OTP to complete appointment letter: https://unknown-example.com/otp"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.HIGH_RISK, result.overallRiskLevel)
        assertTrue(result.highRiskCount >= 1)
    }

    @Test
    fun testLegitimateRecruitmentWithMultipleExternalUrls_returnsLowRisk() {
        val text = """
            Company Name: Infosys
            Post Name: Data Process
            Salary: Up to 4.3 LPA
            Apply Link: https://job4freshers.co.in/infosys-role
            YouTube: https://yt.openinapp.co/success-story
            WhatsApp: https://whatsapp.com/channel/xyz
            Telegram: https://telegram.me/channel/xyz
        """.trimIndent()

        val result = analyzer.analyzeScreenshot(text)

        assertEquals(RiskLevel.LOW, result.overallRiskLevel)
        assertTrue("Score ${result.overallScore} should be <= 29", result.overallScore in 0..29)
        assertEquals(0, result.highRiskCount)
    }

    @Test
    fun testMultiUrlMix_evaluatesIndividuallyAndOverallHighRisk() {
        val text = """
            Apply for internship here:
            https://legitimate-example.com/apply

            Pay ₹999 verification fee:
            https://danger-example.com/pay
        """.trimIndent()

        val result = analyzer.analyzeScreenshot(text)

        assertEquals(2, result.urlResults.size)

        val linkA = result.urlResults.find { it.normalizedUrl.contains("legitimate-example") }
        val linkB = result.urlResults.find { it.normalizedUrl.contains("danger-example") }

        assertNotNull(linkA)
        assertNotNull(linkB)

        assertEquals(RiskLevel.LOW, linkA?.riskLevel)
        assertEquals(RiskLevel.HIGH_RISK, linkB?.riskLevel)
        assertEquals(RiskLevel.HIGH_RISK, result.overallRiskLevel)
    }

    @Test
    fun testDeduplication_sameUrlRepeatedTwice() {
        val text = "Check https://example.com/apply and https://example.com/apply"
        val result = analyzer.analyzeScreenshot(text)

        assertEquals(1, result.urlResults.size)
    }
}