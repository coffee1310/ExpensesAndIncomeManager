    package data.entities

    import androidx.room.*
    import java.util.*

    @Entity(
        tableName = "transactions",
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
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index(value = ["date", "type"]),
            Index(value = ["category_id"]),
            Index(value = ["account_id"]),
            Index(value = ["is_recurring"])
        ]
    )
    data class Transaction(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Int = 0,

        @ColumnInfo(name = "amount")
        val amount: Double,

        @ColumnInfo(name = "type")
        val type: String,

        @ColumnInfo(name = "category_id")
        val categoryId: Int?,

        @ColumnInfo(name = "account_id")
        val accountId: Int,

        @ColumnInfo(name = "description")
        val description: String?,

        @ColumnInfo(name = "date")
        val date: Date,

        @ColumnInfo(name = "time")
        val time: Date = Date(),

        @ColumnInfo(name = "is_recurring")
        val isRecurring: Boolean = false,

        @ColumnInfo(name = "recurring_type")
        val recurringType: String?,

        @ColumnInfo(name = "created_at")
        val createdAt: Date = Date()
    ) {
        companion object {
            const val TYPE_INCOME = "income"
            const val TYPE_EXPENSE = "expense"
        }
    }