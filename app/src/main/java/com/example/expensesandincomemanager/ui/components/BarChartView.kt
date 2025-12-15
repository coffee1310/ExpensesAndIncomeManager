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
        color = ContextCompat.getColor(context, R.color.on_surface_variant)
        textSize = 11f * resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.outline)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val paddingStart = 20f
    private val paddingEnd = 20f
    private val paddingTop = 20f
    private val paddingBottom = 40f
    private val barSpacing = 12f

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

    private fun drawAxes(canvas: Canvas, width: Float, height: Float) {
        // Ось X (базовая линия)
        canvas.drawLine(
            paddingStart,
            height - paddingBottom,
            width - paddingEnd,
            height - paddingBottom,
            axisPaint
        )
    }

    private fun drawBars(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        if (maxAmount == 0.0) return

        val barWidth = (chartWidth / categories.size) - barSpacing

        categories.forEachIndexed { index, category ->
            val barHeight = (category.amount / maxAmount * chartHeight).toFloat()
            val left = paddingStart + index * (barWidth + barSpacing) + barSpacing / 2
            val top = paddingTop + chartHeight - barHeight
            val right = left + barWidth
            val bottom = paddingTop + chartHeight

            // Градиент или тень для эффекта глубины
            barPaint.color = category.color.withAlpha(0xFF)
            canvas.drawRect(RectF(left, top, right, bottom), barPaint)

            // Более светлый верх для 3D эффекта
            val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = category.color.withAlpha(0x30)
                style = Paint.Style.FILL
            }
            canvas.drawRect(RectF(left, top, right, top + 8), highlightPaint)
        }
    }

    private fun drawLabels(canvas: Canvas, chartWidth: Float, chartHeight: Float) {
        if (maxAmount == 0.0) return

        val barWidth = (chartWidth / categories.size) - barSpacing

        // Подписи категорий под столбцами
        categories.forEachIndexed { index, category ->
            val left = paddingStart + index * (barWidth + barSpacing) + barSpacing / 2
            val textX = left + barWidth / 2
            val textY = paddingTop + chartHeight + 20

            // Обрезаем длинные названия
            val displayName = if (category.name.length > 6) {
                category.name.substring(0, 6) + ".."
            } else {
                category.name
            }

            canvas.drawText(displayName, textX, textY, textPaint)
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