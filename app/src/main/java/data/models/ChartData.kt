package data.models

import android.graphics.Color

data class ExpenseCategory(
    val id: Int,
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Int,
    val icon: Int = 0
)