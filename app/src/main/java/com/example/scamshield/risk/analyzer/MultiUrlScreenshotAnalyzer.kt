package com.example.scamshield.risk.analyzer

import com.example.scamshield.ml.OnDeviceFeatureClassifier
import com.example.scamshield.ml.TextClassifier
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.ScreenshotAnalysisResult
import com.example.scamshield.model.SignalSeverity
import com.example.scamshield.model.UrlRiskResult
import com.example.scamshield.risk.fusion.FusionEngine
import com.example.scamshield.risk.intent.MessageIntent
import com.example.scamshield.risk.intent.MessageIntentClassifier

class MultiUrlScreenshotAnalyzer(
    private val extractor: MultiUrlExtractorAndContextAssociator = MultiUrlExtractorAndContextAssociator(),
    private val intentClassifier: MessageIntentClassifier = MessageIntentClassifier(),
    private val urlRoleClassifier: UrlRoleClassifier = UrlRoleClassifier(),
    private val domainAnalyzer: DomainAnalyzer = DomainAnalyzer(),
    private val urlAnalyzer: UrlRiskAnalyzer = UrlRiskAnalyzer(),
    private val textAnalyzer: TextRiskAnalyzer = TextRiskAnalyzer(),
    private val recruitmentScamDetector: RecruitmentScamDetector = RecruitmentScamDetector(),
    private val textClassifier: TextClassifier = OnDeviceFeatureClassifier(),
    private val fusionEngine: FusionEngine = FusionEngine()
) {

    fun analyzeScreenshot(fullText: String): ScreenshotAnalysisResult {
        val extractedContexts = extractor.extractAndAssociateContext(fullText)

        if (extractedContexts.isEmpty()) {
            val singleResult = analyzeSingleUrlContext("", "", fullText)
            return ScreenshotAnalysisResult(
                extractedText = fullText,
                urlResults = emptyList(),
                overallScore = singleResult.riskScore,
                overallRiskLevel = singleResult.riskLevel,
                overallConfidencePercent = singleResult.confidencePercent,
                highRiskCount = 0,
                suspiciousCount = 0,
                lowRiskCount = 0,
                explanation = singleResult.explanation
            )
        }

        val urlResults = mutableListOf<UrlRiskResult>()
        for (extracted in extractedContexts) {
            val urlResult = analyzeSingleUrlContext(
                url = extracted.rawUrl,
                normalizedUrl = extracted.normalizedUrl,
                contextSnippet = extracted.contextSnippet
            )
            urlResults.add(urlResult)
        }

        val highRiskCount = urlResults.count { it.riskLevel == RiskLevel.HIGH_RISK }
        val suspiciousCount = urlResults.count { it.riskLevel == RiskLevel.SUSPICIOUS }
        val lowRiskCount = urlResults.count { it.riskLevel == RiskLevel.LOW }

        val overallScore = if (urlResults.isNotEmpty()) {
            urlResults.maxOf { it.riskScore }
        } else {
            0
        }

        val overallLevel = when {
            overallScore >= 60 -> RiskLevel.HIGH_RISK
            overallScore >= 30 -> RiskLevel.SUSPICIOUS
            else -> RiskLevel.LOW
        }

        val maxConfidence = if (urlResults.isNotEmpty()) urlResults.maxOf { it.confidencePercent } else 85

        val overallExplanation = when {
            highRiskCount > 0 -> "⚠️ HIGH RISK: $highRiskCount high-risk link(s) detected requiring attention."
            suspiciousCount > 0 -> "PROCEED WITH CAUTION: $suspiciousCount suspicious link(s) detected. Verify destinations before opening."
            else -> "LOW RISK: ${urlResults.size} link(s) analyzed with no high-risk scam patterns detected."
        }

        return ScreenshotAnalysisResult(
            extractedText = fullText,
            urlResults = urlResults,
            overallScore = overallScore,
            overallRiskLevel = overallLevel,
            overallConfidencePercent = maxConfidence,
            highRiskCount = highRiskCount,
            suspiciousCount = suspiciousCount,
            lowRiskCount = lowRiskCount,
            explanation = overallExplanation
        )
    }

    fun analyzeSingleUrlContext(
        url: String,
        normalizedUrl: String,
        contextSnippet: String
    ): UrlRiskResult {
        val combinedText = "$contextSnippet $normalizedUrl".trim()
        val intent = intentClassifier.classify(combinedText)
        val isRecruitmentOrCareer = intentClassifier.isRecruitmentOrCareerIntent(intent)

        val host = domainAnalyzer.extractHost(normalizedUrl) ?: "unknown-domain"
        val role = urlRoleClassifier.classifyRole(normalizedUrl, host)

        val signals = mutableListOf<RiskSignal>()

        // 1. Behavioral scam signals strictly from this URL's contextSnippet
        val contextTextSignals = textAnalyzer.analyzeText(contextSnippet)
        signals.addAll(contextTextSignals)

        if (isRecruitmentOrCareer) {
            signals.addAll(recruitmentScamDetector.analyzeRecruitmentScams(combinedText))
        }

        // 2. Domain & URL Analysis
        if (normalizedUrl.isNotBlank()) {
            val domainSignals = domainAnalyzer.analyzeDomain(normalizedUrl)
            val criticalDomainSignals = domainSignals.filter {
                it.code == "URL_LOOKALIKE_BRAND" || it.code == "URL_BRAND_IMPERSONATION" ||
                        it.code == "DOMAIN_IP_HOST" || it.code == "URL_EMBEDDED_CREDENTIALS"
            }
            signals.addAll(criticalDomainSignals)

            if (isRecruitmentOrCareer) {
                if (criticalDomainSignals.isEmpty()) {
                    if (role == UrlRole.UNKNOWN) {
                        signals.add(
                            RiskSignal(
                                code = "URL_UNVERIFIED_DESTINATION",
                                title = "Unverified Link Destination",
                                description = "Destination could not be fully verified locally.",
                                severity = SignalSeverity.INFO,
                                weight = 2
                            )
                        )
                    } else if (role == UrlRole.APPLICATION) {
                        signals.add(
                            RiskSignal(
                                code = "URL_APPLICATION_LINK",
                                title = "Application Link Present",
                                description = "External application link detected.",
                                severity = SignalSeverity.INFO,
                                weight = 2
                            )
                        )
                    }
                }
            } else {
                signals.addAll(urlAnalyzer.analyzeUrl(normalizedUrl))
            }
        }

        // 3. ML Prediction & Fusion
        val mlPrediction = textClassifier.classify(combinedText)
        val fusionResult = fusionEngine.fuse(mlPrediction, signals)

        return UrlRiskResult(
            url = if (url.isNotBlank()) url else "Text Content",
            normalizedUrl = if (normalizedUrl.isNotBlank()) normalizedUrl else "Text Content",
            domain = host,
            riskScore = fusionResult.score,
            riskLevel = fusionResult.level,
            confidencePercent = fusionResult.confidencePercent,
            urlRole = role,
            intent = intent,
            signals = signals.distinctBy { it.code },
            explanation = fusionResult.explanation,
            contextSnippet = contextSnippet,
            verificationStatus = fusionResult.verificationStatus,
            scoreBreakdown = fusionResult.scoreBreakdown
        )
    }
}