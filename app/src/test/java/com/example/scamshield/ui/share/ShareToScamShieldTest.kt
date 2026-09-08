package com.example.scamshield.ui.share

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import com.example.scamshield.risk.analyzer.MultiUrlScreenshotAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShareToScamShieldTest {

    private lateinit var riskEngine: RiskEngine
    private lateinit var multiUrlAnalyzer: MultiUrlScreenshotAnalyzer

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
        multiUrlAnalyzer = MultiUrlScreenshotAnalyzer()
    }

    @Test
    fun test1_shareLegitimateUrl_returnsLowRisk() {
        val sharedUrl = "https://github.com/login"
        val result = riskEngine.analyze(AnalysisInput.UrlInput(sharedUrl))

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
        assertEquals("github.com", result.recipient)
    }

    @Test
    fun test2_shareDeceptiveFraudUrl_returnsHighRisk() {
        val sharedUrl = "https://github.com.verification.invalid/"
        val result = riskEngine.analyze(AnalysisInput.UrlInput(sharedUrl))

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun test3_shareRecruitmentScamText_returnsHighRisk() {
        val sharedText = """
            Congratulations! You have been selected for the job.
            To confirm your interview, pay ₹999 registration fee.
            After payment, send the OTP to HR for verification.
        """.trimIndent()

        val result = riskEngine.analyze(AnalysisInput.TextInput(sharedText))

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 80", result.score >= 80)
        assertTrue(result.signals.any { it.code == "RECRUITMENT_FEE_DEMAND" || it.code == "JOB_PAYMENT_SCAM_COMBINATION" })
        assertTrue(result.signals.any { it.code == "TEXT_OTP_HARVEST" || it.code == "JOB_OTP_SCAM_COMBINATION" })
    }

    @Test
    fun test4_shareLegitimateRecruitmentText_returnsLowRisk() {
        val sharedText = "Infosys is hiring freshers for the Data Process role. Candidates can attend the interview on 5th September 2026."
        val result = riskEngine.analyze(AnalysisInput.TextInput(sharedText))

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 29", result.score <= 29)
    }

    @Test
    fun test5_shareTextContainingSuspiciousUrl_fusesTextAndUrlAnalysis() {
        val sharedText = "SECURITY ALERT: Account blocked. Verify immediately at https://sbi-netbanking-verify.top/update-kyc"
        val result = riskEngine.analyze(AnalysisInput.TextInput(sharedText))

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.signals.any { it.code == "TEXT_ACCOUNT_THREAT" })
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun test6_reEvaluatingSamePayload_returnsIdenticalResult() {
        val sharedUrl = "https://github.com.verification.invalid/"
        val result1 = riskEngine.analyze(AnalysisInput.UrlInput(sharedUrl))
        val result2 = riskEngine.analyze(AnalysisInput.UrlInput(sharedUrl))

        assertEquals(result1.score, result2.score)
        assertEquals(result1.level, result2.level)
        assertEquals(result1.signals.size, result2.signals.size)
    }
}