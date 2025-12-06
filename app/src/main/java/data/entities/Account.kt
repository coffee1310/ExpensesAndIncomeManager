package data.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["is_active"])]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "balance")
    val balance: Double = 0.0,

    @ColumnInfo(name = "currency")
    val currency: String = "RUB",

    @ColumnInfo(name = "color")
    val color: String = "#007AFF",

    @ColumnInfo(name = "icon")
    val icon: String = "credit-card",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)