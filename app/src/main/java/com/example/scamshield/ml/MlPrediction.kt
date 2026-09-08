package com.example.scamshield.ml

data class MlPrediction(
    val intent: MlCategory,
    val scamProbability: Double, // 0.0 to 1.0
    val confidence: Double // 0.0 to 1.0
)