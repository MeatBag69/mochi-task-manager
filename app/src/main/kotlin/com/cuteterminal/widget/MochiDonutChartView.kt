package com.cuteterminal.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class MochiDonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 28f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#15A855F7")
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 28f
        strokeCap = Paint.Cap.ROUND
    }

    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#2D1B4E")
        textSize = 58f
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#7E69AB")
        textSize = 28f
    }

    private val rectF = RectF()

    data class Segment(val color: Int, val percentage: Float)

    private var segments: List<Segment> = listOf(
        Segment(Color.parseColor("#A855F7"), 0.24f), // Apps
        Segment(Color.parseColor("#F472B6"), 0.08f), // Images
        Segment(Color.parseColor("#38BDF8"), 0.10f), // Videos
        Segment(Color.parseColor("#34D399"), 0.03f), // Audio
        Segment(Color.parseColor("#FB923C"), 0.02f), // Documents
        Segment(Color.parseColor("#C084FC"), 0.03f)  // Other
    )

    private var mainText = "50%"
    private var subText = "128 / 256 GB"

    fun setData(pctText: String, detailText: String, segmentList: List<Segment>) {
        mainText = pctText
        subText = detailText
        segments = segmentList
        invalidate()
    }

    fun setTextColor(primaryColor: Int, secondaryColor: Int) {
        centerTextPaint.color = primaryColor
        subTextPaint.color = secondaryColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val size = Math.min(w, h)
        val stroke = 28f
        val padding = stroke / 2f + 16f

        rectF.set(
            (w - size) / 2f + padding,
            (h - size) / 2f + padding,
            (w + size) / 2f - padding,
            (h + size) / 2f - padding
        )

        // Draw track ring
        canvas.drawArc(rectF, 0f, 360f, false, bgPaint)

        // Draw segments
        var startAngle = -90f
        for (seg in segments) {
            val sweep = seg.percentage * 360f
            if (sweep > 0.5f) {
                arcPaint.color = seg.color
                canvas.drawArc(rectF, startAngle, sweep, false, arcPaint)
                startAngle += sweep
            }
        }

        // Draw Center Text
        val cX = w / 2f
        val cY = h / 2f
        canvas.drawText(mainText, cX, cY - 4f, centerTextPaint)
        canvas.drawText(subText, cX, cY + 40f, subTextPaint)
    }
}
