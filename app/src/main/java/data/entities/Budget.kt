package data.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category_id", "month", "year"], unique = true)
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Int,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "month")
    val month: Int, // 1-12

    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    fun isForCurrentMonth(): Boolean {
        val calendar = Calendar.getInstance()
        return month == calendar.get(Calendar.MONTH) + 1 &&
                year == calendar.get(Calendar.YEAR)
    }

    fun getMonthYearString(): String {
        val monthNames = listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        return "${monthNames.getOrElse(month - 1) { "Месяц $month" }} $year"
    }
}