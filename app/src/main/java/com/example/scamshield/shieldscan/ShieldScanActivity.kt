package com.example.scamshield.shieldscan

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.scamshield.MainActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ShieldScanActivity : AppCompatActivity() {

    private lateinit var projectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0

    private var capturedScreenBitmap: Bitmap? = null

    private lateinit var overlayView: ShieldScanSelectionOverlay
    private lateinit var btnAnalyze: Button
    private lateinit var btnCancel: Button
    private lateinit var tvStatus: TextView

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ShieldScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            setupMediaProjection(result.resultCode, result.data!!)
        } else {
            Toast.makeText(this, "Screen capture consent required for Shield Scan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure MediaProjectionService is started before screen capture
        MediaProjectionService.start(this)

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi

        setupUi()
        requestScreenCapture()
    }

    private fun setupUi() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(0xFF050505.toInt())
        }

        overlayView = ShieldScanSelectionOverlay(this).apply {
            onSelectionChanged = { rect ->
                btnAnalyze.isEnabled = rect != null
            }
        }
        rootLayout.addView(overlayView)

        // Control Banner
        val controlPanel = FrameLayout(this).apply {
            setBackgroundColor(0xCC0F0F0F.toInt())
            setPadding(32, 48, 32, 32)
        }

        tvStatus = TextView(this).apply {
            text = "SHIELD SCAN • CAPTURING SCREEN..."
            setTextColor(0xFF00E5FF.toInt())
            textSize = 12f
        }

        btnCancel = Button(this).apply {
            text = "CANCEL"
            setBackgroundColor(0xFF171717.toInt())
            setTextColor(0xFF94A3B8.toInt())
            textSize = 10f
            setOnClickListener { finish() }
        }

        btnAnalyze = Button(this).apply {
            text = "ANALYZE SELECTION →"
            setBackgroundColor(0xFF00E5FF.toInt())
            setTextColor(0xFF000000.toInt())
            textSize = 10f
            isEnabled = false
            setOnClickListener { processSelectedRegion() }
        }

        val buttonContainer = FrameLayout(this).apply {
            val cancelParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START
            }
            val analyzeParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }
            addView(btnCancel, cancelParams)
            addView(btnAnalyze, analyzeParams)
        }

        val panelParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }
        controlPanel.addView(buttonContainer)
        rootLayout.addView(controlPanel, panelParams)

        setContentView(rootLayout)
    }

    private fun requestScreenCapture() {
        val captureIntent = projectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(captureIntent)
    }

    private fun setupMediaProjection(resultCode: Int, data: Intent) {
        try {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    MediaProjectionService.stop(this@ShieldScanActivity)
                }
            }, Handler(Looper.getMainLooper()))

            imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
            imageReader?.setOnImageAvailableListener({ reader ->
                if (capturedScreenBitmap == null) {
                    val image: Image? = reader.acquireLatestImage()
                    if (image != null) {
                        try {
                            val planes = image.planes
                            val buffer = planes[0].buffer
                            val pixelStride = planes[0].pixelStride
                            val rowStride = planes[0].rowStride
                            val rowPadding = rowStride - pixelStride * screenWidth

                            val bitmap = Bitmap.createBitmap(
                                screenWidth + rowPadding / pixelStride,
                                screenHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            bitmap.copyPixelsFromBuffer(buffer)
                            capturedScreenBitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
                            bitmap.recycle()
                        } catch (_: Throwable) {
                        } finally {
                            image.close()
                        }

                        stopScreenCapture()
                        runOnUiThread {
                            tvStatus.text = "DRAG OVER SUSPICIOUS CONTENT TO ANALYZE"
                        }
                    }
                }
            }, Handler(Looper.getMainLooper()))

            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ShieldScanCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )
        } catch (e: Throwable) {
            Toast.makeText(this, "Screen capture error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun processSelectedRegion() {
        val rect = overlayView.selectionRect
        if (rect.isEmpty || rect.width() < 10 || rect.height() < 10) {
            Toast.makeText(this, "Selection area too small", Toast.LENGTH_SHORT).show()
            return
        }

        tvStatus.text = "ANALYZING SELECTION WITH ON-DEVICE ML..."
        btnAnalyze.isEnabled = false

        val sourceBmp = capturedScreenBitmap ?: Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val left = rect.left.toInt().coerceIn(0, sourceBmp.width - 1)
        val top = rect.top.toInt().coerceIn(0, sourceBmp.height - 1)
        val width = rect.width().toInt().coerceIn(1, sourceBmp.width - left)
        val height = rect.height().toInt().coerceIn(1, sourceBmp.height - top)

        val croppedBitmap = Bitmap.createBitmap(sourceBmp, left, top, width, height)

        val recognizer = try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } catch (_: Throwable) {
            null
        }

        if (recognizer == null) {
            Toast.makeText(this, "Unable to initialize OCR engine", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val inputImage = InputImage.fromBitmap(croppedBitmap, 0)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val text = visionText.text.trim()
                if (text.isNotBlank()) {
                    launchRiskResult(text)
                } else {
                    Toast.makeText(this, "NO USABLE TEXT DETECTED. Select a larger or clearer area.", Toast.LENGTH_LONG).show()
                    btnAnalyze.isEnabled = true
                    tvStatus.text = "NO TEXT DETECTED • TRY SELECTING AGAIN"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "NO USABLE TEXT DETECTED. Select a larger or clearer area.", Toast.LENGTH_LONG).show()
                btnAnalyze.isEnabled = true
            }
    }

    private fun launchRiskResult(extractedText: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, extractedText)
            putExtra("isShieldScan", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }

    private fun stopScreenCapture() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            mediaProjection?.stop()
            mediaProjection = null
            MediaProjectionService.stop(this)
        } catch (_: Throwable) {
        }
    }

    override fun onDestroy() {
        capturedScreenBitmap?.recycle()
        capturedScreenBitmap = null
        stopScreenCapture()
        super.onDestroy()
    }
}
