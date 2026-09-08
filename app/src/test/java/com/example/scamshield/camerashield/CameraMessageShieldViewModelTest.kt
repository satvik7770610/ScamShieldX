package com.example.scamshield.camerashield

import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.RuleBasedRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CameraMessageShieldViewModelTest {

    private lateinit var viewModel: CameraMessageShieldViewModel

    @Before
    fun setUp() {
        val riskEngine = RuleBasedRiskEngine()
        viewModel = CameraMessageShieldViewModel(riskEngine = riskEngine)
    }

    @Test
    fun testClearScamMessage_evaluatesHighRiskWithMultipleSignals() {
        val scamText = "SECURITY ALERT\n\nYour account xx6352 will be blocked today due to unusual activity.\n\nVerify your account immediately.\n\nClick the link below to prevent suspension: https://sbi-netbanking-verify.top/update-kyc\n\nIf you do not verify within 12 hours, your account will be permanently suspended.\n\nNever share your OTP or password."
        viewModel.evaluateText(scamText)

        val state = viewModel.uiState.value
        assertFalse(state.isAnalyzing)
        assertFalse(state.isCapturing)
        assertNull(state.errorMessage)
        assertNotNull(state.analysisResult)
        assertEquals(RiskLevel.HIGH_RISK, state.analysisResult?.level)
        assertTrue("Score should be >= 70", (state.analysisResult?.score ?: 0) >= 70)

        val signals = state.analysisResult?.signals ?: emptyList()
        assertTrue(signals.any { it.code == "TEXT_ACCOUNT_THREAT" })
        assertTrue(signals.any { it.code == "TEXT_URGENT_PRESSURE" })
        assertTrue(signals.any { it.code == "TEXT_SUSPICIOUS_VERIFICATION" })
        assertTrue(signals.any { it.code == "TEXT_OTP_HARVEST" })
        assertTrue(signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testNormalMessage_evaluatesLowRisk() {
        val normalText = "Hey, let's meet for dinner at 7 PM tonight."
        viewModel.evaluateText(normalText)

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertEquals(RiskLevel.LOW, state.analysisResult?.level)
        assertEquals(0, state.analysisResult?.score)
    }

    @Test
    fun testEmptyOcrResult_setsErrorStateAndRetakeOption() {
        viewModel.evaluateText("   ")

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.showRetakeOption)
        assertNull(state.analysisResult)
        assertTrue(state.errorMessage!!.contains("No readable text detected"))
    }

    @Test
    fun testPartialOcrResult_flagsUrgentSignal() {
        val partialText = "Verify your account immediately"
        viewModel.evaluateText(partialText)

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        val signals = state.analysisResult?.signals ?: emptyList()
        assertTrue(signals.any { it.code == "TEXT_URGENT_PRESSURE" || it.code == "TEXT_SUSPICIOUS_VERIFICATION" })
    }

    @Test
    fun testMessageContainingUrl_flagsPhishingLinkSignals() {
        val urlMessage = "Security update required at https://sbi-netbanking-verify.top/update-kyc"
        viewModel.evaluateText(urlMessage)

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertEquals(RiskLevel.HIGH_RISK, state.analysisResult?.level)
        assertTrue(state.analysisResult?.signals?.any { it.code == "URL_BRAND_IMPERSONATION" } == true)
    }

    @Test
    fun testMessageContainingOtpRequest_flagsOtpHarvestSignal() {
        val otpMessage = "Share 6-digit OTP 829104 to confirm your password reset."
        viewModel.evaluateText(otpMessage)

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertTrue(state.analysisResult?.signals?.any { it.code == "TEXT_OTP_HARVEST" } == true)
    }

    @Test
    fun testMessageContainingUrgentPaymentRequest_flagsThreatAndPaymentSignals() {
        val paymentMessage = "Your account will be blocked today. Send ₹5,000 immediately to prevent disconnection."
        viewModel.evaluateText(paymentMessage)

        val state = viewModel.uiState.value
        assertNotNull(state.analysisResult)
        assertEquals(RiskLevel.HIGH_RISK, state.analysisResult?.level)
        assertTrue(state.analysisResult?.signals?.any { it.code == "TEXT_ACCOUNT_THREAT" } == true)
        assertTrue(state.analysisResult?.signals?.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" } == true)
    }

    @Test
    fun testResetState_clearsErrorAndResetsToReady() {
        viewModel.evaluateText("")
        assertTrue(viewModel.uiState.value.showRetakeOption)

        viewModel.resetState()
        val state = viewModel.uiState.value
        assertFalse(state.showRetakeOption)
        assertNull(state.errorMessage)
        assertFalse(state.isCapturing)
        assertFalse(state.isAnalyzing)
    }
}