package data.models.chart

data class MonthlyData(
    val month: String,
    val year: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val isSelected: Boolean = false
)