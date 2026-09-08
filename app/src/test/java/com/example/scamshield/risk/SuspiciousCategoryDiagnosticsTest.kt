package com.example.scamshield.risk

import com.example.scamshield.ml.ModelManager
import com.example.scamshield.model.AnalysisInput
import org.junit.Before
import org.junit.Test

class SuspiciousCategoryDiagnosticsTest {

    private lateinit var riskEngine: RuleBasedRiskEngine

    @Before
    fun setUp() {
        riskEngine = RuleBasedRiskEngine()
    }

    @Test
    fun runDiagnosticsOnThreeCases() {
        val case1Text = """
            Your job application has been shortlisted for the next stage.
            Our recruitment team will contact you with the interview schedule.
            Please keep your resume ready for the interview.
        """.trimIndent()

        val case2Text = """
            Your job application has been shortlisted for the next stage.
            Complete candidate verification using:
            https://example.com/candidate-verification
            The verification window expires today.
        """.trimIndent()

        val case3Text = """
            Congratulations! You have been selected for the job.
            To confirm your interview, pay ₹999 registration fee.
            After payment, send the OTP to HR for verification.
        """.trimIndent()

        println("\n==================================================")
        println("CASE 1: Legitimate Recruitment (No Links, No Fees)")
        println("==================================================")
        runCaseDiagnostic(case1Text)

        println("\n==================================================")
        println("CASE 2: Recruitment + External Link + Urgency Expiry")
        println("==================================================")
        runCaseDiagnostic(case2Text)

        println("\n==================================================")
        println("CASE 3: Recruitment + Fee + OTP Request")
        println("==================================================")
        runCaseDiagnostic(case3Text)
    }

    private fun runCaseDiagnostic(text: String) {
        val mlClassifier = ModelManager.getClassifier()
        val mlPrediction = mlClassifier.classify(text)

        val result = riskEngine.analyze(AnalysisInput.TextInput(text))

        println("TEXT_INPUT = $text")
        println("ML_INTENT = ${mlPrediction.intent.name}")
        println("ML_SCAM_PROBABILITY = ${mlPrediction.scamProbability}")
        println("ML_CONFIDENCE = ${mlPrediction.confidence}")
        println("DETECTED_SIGNALS = ${result.signals.map { "${it.code} (${it.weight})" }}")
        println("TEXT_SIGNALS_SCORE = ${result.scoreBreakdown.textSignalsScore}")
        println("URL_SIGNALS_SCORE = ${result.scoreBreakdown.urlSignalsScore}")
        println("DOMAIN_SIGNALS_SCORE = ${result.scoreBreakdown.domainSignalsScore}")
        println("BEHAVIOR_SIGNALS_SCORE = ${result.scoreBreakdown.behaviorSignalsScore}")
        println("FINAL_FUSION_SCORE = ${result.score}")
        println("FINAL_RISK_LEVEL = ${result.level}")
        println("EXPLANATION = ${result.recommendedAction}")
    }
}