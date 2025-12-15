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
        private val tvProgressText: TextView = itemView.findViewById(R.id.tvProgressText)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        private val btnAddMoney: MaterialButton = itemView.findViewById(R.id.btnAddMoney)

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

            btnAddMoney.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddMoneyClick(getItem(position))
                }
            }
        }

        fun bind(goal: SavingsGoal) {
            tvGoalName.text = goal.name
            tvTargetAmount.text = String.format("%,.0f ₽", goal.targetAmount)
            tvCurrentAmount.text = String.format("%,.0f ₽", goal.currentAmount)

            // Расчет прогресса
            val progress = if (goal.targetAmount > 0) {
                ((goal.currentAmount / goal.targetAmount) * 100).toInt()
            } else {
                0
            }
            progressBar.progress = progress
            tvProgressText.text = "$progress%"

            // Кнопка добавления денег
            btnAddMoney.isEnabled = !goal.isCompleted && progress < 100
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