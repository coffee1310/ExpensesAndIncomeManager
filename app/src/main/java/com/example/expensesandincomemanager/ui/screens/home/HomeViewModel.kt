package com.example.expensesandincomemanager.ui.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // LiveData для наблюдения за обновлениями
    val transactionsUpdated: LiveData<Boolean> = repository.transactionsUpdated

    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    init {
        println("DEBUG ViewModel: Инициализация ViewModel")
        loadInitialData()

        // Подписываемся на обновления транзакций
        viewModelScope.launch {
            transactionsUpdated.asFlow().collect { updated ->
                if (updated) {
                    println("DEBUG ViewModel: Получено уведомление об обновлении транзакций!")
                    // Загружаем данные немедленно
                    loadCurrentMonthData(force = true)
                    repository.resetTransactionsUpdated()
                }
            }
        }
    }

    fun loadDataForMonth(year: Int, month: Int) {
        println("DEBUG ViewModel: Загрузка данных для месяца $month/$year")
        selectedYear = year
        selectedMonth = month
        loadCurrentMonthData(force = true)
    }

    // Функция для принудительного обновления всех данных
    fun refreshData() {
        println("DEBUG ViewModel: Принудительное обновление данных")
        loadMonthlyData()
        loadCurrentMonthData(force = true)
    }

    private fun loadInitialData() {
        println("DEBUG ViewModel: Загрузка начальных данных")
        loadMonthlyData()
        loadCurrentMonthData(force = false)
    }

    private fun loadMonthlyData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) + 1

                val months = mutableListOf<MonthData>()
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

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
                println("DEBUG ViewModel: Месячные данные загружены: ${months.size} месяцев")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCurrentMonthData(force: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("DEBUG ViewModel: Загрузка данных за $selectedMonth/$selectedYear")

                val homeData = repository.getHomeData(selectedYear, selectedMonth)

                println("DEBUG ViewModel: Данные получены: доходы=${homeData.incomeTotal}, расходы=${homeData.expenseTotal}, баланс=${homeData.balance}")

                // Обновляем ВСЕ значения одновременно
                _totalIncome.value = homeData.incomeTotal
                _totalExpense.value = homeData.expenseTotal
                _balance.value = homeData.balance

                // Загружаем категории
                loadExpenseCategories(homeData)

                println("DEBUG ViewModel: Данные успешно обновлены в UI")

            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG ViewModel: Ошибка при загрузке данных: ${e.message}")
                showSampleData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadExpenseCategories(homeData: data.repository.FinanceRepository.HomeData) {
        try {
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

            val categories = homeData.expenseByCategory.mapIndexed { index, summary ->
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

                val percentage = if (homeData.expenseTotal > 0) {
                    ((summary.total_amount / homeData.expenseTotal) * 100).toFloat()
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

            _expenseCategories.value = categories
            println("DEBUG ViewModel: Категории загружены: ${categories.size} шт.")
        } catch (e: Exception) {
            println("DEBUG ViewModel: Ошибка при загрузке категорий: ${e.message}")
            _expenseCategories.value = emptyList()
        }
    }

    private fun showSampleData() {
        println("DEBUG ViewModel: Показываем тестовые данные")
        _totalIncome.value = 0.0
        _totalExpense.value = 0.0
        _balance.value = 0.0

        _expenseCategories.value = emptyList()
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