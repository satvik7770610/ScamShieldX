package com.example.scamshield.risk

import com.example.scamshield.ml.ModelManager
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.risk.analyzer.MultiUrlExtractorAndContextAssociator
import org.junit.Before
import org.junit.Test
import java.util.Locale

class TextAnalyzerVsMlLabTest {

    private lateinit var riskEngine: RuleBasedRiskEngine
    private lateinit var urlExtractor: MultiUrlExtractorAndContextAssociator

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
        urlExtractor = MultiUrlExtractorAndContextAssociator()
    }

    @Test
    fun testUserTargetInputDiagnostic() {
        val targetText = """
            Your job application has been shortlisted for the next stage.

            Before your interview can be scheduled, please complete the candidate verification process using the link below:

            https://example.com/candidate-verification

            The verification link will remain active for a limited time. Please complete the process as soon as possible to avoid delays in your interview.
        """.trimIndent()

        println("\n==================================================")
        println("DIAGNOSTIC: USER TARGET INPUT (17/100 Investigation)")
        println("==================================================")

        val classifier = ModelManager.getClassifier()
        val mlPred = classifier.classify(targetText)
        val result = riskEngine.analyze(AnalysisInput.TextInput(targetText))

        val extracted = urlExtractor.extractAndAssociateContext(targetText)

        println("1. Raw input text length: ${targetText.length}")
        println("2. Preprocessed text: ${targetText.trim()}")
        println("3. URLs extracted: ${extracted.map { it.rawUrl }}")
        println("4. Number of valid URLs: ${extracted.size}")

        if (extracted.isNotEmpty()) {
            val singleUrl = extracted[0].normalizedUrl
            val urlResult = riskEngine.analyze(AnalysisInput.UrlInput(singleUrl))
            println("5. URL risk score for ${singleUrl}: ${urlResult.score}")
            println("6. URL/domain signals: ${urlResult.signals.map { "${it.code} (${it.weight})" }}")
        } else {
            println("5. URL risk score: N/A (No valid URLs extracted)")
            println("6. URL/domain signals: N/A")
        }

        println("7. ML intent: ${mlPred.intent.name}")
        println("8. ML scam probability: ${String.format(Locale.ROOT, "%.1f", mlPred.scamProbability * 100)}%")
        println("9. ML confidence: ${String.format(Locale.ROOT, "%.1f", mlPred.confidence * 100)}%")
        println("10. Text/rule signals detected: ${result.signals.map { "${it.code} (${it.weight})" }}")
        println("11. Recruitment signal: ${result.signals.any { it.code == "RECRUITMENT_STRUCTURE_DETECTED" || it.code == "JOB_PAYMENT_SCAM_COMBINATION" }}")
        println("12. Verification signal: ${result.signals.any { it.code == "TEXT_SUSPICIOUS_VERIFICATION" }}")
        println("13. Urgency signal: ${result.signals.any { it.code == "TEXT_URGENT_PRESSURE" }}")
        println("14. External-link signal: ${result.signals.any { it.code == "URL_APPLICATION_LINK" || it.code == "URL_UNVERIFIED_DESTINATION" || it.code == "TEXT_CLICK_LINK_REQUEST" }}")
        println("15. Payment signal: ${result.signals.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" || it.code == "RECRUITMENT_FEE_DEMAND" }}")
        println("16. OTP signal: ${result.signals.any { it.code == "TEXT_OTP_HARVEST" }}")
        println("17. Credential/personal-information signal: ${result.signals.any { it.code == "TEXT_OTP_HARVEST" || it.code == "URL_EMBEDDED_CREDENTIALS" }}")
        println("18. Rule score: ${result.scoreBreakdown.textSignalsScore + result.scoreBreakdown.urlSignalsScore + result.scoreBreakdown.domainSignalsScore + result.scoreBreakdown.behaviorSignalsScore}")
        println("19. ML contribution: ${result.scoreBreakdown.mlScore}")
        println("20. Fusion score: ${result.score}")
        println("21. Final overall score: ${result.score} (${result.level})")
    }

    @Test
    fun testCompareThreeCasesAcrossMlLabAndTextAnalyzer() {
        val case1 = """
            Your job application has been shortlisted for the next stage.
            Our recruitment team will contact you with the interview schedule.
            Please keep your resume ready for the interview.
        """.trimIndent()

        val case2 = """
            Your job application has been shortlisted for the next stage.
            Complete candidate verification using:
            https://example.com/candidate-verification
            The verification window expires today.
        """.trimIndent()

        val case3 = """
            Congratulations! You have been selected for the job.
            To confirm your interview, pay ₹999 registration fee.
            After payment, send the OTP to HR for verification.
        """.trimIndent()

        println("\n==========================================================================================================")
        println("COMPARISON TABLE: ML TEST LAB vs TEXT ANALYZER")
        println("==========================================================================================================")
        println("Case | Path | ML Intent | ML Scam % | Confidence | URL Risk | Rule Signals | Rule Score | Fusion Score | Final Risk")
        println("----------------------------------------------------------------------------------------------------------")

        runTableDiagnostic("CASE 1 (Legitimate)", case1)
        runTableDiagnostic("CASE 2 (Suspicious)", case2)
        runTableDiagnostic("CASE 3 (Scam)", case3)
    }

    private fun runTableDiagnostic(caseName: String, text: String) {
        val classifier = ModelManager.getClassifier()

        // Path A: ML Test Lab (Raw ML Classifier Output)
        val mlPred = classifier.classify(text)
        val mlScamPct = "${String.format(Locale.ROOT, "%.1f", mlPred.scamProbability * 100)}%"
        val mlConfPct = "${String.format(Locale.ROOT, "%.1f", mlPred.confidence * 100)}%"

        println("$caseName | ML Test Lab | ${mlPred.intent.name} | $mlScamPct | $mlConfPct | N/A | N/A | N/A | N/A | N/A")

        // Path B: Text Analyzer (Full Engine & Fusion)
        val result = riskEngine.analyze(AnalysisInput.TextInput(text))
        val extracted = urlExtractor.extractAndAssociateContext(text)
        val urlRiskStr = if (extracted.isNotEmpty()) {
            val urlRes = riskEngine.analyze(AnalysisInput.UrlInput(extracted[0].normalizedUrl))
            "${urlRes.score}"
        } else {
            "0 (No URL)"
        }

        val ruleScore = result.scoreBreakdown.textSignalsScore + result.scoreBreakdown.urlSignalsScore + result.scoreBreakdown.domainSignalsScore + result.scoreBreakdown.behaviorSignalsScore
        val signalsList = result.signals.map { it.code }.joinToString(",")

        println("$caseName | Text Analyzer | ${mlPred.intent.name} | $mlScamPct | $mlConfPct | $urlRiskStr | [$signalsList] | $ruleScore | ${result.score} | ${result.level}")
        println("----------------------------------------------------------------------------------------------------------")
    }
}