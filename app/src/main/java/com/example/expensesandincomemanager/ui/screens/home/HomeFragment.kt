package com.example.expensesandincomemanager.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.expensesandincomemanager.R
import com.example.expensesandincomemanager.ui.components.DoughnutChartView
import com.google.android.material.card.MaterialCardView
import data.models.ExpenseCategory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class HomeFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.home_screen, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(requireContext())
        ).get(HomeViewModel::class.java)

        setupChartTypeSelector()
        setupObservers()

        // Наблюдаем за обновлениями транзакций
        viewModel.transactionsUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                // Принудительно обновляем данные
                viewModel.refreshData()
            }
        }
    }

    private fun setupChartTypeSelector() {
        rootView?.findViewById<MaterialCardView>(R.id.btn_pie_chart)?.setOnClickListener {
            selectChartType("pie")
        }

        rootView?.findViewById<MaterialCardView>(R.id.btn_bar_chart)?.setOnClickListener {
            selectChartType("bar")
        }

        selectChartType("pie")
    }

    private fun selectChartType(type: String) {
        val primaryColor = requireContext().getColor(R.color.primary)
        val outlineColor = requireContext().getColor(R.color.outline)

        val btnPieChart = rootView?.findViewById<MaterialCardView>(R.id.btn_pie_chart)
        val btnBarChart = rootView?.findViewById<MaterialCardView>(R.id.btn_bar_chart)
        val pieChartContainer = rootView?.findViewById<View>(R.id.pie_chart_container)

        if (type == "pie") {
            btnPieChart?.strokeWidth = 2
            btnPieChart?.strokeColor = primaryColor
            btnBarChart?.strokeWidth = 1
            btnBarChart?.strokeColor = outlineColor
            pieChartContainer?.visibility = View.VISIBLE
        } else {
            btnBarChart?.strokeWidth = 2
            btnBarChart?.strokeColor = primaryColor
            btnPieChart?.strokeWidth = 1
            btnPieChart?.strokeColor = outlineColor
            pieChartContainer?.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        // Наблюдаем за месячными данными
        lifecycleScope.launch {
            viewModel.monthlyData.collect { months ->
                months.let { updateMonthUI(it) }
            }
        }

        // Наблюдаем за данными категорий
        lifecycleScope.launch {
            viewModel.expenseCategories.collect { categories ->
                updatePieChart(categories)
                updateCategoryLegend(categories)
            }
        }

        // Наблюдаем за общими суммами
        lifecycleScope.launch {
            viewModel.totalExpense.collect { totalExpense ->
                val totalIncome = viewModel.totalIncome.value
                val balance = viewModel.balance.value
                updateTotalAmounts(totalIncome, totalExpense, balance)
            }
        }

        // Наблюдаем за состоянием загрузки
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // TODO: Показать/скрыть индикатор загрузки
            }
        }
    }

    private fun updateMonthUI(months: List<HomeViewModel.MonthData>) {
        val monthContainer = rootView?.findViewById<ViewGroup>(R.id.month_container)
        monthContainer?.removeAllViews()

        months.forEachIndexed { index, monthData ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_month_card, monthContainer, false)

            val tvMonth = itemView.findViewById<TextView>(R.id.tv_month)
            val tvYear = itemView.findViewById<TextView>(R.id.tv_year)
            val cardView = itemView.findViewById<MaterialCardView>(R.id.card_view)

            tvMonth.text = monthData.monthName
            tvYear.text = monthData.year.toString()

            val primaryColor = requireContext().getColor(R.color.primary)
            val onSurfaceColor = requireContext().getColor(R.color.on_surface)
            val onSurfaceVariantColor = requireContext().getColor(R.color.on_surface_variant)

            if (monthData.isSelected) {
                cardView.strokeWidth = 2
                cardView.strokeColor = primaryColor
                tvMonth.setTextColor(primaryColor)
                tvYear.setTextColor(primaryColor)
            } else {
                cardView.strokeWidth = 1
                cardView.strokeColor = requireContext().getColor(R.color.outline)
                tvMonth.setTextColor(onSurfaceColor)
                tvYear.setTextColor(onSurfaceVariantColor)
            }

            itemView.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.loadDataForMonth(monthData.year, monthData.monthNumber)
                    // И обновить выделение месяца локально
                    updateMonthSelection(index)
                }
            }

            monthContainer?.addView(itemView)
        }
    }

    private fun updateMonthSelection(selectedIndex: Int) {
        val monthContainer = rootView?.findViewById<ViewGroup>(R.id.month_container)
        monthContainer?.let { container ->
            for (i in 0 until container.childCount) {
                val itemView = container.getChildAt(i)
                val tvMonth = itemView.findViewById<TextView>(R.id.tv_month)
                val tvYear = itemView.findViewById<TextView>(R.id.tv_year)
                val cardView = itemView.findViewById<MaterialCardView>(R.id.card_view)

                val primaryColor = requireContext().getColor(R.color.primary)
                val onSurfaceColor = requireContext().getColor(R.color.on_surface)
                val onSurfaceVariantColor = requireContext().getColor(R.color.on_surface_variant)

                if (i == selectedIndex) {
                    cardView.strokeWidth = 2
                    cardView.strokeColor = primaryColor
                    tvMonth.setTextColor(primaryColor)
                    tvYear.setTextColor(primaryColor)
                } else {
                    cardView.strokeWidth = 1
                    cardView.strokeColor = requireContext().getColor(R.color.outline)
                    tvMonth.setTextColor(onSurfaceColor)
                    tvYear.setTextColor(onSurfaceVariantColor)
                }
            }
        }
    }


    private fun updatePieChart(categories: List<HomeViewModel.ExpenseCategoryUI>) {
        val doughnutChart = rootView?.findViewById<DoughnutChartView>(R.id.doughnut_chart)

        val expenseCategories = categories.map {
            ExpenseCategory(
                id = it.id,
                name = it.name,
                amount = it.amount,
                percentage = it.percentage,
                color = it.color
            )
        }

        doughnutChart?.setData(expenseCategories)
    }

    private fun updateTotalAmounts(totalIncome: Double, totalExpense: Double, balance: Double) {
        val tvTotalAmount = rootView?.findViewById<TextView>(R.id.tv_total_amount)
        val tvBalance = rootView?.findViewById<TextView>(R.id.tv_balance)

        tvTotalAmount?.text = formatCurrency(totalExpense)

        val balanceText = "Баланс: ${formatCurrency(balance)}"
        tvBalance?.text = balanceText

        if (balance >= 0) {
            tvBalance?.setTextColor(requireContext().getColor(R.color.success))
        } else {
            tvBalance?.setTextColor(requireContext().getColor(R.color.error))
        }
    }

    private fun updateCategoryLegend(categories: List<HomeViewModel.ExpenseCategoryUI>) {
        val categoryLegend = rootView?.findViewById<ViewGroup>(R.id.category_legend)
        categoryLegend?.removeAllViews()

        // Добавляем заголовок
        val titleView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, categoryLegend, false)
        val titleTextView = titleView.findViewById<TextView>(android.R.id.text1)
        titleTextView.text = "Расходы по категориям"
        titleTextView.textSize = 18f
        titleTextView.setTextColor(requireContext().getColor(R.color.on_surface))
        titleTextView.setTypeface(null, android.graphics.Typeface.BOLD)

        categoryLegend?.addView(titleView)

        // Добавляем категории
        categories.forEach { category ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.category_legend_item, categoryLegend, false)

            val colorIndicator = itemView.findViewById<View>(R.id.color_indicator)
            val tvName = itemView.findViewById<TextView>(R.id.tv_category_name)
            val tvPercentage = itemView.findViewById<TextView>(R.id.tv_category_percentage)
            val tvAmount = itemView.findViewById<TextView>(R.id.tv_category_amount)

            colorIndicator.setBackgroundColor(category.color)
            tvName.text = category.name
            tvPercentage.text = "${category.percentage.toInt()}%"
            tvAmount.text = formatCurrency(category.amount)

            itemView.setOnClickListener {
                navigateToCategoryDetails(category.id)
            }

            categoryLegend?.addView(itemView)
        }
    }

    private fun navigateToCategoryDetails(categoryId: Int) {
        // TODO: Реализовать навигацию к деталям категории
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}