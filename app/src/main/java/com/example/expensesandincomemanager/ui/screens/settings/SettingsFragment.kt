package com.example.expensesandincomemanager.ui.screens.settings

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensesandincomemanager.R
import com.google.android.material.card.MaterialCardView
import data.entities.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import data.repository.FinanceRepository
import data.provider.FinanceRepositoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private var selectedColor: Int = Color.parseColor("#FF6B6B")
    private lateinit var repository: FinanceRepository

    // Map для хранения исходных цветов фона
    private val originalBackgrounds = mutableMapOf<View, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FinanceRepositoryProvider.getRepository(requireContext())
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<MaterialCardView>(R.id.card_add_category).setOnClickListener {
            showAddCategoryDialog()
        }

        view.findViewById<MaterialCardView>(R.id.card_export_data).setOnClickListener {
            showExportDataDialog()
        }

        view.findViewById<MaterialCardView>(R.id.card_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_add_category_dialog, null)

        // Находим views для выбора цвета
        val colorViews = listOf(
            dialogView.findViewById<View>(R.id.color_food),
            dialogView.findViewById<View>(R.id.color_transport),
            dialogView.findViewById<View>(R.id.color_entertainment),
            dialogView.findViewById<View>(R.id.color_cafe),
            dialogView.findViewById<View>(R.id.color_utilities),
            dialogView.findViewById<View>(R.id.color_other)
        )

        // Сохраняем исходные цвета
        colorViews.forEach { view ->
            originalBackgrounds[view] = (view.background as? GradientDrawable)?.color?.defaultColor
                ?: Color.parseColor(view.tag as String)
        }

        var selectedColorView: View? = colorViews[0]

        // Вспомогательная функция для обновления выделения
        fun updateColorSelection(newSelectedView: View?) {
            // Снимаем выделение со всех цветов
            colorViews.forEach { view ->
                // Восстанавливаем исходный цвет фона
                val originalColor = originalBackgrounds[view] ?: Color.parseColor(view.tag as String)
                val shape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(originalColor)
                }
                view.background = shape
            }

            // Выделяем новый выбранный цвет
            newSelectedView?.let { selectedView ->
                val originalColor = originalBackgrounds[selectedView] ?: Color.parseColor(selectedView.tag as String)

                // Создаем LayerDrawable: цветной круг + рамка
                val colorCircle = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(originalColor)
                }

                val borderCircle = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setStroke(4, requireContext().getColor(R.color.primary))
                    setColor(Color.TRANSPARENT)
                }

                val layers = arrayOf(colorCircle, borderCircle)
                val layerDrawable = LayerDrawable(layers)
                layerDrawable.setLayerInset(1, 4, 4, 4, 4) // Отступ для рамки

                selectedView.background = layerDrawable
            }

            selectedColorView = newSelectedView
        }

        // Устанавливаем обработчики кликов для выбора цвета
        colorViews.forEach { colorView ->
            colorView.setOnClickListener {
                updateColorSelection(colorView)

                // Получаем цвет из тега
                val colorTag = colorView.tag as? String
                colorTag?.let {
                    selectedColor = Color.parseColor(it)
                }
            }
        }

        // Выделяем цвет по умолчанию
        updateColorSelection(colorViews[0])

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить категорию")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val categoryName = dialogView.findViewById<EditText>(R.id.et_category_name)
                    .text.toString().trim()

                if (categoryName.isNotEmpty()) {
                    addNewCategory(categoryName, selectedColor)
                } else {
                    Toast.makeText(requireContext(), "Введите название категории", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addNewCategory(name: String, color: Int) {
        lifecycleScope.launch {
            try {
                // Создаем новую категорию
                val newCategory = Category(
                    name = name,
                    color = String.format("#%06X", 0xFFFFFF and color),
                    type = "expense"
                )

                // Сохраняем в базу данных
                repository.insertCategory(newCategory)

                Toast.makeText(
                    requireContext(),
                    "Категория '$name' добавлена",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка при добавлении категории: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showExportDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Экспорт данных")
            .setMessage("Экспортировать все данные в формате JSON?")
            .setPositiveButton("Экспорт") { _, _ ->
                exportToJSON()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun exportToJSON() {
        lifecycleScope.launch {
            try {
                val jsonData = prepareJSONData()
                saveJSONToFile(jsonData)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Данные успешно экспортированы в JSON",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка экспорта: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun prepareJSONData(): JSONObject {
        return withContext(Dispatchers.IO) {
            val jsonObject = JSONObject()

            // Метаданные экспорта
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            jsonObject.put("export_date", dateFormat.format(Date()))
            jsonObject.put("app_version", "1.0")
            jsonObject.put("export_format", "JSON")

            // Получаем данные из базы
            val categories = try {
                repository.categoryDao.getAllCategories()
            } catch (e: Exception) {
                emptyList<Category>()
            }

            val transactions = try {
                repository.transactionDao.getAllTransactions()
            } catch (e: Exception) {
                emptyList<Transaction>()
            }

            val accounts = try {
                repository.accountDao.getAllActiveAccounts().first()
            } catch (e: Exception) {
                emptyList<Account>()
            }

            val savingsGoals = try {
                repository.savingsGoalDao.getAll()
            } catch (e: Exception) {
                emptyList<SavingsGoal>()
            }

            val budgets = try {
                // Проверяем, есть ли метод getAllBudgets
                repository.budgetDao.getAllBudgets()
            } catch (e: Exception) {
                emptyList<Budget>()
            }

            // Категории
            val categoriesArray = JSONArray()
            categories.forEach { category ->
                val categoryJson = JSONObject().apply {
                    put("id", category.id)
                    put("name", category.name)
                    put("type", category.type)
                    put("color", category.color)
                    put("icon", category.icon)
                    put("is_default", category.isDefault)
                    put("is_active", category.isActive)
                    put("sort_order", category.sortOrder)
                    put("created_at", category.createdAt.time)
                }
                categoriesArray.put(categoryJson)
            }
            jsonObject.put("categories", categoriesArray)

            // Операции (транзакции)
            val transactionsArray = JSONArray()
            transactions.forEach { transaction ->
                val transactionJson = JSONObject().apply {
                    put("id", transaction.id)
                    put("amount", transaction.amount)
                    put("type", transaction.type)
                    put("description", transaction.description ?: "")
                    put("category_id", transaction.categoryId ?: 0)
                    put("account_id", transaction.accountId)
                    put("date", transaction.date.time)
                    put("time", transaction.time ?: "")
                    put("is_recurring", transaction.isRecurring)
                    put("created_at", transaction.createdAt.time)
                }
                transactionsArray.put(transactionJson)
            }
            jsonObject.put("transactions", transactionsArray)

            // Счета
            val accountsArray = JSONArray()
            accounts.forEach { account ->
                val accountJson = JSONObject().apply {
                    put("id", account.id)
                    put("name", account.name)
                    put("balance", account.balance)
                    put("currency", account.currency)
                    put("color", account.color)
                    put("icon", account.icon)
                    put("is_active", account.isActive)
                    put("sort_order", account.sortOrder)
                    put("created_at", account.createdAt.time)
                }
                accountsArray.put(accountJson)
            }
            jsonObject.put("accounts", accountsArray)

            // Цели сбережений
            val savingsGoalsArray = JSONArray()
            savingsGoals.forEach { goal ->
                val goalJson = JSONObject().apply {
                    put("id", goal.id)
                    put("name", goal.name)
                    put("target_amount", goal.targetAmount)
                    put("current_amount", goal.currentAmount)
                    put("description", goal.description ?: "")
                    put("target_date", goal.targetDate?.time ?: 0)
                    put("color", goal.color)
                    put("icon", goal.icon)
                    put("is_completed", goal.isCompleted)
                    put("created_at", goal.createdAt.time)
                    put("progress_percentage", goal.getProgress())
                    put("remaining_amount", goal.getRemainingAmount())
                }
                savingsGoalsArray.put(goalJson)
            }
            jsonObject.put("savings_goals", savingsGoalsArray)

            // Бюджеты
            val budgetsArray = JSONArray()
            budgets.forEach { budget ->
                val budgetJson = JSONObject().apply {
                    put("id", budget.id)
                    put("category_id", budget.categoryId)
                    put("amount", budget.amount)
                    put("month", budget.month)
                    put("year", budget.year)
                    put("created_at", budget.createdAt.time)
                    put("updated_at", budget.updatedAt.time)
                }
                budgetsArray.put(budgetJson)
            }
            jsonObject.put("budgets", budgetsArray)

            // Статистика
            val stats = JSONObject().apply {
                put("total_categories", categories.size)
                put("total_transactions", transactions.size)
                put("total_accounts", accounts.size)
                put("total_savings_goals", savingsGoals.size)
                put("total_budgets", budgets.size)

                // Расчет общей суммы доходов и расходов
                val incomeTotal = transactions
                    .filter { it.type == Transaction.TYPE_INCOME }
                    .sumOf { it.amount }
                val expenseTotal = transactions
                    .filter { it.type == Transaction.TYPE_EXPENSE }
                    .sumOf { it.amount }

                put("total_income", incomeTotal)
                put("total_expense", expenseTotal)
                put("net_balance", incomeTotal - expenseTotal)

                // Общий баланс по счетам
                val totalBalance = accounts.sumOf { it.balance }
                put("total_accounts_balance", totalBalance)
            }
            jsonObject.put("statistics", stats)

            jsonObject
        }
    }

    private fun saveJSONToFile(jsonData: JSONObject) {
        try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "finance_export_${dateFormat.format(Date())}.json"

            // Получаем директорию для сохранения
            val downloadsDir = requireContext().getExternalFilesDir(null)
            val exportFile = File(downloadsDir, fileName)

            // Записываем данные в файл
            val outputStream = FileOutputStream(exportFile)
            val writer = OutputStreamWriter(outputStream, "UTF-8")

            // Красивое форматирование JSON
            writer.write(jsonData.toString(4))
            writer.close()

            // Показать уведомление о сохранении
            showExportSuccessDialog(exportFile.absolutePath, fileName)

        } catch (e: Exception) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка сохранения файла: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showExportSuccessDialog(filePath: String, fileName: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Экспорт завершен")
                    .setMessage("Файл успешно сохранен:\n$fileName")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                closeApp()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun closeApp() {
        activity?.finishAffinity()
    }
}