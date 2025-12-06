package com.example.expensesandincomemanager.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import data.models.ExpenseCategory

class DoughnutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    private var segments: List<ChartSegment> = emptyList()
    private var strokeWidth = 40f

    fun setData(categories: List<ExpenseCategory>) {
        segments = categories.map { category ->
            ChartSegment(
                percentage = category.percentage / 100f,
                color = category.color,
                name = category.name
            )
        }
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (segments.isEmpty()) return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) * 0.4f

        // Настройка области для рисования дуг
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var startAngle = -90f // Начинаем с 12 часов

        // Рисуем каждый сегмент
        segments.forEach { segment ->
            val sweepAngle = 360f * segment.percentage

            paint.color = segment.color
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.strokeCap = Paint.Cap.ROUND

            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)

            startAngle += sweepAngle
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 280 // dp

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = minOf(desiredSize, widthSize)
        } else {
            width = desiredSize
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = minOf(desiredSize, heightSize)
        } else {
            height = desiredSize
        }

        setMeasuredDimension(width, height)
    }

    data class ChartSegment(
        val percentage: Float,
        val color: Int,
        val name: String
    )
}