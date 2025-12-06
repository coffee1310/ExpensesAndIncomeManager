package data.dao


import androidx.room.*
import data.entities.Budget
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface BudgetDao {

    // CRUD операции
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Получение бюджетов
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Int): Budget?

    @Query("SELECT * FROM budgets WHERE category_id = :categoryId")
    fun getBudgetsByCategory(categoryId: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetsByMonth(month: Int, year: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category_id = :categoryId AND month = :month AND year = :year")
    suspend fun getBudgetForCategoryAndMonth(
        categoryId: Int,
        month: Int,
        year: Int
    ): Budget?

    // Получение бюджетов с информацией о прогрессе
    @Query("""
        SELECT 
            b.*,
            c.name as categoryName,
            c.color as categoryColor,
            COALESCE(SUM(t.amount), 0) as spentAmount
        FROM budgets b
        LEFT JOIN categories c ON b.category_id = c.id
        LEFT JOIN transactions t ON t.category_id = b.category_id 
            AND t.type = 'expense'
            AND strftime('%m', t.date/1000, 'unixepoch') = :monthStr
            AND strftime('%Y', t.date/1000, 'unixepoch') = :yearStr
        WHERE b.month = :month AND b.year = :year
        GROUP BY b.id
        ORDER BY b.amount DESC
    """)
    fun getBudgetsWithProgress(
        month: Int,
        year: Int,
        monthStr: String,
        yearStr: String
    ): Flow<List<BudgetWithProgress>>

    // Получение общего бюджета на месяц
    @Query("""
        SELECT SUM(amount) as totalBudget
        FROM budgets 
        WHERE month = :month AND year = :year
    """)
    suspend fun getTotalBudgetForMonth(month: Int, year: Int): Double

    // Получение бюджетов за несколько месяцев
    @Query("""
        SELECT * FROM budgets 
        WHERE year = :year 
        AND month BETWEEN :startMonth AND :endMonth
        ORDER BY year DESC, month DESC
    """)
    fun getBudgetsForYearRange(
        year: Int,
        startMonth: Int,
        endMonth: Int
    ): Flow<List<Budget>>

    // Копирование бюджета на следующий месяц
    @Query("""
    INSERT INTO budgets (category_id, amount, month, year, created_at)
    SELECT 
        category_id,
        amount,
        :newMonth as month,
        :newYear as year,
        :currentDate as created_at
    FROM budgets 
    WHERE month = :sourceMonth AND year = :sourceYear 
    ON CONFLICT(category_id, month, year) DO UPDATE SET
        amount = excluded.amount
""")
    suspend fun copyBudgetsToNextMonth(
        sourceMonth: Int,
        sourceYear: Int,
        newMonth: Int,
        newYear: Int,
        currentDate: Date
    )

    // Получение превышенных бюджетов
    @Query("""
        SELECT 
            b.*,
            c.name as categoryName,
            c.color as categoryColor,
            COALESCE(SUM(t.amount), 0) as spentAmount
        FROM budgets b
        LEFT JOIN categories c ON b.category_id = c.id
        LEFT JOIN transactions t ON t.category_id = b.category_id 
            AND t.type = 'expense'
            AND strftime('%m', t.date/1000, 'unixepoch') = :monthStr
            AND strftime('%Y', t.date/1000, 'unixepoch') = :yearStr
        WHERE b.month = :month AND b.year = :year
        GROUP BY b.id
        HAVING spentAmount > b.amount
        ORDER BY (spentAmount - b.amount) DESC
    """)
    fun getExceededBudgets(
        month: Int,
        year: Int,
        monthStr: String,
        yearStr: String
    ): Flow<List<BudgetWithProgress>>

    // Получение оставшегося бюджета по категории
    @Query("""
        SELECT 
            b.amount - COALESCE(SUM(t.amount), 0) as remainingAmount
        FROM budgets b
        LEFT JOIN transactions t ON t.category_id = b.category_id 
            AND t.type = 'expense'
            AND strftime('%m', t.date/1000, 'unixepoch') = :monthStr
            AND strftime('%Y', t.date/1000, 'unixepoch') = :yearStr
        WHERE b.category_id = :categoryId 
        AND b.month = :month AND b.year = :year
    """)
    suspend fun getRemainingBudget(
        categoryId: Int,
        month: Int,
        year: Int,
        monthStr: String,
        yearStr: String
    ): Double?

    // Получение статистики по бюджетам
    @Query("""
        SELECT 
            b.month,
            b.year,
            SUM(b.amount) as totalBudget,
            COALESCE(SUM(t.amount), 0) as totalSpent
        FROM budgets b
        LEFT JOIN categories c ON b.category_id = c.id
        LEFT JOIN transactions t ON t.category_id = b.category_id 
            AND t.type = 'expense'
            AND strftime('%m', t.date/1000, 'unixepoch') = printf('%02d', b.month)
            AND strftime('%Y', t.date/1000, 'unixepoch') = printf('%d', b.year)
        WHERE b.year = :year
        GROUP BY b.month, b.year
        ORDER BY b.year DESC, b.month DESC
    """)
    fun getBudgetStatistics(year: Int): Flow<List<BudgetStatistics>>

    // Обновление даты изменения
    @Query("UPDATE budgets SET updated_at = :date WHERE id = :id")
    suspend fun updateTimestamp(id: Int, date: Date)

    data class BudgetWithProgress(
        @Embedded
        val budget: Budget,
        val categoryName: String,
        val categoryColor: String,
        val spentAmount: Double
    ) {
        val progress: Float
            get() = (if (budget.amount > 0) {
                (spentAmount / budget.amount).coerceAtMost(1.0)
            } else 0f) as Float

        val remainingAmount: Double
            get() = (budget.amount - spentAmount).coerceAtLeast(0.0)

        val isExceeded: Boolean
            get() = spentAmount > budget.amount

        val overspentAmount: Double
            get() = if (spentAmount > budget.amount) spentAmount - budget.amount else 0.0
    }

    data class BudgetStatistics(
        val month: Int,
        val year: Int,
        val totalBudget: Double,
        val totalSpent: Double
    ) {
        val remainingBudget: Double
            get() = totalBudget - totalSpent

        val progressPercentage: Float
            get() = (if (totalBudget > 0) {
                (totalSpent / totalBudget).coerceAtMost(1.0)
            } else 0f) as Float

        val isOverBudget: Boolean
            get() = totalSpent > totalBudget
    }
}