package data.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import data.dao.*
import data.database.FinanceDatabase
import data.entities.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao
) {

    // CoroutineScope для обновлений LiveData
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // MutableLiveData для уведомления об изменениях (private)
    private val _transactionsUpdated = MutableLiveData<Boolean>(false)

    // Public LiveData для наблюдения
    val transactionsUpdated: LiveData<Boolean> = _transactionsUpdated

    // Функция для уведомления об обновлениях
    fun notifyTransactionsUpdated() {
        coroutineScope.launch {
            _transactionsUpdated.value = true
        }
    }

    // Функция для сброса флага
    fun resetTransactionsUpdated() {
        coroutineScope.launch {
            _transactionsUpdated.value = false
        }
    }

    // Категории
    suspend fun insertCategory(category: Category) = categoryDao.insert(category)
    fun getCategoriesByType(type: String) = categoryDao.getCategoriesByType(type)

    // Транзакции
    suspend fun insertTransaction(transaction: Transaction): Long {
        val id = transactionDao.insert(transaction)
        notifyTransactionsUpdated() // Уведомляем об изменении
        return id
    }

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

        // Получаем Flow и преобразуем в списки
        val expenseSummaryFlow = getExpenseSummaryByCategory(startDate, endDate)
        val typeTotalsFlow = getIncomeExpenseTotal(startDate, endDate)

        val expenseList = expenseSummaryFlow.first()
        val totalList = typeTotalsFlow.first()

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

    // Счета
    suspend fun updateAccountBalance(id: Int, amount: Double) {
        accountDao.updateBalance(id, amount)
        notifyTransactionsUpdated() // Уведомляем об изменении
    }

    // Отмена всех корутин при уничтожении
    fun cleanup() {
        coroutineScope.coroutineContext.cancelChildren()
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

    // Очистка репозитория
    fun cleanup() {
        repository?.cleanup()
        repository = null
    }
}