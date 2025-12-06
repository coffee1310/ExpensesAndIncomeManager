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
    val createdAt: Date = Date()
)