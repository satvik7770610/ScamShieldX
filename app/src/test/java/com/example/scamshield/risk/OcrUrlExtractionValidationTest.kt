package com.example.scamshield.risk

import com.example.scamshield.model.RiskLevel
import com.example.scamshield.risk.analyzer.MultiUrlExtractorAndContextAssociator
import com.example.scamshield.risk.analyzer.MultiUrlScreenshotAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OcrUrlExtractionValidationTest {

    private lateinit var extractor: MultiUrlExtractorAndContextAssociator
    private lateinit var multiUrlAnalyzer: MultiUrlScreenshotAnalyzer

    @Before
    fun setUp() {
        extractor = MultiUrlExtractorAndContextAssociator()
        multiUrlAnalyzer = MultiUrlScreenshotAnalyzer()
    }

    @Test
    fun testValidUrlCandidates_areAccepted() {
        assertTrue(extractor.isValidUrlCandidate("https://example.com", "https://example.com"))
        assertTrue(extractor.isValidUrlCandidate("https://github.com/login", "https://github.com/login"))
        assertTrue(extractor.isValidUrlCandidate("https://myaccount.google.com/", "https://myaccount.google.com/"))
        assertTrue(extractor.isValidUrlCandidate("www.example.com", "https://www.example.com"))
        assertTrue(extractor.isValidUrlCandidate("example.com/path", "https://example.com/path"))
    }

    @Test
    fun testInvalidOcrFragments_areRejected() {
        assertFalse(extractor.isValidUrlCandidate("9-89c9-c721a5e756", "https://9-89c9-c721a5e756"))
        assertFalse(extractor.isValidUrlCandidate("abc123", "https://abc123"))
        assertFalse(extractor.isValidUrlCandidate("random text", "https://random text"))
        assertFalse(extractor.isValidUrlCandidate("some-random-ocr-fragment", "https://some-random-ocr-fragment"))
        assertFalse(extractor.isValidUrlCandidate("123456789", "https://123456789"))
        assertFalse(extractor.isValidUrlCandidate("4.3", "https://4.3"))
        assertFalse(extractor.isValidUrlCandidate("18:45", "https://18:45"))
        assertFalse(extractor.isValidUrlCandidate("2023-25", "https://2023-25"))
        assertFalse(extractor.isValidUrlCandidate("pa", "https://pa"))
    }

    @Test
    fun testJobScamOcrText_extractsZeroUrlsAndEvaluatesHighRiskFromText() {
        val ocrText = """
            Congratulations! You have been selected for the job.
            To confirm your interview, pay ₹999 registration fee.
            After payment, send the OTP to HR for verification.
        """.trimIndent()

        val extractedUrls = extractor.extractAndAssociateContext(ocrText)
        assertEquals(0, extractedUrls.size)

        val result = multiUrlAnalyzer.analyzeScreenshot(ocrText)
        assertEquals(0, result.urlResults.size)
        assertEquals(RiskLevel.HIGH_RISK, result.overallRiskLevel)
        assertTrue("Overall score ${result.overallScore} should be >= 60", result.overallScore >= 60)
        assertTrue(result.explanation.contains("HIGH RISK") || result.explanation.contains("CRITICAL"))
    }

    @Test
    fun testTextWithOneValidUrl_extractsAndAnalyzesOneUrl() {
        val ocrText = "Visit https://github.com/login to access your account."

        val extractedUrls = extractor.extractAndAssociateContext(ocrText)
        assertEquals(1, extractedUrls.size)
        assertEquals("https://github.com/login", extractedUrls[0].rawUrl)

        val result = multiUrlAnalyzer.analyzeScreenshot(ocrText)
        assertEquals(1, result.urlResults.size)
        assertEquals("github.com", result.urlResults[0].domain)
        assertEquals(RiskLevel.LOW, result.urlResults[0].riskLevel)
    }
}