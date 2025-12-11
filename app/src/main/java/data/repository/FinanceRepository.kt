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
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // MutableLiveData для уведомления об изменениях (private)
    private val _transactionsUpdated = MutableLiveData<Boolean>(false)

    // Public LiveData для наблюдения
    val transactionsUpdated: LiveData<Boolean> = _transactionsUpdated

    // Функция для уведомления об обновлениях
    fun notifyTransactionsUpdated() {
        // Используем postValue для обновления из любого потока
        _transactionsUpdated.postValue(true)
    }

    // Функция для сброса флага
    fun resetTransactionsUpdated() {
        _transactionsUpdated.value = false
    }

    // Категории
    suspend fun insertCategory(category: Category) = categoryDao.insert(category)
    fun getCategoriesByType(type: String) = categoryDao.getCategoriesByType(type)

    // Транзакции
    suspend fun insertTransaction(transaction: Transaction): Long {
        val id = transactionDao.insert(transaction)

        // ВАЖНО: Обновляем баланс счета
        updateAccountBalanceForTransaction(transaction)

        notifyTransactionsUpdated() // Уведомляем об изменении
        return id
    }

    private suspend fun updateAccountBalanceForTransaction(transaction: Transaction) {
        when (transaction.type) {
            Transaction.TYPE_INCOME -> {
                accountDao.updateBalance(transaction.accountId, transaction.amount)
            }
            Transaction.TYPE_EXPENSE -> {
                accountDao.updateBalance(transaction.accountId, -transaction.amount)
            }
            // Для трансферов нужна отдельная логика
        }
    }

    fun getTransactionsByDateRange(startDate: Date, endDate: Date) =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    fun getExpenseSummaryByCategory(startDate: Date, endDate: Date) =
        transactionDao.getExpenseSummaryByCategory(startDate, endDate)

    fun getIncomeExpenseTotal(startDate: Date, endDate: Date) =
        transactionDao.getIncomeExpenseTotal(startDate, endDate)

    // Получение данных для главного экрана
    suspend fun getHomeData(year: Int, month: Int): HomeData {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startDate = calendar.time

            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endDate = calendar.time

            // Получаем данные синхронно
            val expenseList = getExpenseSummaryByCategory(startDate, endDate).first()
            val totalList = getIncomeExpenseTotal(startDate, endDate).first()

            val incomeTotal = totalList.find { it.type == Transaction.TYPE_INCOME }?.total_amount ?: 0.0
            val expenseTotal = totalList.find { it.type == Transaction.TYPE_EXPENSE }?.total_amount ?: 0.0
            val balance = incomeTotal - expenseTotal

            HomeData(
                incomeTotal = incomeTotal,
                expenseTotal = expenseTotal,
                balance = balance,
                expenseByCategory = expenseList,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    // Счета
    suspend fun updateAccountBalance(id: Int, amount: Double) {
        accountDao.updateBalance(id, amount)
        notifyTransactionsUpdated() // Уведомляем об изменении
    }

    // Добавляем метод для принудительного обновления
    fun forceRefresh() {
        notifyTransactionsUpdated()
    }

    // Отмена всех корутин при уничтожении
    fun cleanup() {
        coroutineScope.cancel()
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