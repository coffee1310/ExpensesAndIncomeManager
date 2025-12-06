package data.models

import androidx.room.Embedded
import androidx.room.Relation
import data.entities.Transaction
import data.entities.Category
import data.entities.Account

data class TransactionWithDetails(
    @Embedded
    val transaction: Transaction,

    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: Category?,

    @Relation(parentColumn = "account_id", entityColumn = "id")
    val account: Account
)