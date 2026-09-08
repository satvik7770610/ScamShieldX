package com.example.scamshield.camerashield

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CameraOcrManager {

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val recognizer = try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (_: Throwable) {
            try { imageProxy.close() } catch (_: Throwable) {}
            onFailure("Unable to analyze this image.")
            return
        }

        try {
            val mediaImage = imageProxy.image
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val inputImage = if (mediaImage != null) {
                try {
                    InputImage.fromMediaImage(mediaImage, rotationDegrees)
                } catch (_: Throwable) {
                    try {
                        val bitmap = imageProxy.toBitmap()
                        InputImage.fromBitmap(bitmap, rotationDegrees)
                    } catch (_: Throwable) {
                        null
                    }
                }
            } else {
                try {
                    val bitmap = imageProxy.toBitmap()
                    InputImage.fromBitmap(bitmap, rotationDegrees)
                } catch (_: Throwable) {
                    null
                }
            }

            if (inputImage == null) {
                try { imageProxy.close() } catch (_: Throwable) {}
                onFailure("Unable to analyze this image.")
                return
            }

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text.trim()
                    if (text.isNotBlank()) {
                        onSuccess(text)
                    } else {
                        onFailure("No readable text detected.")
                    }
                }
                .addOnFailureListener {
                    onFailure("Unable to analyze this image.")
                }
                .addOnCompleteListener {
                    try { imageProxy.close() } catch (_: Throwable) {}
                }
        } catch (_: Throwable) {
            try { imageProxy.close() } catch (_: Throwable) {}
            onFailure("Unable to analyze this image.")
        }
    }
}