package com.example.scamshield.risk.analyzer

import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import java.util.Locale

class TextRiskAnalyzer {

    fun analyzeText(text: String): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        val lowerText = text.lowercase(Locale.ROOT)

        if (lowerText.isBlank()) return emptyList()

        // 1. Account threat / suspension language
        val hasAccountKeyword = lowerText.contains("account") || lowerText.contains("card") || lowerText.contains("kyc") ||
                lowerText.contains("sim") || lowerText.contains("bank") || lowerText.contains("netbanking") ||
                lowerText.contains("credit") || lowerText.contains("debit")
        val hasThreatKeyword = lowerText.contains("block") || lowerText.contains("suspend") || lowerText.contains("close") ||
                lowerText.contains("deactivat") || lowerText.contains("unusual activity") || lowerText.contains("police") ||
                lowerText.contains("legal") || lowerText.contains("fine") || lowerText.contains("hold") ||
                lowerText.contains("restrict") || lowerText.contains("freeze") || lowerText.contains("locked") ||
                lowerText.contains("security alert") || lowerText.contains("unauthorized")

        if (hasAccountKeyword && hasThreatKeyword) {
            signals.add(
                RiskSignal(
                    code = "TEXT_ACCOUNT_THREAT",
                    title = "Account Threat / Suspension Language",
                    description = "Message threatens account blocking, suspension, or legal penalties.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 35
                )
            )
        }

        // 2. Urgent pressure tactics
        val hasUrgencyKeyword = lowerText.contains("immediately") || lowerText.contains("urgent") ||
                lowerText.contains("urgently") || lowerText.contains("blocked today") ||
                lowerText.contains("suspended today") || lowerText.contains("expires today") ||
                lowerText.contains("pay today") || lowerText.contains("due today") ||
                lowerText.contains("act now") || lowerText.contains("within") ||
                lowerText.contains("final warning") || lowerText.contains("disconnection")

        if (hasUrgencyKeyword) {
            signals.add(
                RiskSignal(
                    code = "TEXT_URGENT_PRESSURE",
                    title = "Urgent Action Coercion",
                    description = "Message creates artificial time pressure to force action before independent verification.",
                    severity = SignalSeverity.WARNING,
                    weight = 20
                )
            )
        }

        // 3. Reward / Refund scam language
        val hasRewardKeyword = lowerText.contains("lottery") || lowerText.contains("congratulations") ||
                lowerText.contains("you won") || lowerText.contains("cashback") || lowerText.contains("reward") ||
                lowerText.contains("free gift") || lowerText.contains("prize") || lowerText.contains("refund")

        if (hasRewardKeyword) {
            signals.add(
                RiskSignal(
                    code = "TEXT_REWARD_REFUND_SCAM",
                    title = "Lottery / Prize / Refund Scam Pattern",
                    description = "Promises rewards, prize claims, or refunds that require upfront transfers or fees.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 45
                )
            )
        }

        // 4. Upfront Money transfer demand
        val hasMoneyKeyword = lowerText.contains("send money") || lowerText.contains("transfer money") ||
                lowerText.contains("pay fee") || lowerText.contains("upfront fee") || lowerText.contains("send ₹") ||
                lowerText.contains("transfer ₹") || lowerText.contains("pay ₹") || lowerText.contains("deposit ₹")

        if (hasMoneyKeyword) {
            signals.add(
                RiskSignal(
                    code = "TEXT_MONEY_TRANSFER_DEMAND",
                    title = "Upfront Money Transfer Demand",
                    description = "Message requests transferring or depositing money upfront.",
                    severity = SignalSeverity.WARNING,
                    weight = 25
                )
            )
        }

        // 5. OTP / Credential harvesting
        val hasCredentialKeyword = lowerText.contains("otp") || lowerText.contains("password") ||
                lowerText.contains("pin") || lowerText.contains("cvv") || lowerText.contains("credential") ||
                lowerText.contains("login details")

        if (hasCredentialKeyword) {
            signals.add(
                RiskSignal(
                    code = "TEXT_OTP_HARVEST",
                    title = "Credential / OTP Harvesting",
                    description = "Requests confidential OTP, PIN, password, or login credentials.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 45
                )
            )
        }

        // 6. Suspicious verification / secrecy instruction
        val hasVerificationKeyword = lowerText.contains("verify") || lowerText.contains("confirm") || lowerText.contains("do not share") ||
                lowerText.contains("never share") || lowerText.contains("confidential") || lowerText.contains("secret")

        if (hasVerificationKeyword) {
            signals.add(
                RiskSignal(
                    code = "TEXT_SUSPICIOUS_VERIFICATION",
                    title = "Suspicious Verification & Secrecy Instruction",
                    description = "Instructs urgent verification or demands keeping transaction details secret ('do not share').",
                    severity = SignalSeverity.WARNING,
                    weight = 25
                )
            )
        }

        // 7. Click link request
        val hasLinkKeyword = lowerText.contains("click link") || lowerText.contains("visit link") ||
                lowerText.contains("open link") || lowerText.contains("click here") || lowerText.contains("link below")

        if (hasLinkKeyword) {
            signals.add(
                RiskSignal(
                    code = "TEXT_CLICK_LINK_REQUEST",
                    title = "Unsolicited Link Direction",
                    description = "Directs user to click an embedded web link to resolve an issue.",
                    severity = SignalSeverity.WARNING,
                    weight = 15
                )
            )
        }

        // 8. Multi-Signal Banking Phishing Combination
        val hasBankContext = lowerText.contains("bank") || lowerText.contains("account") || lowerText.contains("kyc") ||
                lowerText.contains("netbanking") || lowerText.contains("upi") || lowerText.contains("security team") ||
                lowerText.contains("unusual activity")
        val hasActionDemand = hasUrgencyKeyword || hasVerificationKeyword || hasLinkKeyword || hasCredentialKeyword

        if (hasBankContext && hasThreatKeyword && hasActionDemand) {
            signals.add(
                RiskSignal(
                    code = "BANK_PHISHING_COMBINATION",
                    title = "Bank Impersonation & Account Suspension Threat",
                    description = "Dangerous combination: Financial institution context combined with account suspension threat and urgent verification or link/credential demand.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 40
                )
            )
        }

        return signals
    }
}
