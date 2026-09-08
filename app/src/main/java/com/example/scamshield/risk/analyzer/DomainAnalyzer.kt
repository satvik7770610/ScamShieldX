package com.example.scamshield.risk.analyzer

import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import java.net.URI
import java.util.Locale

data class ProtectedBrand(
    val brandName: String,
    val officialDomains: Set<String>
)

class DomainAnalyzer {

    private val protectedBrands = listOf(
        ProtectedBrand("paypal", setOf("paypal.com", "paypal.in", "paypal.co.uk", "paypal.me")),
        ProtectedBrand("paytm", setOf("paytm.com", "paytm.in")),
        ProtectedBrand("phonepe", setOf("phonepe.com")),
        ProtectedBrand("google", setOf("google.com", "google.co.in", "youtube.com", "youtu.be")),
        ProtectedBrand("youtube", setOf("youtube.com", "youtu.be")),
        ProtectedBrand("gpay", setOf("pay.google.com", "google.com")),
        ProtectedBrand("sbi", setOf("sbi.co.in", "onlinesbi.sbi", "sbi.sbi")),
        ProtectedBrand("hdfc", setOf("hdfcbank.com", "hdfc.com")),
        ProtectedBrand("icici", setOf("icicibank.com")),
        ProtectedBrand("axis", setOf("axisbank.com", "axisbank.co.in")),
        ProtectedBrand("kotak", setOf("kotak.com", "kotak.in")),
        ProtectedBrand("pnb", setOf("pnbindia.in")),
        ProtectedBrand("canara", setOf("canarabank.com")),
        ProtectedBrand("baroda", setOf("bankofbaroda.in", "bobcard.co.in")),
        ProtectedBrand("indusind", setOf("indusind.com")),
        ProtectedBrand("citi", setOf("citibank.com", "citibank.co.in")),
        ProtectedBrand("hsbc", setOf("hsbc.com", "hsbc.co.in")),
        ProtectedBrand("chase", setOf("chase.com")),
        ProtectedBrand("wellsfargo", setOf("wellsfargo.com")),
        ProtectedBrand("bankofamerica", setOf("bankofamerica.com")),
        ProtectedBrand("amazon", setOf("amazon.com", "amazon.in")),
        ProtectedBrand("apple", setOf("apple.com", "icloud.com")),
        ProtectedBrand("microsoft", setOf("microsoft.com", "live.com", "outlook.com")),
        ProtectedBrand("netflix", setOf("netflix.com")),
        ProtectedBrand("github", setOf("github.com")),
        ProtectedBrand("linkedin", setOf("linkedin.com"))
    )

    private val suspiciousTlds = setOf(
        "xyz", "top", "tk", "ml", "ga", "cf", "gq", "site", "online", "club", "work", "info", "support", "test", "invalid"
    )

