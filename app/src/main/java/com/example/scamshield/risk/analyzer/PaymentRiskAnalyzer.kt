package com.example.scamshield.risk.analyzer

import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.SignalSeverity
import java.net.URLDecoder
import java.util.Locale

class PaymentRiskAnalyzer {

    data class PaymentPayload(
        val payeeVpa: String? = null,
        val payeeName: String? = null,
        val amount: String? = null,
        val transactionNote: String? = null,
        val currency: String? = null,
        val transactionRef: String? = null,
        val merchantCode: String? = null,
        val isUpiScheme: Boolean = false
    )

    fun parsePaymentPayload(rawContent: String): PaymentPayload {
        if (!rawContent.startsWith("upi://pay", ignoreCase = true)) {
            return PaymentPayload(isUpiScheme = false)
        }

        return try {
            val queryString = rawContent.substringAfter("?", "")
            if (queryString.isEmpty()) {
                return PaymentPayload(isUpiScheme = true)
            }

            val params = queryString.split("&").associate { param ->
                val parts = param.split("=")
                val key = parts.getOrNull(0)?.lowercase(Locale.ROOT) ?: ""
                val rawVal = if (parts.size > 1) parts[1] else ""
                val decodedVal = try {
                    URLDecoder.decode(rawVal.replace("+", "%20"), "UTF-8")
                } catch (_: Exception) {
                    rawVal
                }
                key to decodedVal
            }

            PaymentPayload(
                payeeVpa = params["pa"],
                payeeName = params["pn"],
                amount = params["am"],
                transactionNote = params["tn"],
                currency = params["cu"],
                transactionRef = params["tr"],
                merchantCode = params["mc"],
                isUpiScheme = true
            )
        } catch (_: Exception) {
            PaymentPayload(isUpiScheme = true)
        }
    }

    fun analyzePayment(payload: PaymentPayload): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()

        if (!payload.isUpiScheme) return emptyList()

        // 1. QR Validity Signal
        signals.add(
            RiskSignal(
                code = "PAYMENT_VALID_FORMAT",
                title = "Valid Payment QR Detected",
                description = "Valid UPI payment structure parsed successfully for ${payload.payeeName ?: payload.payeeVpa ?: "recipient"}.",
                severity = SignalSeverity.INFO,
                weight = 10
            )
        )

        // 2. Context Verification Disclaimer
        signals.add(
            RiskSignal(
                code = "PAYMENT_CONTEXT_DISCLAIMER",
                title = "Context Verification Required",
                description = "Low risk does not guarantee that a payment is safe. Verify the payment context before proceeding.",
                severity = SignalSeverity.INFO,
                weight = 5
            )
        )

        // 3. Missing recipient handle check
        if (payload.payeeVpa.isNullOrBlank() && payload.payeeName.isNullOrBlank()) {
            signals.add(
                RiskSignal(
                    code = "PAYMENT_MISSING_RECIPIENT",
                    title = "Missing Recipient Handle",
                    description = "Payment payload does not specify a target payee account or name.",
                    severity = SignalSeverity.WARNING,
                    weight = 20
                )
            )
        }

        // 4. Check payee handle keywords
        val vpa = payload.payeeVpa?.lowercase(Locale.ROOT) ?: ""
        if (vpa.contains("scam") || vpa.contains("claim") || vpa.contains("lottery") || vpa.contains("prize") || vpa.contains("agent")) {
            signals.add(
                RiskSignal(
                    code = "PAYMENT_SUSPICIOUS_HANDLE",
                    title = "Suspicious Payee Address Pattern",
                    description = "Payee address '$vpa' contains keywords associated with fraudulent accounts or claim agents.",
                    severity = SignalSeverity.WARNING,
                    weight = 20
                )
            )
        }

        // 5. Check Payee Name and Transaction Note for Prize/Lottery/Reward/Claim/Advance keywords
        val payeeName = payload.payeeName?.lowercase(Locale.ROOT) ?: ""
        val note = payload.transactionNote?.lowercase(Locale.ROOT) ?: ""
        val combinedText = "$payeeName $note"

        val prizeKeywords = listOf(
            "prize", "claim", "reward", "lottery", "cashback", "winner",
            "free gift", "advance", "refund", "verification deposit", "security deposit", "fee", "tax"
        )

        if (prizeKeywords.any { combinedText.contains(it) }) {
            signals.add(
                RiskSignal(
                    code = "PAYMENT_PRIZE_REWARD_CLAIM",
                    title = "Prize / Reward Claim Context",
                    description = "Payment request context '${payload.payeeName ?: payload.transactionNote}' involves prize, reward, or advance deposit claims strongly linked to payment scams.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 40
                )
            )
        }

        // 6. Check Amount demand
        val amt = payload.amount?.toDoubleOrNull()
        if (amt != null) {
            val severity = if (amt >= 5000.0) SignalSeverity.CRITICAL else SignalSeverity.WARNING
            val weight = if (amt >= 5000.0) 25 else 15
            signals.add(
                RiskSignal(
                    code = "PAYMENT_AMOUNT_DEMAND",
                    title = "Upfront Payment Demand",
                    description = "Payment request demands an upfront transfer of ₹${payload.amount}. Confirm recipient identity independently before transferring funds.",
                    severity = severity,
                    weight = weight
                )
            )
        }

        return signals
    }
}