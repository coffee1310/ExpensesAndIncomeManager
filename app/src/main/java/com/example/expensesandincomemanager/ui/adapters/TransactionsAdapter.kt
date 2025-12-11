package com.example.expensesandincomemanager.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesandincomemanager.R
import data.entities.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsAdapter(
    private var transactions: List<Transaction> = emptyList(), // Дефолтное значение
    private val onItemClick: (Transaction) -> Unit = {} // Дефолтное значение
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    init {
        Log.d("TransactionsAdapter", "Adapter created with ${transactions.size} items")
    }

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
        Log.d("TransactionsAdapter", "Data updated with ${newTransactions.size} items")
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