package com.example.scamshield.risk.analyzer

import java.util.Locale

enum class UrlRole {
    APPLICATION,
    CAREER,
    INFORMATION,
    VIDEO,
    SOCIAL,
    COMMUNITY,
    CONTACT,
    PAYMENT,
    LOGIN,
    UNKNOWN
}

class UrlRoleClassifier(
    private val trustedDomainRegistry: TrustedDomainRegistry = TrustedDomainRegistry()
) {

    fun classifyRole(url: String, host: String): UrlRole {
        val lowerUrl = url.lowercase(Locale.ROOT)
        val lowerHost = host.lowercase(Locale.ROOT)

        if (lowerUrl.startsWith("upi://", ignoreCase = true) || lowerUrl.contains("/pay") || lowerUrl.contains("checkout")) {
            return UrlRole.PAYMENT
        }

        if (lowerHost.contains("youtube") || lowerHost.contains("youtu.be") || lowerHost.contains("yt.openinapp") || lowerUrl.contains("/watch")) {
            return UrlRole.VIDEO
        }

        if (lowerHost.contains("whatsapp") || lowerHost.contains("telegram") || lowerHost.contains("t.me") ||
            lowerHost.contains("twitter") || lowerHost.contains("x.com") || lowerHost.contains("facebook") || lowerHost.contains("instagram")) {
            return UrlRole.COMMUNITY
        }

        if (lowerHost.contains("linkedin")) {
            return UrlRole.SOCIAL
        }

        if (lowerUrl.contains("/login") || lowerUrl.contains("/auth") || lowerUrl.contains("/signin") || lowerUrl.contains("/update-kyc")) {
            return UrlRole.LOGIN
        }

        if (lowerUrl.contains("/apply") || lowerUrl.contains("job") || lowerUrl.contains("career") || lowerUrl.contains("walk-in") || lowerUrl.contains("fresher") || lowerUrl.contains("ambassador") || lowerUrl.contains("forms") || lowerUrl.contains("typeform") || lowerUrl.contains("notion")) {
            return UrlRole.APPLICATION
        }

        if (trustedDomainRegistry.isTrustedDomain(lowerHost)) {
            return UrlRole.INFORMATION
        }

        return UrlRole.UNKNOWN
    }
}