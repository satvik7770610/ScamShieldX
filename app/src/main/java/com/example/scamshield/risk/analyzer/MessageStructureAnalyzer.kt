package com.example.scamshield.risk.analyzer

import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import java.util.Locale

class MessageStructureAnalyzer {

    fun analyzeStructure(text: String): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        val lower = text.lowercase(Locale.ROOT)

        var recruitmentHits = 0
        if (lower.contains("company name") || lower.contains("company:")) recruitmentHits++
        if (lower.contains("post name") || lower.contains("role:")) recruitmentHits++
        if (lower.contains("salary") || lower.contains("lpa")) recruitmentHits++
        if (lower.contains("experience") || lower.contains("fresher")) recruitmentHits++
        if (lower.contains("batch")) recruitmentHits++
        if (lower.contains("job location") || lower.contains("location")) recruitmentHits++
        if (lower.contains("walk-in") || lower.contains("interview")) recruitmentHits++

        if (recruitmentHits >= 3) {
            signals.add(
                RiskSignal(
                    code = "RECRUITMENT_STRUCTURE_DETECTED",
                    title = "Coherent Recruitment Structure",
                    description = "Message follows a structured job recruitment format (Company, Role, Salary, Location, Walk-in Date).",
                    severity = SignalSeverity.INFO,
                    weight = 0
                )
            )
        }

        return signals
    }
}