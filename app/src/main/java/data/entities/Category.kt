package data.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "categories",
    indices = [Index(value = ["type", "is_active"])]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "color")
    val color: String = "#007AFF",

    @ColumnInfo(name = "icon")
    val icon: String = "receipt",

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
) {
    companion object {
        const val TYPE_INCOME = "income"
        const val TYPE_EXPENSE = "expense"
    }
}