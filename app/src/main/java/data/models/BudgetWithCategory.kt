package data.models

import androidx.room.Embedded
import androidx.room.Relation

import data.entities.Budget
import data.entities.Category

data class BudgetWithCategory(
    @Embedded
    val budget: Budget,

    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: Category
)