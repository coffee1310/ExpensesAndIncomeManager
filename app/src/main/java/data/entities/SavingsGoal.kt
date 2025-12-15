package data.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "savings_goals",
    indices = [Index(value = ["is_completed"])]
)
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,

    @ColumnInfo(name = "current_amount")
    val currentAmount: Double = 0.0,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "target_date")
    val targetDate: Date? = null,

    @ColumnInfo(name = "color")
    val color: String = "#34C759",

    @ColumnInfo(name = "icon")
    val icon: String = "star",

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
) {
    fun getProgress(): Int {
        return if (targetAmount > 0) {
            ((currentAmount / targetAmount) * 100).toInt()
        } else {
            0
        }
    }

    fun getRemainingAmount(): Double {
        return targetAmount - currentAmount
    }
}