package data.provider

import android.content.Context
import data.database.FinanceDatabase
import data.repository.FinanceRepository

object FinanceRepositoryProvider {
    private var repository: FinanceRepository? = null

    fun getRepository(context: Context): FinanceRepository {
        return repository ?: synchronized(this) {
            repository ?: createRepository(context).also { repository = it }
        }
    }

    private fun createRepository(context: Context): FinanceRepository {
        val database = FinanceDatabase.getDatabase(context)
        return FinanceRepository(
            categoryDao = database.categoryDao(),
            transactionDao = database.transactionDao(),
            accountDao = database.accountDao(),
            budgetDao = database.budgetDao(),
            savingsGoalDao = database.savingsGoalDao()
        )
    }
}