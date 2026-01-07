package com.example.taskmasterfinalproject.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.taskmasterfinalproject.R
import kotlin.math.min

class CompletionRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Int = 0
    private var maxProgress: Int = 100

    // Paints
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    // Dimensions & Colors (Defaults)
    private var ringThickness = 20f
    private var ringColor = Color.LTGRAY
    private var progressColor = Color.BLUE
    private var textColor = Color.BLACK
    private var textSize = 40f

    // Drawing Rect
    private val rectF = RectF()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CompletionRingView,
            0, 0
        ).apply {

            try {
                ringThickness = getDimension(R.styleable.CompletionRingView_ringThickness, 20f)
                ringColor = getColor(R.styleable.CompletionRingView_ringColor, Color.LTGRAY)
                progressColor = getColor(R.styleable.CompletionRingView_progressColor, Color.BLUE)
                textColor = getColor(R.styleable.CompletionRingView_textColor, Color.BLACK)
                textSize = getDimension(R.styleable.CompletionRingView_textSize, 40f)
            } finally {
                recycle()
            }
        }

        // Apply config
        ringPaint.strokeWidth = ringThickness
        ringPaint.color = ringColor

        progressPaint.strokeWidth = ringThickness
        progressPaint.color = progressColor

        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Enforce a minimum size if wrap_content used
        val desiredSize = (120 * resources.displayMetrics.density).toInt()
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredSize, widthSize)
            else -> desiredSize
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredSize, heightSize)
            else -> desiredSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        
        // Center coordinates
        val cx = w / 2
        val cy = h / 2
        
        // Radius is half of min dim minus padding/thickness
        val radius = (min(w, h) / 2) - ringThickness

        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw background ring (full circle)
        canvas.drawOval(rectF, ringPaint)

        // Draw progress arc
        // Start from top (-90 degrees)
        val sweepAngle = 360f * (progress / maxProgress.toFloat())
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)

        // Draw Percentage Text
        val text = "$progress%"
        // Vertically center text: descend + ascend is the height of text relative to baseline
        val textOffset = (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, cx, cy - textOffset, textPaint)
    }

    fun setProgress(value: Int) {
        this.progress = value.coerceIn(0, maxProgress)
        invalidate() // Redraw
    }
}
