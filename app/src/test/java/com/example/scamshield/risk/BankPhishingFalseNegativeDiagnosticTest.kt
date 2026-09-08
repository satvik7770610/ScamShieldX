package com.example.scamshield.risk

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class BankPhishingFalseNegativeDiagnosticTest {

    private lateinit var riskEngine: RuleBasedRiskEngine

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
    }

    @Test
    fun test1_bankPhishingFullMessage_returnsHighOrCriticalRisk() {
        val test1 = """
            Dear Customer,

            Your AXIS Bank account xx6352 will be blocked today due to unusual activity.

            To keep your account active, please verify your account immediately by clicking the link below:

            https://axis-secure-update.com

            If not verified within 12 hours, your account will be permanently suspended.

            For security, please share your OTP or login details if asked by our executive.

            Thank you,
            Axis Bank Security Team
        """.trimIndent()

        val result = riskEngine.analyze(AnalysisInput.TextInput(test1))

        println("\n=== TEST 1 (Bank Phishing Full) ===")
        println("Score: ${result.score} (${result.level})")
        println("Signals: ${result.signals.map { it.code }}")

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 85", result.score >= 85)
        assertTrue(result.signals.any { it.code == "BANK_PHISHING_COMBINATION" || it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun test2_bankingWithOtp_returnsHighRisk() {
        val test2 = "Security alert: Your account requires verification. Share the OTP to confirm your identity."
        val result = riskEngine.analyze(AnalysisInput.TextInput(test2))

        println("\n=== TEST 2 (Banking + OTP) ===")
        println("Score: ${result.score} (${result.level})")
        println("Signals: ${result.signals.map { it.code }}")

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "TEXT_OTP_HARVEST" })
    }

    @Test
    fun test3_bankingWithUrgency_returnsHighOrSuspiciousRisk() {
        val test3 = "Your bank account will be suspended within 12 hours. Verify your details immediately."
        val result = riskEngine.analyze(AnalysisInput.TextInput(test3))

        println("\n=== TEST 3 (Banking + Urgency) ===")
        println("Score: ${result.score} (${result.level})")
        println("Signals: ${result.signals.map { it.code }}")

        assertTrue("Level should be HIGH_RISK or SUSPICIOUS", result.level == RiskLevel.HIGH_RISK || result.level == RiskLevel.SUSPICIOUS)
        assertTrue("Score ${result.score} should be >= 50", result.score >= 50)
        assertTrue(result.signals.any { it.code == "TEXT_ACCOUNT_THREAT" || it.code == "TEXT_URGENT_PRESSURE" })
    }

    @Test
    fun test4_legitimateBankAlert_returnsLowRisk() {
        val test4 = "Your Axis Bank transaction of ₹500 was successful."
        val result = riskEngine.analyze(AnalysisInput.TextInput(test4))

        println("\n=== TEST 4 (Legitimate Bank Alert) ===")
        println("Score: ${result.score} (${result.level})")

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 25", result.score <= 25)
    }

    @Test
    fun test5_legitimateBankApp_returnsLowRisk() {
        val test5 = "Your account statement is ready. Open the official banking app to view it."
        val result = riskEngine.analyze(AnalysisInput.TextInput(test5))

        println("\n=== TEST 5 (Legitimate Bank App) ===")
        println("Score: ${result.score} (${result.level})")

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 25", result.score <= 25)
    }

    @Test
    fun test6_recruitmentScam_returnsHighOrCriticalRisk() {
        val test6 = "Congratulations. Pay ₹999 and send OTP to confirm your job."
        val result = riskEngine.analyze(AnalysisInput.TextInput(test6))

        println("\n=== TEST 6 (Recruitment Scam) ===")
        println("Score: ${result.score} (${result.level})")

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 85", result.score >= 85)
    }

    @Test
    fun test7_lookalikeDomain_returnsHighRisk() {
        val test7 = "https://github.com.verification.invalid/"
        val result = riskEngine.analyze(AnalysisInput.TextInput(test7))

        println("\n=== TEST 7 (Lookalike Domain) ===")
        println("Score: ${result.score} (${result.level})")

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" || it.code == "URL_LOOKALIKE_BRAND" })
    }
}
