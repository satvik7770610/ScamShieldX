package com.example.scamshield.risk.analyzer

import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import java.util.Locale

class UrlRiskAnalyzer(
    private val domainAnalyzer: DomainAnalyzer = DomainAnalyzer()
) {

    private val sensitivePaths = listOf(
        "login", "verify", "secure", "account", "update", "payment",
        "password", "otp", "checkout", "auth", "signin", "netbanking", "kyc", "bank"
    )

    fun analyzeUrl(url: String): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        val cleanUrl = url.trim()

        if (cleanUrl.isBlank()) return emptyList()

        // 1. Check HTTP vs HTTPS
        if (cleanUrl.startsWith("http://", ignoreCase = true)) {
            signals.add(
                RiskSignal(
                    code = "URL_NO_HTTPS",
                    title = "Unencrypted Connection (HTTP)",
                    description = "Destination does not use HTTPS encryption, exposing sensitive traffic in transit.",
                    severity = SignalSeverity.WARNING,
                    weight = 15
                )
            )
        }

        // 2. Domain & Host Level Analysis
        val domainSignals = domainAnalyzer.analyzeDomain(cleanUrl)
        signals.addAll(domainSignals)

        // 3. Path & Query Parameter Sensitivity Check
        val lowerUrl = cleanUrl.lowercase(Locale.ROOT)
        val path = lowerUrl.substringAfter("://", lowerUrl).substringAfter("/", "")

        if (sensitivePaths.any { path.contains(it) }) {
            val host = domainAnalyzer.extractHost(cleanUrl)
            val hasBrandImpersonation = domainSignals.any {
                it.code == "URL_BRAND_IMPERSONATION" || it.code == "URL_LOOKALIKE_BRAND" || it.code == "DOMAIN_TYPOSQUATTING"
            }

            if (hasBrandImpersonation) {
                signals.add(
                    RiskSignal(
                        code = "URL_SUSPICIOUS_PATH",
                        title = "Credential Target on Impersonated Domain",
                        description = "Path ('/$path') targets sensitive authentication or payment credentials on an unverified domain.",
                        severity = SignalSeverity.WARNING,
                        weight = 15
                    )
                )
            } else if (host != null && !isOfficialStandardBrandHost(host)) {
                signals.add(
                    RiskSignal(
                        code = "URL_SENSITIVE_PATH_UNVERIFIED_DOMAIN",
                        title = "Unverified Path Parameter",
                        description = "Path targets login or account parameters on a domain that is not a recognized official brand host.",
                        severity = SignalSeverity.INFO,
                        weight = 3
                    )
                )
            }
        }

        return signals
    }

    private fun isOfficialStandardBrandHost(host: String): Boolean {
        val orgDomain = domainAnalyzer.getOrganizationalDomain(host)
        val standardOfficialDomains = setOf(
            "google.com", "google.co.in", "youtube.com", "youtu.be", "microsoft.com", "apple.com", "icloud.com",
            "amazon.com", "amazon.in", "paytm.com", "paytm.in", "phonepe.com",
            "paypal.com", "paypal.in", "sbi.co.in", "onlinesbi.sbi", "hdfcbank.com", "icicibank.com", "example.com",
            "github.com", "linkedin.com", "wikipedia.org", "twitter.com", "x.com"
        )
        return standardOfficialDomains.contains(orgDomain)
    }
}