package com.example.scamshield.risk

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdversarialUrlSecurityTest {

    private lateinit var riskEngine: RiskEngine

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
    }

    @Test
    fun testCase1_officialPaypalSignin_returnsLowRisk() {
        val input = AnalysisInput.UrlInput("https://www.paypal.com/signin")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
    }

    @Test
    fun testCase2_fraudPaypalTyposquatting_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://www.paypa1-security-alert.test/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "URL_LOOKALIKE_BRAND" })
    }

    @Test
    fun testCase3_officialGithubLogin_returnsLowRisk() {
        val input = AnalysisInput.UrlInput("https://github.com/login")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
    }

    @Test
    fun testCase4_fraudGithubDeceptiveSubdomain_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://github.com.verification.invalid/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testCase5_officialGoogleMyAccountSubdomain_returnsLowRisk() {
        val input = AnalysisInput.UrlInput("https://myaccount.google.com/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
    }

    @Test
    fun testCase6_fraudRawIpAddress_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://192.168.0.45/secure/login/google")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "DOMAIN_IP_HOST" })
    }

    @Test
    fun testCase7_fraudAmazonDeceptiveSubdomain_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://amazon.support.account-update.test/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue("Score ${result.score} should be >= 60", result.score >= 60)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testAdversarial_googleComAttacker_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://google.com.attacker.example/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testAdversarial_paypalComEvil_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://paypal.com.evil.example/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testAdversarial_githubComEvil_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://github.com.evil.example/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testAdversarial_evilGoogle_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://evil-google.com/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testAdversarial_googleSecurity_returnsHighRisk() {
        val input = AnalysisInput.UrlInput("https://google-security.example/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.HIGH_RISK, result.level)
        assertTrue(result.signals.any { it.code == "URL_BRAND_IMPERSONATION" })
    }

    @Test
    fun testAdversarial_accountsGoogleCom_returnsLowRisk() {
        val input = AnalysisInput.UrlInput("https://accounts.google.com/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
    }

    @Test
    fun testAdversarial_subdomainGithubCom_returnsLowRisk() {
        val input = AnalysisInput.UrlInput("https://subdomain.github.com/")
        val result = riskEngine.analyze(input)

        assertEquals(RiskLevel.LOW, result.level)
        assertEquals(0, result.score)
    }
}