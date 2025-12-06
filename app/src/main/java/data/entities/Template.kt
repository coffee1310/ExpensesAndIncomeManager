package data.entities

import androidx.room.*
import java.util.*

@Entity(
    tableName = "templates",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Template(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "category_id")
    val categoryId: Int?,

    @ColumnInfo(name = "account_id")
    val accountId: Int?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)
