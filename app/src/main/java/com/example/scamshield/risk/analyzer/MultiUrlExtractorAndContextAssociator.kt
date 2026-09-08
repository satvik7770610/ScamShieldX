package com.example.scamshield.risk.analyzer

import java.util.Locale

data class ExtractedUrlContext(
    val rawUrl: String,
    val normalizedUrl: String,
    val contextSnippet: String
)

class MultiUrlExtractorAndContextAssociator {

    private val urlRegex = Regex("""(https?://[^\s]+|www\.[^\s]+|[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}[^\s]*)""")

    private val validTlds = setOf(
        "com", "org", "net", "in", "co.in", "io", "me", "edu", "gov", "ai", "co", "app", "dev",
        "xyz", "top", "online", "site", "info", "store", "tech", "ac.in", "gov.in",
        "co.uk", "org.uk", "com.au", "com.br", "co.jp", "sbi", "google", "gle", "link", "pro",
        "test", "invalid"
    )

    fun extractAndAssociateContext(fullText: String): List<ExtractedUrlContext> {
        if (fullText.isBlank()) return emptyList()

        val matches = urlRegex.findAll(fullText).toList()
        val seenNormalized = mutableSetOf<String>()
        val result = mutableListOf<ExtractedUrlContext>()

        for (match in matches) {
            val raw = match.value
            val normalized = normalizeUrl(raw)
            if (normalized.isBlank()) continue

            // Validate candidate structure before processing
            if (!isValidUrlCandidate(raw, normalized)) continue

            // Deduplicate based on normalized URL
            if (seenNormalized.contains(normalized.lowercase(Locale.ROOT))) {
                continue
            }
            seenNormalized.add(normalized.lowercase(Locale.ROOT))

            // Extract nearby context snippet preceding this URL
            val startIndex = match.range.first
            val precedingText = fullText.substring(0, startIndex)
            val lines = precedingText.lines().map { it.trim() }.filter { it.isNotBlank() }

            val contextSnippet = if (lines.isNotEmpty()) {
                val lastLines = lines.takeLast(2).joinToString(" ")
                if (lastLines.length > 140) lastLines.takeLast(140) else lastLines
            } else {
                "General context"
            }

            result.add(
                ExtractedUrlContext(
                    rawUrl = raw,
                    normalizedUrl = normalized,
                    contextSnippet = contextSnippet
                )
            )
        }

        return result
    }

    fun isValidUrlCandidate(rawCandidate: String, normalizedUrl: String): Boolean {
        val cleanRaw = rawCandidate.trim().trimEnd('.', ',', ')', ']', '!', '?', ';', ':')

        // 1. Explicit scheme URLs (https://, http://, upi://)
        if (cleanRaw.startsWith("http://", ignoreCase = true) || cleanRaw.startsWith("https://", ignoreCase = true) || cleanRaw.startsWith("upi://", ignoreCase = true)) {
            val hostPart = cleanRaw.substringAfter("://").substringBefore("/").substringBefore("?").substringBefore(":")
            return isValidHost(hostPart, isExplicitScheme = true)
        }

        // 2. Explicit www URLs
        if (cleanRaw.startsWith("www.", ignoreCase = true)) {
            val hostPart = cleanRaw.substringBefore("/").substringBefore("?")
            return isValidHost(hostPart, isExplicitScheme = true)
        }

        // 3. Must contain a dot to be a domain
        if (!cleanRaw.contains(".")) return false

        // 4. Reject numbers, decimals, timestamps, or ranges (e.g. "4.3", "18:45", "2023-25")
        if (cleanRaw.matches(Regex("""^\d+(\.\d+)*$""")) || cleanRaw.contains(":")) return false

        // 5. Reject email/user handles (@) without scheme
        if (cleanRaw.contains("@")) return false

        // 6. For implicit candidates, TLD MUST be in validTlds and host must be valid
        val hostPart = normalizedUrl.substringAfter("://").substringBefore("/").substringBefore("?").substringBefore(":")
        return isValidHost(hostPart, isExplicitScheme = false)
    }

    private fun isValidHost(host: String, isExplicitScheme: Boolean): Boolean {
        if (host.isBlank()) return false
        val cleanHost = host.lowercase(Locale.ROOT)

        // Host labels
        val labels = cleanHost.split('.').filter { it.isNotBlank() }

        // Must have at least 2 labels (e.g., "example.com")
        if (labels.size < 2) return false

        val tld = labels.last()

        // TLD must consist ONLY of alphabetic characters (a-z)
        if (tld.isBlank() || tld.any { !it.isLetter() }) return false

        // For implicit URLs without http/https/www, TLD must be in validTlds
        if (!isExplicitScheme && !validTlds.contains(tld)) {
            return false
        }

        // Label before TLD must have at least 2 characters (e.g. "is.gd", "t.me", "job4freshers.co.in")
        val domainNameLabel = labels[labels.size - 2]
        if (domainNameLabel.length < 2 && labels.size == 2) return false

        // Reject OCR single word fragments like "pa", "salary", "apply", "date", "freshers"
        if (labels.size == 2 && (tld == "in" || tld == "co" || tld == "com") && domainNameLabel.length <= 2) {
            return false
        }

        return true
    }

    private fun normalizeUrl(url: String): String {
        var clean = url.trim().trimEnd('.', ',', ')', ']', '!', '?', ';', ':')
        if (clean.startsWith("www.", ignoreCase = true)) {
            clean = "https://$clean"
        } else if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
            if (clean.contains(".")) {
                clean = "https://$clean"
            }
        }
        return clean
    }
}