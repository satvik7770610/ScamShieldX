package com.example.scamshield.risk

import com.example.scamshield.ml.ModelManager
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.risk.analyzer.MultiUrlExtractorAndContextAssociator
import org.junit.Before
import org.junit.Test
import java.util.Locale

class CodeLevel17DiagnosticTest {

    private lateinit var riskEngine: RuleBasedRiskEngine
    private lateinit var urlExtractor: MultiUrlExtractorAndContextAssociator

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
        urlExtractor = MultiUrlExtractorAndContextAssociator()
    }

    @Test
    fun testExactUserSuspiciousInputDiagnostic() {
        val userInput = """
            Your job application has been shortlisted for the next stage.

            To proceed with interview scheduling, please complete your candidate verification.

            Use the verification link below:
            https://example.com/candidate-verification

            During verification, you will need to confirm your date of birth and mobile number.

            This verification request expires today. Failure to complete it may result in your interview being delayed.
        """.trimIndent()

        println("\n========================================================================")
        println("STEP 4: CODE-LEVEL COMPONENT BREAKDOWN FOR USER SUSPICIOUS RECRUITMENT INPUT")
        println("========================================================================")

        val classifier = ModelManager.getClassifier()
        val mlPred = classifier.classify(userInput)
        val result = riskEngine.analyze(AnalysisInput.TextInput(userInput))
        val extracted = urlExtractor.extractAndAssociateContext(userInput)

        println("ML intent: ${mlPred.intent.name}")
        println("ML scam probability: ${String.format(Locale.ROOT, "%.4f", mlPred.scamProbability)}")
        println("ML confidence: ${String.format(Locale.ROOT, "%.4f", mlPred.confidence)}")

        println("Extracted URLs: ${extracted.map { it.rawUrl }}")
        println("URL count: ${extracted.size}")

        if (extracted.isNotEmpty()) {
            val singleUrl = extracted[0].normalizedUrl
            val urlRes = riskEngine.analyze(AnalysisInput.UrlInput(singleUrl))
            println("URL score: ${urlRes.score}")
            println("URL signals: ${urlRes.signals.map { "${it.code} (${it.weight})" }}")
        } else {
            println("URL score: N/A (No URL found)")
            println("URL signals: N/A")
        }

        println("Text rule signals: ${result.signals.map { "${it.code} (${it.weight})" }}")
        println("Text rule score: ${result.scoreBreakdown.textSignalsScore + result.scoreBreakdown.urlSignalsScore + result.scoreBreakdown.domainSignalsScore + result.scoreBreakdown.behaviorSignalsScore}")

        println("Urgency signal: ${result.signals.any { it.code == "TEXT_URGENT_PRESSURE" }}")
        println("Verification signal: ${result.signals.any { it.code == "TEXT_SUSPICIOUS_VERIFICATION" }}")
        println("Personal information signal: ${result.signals.any { it.code == "TEXT_OTP_HARVEST" || it.code == "URL_EMBEDDED_CREDENTIALS" }}")
        println("Recruitment signal: ${result.signals.any { it.code == "RECRUITMENT_STRUCTURE_DETECTED" || it.code == "JOB_PAYMENT_SCAM_COMBINATION" || it.code == "RECRUITMENT_FEE_DEMAND" }}")
        println("Payment signal: ${result.signals.any { it.code == "TEXT_MONEY_TRANSFER_DEMAND" || it.code == "RECRUITMENT_FEE_DEMAND" }}")
        println("OTP signal: ${result.signals.any { it.code == "TEXT_OTP_HARVEST" }}")
        println("Credential signal: ${result.signals.any { it.code == "TEXT_OTP_HARVEST" }}")

        println("ML contribution: ${result.scoreBreakdown.mlScore}")
        println("Rule contribution: ${result.scoreBreakdown.textSignalsScore + result.scoreBreakdown.urlSignalsScore + result.scoreBreakdown.domainSignalsScore + result.scoreBreakdown.behaviorSignalsScore}")
        println("URL contribution: ${result.scoreBreakdown.urlSignalsScore}")

        println("Fusion score: ${result.score}")
        println("Final displayed score: ${result.score} (${result.level})")
    }

    @Test
    fun testStep5ClearScamDiagnostic() {
        val scamInput = """
            Congratulations! You have been selected for the job.

            To confirm your interview, pay ₹999 registration fee.

            After payment, send the OTP to HR for verification.
        """.trimIndent()

        println("\n========================================================================")
        println("STEP 5: CODE-LEVEL COMPONENT BREAKDOWN FOR CLEAR SCAM INPUT")
        println("========================================================================")

        val classifier = ModelManager.getClassifier()
        val mlPred = classifier.classify(scamInput)
        val result = riskEngine.analyze(AnalysisInput.TextInput(scamInput))

        println("ML intent: ${mlPred.intent.name}")
        println("ML scam probability: ${String.format(Locale.ROOT, "%.4f", mlPred.scamProbability)}")
        println("ML confidence: ${String.format(Locale.ROOT, "%.4f", mlPred.confidence)}")
        println("Text rule signals: ${result.signals.map { "${it.code} (${it.weight})" }}")
        println("Text rule score: ${result.scoreBreakdown.textSignalsScore + result.scoreBreakdown.urlSignalsScore + result.scoreBreakdown.domainSignalsScore + result.scoreBreakdown.behaviorSignalsScore}")
        println("Fusion score: ${result.score}")
        println("Final displayed score: ${result.score} (${result.level})")
    }

    @Test
    fun testStep6NormalMessageDiagnostic() {
        val normalInput = """
            Your interview is scheduled for Monday at 10 AM.
            Please bring your resume and college ID.
        """.trimIndent()

        println("\n========================================================================")
        println("STEP 6: CODE-LEVEL COMPONENT BREAKDOWN FOR NORMAL MESSAGE INPUT")
        println("========================================================================")

        val classifier = ModelManager.getClassifier()
        val mlPred = classifier.classify(normalInput)
        val result = riskEngine.analyze(AnalysisInput.TextInput(normalInput))

        println("ML intent: ${mlPred.intent.name}")
        println("ML scam probability: ${String.format(Locale.ROOT, "%.4f", mlPred.scamProbability)}")
        println("ML confidence: ${String.format(Locale.ROOT, "%.4f", mlPred.confidence)}")
        println("Text rule signals: ${result.signals.map { "${it.code} (${it.weight})" }}")
        println("Text rule score: ${result.scoreBreakdown.textSignalsScore + result.scoreBreakdown.urlSignalsScore + result.scoreBreakdown.domainSignalsScore + result.scoreBreakdown.behaviorSignalsScore}")
        println("Fusion score: ${result.score}")
        println("Final displayed score: ${result.score} (${result.level})")
    }

    @Test
    fun testStep3ComparisonTable() {
        val case1 = """
            Your job application has been shortlisted for the next stage.
            Our recruitment team will contact you with the interview schedule.
            Please keep your resume ready for the interview.
        """.trimIndent()

        val case2 = """
            Your job application has been shortlisted for the next stage.

            To proceed with interview scheduling, please complete your candidate verification.

            Use the verification link below:
            https://example.com/candidate-verification

            During verification, you will need to confirm your date of birth and mobile number.

            This verification request expires today. Failure to complete it may result in your interview being delayed.
        """.trimIndent()

        val case3 = """
            Congratulations! You have been selected for the job.

            To confirm your interview, pay ₹999 registration fee.

            After payment, send the OTP to HR for verification.
        """.trimIndent()

        println("\n==========================================================================================================")
        println("STEP 3 COMPARISON TABLE: ML TEST LAB vs TEXT ANALYZER")
        println("==========================================================================================================")
        println("Case | Path | ML Intent | ML Scam % | Confidence | URL Risk | Rule Signals | Rule Score | Fusion Score | Final Risk")
        println("----------------------------------------------------------------------------------------------------------")

        runTableDiagnostic("CASE 1 (Legitimate)", case1)
        runTableDiagnostic("CASE 2 (Suspicious)", case2)
        runTableDiagnostic("CASE 3 (Scam)", case3)
    }

    private fun runTableDiagnostic(caseName: String, text: String) {
        val classifier = ModelManager.getClassifier()

        val mlPred = classifier.classify(text)
        val mlScamPct = "${String.format(Locale.ROOT, "%.1f", mlPred.scamProbability * 100)}%"
        val mlConfPct = "${String.format(Locale.ROOT, "%.1f", mlPred.confidence * 100)}%"

        println("$caseName | ML Test Lab | ${mlPred.intent.name} | $mlScamPct | $mlConfPct | N/A | N/A | N/A | N/A | N/A")

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