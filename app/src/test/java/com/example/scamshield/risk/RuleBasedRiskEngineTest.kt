package com.example.scamshield.risk

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RuleBasedRiskEngineTest {

    private lateinit var riskEngine: RiskEngine

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
    }

    @Test
    fun test1_normalInternshipWithApplicationLink_returnsLowRisk() {
        val text = "Software Engineer Intern at Google. Apply here: https://example.com/apply"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 29", result.score in 0..29)
    }

    @Test
    fun test2_campusAmbassadorWithApplicationLink_returnsLowRisk() {
        val text = "Anthropic Campus Ambassador Opportunity. Join our student program: https://forms.gle/xyz"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 29", result.score in 0..29)
        assertEquals("Campus Ambassador", result.threatCategory)
    }

    @Test
    fun test3_normalJobPostWithSalaryDeadlineAndMultipleUrls_returnsLowRisk() {
        val text = """
            Company Name: Infosys
            Post Name: Data Process
            Salary: Up to 4.3 LPA
            Experience: Freshers
            Batch: 2023-25
            Job Location: Mangalore
            Walk-in Date: 5th September 2026

            Apply Link: https://job4freshers.co.in/infosys-walk-in-data-role/
            Interview Experience: https://yt.openinapp.co/success-story
            WhatsApp community: https://whatsapp.com/channel/...
            YouTube: https://yt.openinapp.co/Job4Governmen
            Telegram: https://telegram.me/job4fresherss
        """.trimIndent()

        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 29", result.score in 0..29)
        assertTrue("Confidence ${result.confidencePercent}% should be >= 80", result.confidencePercent >= 80)
    }

    @Test
    fun test4_jobPostDemandingRegistrationFee_returnsHighRisk() {
        val text = "Company Name: Infosys. Post Name: Data Process. Pay ₹1,000 registration fee to confirm your interview slot."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "RECRUITMENT_FEE_DEMAND" })
    }

    @Test
    fun test5_jobPostRequestingOtp_returnsHighRisk() {
        val text = "Job Offer: Infosys. Share 6-digit OTP to confirm your appointment letter."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "TEXT_OTP_HARVEST" })
    }

    @Test
    fun test6_jobPostWithUpiPaymentRequest_returnsHighRisk() {
        val text = "Campus Ambassador Program. Send ₹500 via UPI to receive your welcome kit."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" })
    }

    @Test
    fun test7_bankAccountThreatWithSuspiciousLink_returnsHighRisk() {
        val text = "SECURITY ALERT: Your account xx6352 will be blocked today due to unusual activity. Verify immediately: https://sbi-netbanking-verify.top/update-kyc. Never share your OTP."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 70", result.score >= 70)
        assertTrue(result.signals.any { it.code == "TEXT_ACCOUNT_THREAT" })
    }

    @Test
    fun test8_prizeWithPaymentRequest_returnsHighRisk() {
        val text = "CONGRATULATIONS! You won ₹50,000 lottery gift. Deposit ₹500 fee to claim."
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "TEXT_REWARD_REFUND_SCAM" })
    }

    @Test
    fun test9_normalYouTubeVideoLink_returnsLowRisk() {
        val text = "Watch our video on YouTube: https://www.youtube.com/watch?v=example"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue(result.score in 0..29)
    }

    @Test
    fun test10_lookalikeBrandDomain_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://paypa1-demo.example.com/login")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "URL_LOOKALIKE_BRAND" })
    }

    @Test
    fun test11_unknownApplicationDomainWithNormalRecruitment_returnsLowRisk() {
        val text = "Data Analyst Opportunity. Apply at https://unverified-job-board.co.in/apply"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 29", result.score <= 29)
    }

    @Test
    fun test12_multipleNormalRecruitmentLinks_returnsLowRisk() {
        val text = "Hiring Freshers! Apply at https://example.com/careers. Watch video: https://youtube.com/v1. Join chat: https://t.me/jobs"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue("Score ${result.score} should be <= 29", result.score in 0..29)
    }

    @Test
    fun test13_syntheticBankScam_returnsCriticalHighRisk() {
        val text = "SECURITY ALERT: Your account will be blocked today. Click https://sbi-netbanking-verify.top/update-kyc"
        val input = AnalysisInput.TextInput(text)
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 70", result.score >= 70)
    }

    @Test
    fun test14_emptyOcrResult_returnsZeroScoreAndLowRisk() {
        val input = AnalysisInput.TextInput("")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
    }

    @Test
    fun testNormalLegitimatePaymentQr_phonePe_returnsLowRiskWithDisclaimer() {
        val input = AnalysisInput.QrPayload("upi://pay?pa=merchant@phonepe&pn=PhonePe%20Merchant")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(15, result.score)
        assertEquals("merchant@phonepe", result.recipient)
        assertTrue(result.signals.any { it.code == "PAYMENT_VALID_FORMAT" })
        assertTrue(result.signals.any { it.code == "PAYMENT_CONTEXT_DISCLAIMER" })
    }

    @Test
    fun testSuspiciousPaymentQr_refundFee_returnsHighRisk() {
        val input = AnalysisInput.QrPayload("upi://pay?pa=agent@upi&pn=Support%20Agent&am=2000&tn=Refund%20fee")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 70", result.score >= 70)
        assertTrue(result.signals.any { it.code == "PAYMENT_SUSPICIOUS_HANDLE" })
    }

    @Test
    fun testMalformedQr_handlesGracefully() {
        val input = AnalysisInput.QrPayload("not_a_valid_upi_or_url_string")
        val result = riskEngine.analyze(input)

        assertNotNull(result)
        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
    }
}