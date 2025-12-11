package com.example.expensesandincomemanager.ui.screens.operations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensesandincomemanager.R
import data.database.FinanceDatabase
import data.entities.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {

    private lateinit var transactionsContainer: LinearLayout
    private val transactionsList = mutableListOf<Transaction>()
    private val TAG = "TransactionsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        setupToolbar()
        setupTransactionsContainer()
        setupEmptyState()
        setupRefreshButton()
        loadTransactions()
    }

    private fun setupToolbar() {
        view?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            title = "История операций"
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
        Log.d(TAG, "Toolbar setup complete")
    }

    private fun setupTransactionsContainer() {
        val container = view?.findViewById<LinearLayout>(R.id.transactionsContainer)
        if (container != null) {
            transactionsContainer = container
            Log.d(TAG, "TransactionsContainer setup complete")
        } else {
            Log.e(TAG, "transactionsContainer not found in layout!")
        }
    }

    private fun setupEmptyState() {
        val tvEmptyState = view?.findViewById<TextView>(R.id.tvEmptyState)
        tvEmptyState?.visibility = View.GONE
    }

    private fun setupRefreshButton() {
        view?.findViewById<Button>(R.id.btnRefresh)?.setOnClickListener {
            Log.d(TAG, "Refresh button clicked")
            loadTransactions()
        }
    }

    private fun loadTransactions() {
        Log.d(TAG, "loadTransactions called")

        lifecycleScope.launch {
            try {
                showLoading(true)

                Log.d(TAG, "Trying to get database instance...")
                val database = FinanceDatabase.getDatabase(requireContext())
                Log.d(TAG, "Database instance obtained")

                // ПРОБЛЕМА: Метод getAllTransactions() может возвращать Flow
                // Используем правильный способ получения данных
                val transactions = withContext(Dispatchers.IO) {
                    try {
                        // Попробуем получить как Flow и преобразовать в List
                        val flow = database.transactionDao().getAllTransactions()
                        Log.d(TAG, "Got Flow from DAO")

                        // Если это Flow, получаем первую эмиссию
                        val result = flow.firstOrNull() ?: emptyList()
                        Log.d(TAG, "Converted Flow to List: ${result.size} items")
                        result

                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting as Flow: ${e.message}")

                        // Пробуем получить напрямую как List (если есть suspend метод)
                        try {
                            val directResult = database.transactionDao().getAllTransactionsDirect()
                            Log.d(TAG, "Got direct List: ${directResult.size} items")
                            directResult
                        } catch (e2: Exception) {
                            Log.e(TAG, "Error getting direct list: ${e2.message}")
                            emptyList()
                        }
                    }
                }

                Log.d(TAG, "Loaded ${transactions.size} transactions")

                // Логируем каждую транзакцию
                transactions.forEachIndexed { index, transaction ->
                    Log.d(TAG, "Transaction $index: ID=${transaction.id}, Amount=${transaction.amount}, Type=${transaction.type}, Date=${transaction.date}")
                }

                if (transactions.isNotEmpty()) {
                    // Обновляем UI в главном потоке
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Displaying ${transactions.size} transactions in UI")
                        displayTransactions(transactions)
                        updateTransactionCount(transactions.size)
                        showEmptyState(false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "No transactions found, showing empty state")
                        showEmptyState(true)
                        updateTransactionCount(0)

                        // Показываем информацию о базе данных
                        checkDatabaseStatus()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading transactions: ${e.message}", e)
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState(true)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.d(TAG, "Loading finished")
                }
            }
        }
    }

    private suspend fun checkDatabaseStatus() {
        withContext(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                // Проверяем, есть ли вообще таблицы
                val transactionCount = database.transactionDao().getTransactionCount()
                Log.d(TAG, "Transaction count in DB: $transactionCount")

                if (transactionCount == 0) {
                    // Проверяем другие таблицы
                    val categoryCount = database.categoryDao().getCategoryCount()
                    val accountCount = database.accountDao().getAccountCount()

                    Log.d(TAG, "DB status - Categories: $categoryCount, Accounts: $accountCount, Transactions: $transactionCount")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "База данных пуста. Создайте счета и категории, затем добавьте транзакции.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking DB status: ${e.message}")
            }
        }
    }

    private fun displayTransactions(transactions: List<Transaction>) {
        // Проверяем, что контейнер инициализирован
        if (!::transactionsContainer.isInitialized) {
            Log.e(TAG, "transactionsContainer not initialized!")
            val container = view?.findViewById<LinearLayout>(R.id.transactionsContainer)
            if (container != null) {
                transactionsContainer = container
                Log.d(TAG, "transactionsContainer initialized late")
            } else {
                Log.e(TAG, "Could not find transactionsContainer in view!")
                return
            }
        }

        Log.d(TAG, "displayTransactions called with ${transactions.size} items")

        // Очищаем контейнер
        val removedCount = transactionsContainer.childCount
        transactionsContainer.removeAllViews()
        Log.d(TAG, "Cleared container, removed $removedCount views")

        // Очищаем список
        transactionsList.clear()
        transactionsList.addAll(transactions)
        Log.d(TAG, "Updated transactionsList with ${transactionsList.size} items")

        // Добавляем каждую транзакцию
        if (transactions.isEmpty()) {
            Log.d(TAG, "No transactions to display")
            return
        }

        transactions.forEachIndexed { index, transaction ->
            Log.d(TAG, "Adding transaction $index: ${transaction.id}")
            addTransactionView(transaction, index)
        }

        Log.d(TAG, "Added ${transactions.size} transaction views to container")
        Log.d(TAG, "Container now has ${transactionsContainer.childCount} children")

        // Проверяем видимость
        Log.d(TAG, "Container visibility: ${transactionsContainer.visibility}")
    }

    private fun addTransactionView(transaction: Transaction, index: Int) {
        try {
            // Проверяем контекст
            if (!isAdded) {
                Log.e(TAG, "Fragment not attached to activity")
                return
            }

            // Создаем View для транзакции
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_transaction_simple, null, false)

            Log.d(TAG, "Created view for transaction ${transaction.id}")

            // Настраиваем View
            setupTransactionView(itemView, transaction)

            // Добавляем клик-обработчик
            itemView.setOnClickListener {
                showTransactionDetails(transaction)
            }

            // Добавляем в контейнер
            transactionsContainer.addView(itemView)
            Log.d(TAG, "Added view to container. Container now has ${transactionsContainer.childCount} children")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding transaction view: ${e.message}", e)
        }
    }

    private fun setupTransactionView(view: View, transaction: Transaction) {
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)

        // Форматируем сумму
        val amountText = if (transaction.type == "income") {
            "+${transaction.amount} ₽"
        } else {
            "-${transaction.amount} ₽"
        }
        tvAmount.text = amountText

        // Устанавливаем цвет суммы
        val color = if (transaction.type == "income") {
            requireContext().getColor(R.color.success)
        } else {
            requireContext().getColor(R.color.error)
        }
        tvAmount.setTextColor(color)

        // Форматируем дату
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        tvDate.text = dateFormat.format(transaction.date)

        // Описание (если есть)
        transaction.description?.let {
            tvDescription.text = it
            tvDescription.visibility = View.VISIBLE
        } ?: run {
            tvDescription.visibility = View.GONE
        }

        // Получаем название категории из базы
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val category = transaction.categoryId?.let {
                    database.categoryDao().getCategoryById(it)
                }

                withContext(Dispatchers.Main) {
                    val categoryText = category?.name ?: "Без категории"
                    tvCategory.text = categoryText
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading category: ${e.message}")
                withContext(Dispatchers.Main) {
                    tvCategory.text = "Без категории"
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        val tvEmptyState = view?.findViewById<TextView>(R.id.tvEmptyState)
        val container = view?.findViewById<LinearLayout>(R.id.transactionsContainer)

        if (show) {
            tvEmptyState?.visibility = View.VISIBLE
            container?.visibility = View.GONE
            Log.d(TAG, "Empty state shown")
        } else {
            tvEmptyState?.visibility = View.GONE
            container?.visibility = View.VISIBLE
            Log.d(TAG, "Empty state hidden")
        }
    }

    private fun updateTransactionCount(count: Int) {
        view?.findViewById<TextView>(R.id.tvTransactionCount)?.text =
            "Всего операций: $count"
        Log.d(TAG, "Transaction count updated: $count")
    }

    private fun showTransactionDetails(transaction: Transaction) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val typeText = if (transaction.type == "income") "Доход" else "Расход"
        val description = transaction.description ?: "Без описания"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FinanceDatabase.getDatabase(requireContext())
                val category = transaction.categoryId?.let {
                    database.categoryDao().getCategoryById(it)
                }
                val account = database.accountDao().getAccountById(transaction.accountId)

                val categoryText = category?.name ?: "не указана"
                val accountText = account?.name ?: "неизвестен"

                val message = """
                    $typeText
                    Сумма: ${transaction.amount} ₽
                    Дата: ${dateFormat.format(transaction.date)}
                    Категория: $categoryText
                    Счет: $accountText
                    Описание: $description
                """.trimIndent()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading details: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "$typeText: ${transaction.amount} ₽ (${dateFormat.format(transaction.date)})",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called, refreshing transactions")
        loadTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        if (::transactionsContainer.isInitialized) {
            transactionsContainer.removeAllViews()
        }
        transactionsList.clear()
        Log.d(TAG, "Fragment destroyed, views cleared")
    }
}