    private val knownShorteners = setOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "rb.gy", "cutt.ly", "shorturl.at", "ow.ly"
    )

    fun analyzeDomain(url: String): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        val cleanUrl = url.trim()

        val host = extractHost(cleanUrl) ?: return emptyList()
        val lowerHost = host.lowercase(Locale.ROOT)

        // 1. Embedded Credentials check (e.g., https://paypal.com@scam.com)
        if (cleanUrl.contains("@") && cleanUrl.indexOf("@") < cleanUrl.indexOf(host) + host.length) {
            signals.add(
                RiskSignal(
                    code = "URL_EMBEDDED_CREDENTIALS",
                    title = "Embedded User Credentials in URL",
                    description = "URL contains an '@' symbol before the domain host, which tricks browsers into connecting to an unintended server.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 35
                )
            )
        }

        // 2. Punycode / IDN check
        if (lowerHost.contains("xn--")) {
            signals.add(
                RiskSignal(
                    code = "URL_PUNYCODE_IDN",
                    title = "Punycode IDN Domain Representation",
                    description = "Host uses Internationalized Domain Name Punycode ('xn--'), frequently used in homograph domain spoofing attacks.",
                    severity = SignalSeverity.WARNING,
                    weight = 20
                )
            )
        }

        // 3. Raw IP Address check
        if (isIpAddress(lowerHost)) {
            signals.add(
                RiskSignal(
                    code = "DOMAIN_IP_HOST",
                    title = "Raw IP Host Address",
                    description = "Destination uses a numeric IP address ($host) instead of a registered domain name.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 45
                )
            )
            return signals
        }

        // 4. URL Shortener check
        if (knownShorteners.any { lowerHost == it || lowerHost.endsWith(".$it") }) {
            signals.add(
                RiskSignal(
                    code = "DOMAIN_SHORTENED_URL",
                    title = "Shortened URL Masking Destination",
                    description = "Destination uses a URL shortener service which hides the true destination host.",
                    severity = SignalSeverity.WARNING,
                    weight = 25
                )
            )
        }

        // 5. Suspicious TLD check
        val tld = lowerHost.substringAfterLast('.', "")
        if (suspiciousTlds.contains(tld)) {
            signals.add(
                RiskSignal(
                    code = "DOMAIN_SUSPICIOUS_TLD",
                    title = "High-Risk Domain TLD",
                    description = "The domain uses top-level domain (.$tld) heavily associated with spam and phishing campaigns.",
                    severity = SignalSeverity.WARNING,
                    weight = 25
                )
            )
        }

        // 6. Subdomain count & structural anomaly check
        val labels = lowerHost.split('.')
        if (labels.size >= 5) {
            signals.add(
                RiskSignal(
                    code = "URL_EXCESSIVE_SUBDOMAINS",
                    title = "Excessive Subdomains Detected",
                    description = "Host contains ${labels.size - 2} subdomain levels ($host), a structural pattern often used to obscure malicious domains.",
                    severity = SignalSeverity.WARNING,
                    weight = 15
                )
            )
        }

        // 7. Rigorous Boundary & Brand Impersonation Analysis
        val orgDomain = getOrganizationalDomain(lowerHost)
        val hostParts = lowerHost.split('-').flatMap { it.split('.') }.filter { it.isNotBlank() }

        for (brand in protectedBrands) {
            val isOfficialDomain = brand.officialDomains.any { orgDomain == it || orgDomain.endsWith(".$it") }

            if (isOfficialDomain) {
                // Legitimate brand subdomain (e.g. myaccount.google.com, accounts.google.com, gist.github.com)
                continue
            }

            var isLookalike = false
            var isImpersonation = false
            var detectedLabel: String? = null

            // Check A: Deceptive domain containing brand domain or brand name (e.g., github.com.verification.invalid, google.com.attacker.example)
            for (officialDom in brand.officialDomains) {
                if (lowerHost.contains(officialDom) && orgDomain != officialDom && !orgDomain.endsWith(".$officialDom")) {
                    isImpersonation = true
                    detectedLabel = officialDom
                    break
                }
            }

            if (!isImpersonation) {
                for (part in hostParts) {
                    val normalizedPart = normalizeLeetspeak(part)

                    // Case B1: Leetspeak / Typosquatting look-alike (e.g. paypa1, g00gle, micros0ft, amaz0n)
                    if (part != brand.brandName && normalizedPart.contains(brand.brandName)) {
                        isLookalike = true
                        detectedLabel = part
                        break
                    }

                    // Case B2: Edit distance look-alike (e.g. paypall, aapple)
                    if (part.length >= 4 && part != brand.brandName) {
                        val dist = levenshteinDistance(part, brand.brandName)
                        val normDist = levenshteinDistance(normalizedPart, brand.brandName)
                        if (dist in 1..2 || normDist in 1..2) {
                            isLookalike = true
                            detectedLabel = part
                            break
                        }
                    }

                    // Case B3: Brand name embedded in host label on an unverified registrable domain (e.g., evil-google.com, google-security.example)
                    if (part == brand.brandName && orgDomain != brand.brandName) {
                        isImpersonation = true
                        detectedLabel = part
                        break
                    }
                }
            }

            if (isLookalike && detectedLabel != null) {
                signals.add(
                    RiskSignal(
                        code = "URL_LOOKALIKE_BRAND",
                        title = "Look-Alike Brand Substitution",
                        description = "Host label '$detectedLabel' simulates protected brand '${brand.brandName}' using character substitutions or visual homoglyphs.",
                        severity = SignalSeverity.CRITICAL,
                        weight = 35
                    )
                )
                break
            }

            if (isImpersonation && detectedLabel != null) {
                signals.add(
                    RiskSignal(
                        code = "URL_BRAND_IMPERSONATION",
                        title = "Deceptive Brand Impersonation",
                        description = "Host references protected brand '$detectedLabel' on an unverified registered domain ('$orgDomain').",
                        severity = SignalSeverity.CRITICAL,
                        weight = 35
                    )
                )
                break
            }
        }

        return signals
    }

    fun extractHost(url: String): String? {
        return try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            val uri = URI(formattedUrl)
            val host = uri.host
            if (host != null) {
                host
            } else if (cleanUserInfoHost(formattedUrl) != null) {
                cleanUserInfoHost(formattedUrl)
            } else {
                null
            }
        } catch (_: Exception) {
            cleanUserInfoHost(url)
        }
    }

    fun getOrganizationalDomain(host: String): String {
        val cleanHost = host.lowercase(Locale.ROOT).trimEnd('.')
        val labels = cleanHost.split('.')
        if (labels.size <= 2) return cleanHost

        val twoLevelTlds = setOf("co.in", "com.au", "co.uk", "org.uk", "net.in", "ac.in", "gov.in", "com.br", "co.jp")
        val lastTwo = "${labels[labels.size - 2]}.${labels[labels.size - 1]}"

        return if (twoLevelTlds.contains(lastTwo) && labels.size >= 3) {
            "${labels[labels.size - 3]}.$lastTwo"
        } else {
            lastTwo
        }
    }

    fun normalizeLeetspeak(input: String): String {
        return input.lowercase(Locale.ROOT)
            .replace('1', 'l')
            .replace('0', 'o')
            .replace('4', 'a')
            .replace('3', 'e')
            .replace('5', 's')
            .replace('@', 'a')
            .replace('!', 'i')
            .replace('$', 's')
    }

    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    private fun isIpAddress(host: String): Boolean {
        val ipRegex = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
        return ipRegex.matches(host)
    }

    private fun cleanUserInfoHost(url: String): String? {
        return try {
            val afterScheme = url.substringAfter("://", url)
            val hostPortPath = afterScheme.substringAfter("@", afterScheme)
            val hostOnly = hostPortPath.substringBefore("/").substringBefore(":").substringBefore("?")
            if (hostOnly.isNotBlank()) hostOnly else null
        } catch (_: Exception) {
            null
        }
    }
}