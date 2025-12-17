package com.example.expensesandincomemanager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesandincomemanager.R
import com.google.android.material.button.MaterialButton
import data.entities.SavingsGoal

class SavingsGoalsAdapter(
    private val onEditClick: (SavingsGoal) -> Unit,
    private val onDeleteClick: (SavingsGoal) -> Unit,
    private val onAddMoneyClick: (SavingsGoal) -> Unit
) : ListAdapter<SavingsGoal, SavingsGoalsAdapter.ViewHolder>(SavingsGoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_savings_goal_simple, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = getItem(position)
        holder.bind(goal)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGoalName: TextView = itemView.findViewById(R.id.tvGoalName)
        private val tvTargetAmount: TextView = itemView.findViewById(R.id.tvTargetAmount)
        private val tvCurrentAmount: TextView = itemView.findViewById(R.id.tvCurrentAmount)
        private val tvProgressText: TextView = itemView.findViewById(R.id.tvProgressPercent)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit) // Было btnAddMoney
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        // Новые поля
        private val tvGoalStatus: TextView = itemView.findViewById(R.id.tvGoalStatus)
        private val tvRemainingText: TextView = itemView.findViewById(R.id.tvRemainingText)
        private val tvTargetDate: TextView = itemView.findViewById(R.id.tvTargetDate)
        private val actionButtons: ViewGroup? = itemView.findViewById(R.id.actionButtons)

        init {
            btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
            }

            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }

            // Клик по всей карточке вызывает добавление денег
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddMoneyClick(getItem(position))
                }
            }

            // Долгое нажатие показывает кнопки действий
            itemView.setOnLongClickListener {
                actionButtons?.visibility = View.VISIBLE
                true
            }
        }

        fun bind(goal: SavingsGoal) {
            tvGoalName.text = goal.name
            tvTargetAmount.text = String.format("%,.0f ₽", goal.targetAmount)
            tvCurrentAmount.text = String.format("%,.0f ₽", goal.currentAmount)

            // Расчет прогресса
            val progress = if (goal.targetAmount > 0) {
                ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            progressBar.progress = progress
            tvProgressText.text = "$progress%"

            // Оставшаяся сумма
            val remaining = goal.targetAmount - goal.currentAmount
            tvRemainingText.text = if (remaining >= 0) {
                "Осталось: ${String.format("%,.0f ₽", remaining)}"
            } else {
                "Превышено на: ${String.format("%,.0f ₽", -remaining)}"
            }

            // Статус цели
            val statusText = when {
                goal.isCompleted || progress >= 100 -> "Выполнено"
                progress >= 75 -> "Почти готово"
                else -> "В процессе"
            }
            tvGoalStatus.text = statusText

            // Дата цели
            goal.targetDate?.let { date ->
                val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                tvTargetDate.text = dateFormat.format(date)
            } ?: run {
                tvTargetDate.text = "Без срока"
            }

            // Скрываем кнопки действий по умолчанию
            actionButtons?.visibility = View.GONE
        }
    }
}

class SavingsGoalDiffCallback : DiffUtil.ItemCallback<SavingsGoal>() {
    override fun areItemsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal): Boolean {
        return oldItem == newItem
    }
}