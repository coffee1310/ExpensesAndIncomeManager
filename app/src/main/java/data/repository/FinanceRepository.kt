package data.repository

import data.dao.*
import data.database.FinanceDatabase
import data.entities.*
import kotlinx.coroutines.flow.first
import java.util.*

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao
) {

    // Категории
    suspend fun insertCategory(category: Category) = categoryDao.insert(category)
    fun getCategoriesByType(type: String) = categoryDao.getCategoriesByType(type)

    // Транзакции
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insert(transaction)
    fun getTransactionsByDateRange(startDate: Date, endDate: Date) =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    fun getExpenseSummaryByCategory(startDate: Date, endDate: Date) =
        transactionDao.getExpenseSummaryByCategory(startDate, endDate)

    fun getIncomeExpenseTotal(startDate: Date, endDate: Date) =
        transactionDao.getIncomeExpenseTotal(startDate, endDate)

    // Получение данных для главного экрана
    suspend fun getHomeData(year: Int, month: Int): HomeData {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
        }
        val startDate = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val endDate = calendar.time

        val expenseSummary = transactionDao.getExpenseSummaryByCategory(startDate, endDate)
        val typeTotals = transactionDao.getIncomeExpenseTotal(startDate, endDate)

        // Преобразуем Flow в списки
        val expenseList = expenseSummary.first()
        val totalList = typeTotals.first()

        val incomeTotal = totalList.find { it.type == Transaction.TYPE_INCOME }?.total_amount ?: 0.0
        val expenseTotal = totalList.find { it.type == Transaction.TYPE_EXPENSE }?.total_amount ?: 0.0
        val balance = incomeTotal - expenseTotal

        return HomeData(
            incomeTotal = incomeTotal,
            expenseTotal = expenseTotal,
            balance = balance,
            expenseByCategory = expenseList,
            startDate = startDate,
            endDate = endDate
        )
    }

    data class HomeData(
        val incomeTotal: Double,
        val expenseTotal: Double,
        val balance: Double,
        val expenseByCategory: List<TransactionDao.CategoryExpenseSummary>,
        val startDate: Date,
        val endDate: Date
    )
}

// Создание репозитория
object FinanceRepositoryProvider {
    private var repository: FinanceRepository? = null

    fun getRepository(context: android.content.Context): FinanceRepository {
        return repository ?: synchronized(this) {
            val database = FinanceDatabase.getDatabase(context)
            FinanceRepository(
                categoryDao = database.categoryDao(),
                transactionDao = database.transactionDao(),
                accountDao = database.accountDao(),
                budgetDao = database.budgetDao()
            ).also { repository = it }
        }
    }
}