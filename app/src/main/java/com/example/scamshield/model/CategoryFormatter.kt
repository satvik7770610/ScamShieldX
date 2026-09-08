package com.example.scamshield.model

object CategoryFormatter {
    fun formatCategory(threatCategory: String?, signals: List<RiskSignal> = emptyList()): String {
        val catUpper = (threatCategory ?: "").uppercase().trim()
        val signalCodes = signals.map { it.code }.toSet()

        if (signalCodes.contains("TEXT_OTP_HARVEST") || signalCodes.contains("JOB_OTP_SCAM_COMBINATION") || signalCodes.contains("URL_EMBEDDED_CREDENTIALS")) {
            return "OTP / ACCOUNT SECURITY"
        }

        if (catUpper.contains("ACCOUNT SECURITY") || catUpper.contains("OTP")) {
            return "OTP / ACCOUNT SECURITY"
        }

        if (catUpper.contains("INTERNSHIP")) {
            return "INTERNSHIP"
        }

        if (catUpper.contains("RECRUITMENT") || catUpper.contains("JOB") || catUpper.contains("AMBASSADOR") || catUpper.contains("FELLOWSHIP") || catUpper.contains("HIRING")) {
            return "RECRUITMENT"
        }

        if (catUpper.contains("BANKING") || catUpper.contains("BANK")) {
            return "BANKING"
        }

        if (catUpper.contains("PAYMENT") || catUpper.contains("UPI") || catUpper.contains("REFUND")) {
            return "PAYMENT"
        }

        if (catUpper.contains("DELIVERY")) {
            return "DELIVERY"
        }

        if (catUpper.contains("PRIZE") || catUpper.contains("REWARD")) {
            return "PRIZE / REWARD"
        }

        if (catUpper.contains("PHISHING") || catUpper.contains("WEB LINK") || catUpper.contains("URL")) {
            return "PHISHING"
        }

        if (catUpper.contains("TEXT") || catUpper.contains("SMS") || catUpper.contains("GENERAL")) {
            return "GENERAL"
        }

        if (catUpper.isBlank() || catUpper == "UNKNOWN") {
            return "UNKNOWN"
        }

        return catUpper
    }
}
