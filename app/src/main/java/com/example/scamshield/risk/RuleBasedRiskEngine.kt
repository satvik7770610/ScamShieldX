package com.example.scamshield.risk

import com.example.scamshield.ml.ModelManager
import com.example.scamshield.ml.TextClassifier
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.model.RiskLevel
import com.example.scamshield.model.RiskSignal
import com.example.scamshield.model.ScoreBreakdown
import com.example.scamshield.model.SignalSeverity
import com.example.scamshield.model.VerificationStatus
import com.example.scamshield.risk.analyzer.DomainAnalyzer
import com.example.scamshield.risk.analyzer.MessageStructureAnalyzer
import com.example.scamshield.risk.analyzer.MultiUrlScreenshotAnalyzer
import com.example.scamshield.risk.analyzer.PaymentRiskAnalyzer
import com.example.scamshield.risk.analyzer.RecruitmentScamDetector
import com.example.scamshield.risk.analyzer.RedirectAnalyzer
import com.example.scamshield.risk.analyzer.TextRiskAnalyzer
import com.example.scamshield.risk.analyzer.TrustedDomainRegistry
import com.example.scamshield.risk.analyzer.UrlRiskAnalyzer
import com.example.scamshield.risk.analyzer.UrlRole
import com.example.scamshield.risk.analyzer.UrlRoleClassifier
import com.example.scamshield.risk.fusion.FusionEngine
import com.example.scamshield.risk.intent.MessageIntent
import com.example.scamshield.risk.intent.MessageIntentClassifier

