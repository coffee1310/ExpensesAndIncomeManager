package com.example.expensesandincomemanager.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _monthlyData = MutableStateFlow<List<MonthData>>(emptyList())
    val monthlyData: StateFlow<List<MonthData>> = _monthlyData

    private val _expenseCategories = MutableStateFlow<List<ExpenseCategoryData>>(emptyList())
    val expenseCategories: StateFlow<List<ExpenseCategoryData>> = _expenseCategories

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    init {
        loadInitialData()
    }

    fun loadDataForMonth(monthIndex: Int) {
        viewModelScope.launch {
            // TODO: Загрузка данных из базы данных
            // Примерные данные
            val categories = listOf(
                ExpenseCategoryData(
                    id = 1,
                    name = "Продукты",
                    amount = 15000.0,
                    percentage = 30f,
                    color = android.graphics.Color.parseColor("#FF6B6B")
                ),
                ExpenseCategoryData(
                    id = 2,
                    name = "Транспорт",
                    amount = 8000.0,
                    percentage = 16f,
                    color = android.graphics.Color.parseColor("#5856D6")
                ),
                ExpenseCategoryData(
                    id = 3,
                    name = "Развлечения",
                    amount = 7000.0,
                    percentage = 14f,
                    color = android.graphics.Color.parseColor("#FFD166")
                ),
                ExpenseCategoryData(
                    id = 4,
                    name = "Кафе",
                    amount = 6000.0,
                    percentage = 12f,
                    color = android.graphics.Color.parseColor("#06D6A0")
                ),
                ExpenseCategoryData(
                    id = 5,
                    name = "Коммуналка",
                    amount = 5000.0,
                    percentage = 10f,
                    color = android.graphics.Color.parseColor("#118AB2")
                ),
                ExpenseCategoryData(
                    id = 6,
                    name = "Прочее",
                    amount = 9000.0,
                    percentage = 18f,
                    color = android.graphics.Color.parseColor("#9B5DE5")
                )
            )

            _expenseCategories.value = categories
            _totalExpense.value = categories.sumOf { it.amount }
            _totalIncome.value = 80000.0 // Пример
        }
    }

    private fun loadInitialData() {
        val months = listOf(
            MonthData("Янв", 2024, true),
            MonthData("Фев", 2024),
            MonthData("Мар", 2024),
            MonthData("Апр", 2024),
            MonthData("Май", 2024),
            MonthData("Июн", 2024),
            MonthData("Июл", 2024)
        )

        _monthlyData.value = months
        loadDataForMonth(0)
    }

    data class MonthData(
        val month: String,
        val year: Int,
        val isSelected: Boolean = false
    )

    data class ExpenseCategoryData(
        val id: Int,
        val name: String,
        val amount: Double,
        val percentage: Float,
        val color: Int
    )
}