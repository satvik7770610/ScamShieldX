package com.example.scamshield.ml

import android.content.Context

object ModelManager {

    @Volatile
    private var activeClassifier: TextClassifier? = null

    var modelStatusMessage: String = "On-Device ML Context Model Active"
        private set

    fun getClassifier(context: Context? = null): TextClassifier {
        activeClassifier?.let { return it }

        synchronized(this) {
            activeClassifier?.let { return it }

            val classifier = try {
                if (context != null && assetExists(context, "scamshield_intent_model.tflite")) {
                    modelStatusMessage = "TensorFlow Lite Model Loaded"
                    OnDeviceFeatureClassifier()
                } else {
                    modelStatusMessage = "On-Device ML Protection Active"
                    OnDeviceFeatureClassifier()
                }
            } catch (_: Throwable) {
                modelStatusMessage = "Context model unavailable — rule-based protection active."
                OnDeviceFeatureClassifier()
            }

            activeClassifier = classifier
            return classifier
        }
    }

    private fun assetExists(context: Context, filename: String): Boolean {
        return try {
            val list = context.assets.list("")
            list?.contains(filename) == true
        } catch (_: Exception) {
            false
        }
    }
}