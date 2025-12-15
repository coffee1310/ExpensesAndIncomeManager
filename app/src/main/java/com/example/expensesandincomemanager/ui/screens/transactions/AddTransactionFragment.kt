package com.example.expensesandincomemanager.ui.screens.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensesandincomemanager.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import data.database.FinanceDatabase
import data.entities.Account
import data.entities.Category
import data.entities.Transaction
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var selectedCategory: Category? = null
    private var selectedAccount: Account? = null
    private var selectedDate = Calendar.getInstance()
    private var transactionType = "expense" // По умолчанию расход

    // Используем viewLifecycleOwner для корутин
    private val coroutineScope get() = viewLifecycleOwner.lifecycleScope

    // Job для управления корутинами
    private var currentJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupTypeSelector()
        setupCategorySelector()
        setupAccountSelector()
        setupDateSelector()
        setupSaveButton()

        // Загружаем данные по умолчанию
        loadDefaultData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Отменяем все корутины при уничтожении View
        currentJob?.cancel()
    }

    private fun setupToolbar() {
        view?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun setupTypeSelector() {
        view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.incomeCard)?.setOnClickListener {
            selectType("income")
        }

        view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.expenseCard)?.setOnClickListener {
            selectType("expense")
        }
    }

    private fun selectType(type: String) {
        transactionType = type
        val primaryColor = requireContext().getColor(R.color.primary)
        val outlineColor = requireContext().getColor(R.color.outline)

        val incomeCard = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.incomeCard)
        val expenseCard = view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.expenseCard)

        if (type == "income") {
            incomeCard?.strokeWidth = 2
            incomeCard?.strokeColor = primaryColor
            expenseCard?.strokeWidth = 1
            expenseCard?.strokeColor = outlineColor
        } else {
            expenseCard?.strokeWidth = 2
            expenseCard?.strokeColor = primaryColor
            incomeCard?.strokeWidth = 1
            incomeCard?.strokeColor = outlineColor
        }

        // ВАЖНО: Обновляем категории при изменении типа
        loadCategoriesByType(type)
    }

    private fun setupCategorySelector() {
        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)?.setOnClickListener {
            showCategoryDialog()
        }
    }

    private fun setupAccountSelector() {
        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)?.setOnClickListener {
            showAccountDialog()
        }
    }

    private fun setupDateSelector() {
        val dateEditText = view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dateEditText)
        updateDateText()

        dateEditText?.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showCategoryDialog() {
        // Проверяем контекст перед началом
        val safeContext = context ?: return
        if (!isAdded) return

        currentJob = coroutineScope.launch {
            try {
                val database = FinanceDatabase.getDatabase(safeContext)
                val categoriesFlow = database.categoryDao().getCategoriesByType(transactionType)

                withContext(Dispatchers.Main) {
                    // Еще раз проверяем, что фрагмент все еще привязан
                    if (!isAdded || context == null) return@withContext

                    // Собираем категории в Main потоке
                    coroutineScope.launch {
                        categoriesFlow.collect { categoryList ->
                            val categoryNames = categoryList.map { it.name }

                            // Показываем диалог только если фрагмент активен
                            if (isAdded && context != null) {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Выберите категорию")
                                    .setItems(categoryNames.toTypedArray()) { dialog, which ->
                                        selectedCategory = categoryList[which]
                                        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                                            ?.setText(selectedCategory?.name ?: "")
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Отмена", null)
                                    .show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Можно показать Toast, но только если фрагмент активен
                if (isAdded && context != null) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки категорий",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showAccountDialog() {
        // Безопасная проверка контекста (строка 147 была здесь)
        val safeContext = context ?: return
        if (!isAdded) return

        currentJob = coroutineScope.launch {
            try {
                val database = FinanceDatabase.getDatabase(safeContext)
                val accountsFlow = database.accountDao().getAllActiveAccounts()

                withContext(Dispatchers.Main) {
                    // Еще раз проверяем, что фрагмент все еще привязан
                    if (!isAdded || context == null) return@withContext

                    // Собираем счета в Main потоке
                    coroutineScope.launch {
                        accountsFlow.collect { accountList ->
                            val accountNames = accountList.map { it.name }

                            // Показываем диалог только если фрагмент активен
                            if (isAdded && context != null) {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Выберите счет")
                                    .setItems(accountNames.toTypedArray()) { dialog, which ->
                                        selectedAccount = accountList[which]
                                        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                                            ?.setText(selectedAccount?.name ?: "")
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Отмена", null)
                                    .show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Можно показать Toast, но только если фрагмент активен
                if (isAdded && context != null) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки счетов",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        val safeContext = context ?: return
        if (!isAdded) return

        val datePicker = DatePickerDialog(
            safeContext,
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                updateDateText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dateEditText)
            ?.setText(dateFormat.format(selectedDate.time))
    }

    private fun loadDefaultData() {
        val safeContext = context ?: return
        if (!isAdded) return

        currentJob = coroutineScope.launch {
            try {
                val database = FinanceDatabase.getDatabase(safeContext)

                // Загружаем категории по умолчанию
                loadCategoriesByType(transactionType)

                // Загружаем счета
                val accountsFlow = database.accountDao().getAllActiveAccounts()

                coroutineScope.launch {
                    accountsFlow.collect { accountList ->
                        if (accountList.isNotEmpty()) {
                            selectedAccount = accountList.first()
                            withContext(Dispatchers.Main) {
                                if (isAdded) {
                                    view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                                        ?.setText(selectedAccount?.name ?: "")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCategoriesByType(type: String) {
        val safeContext = context ?: return
        if (!isAdded) return

        currentJob?.cancel()

        currentJob = coroutineScope.launch {
            try {
                val database = FinanceDatabase.getDatabase(safeContext)
                val categoriesFlow = database.categoryDao().getCategoriesByType(type)

                coroutineScope.launch {
                    categoriesFlow.collect { categoryList ->
                        if (categoryList.isNotEmpty()) {
                            // Автоматически выбираем первую категорию при смене типа
                            selectedCategory = categoryList.first()
                            withContext(Dispatchers.Main) {
                                if (isAdded) {
                                    view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                                        ?.setText(selectedCategory?.name ?: "")

                                    // Показываем сообщение о смене категории
                                    Toast.makeText(
                                        requireContext(),
                                        "Категория изменена на: ${selectedCategory?.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // Если нет категорий для выбранного типа
                            withContext(Dispatchers.Main) {
                                if (isAdded) {
                                    selectedCategory = null
                                    view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                                        ?.setText("")

                                    Toast.makeText(
                                        requireContext(),
                                        "Нет категорий для типа: ${if (type == "income") "Доход" else "Расход"}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка загрузки категорий",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupSaveButton() {
        view?.findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)?.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val safeContext = context ?: return
        if (!isAdded) return

        val amountEditText = view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amountEditText)
        val descriptionEditText = view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionEditText)

        val amountText = amountEditText?.text.toString()
        val description = descriptionEditText?.text.toString()

        if (amountText.isBlank()) {
            amountEditText?.error = "Введите сумму"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountEditText?.error = "Введите корректную сумму"
            return
        }

        if (selectedCategory == null) {
            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                ?.error = "Выберите категорию"
            return
        }

        if (selectedAccount == null) {
            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                ?.error = "Выберите счет"
            return
        }

        // Создаем транзакцию
        val transaction = Transaction(
            amount = amount,
            type = transactionType,
            categoryId = selectedCategory?.id,
            accountId = selectedAccount!!.id,
            description = description.ifEmpty { null },
            date = selectedDate.time,
            time = Date(),
            isRecurring = false,
            recurringType = null
        )

        currentJob = coroutineScope.launch {
            try {
                val database = FinanceDatabase.getDatabase(safeContext)

                // Вставляем транзакцию
                val transactionId = database.transactionDao().insert(transaction)

                // Обновляем баланс счета
                if (transactionType == "income") {
                    database.accountDao().updateBalance(selectedAccount!!.id, amount)
                } else {
                    database.accountDao().updateBalance(selectedAccount!!.id, -amount)
                }

                withContext(Dispatchers.Main) {
                    // Показываем сообщение об успехе только если фрагмент активен
                    if (isAdded && context != null) {
                        Toast.makeText(
                            requireContext(),
                            "Транзакция сохранена",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Возвращаемся на предыдущий экран
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Показываем ошибку только если фрагмент активен
                    if (isAdded && context != null) {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка при сохранении: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}