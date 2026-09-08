package com.example.scamshield.risk.analyzer

import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import java.util.Locale

class RedirectAnalyzer(
    private val trustedRegistry: TrustedDomainRegistry = TrustedDomainRegistry()
) {

    fun analyzeRedirect(url: String, host: String): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        val lowerHost = host.lowercase(Locale.ROOT)

        if (trustedRegistry.isRecognizedRedirector(lowerHost)) {
            signals.add(
                RiskSignal(
                    code = "URL_RECOGNIZED_REDIRECTOR",
                    title = "Recognized Content Redirector",
                    description = "URL uses a recognized deep-link redirector for video or community content.",
                    severity = SignalSeverity.INFO,
                    weight = 0
                )
            )
        }

        return signals
    }
}