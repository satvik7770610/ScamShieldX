package com.example.scamshield.risk.fusion

import com.example.scamshield.ml.MlCategory
import com.example.scamshield.ml.MlPrediction
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.ScoreBreakdown
import com.example.scamshield.model.VerificationStatus

class FusionEngine {

    fun fuse(
        prediction: MlPrediction,
        ruleSignals: List<RiskSignal>
    ): FusionResult {
        // 1. Categorize signals into score breakdown components
        val behaviorSignals = ruleSignals.filter {
            it.code == "RECRUITMENT_FEE_DEMAND" || it.code == "RECRUITMENT_PAY_FOR_OFFER" ||
            it.code == "TEXT_MONEY_TRANSFER_DEMAND" || it.code == "JOB_PAYMENT_SCAM_COMBINATION" ||
            it.code == "TEXT_OTP_HARVEST" || it.code == "JOB_OTP_SCAM_COMBINATION" ||
            it.code == "TEXT_ACCOUNT_THREAT" || it.code == "BANK_PHISHING_COMBINATION" ||
            it.code == "RECRUITMENT_APK_REMOTE_APP"
        }

        val domainSignals = ruleSignals.filter {
            it.code == "URL_LOOKALIKE_BRAND" || it.code == "URL_BRAND_IMPERSONATION" ||
            it.code == "DOMAIN_IP_HOST" || it.code == "URL_EMBEDDED_CREDENTIALS" ||
            it.code == "DOMAIN_SHORTENED_URL" || it.code == "DOMAIN_SUSPICIOUS_TLD"
        }

        val urlSignals = ruleSignals.filter {
            it.code == "URL_NO_HTTPS" || it.code == "URL_SUSPICIOUS_PATH" ||
            it.code == "URL_SENSITIVE_PATH_UNVERIFIED_DOMAIN" || it.code == "URL_UNVERIFIED_PATH" ||
            it.code == "URL_UNVERIFIED_DESTINATION" || it.code == "URL_APPLICATION_LINK"
        }

        val textSignals = ruleSignals.filter {
            it.code == "RECRUITMENT_STRUCTURE_DETECTED" || it.code == "TEXT_URGENT_PRESSURE" ||
            it.code == "TEXT_SUSPICIOUS_VERIFICATION" || it.code == "TEXT_CLICK_LINK_REQUEST"
        }

        val behaviorScore = behaviorSignals.sumOf { it.weight }
        val domainScore = domainSignals.sumOf { it.weight }
        val urlScore = urlSignals.sumOf { it.weight }
        val textScore = textSignals.sumOf { it.weight }
        val ruleSum = ruleSignals.sumOf { it.weight }

        val hasConcreteHarmRule = behaviorSignals.isNotEmpty() ||
                domainSignals.any { it.code == "URL_LOOKALIKE_BRAND" || it.code == "URL_BRAND_IMPERSONATION" || it.code == "DOMAIN_IP_HOST" }

        val mlBaseScore = (prediction.scamProbability * 100).toInt()

        val isSafeCategory = prediction.intent == MlCategory.SAFE_RECRUITMENT ||
                prediction.intent == MlCategory.CAMPUS_PROGRAM ||
                prediction.intent == MlCategory.INTERNSHIP ||
                prediction.intent == MlCategory.JOB_RECRUITMENT ||
                prediction.intent == MlCategory.GENERAL

        val finalScore: Int = if (isSafeCategory && !hasConcreteHarmRule) {
            if (mlBaseScore < 10 && ruleSum <= 3) {
                0
            } else {
                minOf(mlBaseScore + ruleSum, 25)
            }
        } else if (hasConcreteHarmRule || prediction.scamProbability >= 0.70) {
            val combined = maxOf((prediction.scamProbability * 60).toInt() + ruleSum, ruleSum)
            combined.coerceIn(60, 100)
        } else {
            (mlBaseScore * 0.4 + ruleSum * 0.6).toInt().coerceIn(0, 100)
        }

        val level = when {
            finalScore >= 60 -> RiskLevel.HIGH_RISK
            finalScore >= 30 -> RiskLevel.SUSPICIOUS
            else -> RiskLevel.LOW
        }

        val baseConf = (prediction.confidence * 100).toInt()
        val finalConfidence = when {
            isSafeCategory && !hasConcreteHarmRule -> maxOf(baseConf, 85)
            hasConcreteHarmRule -> maxOf(baseConf, 92)
            else -> baseConf.coerceIn(65, 90)
        }

        val categoryName = if (ruleSignals.any { it.code == "BANK_PHISHING_COMBINATION" }) {
            "BANKING / ACCOUNT SECURITY"
        } else {
            prediction.intent.name.replace("_", " ")
        }

        val explanation = when {
            finalScore >= 95 -> "CRITICAL HIGH RISK. Bank impersonation, account threat, or credential harvesting detected. DO NOT CLICK or share credentials."
            finalScore >= 80 -> "VERY HIGH RISK. Multiple severe threat signals detected. High probability of scam, account takeover, or credential theft."
            finalScore >= 60 -> "HIGH RISK. Concrete scam or phishing behavior detected. Do not click or proceed before verifying independently."
            finalScore >= 30 -> "PROCEED WITH CAUTION. Destination could not be fully verified. Verify recipient or website via official channel."
            isSafeCategory -> "LOW RISK. $categoryName structure detected with no fee or credential demands found. Low risk does not guarantee legitimacy. Verify through official channels."
            else -> "SAFE TO PROCEED. No suspicious scam patterns detected."
        }

        val breakdown = ScoreBreakdown(
            baseScore = mlBaseScore,
            textSignalsScore = textScore,
            urlSignalsScore = urlScore,
            domainSignalsScore = domainScore,
            behaviorSignalsScore = behaviorScore,
            mlScore = mlBaseScore,
            mlConfidencePercent = finalConfidence,
            finalScore = finalScore
        )

        return FusionResult(
            score = finalScore,
            level = level,
            confidencePercent = finalConfidence,
            explanation = explanation,
            verificationStatus = VerificationStatus.UNVERIFIED_LOCAL,
            scoreBreakdown = breakdown
        )
    }
}
