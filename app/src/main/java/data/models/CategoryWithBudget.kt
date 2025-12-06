package data.models

import androidx.room.Embedded
import androidx.room.Relation

import data.entities.Budget
import data.entities.Category

data class CategoryWithBudget(
    @Embedded
    val category: Category,

    @Relation(parentColumn = "id", entityColumn = "category_id")
    val budget: Budget?
)