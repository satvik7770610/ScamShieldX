package com.example.scamshield.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenshotOcrManager {

    fun processImageUri(
        context: Context,
        imageUri: Uri,
        scope: CoroutineScope,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            val recognizer = try {
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            } catch (_: Throwable) {
                null
            }

            if (recognizer == null) {
                withContext(Dispatchers.Main) {
                    onFailure("Unable to analyze this image.")
                }
                return@launch
            }

            try {
                val inputImage = loadInputImage(context, imageUri)
                if (inputImage == null) {
                    withContext(Dispatchers.Main) {
                        onFailure("Unable to analyze this image.")
                    }
                    return@launch
                }

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text.trim()
                        if (text.isNotEmpty()) {
                            onSuccess(text)
                        } else {
                            onFailure("No readable text detected.")
                        }
                    }
                    .addOnFailureListener {
                        onFailure("Unable to analyze this image.")
                    }
            } catch (_: Throwable) {
                withContext(Dispatchers.Main) {
                    onFailure("Unable to analyze this image.")
                }
            }
        }
    }

    private fun loadInputImage(context: Context, uri: Uri): InputImage? {
        return try {
            InputImage.fromFilePath(context, uri)
        } catch (_: Throwable) {
            val bitmap = loadSafeBitmap(context, uri)
            if (bitmap != null) {
                try {
                    InputImage.fromBitmap(bitmap, 0)
                } catch (_: Throwable) {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun loadSafeBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver = context.contentResolver
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            var sampleSize = 1
            val maxDimension = 2048
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            }
        } catch (_: Throwable) {
            null
        }
    }
}