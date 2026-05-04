package com.example.taskmasterfinalproject.views

// ספרייה לגישה למשאבי המערכת
import android.content.Context
// ספרייה לציור על גבי המסך
import android.graphics.Canvas
// ספרייה לניהול צבעים
import android.graphics.Color
// ספרייה להגדרת כלי ציור (מכחול)
import android.graphics.Paint
// ספרייה להגדרת מלבן בפורמט Float
import android.graphics.RectF
// ספרייה לניהול תכונות (Attributes) מ-XML
import android.util.AttributeSet
// מחלקת הבסיס לכל רכיב תצוגה
import android.view.View
// משאבי האפליקציה
import com.example.taskmasterfinalproject.R
// ספרייה לפונקציות מתמטיות (min)
import kotlin.math.min

// רכיב תצוגה מותאם אישית המציג טבעת התקדמות מעגלית
class CompletionRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Int = 0
    private var maxProgress: Int = 100

    // הגדרת המכחולים (Paints) לציור
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

    // מידות וצבעים (ברירות מחדל)
    private var ringThickness = 20f
    private var ringColor = Color.LTGRAY
    private var progressColor = Color.BLUE
    private var textColor = Color.BLACK
    private var textSize = 40f

    // מלבן עזר לציור הקשת
    private val rectF = RectF()

    init {
        // טעינת תכונות מותאמות אישית מ-XML
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

        // החלת הגדרות על המכחולים
        ringPaint.strokeWidth = ringThickness
        ringPaint.color = ringColor

        progressPaint.strokeWidth = ringThickness
        progressPaint.color = progressColor

        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    // פונקציה למדידת גודל הרכיב
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // אכיפת גודל מינימלי במידה ומוגדר wrap_content
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

    // פונקציה לציור הרכיב על המסך
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        
        // נקודת המרכז
        val cx = w / 2
        val cy = h / 2
        
        // רדיוס הוא חצי מהמימד הקטן ביותר פחות העובי
        val radius = (min(w, h) / 2) - ringThickness

        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // ציור טבעת הרקע (עיגול מלא)
        canvas.drawOval(rectF, ringPaint)

        // ציור קשת ההתקדמות
        // מתחיל מלמעלה (-90 מעלות)
        val sweepAngle = 360f * (progress / maxProgress.toFloat())
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)

        // ציור טקסט האחוזים
        val text = "$progress%"
        // מירכוז אנכי של הטקסט
        val textOffset = (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, cx, cy - textOffset, textPaint)
    }

    // פונקציה לעדכון ערך ההתקדמות
    fun setProgress(value: Int) {
        this.progress = value.coerceIn(0, maxProgress)
        invalidate() // בקשה לציור מחדש
    }
}
