package data.models

import androidx.room.Embedded
import data.entities.Account

data class AccountWithBalance(
    @Embedded
    val account: Account,

    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)