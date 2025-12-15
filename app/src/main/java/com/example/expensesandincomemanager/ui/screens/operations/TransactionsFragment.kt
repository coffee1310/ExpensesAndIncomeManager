package com.example.expensesandincomemanager.ui.screens.operations

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
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
        setupRecyclerView()
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

    private fun setupRecyclerView() {
        recyclerView = view?.findViewById(R.id.recyclerView) ?: return
        adapter = TransactionAdapter(emptyList()) { transaction ->
            showTransactionDetails(transaction)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        Log.d(TAG, "RecyclerView setup complete")
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

                // Получаем транзакции
                val transactions = withContext(Dispatchers.IO) {
                    try {
                        // Пробуем получить как suspend метод
                        database.transactionDao().getAllTransactions()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting transactions: ${e.message}")
                        emptyList()
                    }
                }

                Log.d(TAG, "Loaded ${transactions.size} transactions")

                if (transactions.isNotEmpty()) {
                    // Обновляем UI в главном потоке
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Displaying ${transactions.size} transactions in UI")
                        adapter.updateData(transactions)
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
                    val accountCount = database.accountDao().getCount() // Используйте getCount()

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

    private fun showLoading(show: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        val tvEmptyState = view?.findViewById<TextView>(R.id.tvEmptyState)
        val container = view?.findViewById<RecyclerView>(R.id.recyclerView)

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
        // Открываем фрагмент редактирования
        val editFragment = EditTransactionFragment.newInstance(transaction.id)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, editFragment)
            .addToBackStack("edit_transaction")
            .commit()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called, refreshing transactions")
        loadTransactions()
    }
}

// Простой адаптер для RecyclerView
class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
        holder.itemView.setOnClickListener { onItemClick(transaction) }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val ivTypeIcon: View = itemView.findViewById(R.id.typeIndicator)

        fun bind(transaction: Transaction) {
            // Форматируем сумму
            val amountText = if (transaction.type == "income") {
                "+${transaction.amount} ₽"
            } else {
                "-${transaction.amount} ₽"
            }
            tvAmount.text = amountText

            // Устанавливаем цвет суммы
            val color = if (transaction.type == "income") {
                itemView.context.getColor(R.color.success)
            } else {
                itemView.context.getColor(R.color.error)
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

            // TODO: Загрузить название категории из базы
            tvCategory.text = "Категория ${transaction.categoryId ?: 0}"

            // Индикатор типа (цветной круг)
            val indicatorColor = if (transaction.type == "income") {
                itemView.context.getColor(R.color.success)
            } else {
                itemView.context.getColor(R.color.error)
            }
            ivTypeIcon.setBackgroundColor(indicatorColor)
        }
    }
}