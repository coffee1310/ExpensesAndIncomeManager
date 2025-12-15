package com.example.expensesandincomemanager.ui.screens.operations

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensesandincomemanager.R
import com.google.android.material.card.MaterialCardView
import data.database.FinanceDatabase
import data.entities.Transaction
import data.entities.Category
import data.entities.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionFragment : Fragment() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var incomeCard: MaterialCardView
    private lateinit var expenseCard: MaterialCardView
    private lateinit var etAmount: EditText
    private lateinit var etCategory: EditText
    private lateinit var etAccount: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    private var transaction: Transaction? = null
    private val categories = mutableListOf<Category>()
    private val accounts = mutableListOf<Account>()
    private var selectedType: String = "expense"
    private var selectedCategoryId: Int? = null
    private var selectedAccountId: Int? = null

    private val TAG = "EditTransactionFragment"

    companion object {
        private const val ARG_TRANSACTION_ID = "transaction_id"

        fun newInstance(transactionId: Int): EditTransactionFragment {
            val fragment = EditTransactionFragment()
            val args = Bundle()
            args.putInt(ARG_TRANSACTION_ID, transactionId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupToolbar()
        setupTypeSelection()
        setupCategoryClickListener()
        setupAccountClickListener()
        setupDateClickListener()
        setupTimeClickListener()
        setupSaveButton()
        setupDeleteButton()
        loadTransaction()
        loadCategoriesAndAccounts()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        incomeCard = view.findViewById(R.id.incomeCard)
        expenseCard = view.findViewById(R.id.expenseCard)
        etAmount = view.findViewById(R.id.etAmount)
        etCategory = view.findViewById(R.id.etCategory)
        etAccount = view.findViewById(R.id.etAccount)
        etDate = view.findViewById(R.id.etDate)
        etTime = view.findViewById(R.id.etTime)
        etDescription = view.findViewById(R.id.etDescription)
        btnSave = view.findViewById(R.id.btnSave)
        btnDelete = view.findViewById(R.id.btnDelete)
    }

    private fun setupToolbar() {
        toolbar.apply {
            title = "Редактирование операции"
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupTypeSelection() {
        incomeCard.setOnClickListener {
            selectIncome()
        }

        expenseCard.setOnClickListener {
            selectExpense()
        }
    }

    private fun selectIncome() {
        incomeCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.success)
        expenseCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.outline)
        incomeCard.strokeWidth = 2
        expenseCard.strokeWidth = 1
        selectedType = "income"
    }

    private fun selectExpense() {
        expenseCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.error)
        incomeCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.outline)
        expenseCard.strokeWidth = 2
        incomeCard.strokeWidth = 1
        selectedType = "expense"
    }

    private fun setupCategoryClickListener() {
        etCategory.setOnClickListener {
            showCategoryDialog()
        }
    }

    private fun setupAccountClickListener() {
        etAccount.setOnClickListener {
            showAccountDialog()
        }
    }

    private fun setupDateClickListener() {
        etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupTimeClickListener() {
        etTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupDeleteButton() {
        btnDelete.setOnClickListener {
            deleteTransaction()
        }
    }

    private fun loadTransaction() {
        val transactionId = arguments?.getInt(ARG_TRANSACTION_ID) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val transactionDao = database.transactionDao()

                // Используем существующий метод getById
                val loadedTransaction = transactionDao.getById(transactionId)

                if (loadedTransaction != null) {
                    transaction = loadedTransaction
                    withContext(Dispatchers.Main) {
                        populateForm()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Транзакция не найдена",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading transaction: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки транзакции",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadCategoriesAndAccounts() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())

                // Загружаем категории - используем suspend метод getAllCategories()
                categories.clear()
                try {
                    val categoriesList = database.categoryDao().getAllCategories()
                    categories.addAll(categoriesList)
                    Log.d(TAG, "Loaded ${categories.size} categories")
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting categories: ${e.message}")
                }

                // Загружаем счета - используем Flow метод getAllActiveAccounts()
                accounts.clear()
                try {
                    val accountsFlow = database.accountDao().getAllActiveAccounts()
                    val accountsList = accountsFlow.firstOrNull() ?: emptyList()
                    accounts.addAll(accountsList)
                    Log.d(TAG, "Loaded ${accounts.size} accounts")
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting accounts: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories and accounts: ${e.message}")
            }
        }
    }

    private fun populateForm() {
        transaction?.let { trans ->
            // Устанавливаем тип
            if (trans.type == "income") {
                selectIncome()
            } else {
                selectExpense()
            }

            // Устанавливаем сумму
            etAmount.setText(trans.amount.toString())

            // Устанавливаем категорию
            selectedCategoryId = trans.categoryId
            if (selectedCategoryId != null) {
                loadCategoryName(selectedCategoryId!!)
            }

            // Устанавливаем счет
            selectedAccountId = trans.accountId
            if (selectedAccountId != null) {
                loadAccountName(selectedAccountId!!)
            }

            // Устанавливаем дату и время
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            etDate.setText(dateFormat.format(trans.date))
            etTime.setText(timeFormat.format(trans.date))

            // Устанавливаем описание
            etDescription.setText(trans.description ?: "")
        }
    }

    private fun loadCategoryName(categoryId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val categoryDao = database.categoryDao()

                // Используем существующий метод getById (не getCategoryById)
                val category = categoryDao.getById(categoryId) // Измените getCategoryById на getById

                withContext(Dispatchers.Main) {
                    category?.let {
                        etCategory.setText(it.name)
                    } ?: run {
                        etCategory.setText("Категория не найдена")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading category name: ${e.message}")
                withContext(Dispatchers.Main) {
                    etCategory.setText("Ошибка загрузки")
                }
            }
        }
    }

    private fun loadAccountName(accountId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val accountDao = database.accountDao()

                // Используем существующий метод getById (не getAccountById)
                val account = accountDao.getById(accountId) // Измените getAccountById на getById

                withContext(Dispatchers.Main) {
                    account?.let {
                        etAccount.setText(it.name)
                    } ?: run {
                        etAccount.setText("Счет не найден")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading account name: ${e.message}")
                withContext(Dispatchers.Main) {
                    etAccount.setText("Ошибка загрузки")
                }
            }
        }
    }

    private fun showCategoryDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Сначала создайте категории", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryNames = categories.map { it.name }
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Выберите категорию")
        builder.setItems(categoryNames.toTypedArray()) { dialog, which ->
            selectedCategoryId = categories[which].id
            etCategory.setText(categories[which].name)
            dialog.dismiss()
        }
        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showAccountDialog() {
        if (accounts.isEmpty()) {
            Toast.makeText(requireContext(), "Сначала создайте счета", Toast.LENGTH_SHORT).show()
            return
        }

        val accountNames = accounts.map { it.name }
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Выберите счет")
        builder.setItems(accountNames.toTypedArray()) { dialog, which ->
            selectedAccountId = accounts[which].id
            etAccount.setText(accounts[which].name)
            dialog.dismiss()
        }
        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showDatePicker() {
        transaction?.let { trans ->
            val calendar = Calendar.getInstance()
            calendar.time = trans.date

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // Создаем новый объект Transaction для обновления даты
                    val updatedTransaction = trans.copy(date = calendar.time)
                    transaction = updatedTransaction

                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    etDate.setText(dateFormat.format(updatedTransaction.date))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun showTimePicker() {
        transaction?.let { trans ->
            val calendar = Calendar.getInstance()
            calendar.time = trans.date

            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    // Создаем новый объект Transaction для обновления времени
                    val updatedTransaction = trans.copy(date = calendar.time)
                    transaction = updatedTransaction

                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    etTime.setText(timeFormat.format(updatedTransaction.date))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
    }

    private fun saveTransaction() {
        val currentTransaction = transaction ?: return

        // Валидация суммы
        val amountText = etAmount.text.toString()
        if (amountText.isBlank()) {
            etAmount.error = "Введите сумму"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            etAmount.error = "Введите корректную сумму"
            return
        }

        // Валидация категории
        if (selectedCategoryId == null) {
            Toast.makeText(requireContext(), "Выберите категорию", Toast.LENGTH_SHORT).show()
            return
        }

        // Валидация счета
        if (selectedAccountId == null) {
            Toast.makeText(requireContext(), "Выберите счет", Toast.LENGTH_SHORT).show()
            return
        }

        // Валидация даты и времени
        if (etDate.text.isBlank() || etTime.text.isBlank()) {
            Toast.makeText(requireContext(), "Укажите дату и время", Toast.LENGTH_SHORT).show()
            return
        }

        // Объединяем дату и время
        val combinedDate = combineDateTime()
        if (combinedDate == null) {
            Toast.makeText(requireContext(), "Некорректная дата или время", Toast.LENGTH_SHORT).show()
            return
        }

        // Создаем обновленный объект транзакции
        val updatedTransaction = currentTransaction.copy(
            amount = amount,
            type = selectedType,
            categoryId = selectedCategoryId!!,
            accountId = selectedAccountId!!,
            description = etDescription.text.toString().takeIf { it.isNotBlank() },
            date = combinedDate
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val transactionDao = database.transactionDao()

                // Обновляем транзакцию в базе данных
                transactionDao.update(updatedTransaction)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Транзакция сохранена",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving transaction: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка сохранения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun combineDateTime(): Date? {
        return try {
            val dateStr = etDate.text.toString()
            val timeStr = etTime.text.toString()

            if (dateStr.isNotBlank() && timeStr.isNotBlank()) {
                val dateTimeStr = "$dateStr $timeStr"
                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                format.parse(dateTimeStr)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error combining date and time: ${e.message}")
            null
        }
    }

    private fun deleteTransaction() {
        transaction?.let { trans ->
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Удаление транзакции")
            builder.setMessage("Вы уверены, что хотите удалить эту транзакцию?")
            builder.setPositiveButton("Удалить") { dialog, _ ->
                performDeleteTransaction(trans)
                dialog.dismiss()
            }
            builder.setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun performDeleteTransaction(transaction: Transaction) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val transactionDao = database.transactionDao()

                // Удаляем транзакцию
                transactionDao.delete(transaction)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Транзакция удалена",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting transaction: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка удаления: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}