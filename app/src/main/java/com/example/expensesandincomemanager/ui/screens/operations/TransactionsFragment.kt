package com.example.expensesandincomemanager.ui.screens.operations


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensesandincomemanager.R
import data.database.FinanceDatabase
import data.entities.Transaction
import com.example.expensesandincomemanager.ui.adapters.TransactionsAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {

    private lateinit var adapter: TransactionsAdapter
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
        val recyclerView = view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)

        adapter = TransactionsAdapter(transactionsList) { transaction ->
            showTransactionDetails(transaction)
        }

        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        Log.d(TAG, "RecyclerView setup complete. Adapter set: ${recyclerView?.adapter != null}")
    }

    private fun setupEmptyState() {
        val tvEmptyState = view?.findViewById<android.widget.TextView>(R.id.tvEmptyState)
        tvEmptyState?.visibility = View.GONE
    }

    private fun setupRefreshButton() {
        view?.findViewById<android.widget.Button>(R.id.btnRefresh)?.setOnClickListener {
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

                // Сначала проверим количество транзакций
                val count = database.transactionDao().getTransactionCount()
                Log.d(TAG, "Transaction count in database: $count")

                if (count > 0) {
                    // Загружаем ВСЕ транзакции
                    Log.d(TAG, "Loading all transactions...")
                    val transactions = database.transactionDao().getAllTransactions()
                    Log.d(TAG, "Loaded ${transactions.size} transactions")

                    if (transactions.isNotEmpty()) {
                        // Логируем информацию о каждой транзакции
                        transactions.forEachIndexed { index, transaction ->
                            Log.d(TAG, "Transaction $index: ID=${transaction.id}, Type=${transaction.type}, Amount=${transaction.amount}, Date=${transaction.date}")
                        }

                        // Обновляем список
                        transactionsList.clear()
                        transactionsList.addAll(transactions)

                        // Обновляем UI
                        launch {
                            adapter.updateData(transactions)
                            updateTransactionCount(transactions.size)
                            showEmptyState(false)
                            Log.d(TAG, "Adapter updated with ${transactions.size} items")
                        }
                    } else {
                        Log.d(TAG, "No transactions returned from database")
                        showEmptyState(true)
                        updateTransactionCount(0)
                    }
                } else {
                    Log.d(TAG, "Database is empty (count = 0)")
                    showEmptyState(true)
                    updateTransactionCount(0)
                    Toast.makeText(requireContext(), "База данных пуста. Добавьте транзакции на главном экране.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading transactions: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                showEmptyState(true)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        view?.findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        val tvEmptyState = view?.findViewById<android.widget.TextView>(R.id.tvEmptyState)
        val recyclerView = view?.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)

        if (show) {
            tvEmptyState?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
            Log.d(TAG, "Empty state shown")
        } else {
            tvEmptyState?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
            Log.d(TAG, "Empty state hidden")
        }
    }

    private fun updateTransactionCount(count: Int) {
        view?.findViewById<android.widget.TextView>(R.id.tvTransactionCount)?.text =
            "Всего операций: $count"
        Log.d(TAG, "Transaction count updated: $count")
    }

    private fun showTransactionDetails(transaction: Transaction) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val typeText = if (transaction.type == "income") "Доход" else "Расход"
        val description = transaction.description ?: "Без описания"

        Toast.makeText(
            requireContext(),
            "$typeText\nСумма: ${transaction.amount} ₽\nДата: ${dateFormat.format(transaction.date)}\nОписание: $description",
            Toast.LENGTH_LONG
        ).show()

        Log.d(TAG, "Transaction details shown: ID=${transaction.id}, Type=$typeText")
    }
}