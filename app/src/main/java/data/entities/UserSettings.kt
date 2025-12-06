package data.entities


import androidx.room.*
import java.util.*

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "currency")
    val currency: String = "RUB",

    @ColumnInfo(name = "language")
    val language: String = "ru",

    @ColumnInfo(name = "theme")
    val theme: String = "light",

    @ColumnInfo(name = "monthly_income")
    val monthlyIncome: Double = 0.0,

    @ColumnInfo(name = "savings_target")
    val savingsTarget: Double = 0.0,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)