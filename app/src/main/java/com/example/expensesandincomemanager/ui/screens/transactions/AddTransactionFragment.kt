package com.example.expensesandincomemanager.ui.screens.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensesandincomemanager.R
import data.database.FinanceDatabase
import data.entities.Category
import data.entities.Transaction
import data.entities.Account
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import data.provider.FinanceRepositoryProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var selectedCategory: Category? = null
    private var selectedAccount: Account? = null
    private val selectedDate = Calendar.getInstance()
    private var transactionType = "expense"

    private var coroutineJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

        loadDefaultData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineJob?.cancel()
        coroutineScope.cancel()
    }

    private fun setupToolbar() {
        view?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupTypeSelector() {
        view?.findViewById<View>(R.id.incomeCard)?.setOnClickListener {
            selectType("income")
        }

        view?.findViewById<View>(R.id.expenseCard)?.setOnClickListener {
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
            loadCategoriesByType("income")
        } else {
            expenseCard?.strokeWidth = 2
            expenseCard?.strokeColor = primaryColor
            incomeCard?.strokeWidth = 1
            incomeCard?.strokeColor = outlineColor
            loadCategoriesByType("expense")
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
        updateDateText()
        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dateEditText)?.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showCategoryDialog() {
        coroutineJob?.cancel()

        coroutineJob = coroutineScope.launch {
            try {
                if (!isAdded) return@launch

                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val categoriesFlow = database.categoryDao().getCategoriesByType(transactionType)
                val categories = categoriesFlow.firstOrNull() ?: emptyList()

                if (!isAdded) return@launch

                if (categories.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет доступных категорий", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val categoryNames = categories.map { it.name }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Выберите категорию")
                    .setItems(categoryNames) { dialog, which ->
                        selectedCategory = categories[which]
                        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                            ?.setText(selectedCategory?.name)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Ошибка загрузки категорий", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAccountDialog() {
        coroutineJob?.cancel()

        coroutineJob = coroutineScope.launch {
            try {
                if (!isAdded) return@launch

                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val accountsFlow = database.accountDao().getAllActiveAccounts()
                val accounts = accountsFlow.firstOrNull() ?: emptyList()

                if (!isAdded) return@launch

                if (accounts.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет доступных счетов", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val accountNames = accounts.map { it.name }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Выберите счет")
                    .setItems(accountNames) { dialog, which ->
                        selectedAccount = accounts[which]
                        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                            ?.setText(selectedAccount?.name)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Ошибка загрузки счетов", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                updateDateText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dateEditText)
            ?.setText(dateFormat.format(selectedDate.time))
    }

    private fun loadDefaultData() {
        loadCategoriesByType(transactionType)
        loadAccounts()
    }

    private fun loadCategoriesByType(type: String) {
        coroutineJob?.cancel()

        coroutineJob = coroutineScope.launch {
            try {
                if (!isAdded) return@launch

                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val categoriesFlow = database.categoryDao().getCategoriesByType(type)
                val categories = categoriesFlow.firstOrNull() ?: emptyList()

                if (isAdded && categories.isNotEmpty()) {
                    selectedCategory = categories.first()
                    view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                        ?.setText(selectedCategory?.name)
                }
            } catch (e: Exception) {
                // Игнорируем ошибки при загрузке данных по умолчанию
            }
        }
    }

    private fun loadAccounts() {
        coroutineJob?.cancel()

        coroutineJob = coroutineScope.launch {
            try {
                if (!isAdded) return@launch

                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val accountsFlow = database.accountDao().getAllActiveAccounts()
                val accounts = accountsFlow.firstOrNull() ?: emptyList()

                if (isAdded && accounts.isNotEmpty()) {
                    selectedAccount = accounts.first()
                    view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                        ?.setText(selectedAccount?.name)
                }
            } catch (e: Exception) {
                // Игнорируем ошибки при загрузке данных по умолчанию
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
            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                ?.error = "Выберите категорию"
            return
        }

        if (selectedAccount == null) {
            view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                ?.error = "Выберите счет"
            return
        }

        coroutineJob?.cancel()

        coroutineJob = coroutineScope.launch {
            try {
                // Используем репозиторий для сохранения
                val repository = FinanceRepositoryProvider.getRepository(requireContext())

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

                // Сохраняем через репозиторий (он сам обновит баланс и уведомит)
                val transactionId = repository.insertTransaction(transaction)

                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Транзакция сохранена",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Немедленно обновляем данные на главном экране
                    repository.forceRefresh()

                    // Возвращаемся назад
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                if (isAdded) {
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