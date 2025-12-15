package com.example.expensesandincomemanager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesandincomemanager.R
import com.google.android.material.button.MaterialButton
import data.entities.ExpensePlan
import kotlin.math.roundToInt

class ExpensePlanAdapter(
    private val onEditClick: (ExpensePlan) -> Unit,
    private val onDeleteClick: (ExpensePlan) -> Unit
) : ListAdapter<ExpensePlan, ExpensePlanAdapter.ViewHolder>(ExpensePlanDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense_plan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plan = getItem(position)
        holder.bind(plan)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvSpent: TextView = itemView.findViewById(R.id.tvSpent)
        private val tvRemaining: TextView = itemView.findViewById(R.id.tvRemaining)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

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
        }

        fun bind(plan: ExpensePlan) {
            tvCategoryName.text = plan.categoryName
            tvAmount.text = String.format("%,.0f ₽", plan.amount)
            tvSpent.text = String.format("%,.0f ₽", plan.spentAmount)
            tvRemaining.text = String.format("%,.0f ₽", plan.remainingAmount)

            // Расчет прогресса
            val progress = if (plan.amount > 0) {
                ((plan.spentAmount / plan.amount) * 100).toInt()
            } else {
                0
            }
            progressBar.progress = progress
            tvProgress.text = "$progress%"

            // Цвет оставшейся суммы
            val remainingColor = if (plan.remainingAmount >= 0) {
                itemView.context.getColor(R.color.success)
            } else {
                itemView.context.getColor(R.color.error)
            }
            tvRemaining.setTextColor(remainingColor)
        }
    }
}

class ExpensePlanDiffCallback : DiffUtil.ItemCallback<ExpensePlan>() {
    override fun areItemsTheSame(oldItem: ExpensePlan, newItem: ExpensePlan): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ExpensePlan, newItem: ExpensePlan): Boolean {
        return oldItem == newItem
    }
}