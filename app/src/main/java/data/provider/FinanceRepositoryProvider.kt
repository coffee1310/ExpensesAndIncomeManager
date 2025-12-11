package data.provider

import android.content.Context
import data.database.FinanceDatabase
import data.repository.FinanceRepository

object FinanceRepositoryProvider {
    @Volatile
    private var INSTANCE: FinanceRepository? = null

    fun getRepository(context: Context): FinanceRepository {
        return INSTANCE ?: synchronized(this) {
            val database = FinanceDatabase.getDatabase(context)
            val instance = FinanceRepository(
                categoryDao = database.categoryDao(),
                transactionDao = database.transactionDao(),
                accountDao = database.accountDao(),
                budgetDao = database.budgetDao()
            )
            INSTANCE = instance
            instance
        }
    }

    fun cleanup() {
        INSTANCE?.cleanup()
        INSTANCE = null
    }
}