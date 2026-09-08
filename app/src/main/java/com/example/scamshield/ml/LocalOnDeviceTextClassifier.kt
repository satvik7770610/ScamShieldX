package com.example.scamshield.ml

import java.util.Locale

class LocalOnDeviceTextClassifier : TextClassifier {

    override fun isModelLoaded(): Boolean = true

    override fun classify(text: String): MlPrediction {
        val lower = text.lowercase(Locale.ROOT).trim()

        if (lower.isBlank()) {
            return MlPrediction(
                intent = MlCategory.GENERAL,
                scamProbability = 0.0,
                confidence = 0.80
            )
        }

        // 1. Feature Extraction
        val hasCampusAmbassador = lower.contains("campus ambassador") || lower.contains("student ambassador") || lower.contains("campus lead")
        val hasInternship = lower.contains("internship") || lower.contains("intern ") || lower.contains("trainee") || lower.contains("stipend")
        val hasFellowship = lower.contains("fellowship") || lower.contains("scholarship")
        val hasSafeJobFeatures = lower.contains("company name") || lower.contains("post name") ||
                lower.contains("job location") || lower.contains("walk-in date") || lower.contains("batch") ||
                lower.contains("lpa") || lower.contains("freshers") || lower.contains("apply link") ||
                lower.contains("job description") || lower.contains("selection process") || lower.contains("careers") ||
                lower.contains("apply here") || lower.contains("opportunity")

        val hasRecruitmentFeeScam = lower.contains("registration fee") || lower.contains("interview fee") ||
                lower.contains("security deposit") || lower.contains("pay to receive offer") || lower.contains("pay for interview")

        val hasBankOrAccount = lower.contains("bank") || lower.contains("account") || lower.contains("kyc") || lower.contains("card") || lower.contains("netbanking")
        val hasThreatOrSuspension = lower.contains("block") || lower.contains("suspend") || lower.contains("hold") || lower.contains("restrict") || lower.contains("unusual activity") || lower.contains("security alert") || lower.contains("disconnection") || lower.contains("expired")
        val hasActionOrLink = lower.contains("verify") || lower.contains("confirm") || lower.contains("click link") || lower.contains("link below") || lower.contains("share your otp") || lower.contains("login details") || lower.contains("never share")

        val hasPhishingThreat = (hasBankOrAccount && hasThreatOrSuspension && hasActionOrLink) ||
                lower.contains("account xx") || lower.contains("paypa1") || lower.contains("prevent disconnection")

        val hasPrizeScam = lower.contains("lottery winner") || lower.contains("congratulations you won") ||
                lower.contains("cashback reward") || lower.contains("claim prize")

        val hasDelivery = lower.contains("parcel") || lower.contains("shipment") || lower.contains("delivery failed")
        val hasPaymentQr = lower.startsWith("upi://pay", ignoreCase = true)

        // 2. Class Probabilities & Intent Inference
        return when {
            hasPhishingThreat -> MlPrediction(
                intent = MlCategory.PHISHING,
                scamProbability = 0.88,
                confidence = 0.94
            )
            hasRecruitmentFeeScam -> MlPrediction(
                intent = MlCategory.RECRUITMENT_SCAM,
                scamProbability = 0.85,
                confidence = 0.92
            )
            hasPrizeScam -> MlPrediction(
                intent = MlCategory.PRIZE_REWARD,
                scamProbability = 0.82,
                confidence = 0.90
            )
            hasCampusAmbassador -> MlPrediction(
                intent = MlCategory.CAMPUS_PROGRAM,
                scamProbability = 0.05,
                confidence = 0.92
            )
            hasInternship -> MlPrediction(
                intent = MlCategory.INTERNSHIP,
                scamProbability = 0.05,
                confidence = 0.90
            )
            hasFellowship -> MlPrediction(
                intent = MlCategory.CAMPUS_PROGRAM,
                scamProbability = 0.05,
                confidence = 0.88
            )
            hasSafeJobFeatures -> MlPrediction(
                intent = MlCategory.SAFE_RECRUITMENT,
                scamProbability = 0.05,
                confidence = 0.91
            )
            hasPaymentQr -> MlPrediction(
                intent = MlCategory.PAYMENT,
                scamProbability = 0.15,
                confidence = 0.88
            )
            hasDelivery -> MlPrediction(
                intent = MlCategory.DELIVERY,
                scamProbability = 0.20,
                confidence = 0.85
            )
            else -> MlPrediction(
                intent = MlCategory.GENERAL,
                scamProbability = 0.02,
                confidence = 0.82
            )
        }
    }
}
