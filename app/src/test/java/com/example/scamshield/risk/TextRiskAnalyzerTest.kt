package com.example.scamshield.risk

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.analyzer.TextRiskAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TextRiskAnalyzerTest {

    private lateinit var textAnalyzer: TextRiskAnalyzer
    private lateinit var riskEngine: RiskEngine

    @Before
    fun setUp() {
        textAnalyzer = TextRiskAnalyzer()
        riskEngine = RuleBasedRiskEngine(textAnalyzer = textAnalyzer)
    }

    @Test
    fun testNormalJobOpeningWithYouTubeLink_returnsLowRisk() {
        val text = "Watch our company job opening video on YouTube: https://www.youtube.com/watch?v=example_job_opening"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be in 0..29 range", result.score in 0..29)
        assertTrue(result.signals.isEmpty())
    }

    @Test
    fun testNormalInformationalMessage_returnsLowRisk() {
        val text = "Hey, are we still meeting for lunch today at 1 PM?"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
        assertTrue(result.signals.isEmpty())
    }

    @Test
    fun testBankAccountSuspensionWithOtpRequest_returnsHighRisk() {
        val text = "SECURITY ALERT: Your account xx6352 will be blocked today due to unusual activity. Verify immediately: https://sbi-netbanking-verify.top/update-kyc. Never share your OTP."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 70", result.score >= 70)
        assertTrue(result.signals.any { it.code == "TEXT_ACCOUNT_THREAT" })
        assertTrue(result.signals.any { it.code == "TEXT_URGENT_PRESSURE" })
        assertTrue(result.signals.any { it.code == "TEXT_OTP_HARVEST" })
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testUrgentVerificationMessage_returnsSuspiciousOrHighRisk() {
        val text = "Verify your account immediately to prevent disconnection."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertTrue("Score ${result.score} should be >= 30", result.score >= 30)
        assertTrue(result.signals.any { it.code == "TEXT_URGENT_PRESSURE" || it.code == "TEXT_SUSPICIOUS_VERIFICATION" })
    }

    @Test
    fun testPrizeScam_returnsHighRisk() {
        val text = "CONGRATULATIONS! You won ₹50,000 lottery gift. Deposit ₹500 fee to claim."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.score >= 70)
        assertTrue(result.signals.any { it.code == "TEXT_REWARD_REFUND_SCAM" })
        assertTrue(result.signals.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" })
    }

    @Test
    fun testRefundScam_returnsHighRisk() {
        val text = "Send ₹5,000 immediately to receive your pending refund."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertTrue(result.score >= 35)
        assertTrue(result.signals.any { it.code == "TEXT_REWARD_REFUND_SCAM" })
        assertTrue(result.signals.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" })
    }

    @Test
    fun testPaymentRequest_flagsMoneyTransferSignal() {
        val text = "Please send ₹500 for the dinner bill."
        val signals = textAnalyzer.analyzeText(text)

        assertTrue(signals.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" })
    }

    @Test
    fun testMessageContainingOnlyNormalUrl_returnsLowRisk() {
        val text = "Check out https://www.google.com"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
        assertTrue(result.signals.isEmpty())
    }

    @Test
    fun testEmptyOcrText_returnsZeroScoreAndLowRisk() {
        val input = AnalysisInput.TextInput("")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
        assertTrue(result.signals.isEmpty())
    }

    @Test
    fun testGarbledOcrText_returnsLowRiskWithoutCrash() {
        val text = "a#$ %^& *() 123 !@#"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertNotNull(result)
        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
        assertTrue(result.signals.isEmpty())
    }
}