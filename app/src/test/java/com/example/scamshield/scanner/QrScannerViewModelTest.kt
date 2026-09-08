package com.example.scamshield.scanner

import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.RuleBasedRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QrScannerViewModelTest {

    private lateinit var viewModel: QrScannerViewModel

    @Before
    fun setUp() {
        val riskEngine = RuleBasedRiskEngine()
        viewModel = QrScannerViewModel(riskEngine = riskEngine)
    }

    @Test
    fun testScannedPayload_triggersRiskEngineAndUpdatesState() {
        val payload = "upi://pay?pa=refund-agent@upi&pn=Merchant&am=5000&tn=Refund+processing+fee+deposit"
        viewModel.onQrCodeScanned(payload)

        val state = viewModel.uiState.value
        assertEquals(payload, state.scannedPayload)
        assertFalse(state.isScanningActive)
        assertFalse(state.isProcessing)
        assertNotNull(state.analysisResult)
        assertEquals(RiskLevel.HIGH_RISK, state.analysisResult?.level)
    }

    @Test
    fun testDuplicateScan_ignoredWhileInactive() {
        val payload1 = "https://www.google.com"
        val payload2 = "https://phishing.xyz"

        viewModel.onQrCodeScanned(payload1)
        viewModel.onQrCodeScanned(payload2)

        val state = viewModel.uiState.value
        assertEquals(payload1, state.scannedPayload)
    }

    @Test
    fun testEmptyPayload_setsErrorMessage() {
        viewModel.onQrCodeScanned("   ")

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertNull(state.scannedPayload)
    }

    @Test
    fun testToggleTorch_updatesTorchState() {
        assertFalse(viewModel.uiState.value.isTorchEnabled)
        viewModel.toggleTorch()
        assertTrue(viewModel.uiState.value.isTorchEnabled)
        viewModel.toggleTorch()
        assertFalse(viewModel.uiState.value.isTorchEnabled)
    }

    @Test
    fun testResetScanner_resetsState() {
        viewModel.onQrCodeScanned("https://example.com")
        assertNotNull(viewModel.uiState.value.scannedPayload)

        viewModel.resetScanner()
        val state = viewModel.uiState.value
        assertTrue(state.isScanningActive)
        assertNull(state.scannedPayload)
        assertNull(state.analysisResult)
    }
}