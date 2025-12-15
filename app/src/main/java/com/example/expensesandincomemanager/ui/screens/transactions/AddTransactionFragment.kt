package com.example.expensesandincomemanager.ui.screens.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensesandincomemanager.R
import data.database.FinanceDatabase
import data.entities.Category
import data.entities.Transaction
import data.entities.Account
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private lateinit var selectedCategory: Category
    private lateinit var selectedAccount: Account
    private var selectedDate = Calendar.getInstance()
    private var transactionType = "expense" // По умолчанию расход

    private val calendar = Calendar.getInstance()

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
        CoroutineScope(Dispatchers.IO).launch {
            val database = FinanceDatabase.getDatabase(requireContext())
            val categories = database.categoryDao().getCategoriesByType(transactionType)

            categories.collect { categoryList ->
                withContext(Dispatchers.Main) {
                    val categoryNames = categoryList.map { it.name }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Выберите категорию")
                        .setItems(categoryNames.toTypedArray()) { dialog, which ->
                            selectedCategory = categoryList[which]
                            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)?.setText(selectedCategory.name)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                }
            }
        }
    }

    private fun showAccountDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = FinanceDatabase.getDatabase(requireContext())
            val accountsFlow = database.accountDao().getAllActiveAccounts()

            accountsFlow.collect { accountList ->
                withContext(Dispatchers.Main) {
                    val accountNames = accountList.map { it.name }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Выберите счет")
                        .setItems(accountNames.toTypedArray()) { dialog, which ->
                            selectedAccount = accountList[which]
                            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)?.setText(selectedAccount.name)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
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
        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dateEditText)?.setText(dateFormat.format(selectedDate.time))
    }

    private fun loadDefaultData() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = FinanceDatabase.getDatabase(requireContext())

            // Загружаем первую активную категорию
            val categories = database.categoryDao().getCategoriesByType(transactionType)
            categories.collect { categoryList ->
                if (categoryList.isNotEmpty()) {
                    selectedCategory = categoryList.first()
                    withContext(Dispatchers.Main) {
                        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)?.setText(selectedCategory.name)
                    }
                }
            }

            // Загружаем первый активный счет
            val accountsFlow = database.accountDao().getAllActiveAccounts()
            accountsFlow.collect { accountList ->
                if (accountList.isNotEmpty()) {
                    selectedAccount = accountList.first()
                    withContext(Dispatchers.Main) {
                        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)?.setText(selectedAccount.name)
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
            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)?.error = "Выберите категорию"
            return
        }

        if (selectedAccount == null) {
            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)?.error = "Выберите счет"
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
            recurringType = null // Добавляем это поле
        )

        // Сохраняем в базу данных
        CoroutineScope(Dispatchers.IO).launch {
            val database = FinanceDatabase.getDatabase(requireContext())

            try {
                // Вставляем транзакцию
                val transactionId = database.transactionDao().insert(transaction)

                // Обновляем баланс счета
                if (transactionType == "income") {
                    database.accountDao().updateBalance(selectedAccount!!.id, amount)
                } else {
                    database.accountDao().updateBalance(selectedAccount!!.id, -amount)
                }

                withContext(Dispatchers.Main) {
                    // Показываем сообщение об успехе
                    Toast.makeText(
                        requireContext(),
                        "Транзакция сохранена",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Возвращаемся на предыдущий экран
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
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