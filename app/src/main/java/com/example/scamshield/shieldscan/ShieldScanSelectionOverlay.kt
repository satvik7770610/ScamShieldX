package com.example.scamshield.shieldscan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ShieldScanSelectionOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dimPaint = Paint().apply {
        color = Color.parseColor("#CC050505")
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#00E5FF")
        textSize = 32f
        isAntiAlias = true
        isFakeBoldText = true
    }

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var isDragging = false

    val selectionRect = RectF()

    var onSelectionChanged: ((RectF?) -> Unit)? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                currentX = event.x
                currentY = event.y
                isDragging = true
                updateSelectionRect()
                invalidate()
                performClick()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    currentX = event.x
                    currentY = event.y
                    updateSelectionRect()
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    currentX = event.x
                    currentY = event.y
                    isDragging = false
                    updateSelectionRect()
                    invalidate()
                    onSelectionChanged?.invoke(if (selectionRect.width() > 20 && selectionRect.height() > 20) selectionRect else null)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateSelectionRect() {
        selectionRect.set(
            minOf(startX, currentX),
            minOf(startY, currentY),
            maxOf(startX, currentX),
            maxOf(startY, currentY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw dim background over entire screen
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)

        // 2. Clear selected area
        if (selectionRect.width() > 10 && selectionRect.height() > 10) {
            canvas.drawRect(selectionRect, clearPaint)
            canvas.drawRect(selectionRect, borderPaint)

            // Draw selection dimensions
            val dimText = "${selectionRect.width().toInt()} × ${selectionRect.height().toInt()} px"
            canvas.drawText(dimText, selectionRect.left + 12f, selectionRect.bottom - 12f, textPaint)
        } else {
            // Hint text
            canvas.drawText("DRAG TO SELECT SUSPICIOUS AREA", width / 2f - 220f, height / 2f, textPaint)
        }
    }

    fun resetSelection() {
        selectionRect.setEmpty()
        invalidate()
        onSelectionChanged?.invoke(null)
    }
}
