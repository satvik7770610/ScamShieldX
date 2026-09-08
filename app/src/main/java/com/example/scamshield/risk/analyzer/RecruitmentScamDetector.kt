package com.example.scamshield.risk.analyzer

import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import java.util.Locale

class RecruitmentScamDetector {

    private val recruitmentFeeKeywords = listOf(
        "registration fee", "interview fee", "security deposit", "job fee",
        "pay to receive offer", "pay for interview", "training fee deposit",
        "pay fee", "pay ₹", "processing fee", "advance fee"
    )

    fun analyzeRecruitmentScams(text: String): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        val lower = text.lowercase(Locale.ROOT)

        if (recruitmentFeeKeywords.any { lower.contains(it) }) {
            signals.add(
                RiskSignal(
                    code = "RECRUITMENT_FEE_DEMAND",
                    title = "Upfront Job / Application Fee Demand",
                    description = "Message requires paying a registration fee, application fee, or security deposit to secure a job or interview.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 35
                )
            )
        }

        // 2. Pay to receive offer / interview
        if (lower.contains("pay to receive offer") || lower.contains("pay to get interview") || lower.contains("pay for appointment")) {
            signals.add(
                RiskSignal(
                    code = "RECRUITMENT_PAY_FOR_OFFER",
                    title = "Payment Demand for Job Offer / Interview",
                    description = "Demands money to issue a job offer or confirm an interview slot.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 40
                )
            )
        }

        // 3. Request to install APK or remote access app
        if (lower.contains(".apk") || lower.contains("install app") || lower.contains("anydesk") || lower.contains("teamviewer") || lower.contains("quicksupport")) {
            signals.add(
                RiskSignal(
                    code = "RECRUITMENT_APK_REMOTE_APP",
                    title = "Suspicious APK / Remote Access App Request",
                    description = "Instructs user to download an unverified APK or remote screen-sharing software.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 35
                )
            )
        }

        return signals
    }
}