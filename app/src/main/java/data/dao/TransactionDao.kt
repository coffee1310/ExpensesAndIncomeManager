package data.dao

import androidx.room.*
import data.entities.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): Transaction?

    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, time DESC
    """)
    fun getTransactionsByDateRange(
        startDate: Date,
        endDate: Date
    ): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE type = :type 
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getTransactionsByTypeAndDateRange(
        type: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<Transaction>>

    @Query("""
        SELECT 
            category_id,
            SUM(amount) as total_amount,
            COUNT(*) as count
        FROM transactions 
        WHERE type = 'expense' 
        AND date BETWEEN :startDate AND :endDate
        GROUP BY category_id
        ORDER BY total_amount DESC
    """)
    fun getExpenseSummaryByCategory(
        startDate: Date,
        endDate: Date
    ): Flow<List<CategoryExpenseSummary>>

    @Query("""
        SELECT 
            type,
            SUM(amount) as total_amount
        FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY type
    """)
    fun getIncomeExpenseTotal(
        startDate: Date,
        endDate: Date
    ): Flow<List<TypeTotal>>

    data class CategoryExpenseSummary(
        val category_id: Int?,
        val total_amount: Double,
        val count: Int
    )

    data class TypeTotal(
        val type: String,
        val total_amount: Double
    )
}