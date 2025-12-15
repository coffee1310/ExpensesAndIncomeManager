package com.example.expensesandincomemanager.ui.screens.plan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensesandincomemanager.R
import com.example.expensesandincomemanager.ui.adapters.SavingsGoalsAdapter
import com.example.expensesandincomemanager.ui.screens.savings.AddEditSavingsGoalActivity
import data.entities.SavingsGoal
import data.provider.FinanceRepositoryProvider
import kotlinx.coroutines.launch

class PlanFragment : Fragment() {

    private lateinit var savingsGoalsAdapter: SavingsGoalsAdapter
    private val repository by lazy { FinanceRepositoryProvider.getRepository(requireContext()) }

    // Views
    private lateinit var rvSavingsGoals: androidx.recyclerview.widget.RecyclerView
    private lateinit var emptySavingsGoals: View
    private lateinit var fabAddSavingsGoal: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var tvTotalGoals: android.widget.TextView
    private lateinit var tvTotalSaved: android.widget.TextView
    private lateinit var progressGoals: android.widget.ProgressBar
    private lateinit var tvGoalsProgress: android.widget.TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_plan_simple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupUI()
        setupListeners()
        loadData()
    }

    private fun initViews(view: View) {
        rvSavingsGoals = view.findViewById(R.id.rvSavingsGoals)
        emptySavingsGoals = view.findViewById(R.id.emptySavingsGoals)
        fabAddSavingsGoal = view.findViewById(R.id.fabAddSavingsGoal)
        tvTotalGoals = view.findViewById(R.id.tvTotalGoals)
        tvTotalSaved = view.findViewById(R.id.tvTotalSaved)
        progressGoals = view.findViewById(R.id.progressGoals)
        tvGoalsProgress = view.findViewById(R.id.tvGoalsProgress)
    }

    private fun setupUI() {
        // Настройка RecyclerView для целей накопления
        savingsGoalsAdapter = SavingsGoalsAdapter(
            onEditClick = { goal ->
                navigateToEditGoal(goal.id)
            },
            onDeleteClick = { goal ->
                showDeleteConfirmation(goal)
            },
            onAddMoneyClick = { goal ->
                navigateToEditGoal(goal.id)
            }
        )

        rvSavingsGoals.layoutManager = LinearLayoutManager(requireContext())
        rvSavingsGoals.adapter = savingsGoalsAdapter
    }

    private fun setupListeners() {
        fabAddSavingsGoal.setOnClickListener {
            navigateToAddGoal()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                // Получаем цели из репозитория
                val goals = repository.getAllSavingsGoals()
                savingsGoalsAdapter.submitList(goals)

                // Показываем/скрываем пустое состояние
                if (goals.isEmpty()) {
                    emptySavingsGoals.visibility = View.VISIBLE
                    rvSavingsGoals.visibility = View.GONE
                } else {
                    emptySavingsGoals.visibility = View.GONE
                    rvSavingsGoals.visibility = View.VISIBLE
                }

                updateStatistics(goals)
            } catch (e: Exception) {
                // Обработка ошибок, если таблица еще не создана
                emptySavingsGoals.visibility = View.VISIBLE
                rvSavingsGoals.visibility = View.GONE
                // Для отладки
                e.printStackTrace()
            }
        }
    }

    private fun updateStatistics(goals: List<SavingsGoal>) {
        val totalGoals = goals.filter { !it.isCompleted }.sumOf { it.targetAmount }
        val savedAmount = goals.filter { !it.isCompleted }.sumOf { it.currentAmount }
        val goalsProgress = if (totalGoals > 0) {
            ((savedAmount / totalGoals) * 100).toInt()
        } else {
            0
        }

        tvTotalGoals.text = String.format("%,.0f ₽", totalGoals)
        tvTotalSaved.text = String.format("%,.0f ₽", savedAmount)
        progressGoals.progress = goalsProgress
        tvGoalsProgress.text = "$goalsProgress%"
    }

    private fun navigateToAddGoal() {
        val intent = Intent(requireContext(), AddEditSavingsGoalActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToEditGoal(goalId: Int) {
        val intent = Intent(requireContext(), AddEditSavingsGoalActivity::class.java)
        intent.putExtra("GOAL_ID", goalId)
        startActivity(intent)
    }

    private fun showDeleteConfirmation(goal: SavingsGoal) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удаление цели")
            .setMessage("Вы уверены, что хотите удалить цель \"${goal.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    try {
                        repository.deleteSavingsGoal(goal)
                        loadData()
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Цель удалена",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Ошибка: ${e.message}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возвращении на фрагмент
        loadData()
    }
}