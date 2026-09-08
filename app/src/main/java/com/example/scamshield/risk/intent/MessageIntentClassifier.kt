package com.example.scamshield.risk.intent

import java.util.Locale

class MessageIntentClassifier {

    private val campusAmbassadorKeywords = listOf(
        "campus ambassador", "student ambassador", "campus lead", "campus program", "campus representative"
    )

    private val internshipKeywords = listOf(
        "internship", "intern", "trainee", "stipend", "summer intern", "winter intern"
    )

    private val fellowshipScholarshipKeywords = listOf(
        "fellowship", "scholarship", "grant program", "research fellowship"
    )

    private val jobKeywords = listOf(
        "company name", "post name", "job location", "walk-in date", "batch",
        "salary", "lpa", "fresher", "apply link", "interview experience",
        "hiring", "vacancy", "job opening", "job role", "recruitment", "walk-in",
        "job description", "selection process", "resume submission", "eligibility",
        "careers", "apply here", "application deadline", "interview", "interview slot",
        "job", "offer letter", "appointment letter"
    )

    private val accountSecurityKeywords = listOf(
        "security alert", "account xx", "account will be blocked", "suspended today",
        "kyc expired", "unusual activity", "unauthorized access", "card blocked",
        "permanently suspended", "prevent suspension"
    )

    private val prizeRewardKeywords = listOf(
        "congratulations", "you won", "lottery winner", "cashback reward",
        "free gift", "claim prize", "lucky winner"
    )

    private val refundKeywords = listOf(
        "receive your refund", "pending refund", "refund fee", "refund amount"
    )

    fun classify(text: String): MessageIntent {
        val lower = text.lowercase(Locale.ROOT)

        if (campusAmbassadorKeywords.any { lower.contains(it) }) {
            return MessageIntent.CAMPUS_AMBASSADOR
        }

        if (internshipKeywords.any { lower.contains(it) }) {
            return MessageIntent.INTERNSHIP
        }

        if (fellowshipScholarshipKeywords.any { lower.contains(it) }) {
            return MessageIntent.FELLOWSHIP
        }

        var jobScore = 0
        for (kw in jobKeywords) {
            if (lower.contains(kw)) jobScore++
        }

        var securityScore = 0
        for (kw in accountSecurityKeywords) {
            if (lower.contains(kw)) securityScore++
        }

        var prizeScore = 0
        for (kw in prizeRewardKeywords) {
            if (lower.contains(kw)) prizeScore++
        }

        var refundScore = 0
        for (kw in refundKeywords) {
            if (lower.contains(kw)) refundScore++
        }

        if (lower.startsWith("upi://pay", ignoreCase = true)) {
            return MessageIntent.PAYMENT
        }

        return when {
            jobScore >= 1 ->
                MessageIntent.JOB_RECRUITMENT
            securityScore >= 2 || (securityScore >= 1 && lower.contains("blocked")) ->
                MessageIntent.ACCOUNT_SECURITY
            prizeScore >= 1 ->
                MessageIntent.PRIZE_REWARD
            refundScore >= 1 ->
                MessageIntent.REFUND
            lower.contains("delivery") || lower.contains("parcel") || lower.contains("tracking") ->
                MessageIntent.DELIVERY
            else ->
                MessageIntent.GENERAL_INFORMATION
        }
    }

    fun isRecruitmentOrCareerIntent(intent: MessageIntent): Boolean {
        return intent == MessageIntent.JOB_RECRUITMENT ||
                intent == MessageIntent.INTERNSHIP ||
                intent == MessageIntent.CAMPUS_AMBASSADOR ||
                intent == MessageIntent.FELLOWSHIP ||
                intent == MessageIntent.SCHOLARSHIP ||
                intent == MessageIntent.HIRING ||
                intent == MessageIntent.CAMPUS_PROGRAM ||
                intent == MessageIntent.CAREER_INFORMATION
    }
}