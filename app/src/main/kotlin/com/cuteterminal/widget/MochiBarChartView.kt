package com.cuteterminal.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class MochiBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#C084FC")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#7E69AB")
        textSize = 24f
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        color = Color.parseColor("#15A855F7")
    }

    private val barRect = RectF()

    // 18 data points representing last 24 hours
    private var values = floatArrayOf(
        85f, 80f, 75f, 70f, 62f, 55f, 50f, 45f, 40f, 58f, 75f, 92f, 88f, 82f, 76f, 70f, 85f, 82f
    )

    private val labels = listOf("00", "04", "08", "12", "16", "20")

    fun setValues(newValues: FloatArray) {
        values = newValues
        invalidate()
    }

    fun setBarColor(color: Int) {
        barPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val labelH = 34f
        val chartH = h - labelH
        val barCount = values.size
        val totalSpacing = w * 0.25f
        val barSpacing = totalSpacing / (barCount + 1)
        val barW = (w - totalSpacing) / barCount

        // Horizontal grid lines
        for (i in 1..3) {
            val y = (chartH / 4f) * i
            canvas.drawLine(0f, y, w, y, gridPaint)
        }

        // Draw Bars
        var curX = barSpacing
        for (v in values) {
            val ratio = (v / 100f).coerceIn(0.08f, 1f)
            val barHeight = chartH * ratio
            val top = chartH - barHeight

            barRect.set(curX, top, curX + barW, chartH)
            canvas.drawRoundRect(barRect, 8f, 8f, barPaint)

            curX += barW + barSpacing
        }

        // Draw Time Labels (00, 04, 08, 12, 16, 20)
        val stepLabel = w / (labels.size + 1)
        for (i in labels.indices) {
            val x = stepLabel * (i + 1)
            canvas.drawText(labels[i], x, h - 6f, textPaint)
        }
    }
}
