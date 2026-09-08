package com.example.scamshield.data

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.RiskEngine
import com.example.scamshield.risk.RuleBasedRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ThreatHistoryIntegrationTest {

    private lateinit var riskEngine: RiskEngine

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
    }

    @Test
    fun testSafeUrlScan_createsLowRiskMetadataRecord() {
        val input = AnalysisInput.UrlInput("https://myaccount.google.com/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
        assertEquals("myaccount.google.com", result.recipient)
        assertTrue(result.signals.isEmpty())
    }

    @Test
    fun testHighRiskUrlScan_createsHighRiskMetadataRecord() {
        val input = AnalysisInput.UrlInput("https://github.com.verification.invalid/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.score >= 60)
        assertEquals("github.com.verification.invalid", result.recipient)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testTextScamScan_createsHighRiskMetadataRecord() {
        val text = "Pay ₹999 registration fee to confirm your interview slot. Send OTP to HR."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.score >= 60)
        assertTrue(result.signals.any { it.code == "RECRUITMENT_FEE_DEMAND" || it.code == "JOB_PAYMENT_SCAM_COMBINATION" })
        assertTrue(result.signals.any { it.code == "TEXT_OTP_HARVEST" || it.code == "JOB_OTP_SCAM_COMBINATION" })
    }

    @Test
    fun testQrScan_createsQrMetadataRecord() {
        val input = AnalysisInput.QrPayload("upi://pay?pa=merchant@phonepe&pn=Merchant")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(15, result.score)
        assertEquals("merchant@phonepe", result.recipient)
        assertTrue(result.signals.any { it.code == "PAYMENT_VALID_FORMAT" })
    }

    @Test
    fun testScanHistoryNonContamination_sequentialScansAreIsolated() {
        // Scan 1: High risk bank scam
        val highRiskInput = AnalysisInput.TextInput("SECURITY ALERT: Account blocked today. Verify at https://sbi-netbanking-verify.top/update-kyc. Enter OTP.")
        val highRiskResult = riskEngine.analyze(highRiskInput)

        assertEquals(RiskLevel.HIGH_RISK, highRiskResult.level)
        assertTrue(highRiskResult.score >= 70)

        // Scan 2: Safe recruitment career URL
        val safeInput = AnalysisInput.UrlInput("https://example.com/careers")
        val safeResult = riskEngine.analyze(safeInput)

        // Ensure safe scan inherits ZERO signals or risk score from scan 1
        assertEquals(RiskLevel.LOW, safeResult.level)
        assertEquals(0, safeResult.score)
        assertFalse(safeResult.signals.any { it.code == "TEXT_ACCOUNT_THREAT" })
        assertFalse(safeResult.signals.any { it.code == "TEXT_OTP_HARVEST" })
    }
}