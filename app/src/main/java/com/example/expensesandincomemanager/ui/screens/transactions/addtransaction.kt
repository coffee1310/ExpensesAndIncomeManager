package com.example.expensesandincomemanager.ui.screens.home

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

    private var fragmentView: View? = null
    private var coroutineJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)
        fragmentView = view
        return view
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
        fragmentView = null
    }

    private fun setupToolbar() {
        fragmentView?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupTypeSelector() {
        fragmentView?.findViewById<View>(R.id.incomeCard)?.setOnClickListener {
            selectType("income")
        }

        fragmentView?.findViewById<View>(R.id.expenseCard)?.setOnClickListener {
            selectType("expense")
        }
    }

    private fun selectType(type: String) {
        transactionType = type
        val primaryColor = requireContext().getColor(R.color.primary)
        val outlineColor = requireContext().getColor(R.color.outline)

        val incomeCard = fragmentView?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.incomeCard)
        val expenseCard = fragmentView?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.expenseCard)

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
        fragmentView?.findViewById<View>(R.id.categoryEditText)?.setOnClickListener {
            showCategoryDialog()
        }
    }

    private fun setupAccountSelector() {
        fragmentView?.findViewById<View>(R.id.accountEditText)?.setOnClickListener {
            showAccountDialog()
        }
    }

    private fun setupDateSelector() {
        updateDateText()
        fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dateEditText)?.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showCategoryDialog() {
        coroutineJob?.cancel()

        coroutineJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val categoriesFlow = database.categoryDao().getCategoriesByType(transactionType)

                val categories = withContext(Dispatchers.IO) {
                    categoriesFlow.firstOrNull() ?: emptyList()
                }

                if (categories.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет доступных категорий", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val categoryNames = categories.map { it.name }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Выберите категорию")
                    .setItems(categoryNames) { dialog, which ->
                        selectedCategory = categories[which]
                        fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                            ?.setText(selectedCategory?.name)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } catch (e: Exception) {
                if (isActive && isAdded) {
                    Toast.makeText(requireContext(), "Ошибка загрузки категорий", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAccountDialog() {
        coroutineJob?.cancel()

        coroutineJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val accountsFlow = database.accountDao().getAllActiveAccounts()

                val accounts = withContext(Dispatchers.IO) {
                    accountsFlow.firstOrNull() ?: emptyList()
                }

                if (accounts.isEmpty()) {
                    Toast.makeText(requireContext(), "Нет доступных счетов", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val accountNames = accounts.map { it.name }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Выберите счет")
                    .setItems(accountNames) { dialog, which ->
                        selectedAccount = accounts[which]
                        fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                            ?.setText(selectedAccount?.name)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } catch (e: Exception) {
                if (isActive && isAdded) {
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
        fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dateEditText)
            ?.setText(dateFormat.format(selectedDate.time))
    }

    private fun loadDefaultData() {
        loadCategoriesByType(transactionType)
        loadAccounts()
    }

    private fun loadCategoriesByType(type: String) {
        coroutineJob?.cancel()

        coroutineJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val categoriesFlow = database.categoryDao().getCategoriesByType(type)

                val categories = withContext(Dispatchers.IO) {
                    categoriesFlow.firstOrNull() ?: emptyList()
                }

                if (categories.isNotEmpty()) {
                    selectedCategory = categories.first()
                    fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                        ?.setText(selectedCategory?.name)
                }
            } catch (e: Exception) {
                // Игнорируем ошибки при загрузке данных по умолчанию
            }
        }
    }

    private fun loadAccounts() {
        coroutineJob?.cancel()

        coroutineJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val database = withContext(Dispatchers.IO) {
                    FinanceDatabase.getDatabase(requireContext())
                }

                val accountsFlow = database.accountDao().getAllActiveAccounts()

                val accounts = withContext(Dispatchers.IO) {
                    accountsFlow.firstOrNull() ?: emptyList()
                }

                if (accounts.isNotEmpty()) {
                    selectedAccount = accounts.first()
                    fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                        ?.setText(selectedAccount?.name)
                }
            } catch (e: Exception) {
                // Игнорируем ошибки при загрузке данных по умолчанию
            }
        }
    }

    private fun setupSaveButton() {
        fragmentView?.findViewById<View>(R.id.saveButton)?.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amountEditText = fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amountEditText)
        val descriptionEditText = fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionEditText)

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
            fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.categoryEditText)
                ?.error = "Выберите категорию"
            return
        }

        if (selectedAccount == null) {
            fragmentView?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.accountEditText)
                ?.error = "Выберите счет"
            return
        }

        // Создаем транзакцию
        val transaction = data.entities.Transaction(
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

        // Сохраняем в базу данных
        coroutineJob?.cancel()

        coroutineJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val repository = FinanceRepositoryProvider.getRepository(requireContext())

                // Вставляем транзакцию через репозиторий
                val transactionId = repository.insertTransaction(transaction)

                // Обновляем баланс счета через репозиторий
                if (transactionType == data.entities.Transaction.TYPE_INCOME) {
                    repository.updateAccountBalance(selectedAccount!!.id, amount)
                } else {
                    repository.updateAccountBalance(selectedAccount!!.id, -amount)
                }

                withContext(Dispatchers.Main) {
                    if (isActive && isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Транзакция сохранена",
                            Toast.LENGTH_SHORT
                        ).show()

                        parentFragmentManager.popBackStack()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isActive && isAdded) {
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
