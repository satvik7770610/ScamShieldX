package com.example.scamshield.ml

interface TextClassifier {
    fun classify(text: String): MlPrediction
    fun isModelLoaded(): Boolean
}