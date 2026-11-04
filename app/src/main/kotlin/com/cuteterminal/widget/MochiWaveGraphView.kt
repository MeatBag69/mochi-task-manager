package com.cuteterminal.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class MochiWaveGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        color = Color.parseColor("#15A855F7")
    }

    private val linePath = Path()
    private val fillPath = Path()

    private var lineColor = Color.parseColor("#A855F7")
    private var dataPoints = floatArrayOf(25f, 32f, 28f, 45f, 40f, 52f, 48f, 65f, 58f, 70f, 62f, 75f)
    private var maxValue = 100f

    // Multi-line support for System Load (Last 60s)
    private var multiData: List<Pair<Int, FloatArray>>? = null

    init {
        linePaint.color = lineColor
    }

    fun setWaveColor(color: Int) {
        lineColor = color
        linePaint.color = color
        invalidate()
    }

    fun setData(points: FloatArray, max: Float = 100f) {
        dataPoints = points
        maxValue = max
        multiData = null
        invalidate()
    }

    fun setMultiData(datasets: List<Pair<Int, FloatArray>>, max: Float = 100f) {
        multiData = datasets
        maxValue = max
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // Draw 3 subtle dashed/solid grid lines
        val stepY = h / 4f
        for (i in 1..3) {
            val y = stepY * i
            canvas.drawLine(0f, y, w, y, gridPaint)
        }

        val datasets = multiData ?: listOf(Pair(lineColor, dataPoints))

        for ((color, points) in datasets) {
            if (points.isEmpty()) continue
            linePaint.color = color
            drawSingleWave(canvas, points, color, w, h)
        }
    }

    private fun drawSingleWave(canvas: Canvas, points: FloatArray, color: Int, w: Float, h: Float) {
        if (points.size < 2) return

        val stepX = w / (points.size - 1)
        linePath.reset()
        fillPath.reset()

        val paddingBottom = 4f
        val effectiveH = h - paddingBottom

        fun getY(value: Float): Float {
            val ratio = (value / maxValue).coerceIn(0f, 1f)
            return (h - (ratio * effectiveH)).coerceAtLeast(paddingBottom)
        }

        var prevX = 0f
        var prevY = getY(points[0])

        linePath.moveTo(prevX, prevY)
        fillPath.moveTo(prevX, h)
        fillPath.lineTo(prevX, prevY)

        for (i in 1 until points.size) {
            val curX = i * stepX
            val curY = getY(points[i])
            val cX1 = (prevX + curX) / 2f
            val cY1 = prevY
            val cX2 = (prevX + curX) / 2f
            val cY2 = curY

            linePath.cubicTo(cX1, cY1, cX2, cY2, curX, curY)
            fillPath.cubicTo(cX1, cY1, cX2, cY2, curX, curY)

            prevX = curX
            prevY = curY
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        // Gradient Fill
        val alphaColor = Color.argb(45, Color.red(color), Color.green(color), Color.blue(color))
        val endColor = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))
        fillPaint.shader = LinearGradient(0f, 0f, 0f, h, alphaColor, endColor, Shader.TileMode.CLAMP)

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(linePath, linePaint)
    }
}