class RuleBasedRiskEngine(
    private val textAnalyzer: TextRiskAnalyzer = TextRiskAnalyzer(),
    private val urlAnalyzer: UrlRiskAnalyzer = UrlRiskAnalyzer(),
    private val paymentAnalyzer: PaymentRiskAnalyzer = PaymentRiskAnalyzer(),
    private val domainAnalyzer: DomainAnalyzer = DomainAnalyzer(),
    private val intentClassifier: MessageIntentClassifier = MessageIntentClassifier(),
    private val structureAnalyzer: MessageStructureAnalyzer = MessageStructureAnalyzer(),
    private val urlRoleClassifier: UrlRoleClassifier = UrlRoleClassifier(),
    private val trustedDomainRegistry: TrustedDomainRegistry = TrustedDomainRegistry(),
    private val redirectAnalyzer: RedirectAnalyzer = RedirectAnalyzer(),
    private val recruitmentScamDetector: RecruitmentScamDetector = RecruitmentScamDetector(),
    private val textClassifier: TextClassifier = ModelManager.getClassifier(),
    private val fusionEngine: FusionEngine = FusionEngine(),
    private val multiUrlScreenshotAnalyzer: MultiUrlScreenshotAnalyzer = MultiUrlScreenshotAnalyzer()
) : RiskEngine {

    override fun analyze(input: AnalysisInput): RiskAnalysisResult {
        return when (input) {
            is AnalysisInput.QrPayload -> analyzeQrPayload(input.rawContent)
            is AnalysisInput.UrlInput -> analyzeUrlInput(input.url)
            is AnalysisInput.TextInput -> analyzeTextInput(input.text)
            is AnalysisInput.ScreenshotInput -> analyzeScreenshotInput(input.extractedText)
        }
    }

    private fun analyzeQrPayload(rawContent: String): RiskAnalysisResult {
        val trimmed = rawContent.trim()
        val signals = mutableListOf<RiskSignal>()
        var recipient: String? = null
        var threatCategory = "QR Payload"

        val paymentPayload = paymentAnalyzer.parsePaymentPayload(trimmed)
        if (paymentPayload.isUpiScheme) {
            threatCategory = "UPI / Payment QR"
            recipient = paymentPayload.payeeVpa ?: paymentPayload.payeeName
            signals.addAll(paymentAnalyzer.analyzePayment(paymentPayload))

            if (!paymentPayload.transactionNote.isNullOrBlank()) {
                signals.addAll(textAnalyzer.analyzeText(paymentPayload.transactionNote))
            }
        } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains(".")) {
            threatCategory = "Web Link QR"
            signals.addAll(urlAnalyzer.analyzeUrl(trimmed))
            signals.addAll(textAnalyzer.analyzeText(trimmed))
        } else {
            threatCategory = "Text QR"
            signals.addAll(textAnalyzer.analyzeText(trimmed))
        }

        return calculateResult(
            signals = signals,
            rawPayload = trimmed,
            recipient = recipient,
            threatCategory = threatCategory
        )
    }

    private fun analyzeUrlInput(url: String): RiskAnalysisResult {
        val trimmed = url.trim()
        val signals = mutableListOf<RiskSignal>()

        val intent = intentClassifier.classify(trimmed)
        val isRecruitmentOrCareer = intentClassifier.isRecruitmentOrCareerIntent(intent)

        val host = domainAnalyzer.extractHost(trimmed) ?: "unknown-host"
        val role = urlRoleClassifier.classifyRole(trimmed, host)

        signals.addAll(redirectAnalyzer.analyzeRedirect(trimmed, host))

        val domainSignals = domainAnalyzer.analyzeDomain(trimmed)
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
            signals.addAll(urlAnalyzer.analyzeUrl(trimmed))
            signals.addAll(textAnalyzer.analyzeText(trimmed))
        }

        return calculateResult(
            signals = signals,
            rawPayload = trimmed,
            recipient = host,
            threatCategory = "URL / Web Link"
        )
    }

    private fun analyzeTextInput(text: String): RiskAnalysisResult {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return calculateResult(
                signals = emptyList(),
                rawPayload = trimmed,
                recipient = null,
                threatCategory = "Text / SMS"
            )
        }

        val signals = mutableListOf<RiskSignal>()

        // Module 1: Intent Classification
        val intent = intentClassifier.classify(trimmed)
        val isRecruitmentOrCareer = intentClassifier.isRecruitmentOrCareerIntent(intent)

        // Module 2: Structure Analysis
        signals.addAll(structureAnalyzer.analyzeStructure(trimmed))

        // Module 6: Recruitment Scam Detector
        if (isRecruitmentOrCareer) {
            signals.addAll(recruitmentScamDetector.analyzeRecruitmentScams(trimmed))
        }

        // Module 7: Behavioral Text Signals
        val textSignals = textAnalyzer.analyzeText(trimmed)
        val hasFeeOrPaymentDemand = signals.any { it.code == "RECRUITMENT_FEE_DEMAND" || it.code == "RECRUITMENT_PAY_FOR_OFFER" } ||
                textSignals.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" || it.code == "TEXT_REWARD_REFUND_SCAM" }
        val hasOtpOrCredentialDemand = textSignals.any { it.code == "TEXT_OTP_HARVEST" }

        // Filter text signals if recruitment context has NO fee/OTP demands
        if (isRecruitmentOrCareer && !hasFeeOrPaymentDemand && !hasOtpOrCredentialDemand) {
            val nonThreatTextSignals = textSignals.filter {
                it.code != "TEXT_URGENT_PRESSURE" && it.code != "TEXT_CLICK_LINK_REQUEST"
            }
            signals.addAll(nonThreatTextSignals)
        } else {
            signals.addAll(textSignals)
        }

        // Contextual Interaction Rules:
        if (isRecruitmentOrCareer && hasFeeOrPaymentDemand) {
            signals.add(
                RiskSignal(
                    code = "JOB_PAYMENT_SCAM_COMBINATION",
                    title = "Job Opportunity & Upfront Payment Combination",
                    description = "Dangerous combination: Recruitment post requires paying an upfront fee or deposit.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 35
                )
            )
        }

        if (isRecruitmentOrCareer && hasOtpOrCredentialDemand) {
            signals.add(
                RiskSignal(
                    code = "JOB_OTP_SCAM_COMBINATION",
                    title = "Job Opportunity & OTP Harvest Combination",
                    description = "Dangerous combination: Recruitment post requests confidential OTP or login credentials.",
                    severity = SignalSeverity.CRITICAL,
                    weight = 35
                )
            )
        }

        // Context-Aware Embedded URL Processing
        val embeddedUrls = extractUrlsFromText(trimmed)
        for (url in embeddedUrls) {
            val host = domainAnalyzer.extractHost(url) ?: continue
            val role = urlRoleClassifier.classifyRole(url, host)

            // Module 5: Redirect Analysis
            signals.addAll(redirectAnalyzer.analyzeRedirect(url, host))

            // Check for explicit critical domain phishing/impersonation
            val domainSignals = domainAnalyzer.analyzeDomain(url)
            val criticalDomainSignals = domainSignals.filter {
                it.code == "URL_LOOKALIKE_BRAND" || it.code == "URL_BRAND_IMPERSONATION" ||
                        it.code == "DOMAIN_IP_HOST" || it.code == "URL_EMBEDDED_CREDENTIALS"
            }
            signals.addAll(criticalDomainSignals)

            // URL Risk Weight in Recruitment / Career Context:
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
                signals.addAll(urlAnalyzer.analyzeUrl(url))
            }
        }

        val threatCategoryName = when (intent) {
            MessageIntent.JOB_RECRUITMENT -> "Job Recruitment"
            MessageIntent.CAMPUS_AMBASSADOR -> "Campus Ambassador"
            MessageIntent.INTERNSHIP -> "Internship Opportunity"
            MessageIntent.FELLOWSHIP -> "Fellowship Program"
            MessageIntent.ACCOUNT_SECURITY -> "Account Security"
            MessageIntent.PRIZE_REWARD -> "Prize / Reward"
            MessageIntent.REFUND -> "Refund Request"
            MessageIntent.PAYMENT -> "Payment Request"
            else -> "Text / SMS"
        }

        return calculateResult(
            signals = signals,
            rawPayload = trimmed,
            recipient = if (embeddedUrls.isNotEmpty()) domainAnalyzer.extractHost(embeddedUrls.first()) else null,
            threatCategory = threatCategoryName
        )
    }

    private fun analyzeScreenshotInput(extractedText: String): RiskAnalysisResult {
        val screenshotResult = multiUrlScreenshotAnalyzer.analyzeScreenshot(extractedText)
        val textSignals = textAnalyzer.analyzeText(extractedText)

        val allSignals = (screenshotResult.urlResults.flatMap { it.signals } + textSignals).distinctBy { it.code }

        val textBehaviorScore = textSignals.filter {
            it.code == "TEXT_ACCOUNT_THREAT" || it.code == "TEXT_OTP_HARVEST" ||
                    it.code == "TEXT_MONEY_TRANSFER_DEMAND" || it.code == "RECRUITMENT_FEE_DEMAND"
        }.sumOf { it.weight }

        val finalOverallScore = maxOf(screenshotResult.overallScore, textBehaviorScore).coerceIn(0, 100)

        val finalLevel = when {
            finalOverallScore >= 60 -> RiskLevel.HIGH_RISK
            finalOverallScore >= 30 -> RiskLevel.SUSPICIOUS
            else -> RiskLevel.LOW
        }

        return RiskAnalysisResult(
            score = finalOverallScore,
            level = finalLevel,
            recipient = screenshotResult.urlResults.firstOrNull()?.domain,
            signals = allSignals,
            threatCategory = "Screenshot OCR",
            recommendedAction = screenshotResult.explanation,
            rawPayload = extractedText,
            confidencePercent = screenshotResult.overallConfidencePercent,
            verificationStatus = VerificationStatus.UNVERIFIED_LOCAL,
            scoreBreakdown = screenshotResult.urlResults.firstOrNull()?.scoreBreakdown ?: ScoreBreakdown()
        )
    }

    private fun calculateResult(
        signals: List<RiskSignal>,
        rawPayload: String,
        recipient: String?,
        threatCategory: String
    ): RiskAnalysisResult {
        val distinctSignals = signals.distinctBy { it.code }

        // Module: On-Device ML Prediction
        val mlPrediction = textClassifier.classify(rawPayload)

        // Module: Fusion Engine (ML Prediction + Deterministic Rule Signals)
        val fusionResult = fusionEngine.fuse(
            prediction = mlPrediction,
            ruleSignals = distinctSignals
        )

        return RiskAnalysisResult(
            score = fusionResult.score,
            level = fusionResult.level,
            recipient = recipient,
            signals = distinctSignals,
            threatCategory = threatCategory,
            recommendedAction = fusionResult.explanation,
            rawPayload = rawPayload,
            confidencePercent = fusionResult.confidencePercent,
            verificationStatus = fusionResult.verificationStatus,
            scoreBreakdown = fusionResult.scoreBreakdown
        )
    }

    private fun extractUrlsFromText(text: String): List<String> {
        val urlRegex = Regex("""(https?://[^\s]+|[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}[^\s]*)""")
        return urlRegex.findAll(text).map {
            it.value.trimEnd('.', ',', ')', ']', '!', '?', ';', ':')
        }.filter { it.isNotBlank() }.toList()
    }
}