package data.initial

import android.content.Context
import android.util.Log
import androidx.room.Room
import data.database.FinanceDatabase
import data.entities.*
import data.entities.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

object InitialData {

    private const val TAG = "InitialData"

    fun insertInitialData(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = FinanceDatabase.getDatabase(context)

            Log.d(TAG, "Начало добавления начальных данных...")

            // Проверяем, есть ли уже данные
            val categoryCount = try {
                database.categoryDao().getCategoryCount()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при проверке категорий: ${e.message}")
                0
            }

            Log.d(TAG, "Количество категорий в базе: $categoryCount")

            if (categoryCount > 0) {
                Log.d(TAG, "Данные уже существуют, пропускаем инициализацию")
                return@launch
            }

            Log.d(TAG, "Добавление категорий расходов...")

            // Добавляем категории расходов
            val expenseCategories = listOf(
                Category(
                    name = "Продукты",
                    type = Category.TYPE_EXPENSE,
                    color = "#FF6B6B",
                    icon = "cart",
                    isDefault = true,
                    sortOrder = 1
                ),
                Category(
                    name = "Транспорт",
                    type = Category.TYPE_EXPENSE,
                    color = "#5856D6",
                    icon = "car",
                    isDefault = true,
                    sortOrder = 2
                ),
                Category(
                    name = "Развлечения",
                    type = Category.TYPE_EXPENSE,
                    color = "#FFD166",
                    icon = "film",
                    isDefault = true,
                    sortOrder = 3
                ),
                Category(
                    name = "Кафе",
                    type = Category.TYPE_EXPENSE,
                    color = "#06D6A0",
                    icon = "fork.knife",
                    isDefault = true,
                    sortOrder = 4
                ),
                Category(
                    name = "Коммуналка",
                    type = Category.TYPE_EXPENSE,
                    color = "#118AB2",
                    icon = "bolt",
                    isDefault = true,
                    sortOrder = 5
                ),
                Category(
                    name = "Здоровье",
                    type = Category.TYPE_EXPENSE,
                    color = "#FF2D55",
                    icon = "heart",
                    isDefault = true,
                    sortOrder = 6
                ),
                Category(
                    name = "Одежда",
                    type = Category.TYPE_EXPENSE,
                    color = "#AF52DE",
                    icon = "bag",
                    isDefault = true,
                    sortOrder = 7
                )
            )

            expenseCategories.forEach { category ->
                try {
                    val id = database.categoryDao().insert(category)
                    Log.d(TAG, "Добавлена категория расходов: ${category.name}, ID: $id")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при добавлении категории ${category.name}: ${e.message}")
                }
            }

            Log.d(TAG, "Добавление категорий доходов...")

            // Добавляем категории доходов
            val incomeCategories = listOf(
                Category(
                    name = "Зарплата",
                    type = Category.TYPE_INCOME,
                    color = "#32D74B",
                    icon = "dollarsign.circle",
                    isDefault = true,
                    sortOrder = 21
                ),
                Category(
                    name = "Фриланс",
                    type = Category.TYPE_INCOME,
                    color = "#0A84FF",
                    icon = "laptopcomputer",
                    isDefault = true,
                    sortOrder = 22
                ),
                Category(
                    name = "Инвестиции",
                    type = Category.TYPE_INCOME,
                    color = "#BF5AF2",
                    icon = "chart.line.uptrend.xyaxis",
                    isDefault = true,
                    sortOrder = 23
                )
            )

            incomeCategories.forEach { category ->
                try {
                    val id = database.categoryDao().insert(category)
                    Log.d(TAG, "Добавлена категория доходов: ${category.name}, ID: $id")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при добавлении категории ${category.name}: ${e.message}")
                }
            }

            Log.d(TAG, "Добавление счетов...")

            // Добавляем счета
            val accounts = listOf(
                Account(
                    name = "Наличные",
                    balance = 5000.0,
                    color = "#32D74B",
                    icon = "banknote",
                    sortOrder = 1
                ),
                Account(
                    name = "Основная карта",
                    balance = 25000.0,
                    color = "#0A84FF",
                    icon = "creditcard",
                    sortOrder = 2
                ),
                Account(
                    name = "Сберегательный счет",
                    balance = 100000.0,
                    color = "#FF453A",
                    icon = "building.columns",
                    sortOrder = 3
                )
            )

            accounts.forEach { account ->
                try {
                    val id = database.accountDao().insert(account)
                    Log.d(TAG, "Добавлен счет: ${account.name}, ID: $id")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при добавлении счета ${account.name}: ${e.message}")
                }
            }

            // Проверяем результат
            val finalCategoryCount = database.categoryDao().getCategoryCount()
            Log.d(TAG, "Итоговое количество категорий: $finalCategoryCount")

            // Получаем и логируем все категории
            val allCategories = database.categoryDao().getAllCategories()
            Log.d(TAG, "Список всех категорий:")
            allCategories.forEach { category ->
                Log.d(TAG, "- ${category.id}: ${category.name} (${category.type})")
            }

            Log.d(TAG, "Начальные данные успешно добавлены!")
        }
    }
}