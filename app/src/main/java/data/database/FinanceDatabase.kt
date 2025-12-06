package data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import data.converters.Converters
import data.entities.*

@Database(
    entities = [
        UserSettings::class,
        Category::class,
        Account::class,
        Transaction::class,
        Budget::class,
        SavingsGoal::class,
        Template::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinanceDatabase : RoomDatabase() {
//    abstract fun userSettingsDao(): UserSettingsDao
//    abstract fun categoryDao(): CategoryDao
//    abstract fun accountDao(): AccountDao
//    abstract fun transactionDao(): TransactionDao
//    abstract fun budgetDao(): BudgetDao
//    abstract fun savingsGoalDao(): SavingsGoalDao
//    abstract fun templateDao(): TemplateDao

    companion object {
        const val DATABASE_NAME = "finance.db"
    }
}