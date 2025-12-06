package data.models.chart

import androidx.compose.ui.graphics.Color

data class ExpenseCategory(
    val id: Int,
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Color,
    val icon: String
)
