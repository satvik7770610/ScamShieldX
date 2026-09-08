package com.example.scamshield.risk

import com.example.scamshield.ml.ModelManager
import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.risk.analyzer.MultiUrlExtractorAndContextAssociator
import org.junit.Before
import org.junit.Test
import java.util.Locale

class RuntimeValuesPrinterTest {

    private lateinit var riskEngine: RuleBasedRiskEngine
    private lateinit var urlExtractor: MultiUrlExtractorAndContextAssociator

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
        urlExtractor = MultiUrlExtractorAndContextAssociator()
    }

    @Test
    fun printRuntimeValuesForBothInputs() {
        val input1Text = """
            Your job application has been shortlisted for the next stage.

            To proceed with interview scheduling, please complete your candidate verification.

            Use the verification link below:
            https://example.com/candidate-verification

            During verification, you will need to confirm your date of birth and mobile number.

            This verification request expires today. Failure to complete it may result in your interview being delayed.
        """.trimIndent()

        val input2Text = """
            Congratulations! You have been selected for the job.

            To confirm your interview, pay ₹999 registration fee.

            After payment, send the OTP to HR for verification.
        """.trimIndent()

        println("\n========================================================================")
        println("INPUT 1: SUSPICIOUS RECRUITMENT VERIFICATION MESSAGE")
        println("========================================================================")
        printExactValuesForInput(input1Text)

        println("\n========================================================================")
        println("INPUT 2: OBVIOUS RECRUITMENT SCAM MESSAGE")
        println("========================================================================")
        printExactValuesForInput(input2Text)
    }

    private fun printExactValuesForInput(text: String) {
        val classifier = ModelManager.getClassifier()
        val mlPred = classifier.classify(text)

        val result = riskEngine.analyze(AnalysisInput.TextInput(text))
        val extracted = urlExtractor.extractAndAssociateContext(text)

        val urlRiskValue = if (extracted.isNotEmpty()) {
            val urlRes = riskEngine.analyze(AnalysisInput.UrlInput(extracted[0].normalizedUrl))
            "${urlRes.score}"
        } else {
            "0 (No URL)"
        }

        println("ML intent = ${mlPred.intent.name}")
        println("ML scam probability = ${String.format(Locale.ROOT, "%.2f", mlPred.scamProbability)}")
        println("ML confidence = ${String.format(Locale.ROOT, "%.2f", mlPred.confidence)}")
        println()
        println("Extracted URL = ${if (extracted.isNotEmpty()) extracted[0].rawUrl else "None"}")
        println("URL risk = $urlRiskValue")
        println()
        println("Text rule signals = ${result.signals.map { "${it.code} (${it.weight})" }}")
        println("Text rule score = ${result.scoreBreakdown.textSignalsScore}")
        println()
        println("ML contribution = ${result.scoreBreakdown.mlScore}")
        println("Rule contribution = ${result.scoreBreakdown.textSignalsScore + result.scoreBreakdown.urlSignalsScore + result.scoreBreakdown.domainSignalsScore + result.scoreBreakdown.behaviorSignalsScore}")
        println("URL contribution = ${result.scoreBreakdown.urlSignalsScore}")
        println()
        println("Fusion score = ${result.score}")
        println("Final displayed score = ${result.score} / 100 (${result.level})")
        println("Producing Function = FusionEngine.fuse() in com.example.scamshield.risk.fusion.FusionEngine")
    }
}