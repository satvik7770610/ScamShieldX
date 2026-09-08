package com.example.scamshield.risk

import com.example.scamshield.model.AnalysisInput
import com.example.scamshield.model.RiskAnalysisResult

/**
 * Modular risk-analysis engine interface for ScamShield X.
 * Designed to support rule-based evaluation and future ML models without altering caller code.
 */
interface RiskEngine {
    fun analyze(input: AnalysisInput): RiskAnalysisResult
}