package data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import data.converters.Converters
import data.dao.AccountDao
import data.dao.BudgetDao
import data.dao.CategoryDao
import data.dao.SavingsGoalDao
import data.dao.TransactionDao
import data.entities.*

@Database(
    entities = [
        Category::class,
        Transaction::class,
        Account::class,
        Budget::class,
        SavingsGoal::class  // Добавлено
    ],
    version = 2,  // Увеличьте версию
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao  // Добавлено

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .fallbackToDestructiveMigration() // для тестирования
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}