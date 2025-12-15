package com.example.expensesandincomemanager.ui.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: FinanceRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val _monthlyData = MutableStateFlow<List<MonthData>>(emptyList())
    val monthlyData: StateFlow<List<MonthData>> = _monthlyData

    private val _expenseCategories = MutableStateFlow<List<ExpenseCategoryUI>>(emptyList())
    val expenseCategories: StateFlow<List<ExpenseCategoryUI>> = _expenseCategories

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // LiveData для наблюдения за обновлениями
    val transactionsUpdated: LiveData<Boolean> = repository.transactionsUpdated

    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    init {
        loadInitialData()
        loadCurrentMonthData()

        // Подписываемся на обновления транзакций
        viewModelScope.launch {
            transactionsUpdated.asFlow().collect { updated ->
                if (updated) {
                    // При изменении транзакций перезагружаем данные
                    loadCurrentMonthData()
                    // Сбрасываем флаг через репозиторий
                    repository.resetTransactionsUpdated()
                }
            }
        }
    }

    fun loadDataForMonth(year: Int, month: Int) {
        selectedYear = year
        selectedMonth = month
        loadCurrentMonthData()
    }

    // Функция для принудительного обновления данных
    fun refreshData() {
        loadCurrentMonthData()
    }

    private fun loadInitialData() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        val months = mutableListOf<MonthData>()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Генерируем данные за последние 6 месяцев
        for (i in 5 downTo 0) {
            calendar.add(Calendar.MONTH, -1)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            months.add(
                MonthData(
                    monthName = monthFormat.format(calendar.time).replaceFirstChar { it.uppercase() },
                    monthNumber = month,
                    year = year,
                    isSelected = month == currentMonth && year == currentYear
                )
            )
        }

        months.reverse()
        _monthlyData.value = months
    }

    private fun loadCurrentMonthData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val homeData = repository.getHomeData(selectedYear, selectedMonth)

                _totalIncome.value = homeData.incomeTotal
                _totalExpense.value = homeData.expenseTotal
                _balance.value = homeData.balance

                // Преобразуем данные для UI СИНХРОННО
                val categories = convertToUICategories(homeData.expenseByCategory, homeData.expenseTotal)
                _expenseCategories.value = categories

            } catch (e: Exception) {
                e.printStackTrace()
                showSampleData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun convertToUICategories(
        expenseSummary: List<data.dao.TransactionDao.CategoryExpenseSummary>,
        totalExpense: Double
    ): List<ExpenseCategoryUI> {
        if (expenseSummary.isEmpty() || totalExpense == 0.0) {
            return emptyList()
        }

        return try {
            // Загружаем реальные категории из базы СИНХРОННО
            val database = data.database.FinanceDatabase.getDatabase(context)
            val allCategories = database.categoryDao().getAllCategories()

            val colors = listOf(
                android.graphics.Color.parseColor("#FF6B6B"),
                android.graphics.Color.parseColor("#5856D6"),
                android.graphics.Color.parseColor("#FFD166"),
                android.graphics.Color.parseColor("#06D6A0"),
                android.graphics.Color.parseColor("#118AB2"),
                android.graphics.Color.parseColor("#9B5DE5")
            )

            expenseSummary.mapIndexed { index, summary ->
                val category = allCategories.find { it.id == summary.category_id }
                val categoryName = category?.name ?: "Категория ${index + 1}"
                val categoryColor = if (category != null && category.color.isNotBlank()) {
                    try {
                        android.graphics.Color.parseColor(category.color)
                    } catch (e: Exception) {
                        colors.getOrElse(index) { android.graphics.Color.GRAY }
                    }
                } else {
                    colors.getOrElse(index) { android.graphics.Color.GRAY }
                }

                val percentage = if (totalExpense > 0) {
                    ((summary.total_amount / totalExpense) * 100).toFloat()
                } else {
                    0f
                }

                ExpenseCategoryUI(
                    id = summary.category_id ?: 0,
                    name = categoryName,
                    amount = summary.total_amount,
                    percentage = percentage,
                    color = categoryColor
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    private fun showSampleData() {
        _totalIncome.value = 80000.0
        _totalExpense.value = 50000.0
        _balance.value = 30000.0

        _expenseCategories.value = listOf(
            ExpenseCategoryUI(
                id = 1,
                name = "Продукты",
                amount = 15000.0,
                percentage = 30f,
                color = android.graphics.Color.parseColor("#FF6B6B")
            ),
            ExpenseCategoryUI(
                id = 2,
                name = "Транспорт",
                amount = 8000.0,
                percentage = 16f,
                color = android.graphics.Color.parseColor("#5856D6")
            ),
            ExpenseCategoryUI(
                id = 3,
                name = "Развлечения",
                amount = 7000.0,
                percentage = 14f,
                color = android.graphics.Color.parseColor("#FFD166")
            ),
            ExpenseCategoryUI(
                id = 4,
                name = "Кафе",
                amount = 6000.0,
                percentage = 12f,
                color = android.graphics.Color.parseColor("#06D6A0")
            ),
            ExpenseCategoryUI(
                id = 5,
                name = "Коммуналка",
                amount = 5000.0,
                percentage = 10f,
                color = android.graphics.Color.parseColor("#118AB2")
            ),
            ExpenseCategoryUI(
                id = 6,
                name = "Прочее",
                amount = 9000.0,
                percentage = 18f,
                color = android.graphics.Color.parseColor("#9B5DE5")
            )
        )
    }

    data class MonthData(
        val monthName: String,
        val monthNumber: Int,
        val year: Int,
        val isSelected: Boolean = false
    )

    data class ExpenseCategoryUI(
        val id: Int,
        val name: String,
        val amount: Double,
        val percentage: Float,
        val color: Int
    )
}