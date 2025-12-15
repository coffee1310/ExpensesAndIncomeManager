package com.example.expensesandincomemanager.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.expensesandincomemanager.R
import data.models.ExpenseCategory

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var categories: List<ExpenseCategory> = emptyList()
    private var maxAmount: Double = 0.0

    // Paint объекты
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.on_surface)
        textSize = 12f * resources.displayMetrics.density
        textAlign = Paint.Align.LEFT
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.outline)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.outline).withAlpha(0x33)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val paddingStart = 60f
    private val paddingEnd = 20f
    private val paddingTop = 40f
    private val paddingBottom = 60f
    private val barSpacing = 8f

    fun setData(categories: List<ExpenseCategory>) {
        this.categories = categories
        this.maxAmount = categories.maxOfOrNull { it.amount } ?: 0.0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (categories.isEmpty()) {
            drawNoData(canvas)
            return
        }

        val width = width.toFloat()
        val height = height.toFloat()
        val chartWidth = width - paddingStart - paddingEnd
        val chartHeight = height - paddingTop - paddingBottom

        drawGrid(canvas, width, height, chartHeight)
        drawAxes(canvas, width, height)
        drawBars(canvas, chartWidth, chartHeight)
        drawLabels(canvas, chartWidth, chartHeight)
    }

    private fun drawNoData(canvas: Canvas) {
        val text = "Нет данных"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.on_surface_variant)
            textSize = 16f * resources.displayMetrics.density
            textAlign = Paint.Align.CENTER
        }

        val x = width / 2f
        val y = height / 2f
        canvas.drawText(text, x, y, paint)
    }

    private fun drawGrid(canvas: Canvas, width: Float, height: Float, chartHeight: Float) {
        // Горизонтальные линии сетки
        for (i in 0..4) {
            val y = paddingTop + (chartHeight / 4) * i
            canvas.drawLine(paddingStart, y, width - paddingEnd, y, gridPaint)
        }
    }

    private fun drawAxes(canvas: Canvas, width: Float, height: Float) {
        // Ось X
        canvas.drawLine(
            paddingStart,
            height - paddingBottom,
            width - paddingEnd,
            height - paddingBottom,
            axisPaint
        )

        // Ось Y
        canvas.drawLine(
            paddingStart,
            paddingTop,
            paddingStart,
            height - paddingBottom,
            axisPaint
        )
    }

    private fun drawBars(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        if (maxAmount == 0.0) return

        val barWidth = (chartWidth / categories.size) - barSpacing

        categories.forEachIndexed { index, category ->
            val barHeight = (category.amount / maxAmount * chartHeight).toFloat()
            val left = paddingStart + index * (barWidth + barSpacing)
            val top = paddingTop + chartHeight - barHeight
            val right = left + barWidth
            val bottom = paddingTop + chartHeight

            barPaint.color = category.color
            canvas.drawRect(RectF(left, top, right, bottom), barPaint)

            // Процентное значение на вершине столбца
            if (category.percentage > 0) {
                val percentText = "${category.percentage.toInt()}%"
                val textX = left + barWidth / 2
                val textY = top - 5

                val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = ContextCompat.getColor(context, R.color.on_surface)
                    textSize = 10f * resources.displayMetrics.density
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(percentText, textX, textY, percentPaint)
            }
        }
    }

    private fun drawLabels(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        if (maxAmount == 0.0) return

        val barWidth = (chartWidth / categories.size) - barSpacing

        // Подписи категорий под столбцами
        categories.forEachIndexed { index, category ->
            val left = paddingStart + index * (barWidth + barSpacing)
            val textX = left + barWidth / 2
            val textY = paddingTop + chartHeight + 15

            // Обрезаем длинные названия
            val displayName = if (category.name.length > 8) {
                category.name.substring(0, 8) + ".."
            } else {
                category.name
            }

            canvas.drawText(displayName, textX, textY, textPaint.apply { textAlign = Paint.Align.CENTER })
        }

        // Подписи значений на оси Y
        for (i in 0..4) {
            val value = maxAmount / 4 * (4 - i)
            val text = if (value >= 1000) "${(value / 1000).toInt()}к" else value.toInt().toString()
            val y = paddingTop + (chartHeight / 4) * i

            canvas.drawText(text, 5f, y - 5, textPaint.apply { textAlign = Paint.Align.LEFT })
        }
    }

    private fun Int.withAlpha(alpha: Int): Int {
        return Color.argb(
            alpha,
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    }
}