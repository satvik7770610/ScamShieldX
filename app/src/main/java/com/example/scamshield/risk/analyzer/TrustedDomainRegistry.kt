package com.example.scamshield.risk.analyzer

import java.util.Locale

class TrustedDomainRegistry {

    private val configurableTrustedDomains = mutableSetOf(
        "google.com", "google.co.in", "youtube.com", "youtu.be",
        "whatsapp.com", "t.me", "telegram.me", "telegram.org",
        "linkedin.com", "github.com", "twitter.com", "x.com",
        "facebook.com", "instagram.com", "microsoft.com", "apple.com",
        "amazon.com", "amazon.in", "wikipedia.org", "example.com"
    )

    private val recognizedContentRedirectors = mutableSetOf(
        "openinapp.co", "yt.openinapp.co", "linktree.com", "lnk.bio", "wa.me"
    )

    fun isTrustedDomain(orgDomain: String): Boolean {
        val clean = orgDomain.lowercase(Locale.ROOT)
        return configurableTrustedDomains.any { clean == it || clean.endsWith(".$it") }
    }

    fun isRecognizedRedirector(host: String): Boolean {
        val clean = host.lowercase(Locale.ROOT)
        return recognizedContentRedirectors.any { clean == it || clean.endsWith(".$it") }
    }

    fun addTrustedDomain(domain: String) {
        configurableTrustedDomains.add(domain.lowercase(Locale.ROOT))
    }
}