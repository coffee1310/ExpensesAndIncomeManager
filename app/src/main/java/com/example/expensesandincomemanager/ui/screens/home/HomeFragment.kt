package com.example.expensesandincomemanager.ui.screens.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.expensesandincomemanager.R
import data.models.ExpenseCategory
import java.text.NumberFormat
import java.util.*

class HomeFragment : Fragment() {

    private var rootView: View? = null

    private var selectedChartType = "pie"
    private var selectedMonthIndex = 0

    private val expenseCategories = listOf(
        ExpenseCategory(
            id = 1,
            name = "Продукты",
            amount = 15000.0,
            percentage = 30f,
            color = Color.parseColor("#FF6B6B")
        ),
        ExpenseCategory(
            id = 2,
            name = "Транспорт",
            amount = 8000.0,
            percentage = 16f,
            color = Color.parseColor("#5856D6")
        ),
        ExpenseCategory(
            id = 3,
            name = "Развлечения",
            amount = 7000.0,
            percentage = 14f,
            color = Color.parseColor("#FFD166")
        ),
        ExpenseCategory(
            id = 4,
            name = "Кафе",
            amount = 6000.0,
            percentage = 12f,
            color = Color.parseColor("#06D6A0")
        ),
        ExpenseCategory(
            id = 5,
            name = "Коммуналка",
            amount = 5000.0,
            percentage = 10f,
            color = Color.parseColor("#118AB2")
        ),
        ExpenseCategory(
            id = 6,
            name = "Прочее",
            amount = 9000.0,
            percentage = 18f,
            color = Color.parseColor("#9B5DE5")
        )
    )

    private val monthlyData = listOf(
        MonthData("Янв", 2024, true),
        MonthData("Фев", 2024),
        MonthData("Мар", 2024),
        MonthData("Апр", 2024),
        MonthData("Май", 2024),
        MonthData("Июн", 2024),
        MonthData("Июл", 2024)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.home_screen, container, false)
        return rootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChartTypeSelector()
        setupMonthSelector()
        setupPieChart()
        setupCategoryLegend()
    }

    private fun setupChartTypeSelector() {
        rootView?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_pie_chart)?.setOnClickListener {
            selectChartType("pie")
        }

        rootView?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_bar_chart)?.setOnClickListener {
            selectChartType("bar")
        }

        selectChartType("pie")
    }

    private fun selectChartType(type: String) {
        selectedChartType = type

        val primaryColor = requireContext().getColor(R.color.primary)
        val outlineColor = requireContext().getColor(R.color.outline)

        val btnPieChart = rootView?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_pie_chart)
        val btnBarChart = rootView?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_bar_chart)
        val pieChartContainer = rootView?.findViewById<android.view.View>(R.id.pie_chart_container)

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

    private fun setupMonthSelector() {
        val monthContainer = rootView?.findViewById<android.view.ViewGroup>(R.id.month_container)
        monthContainer?.removeAllViews()

        monthlyData.forEachIndexed { index, monthData ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_month_card, monthContainer, false)

            val tvMonth = itemView.findViewById<android.widget.TextView>(R.id.tv_month)
            val tvYear = itemView.findViewById<android.widget.TextView>(R.id.tv_year)
            val cardView = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_view)

            tvMonth.text = monthData.month
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
                updateMonthSelection(index)
            }

            monthContainer?.addView(itemView)
        }
    }

    private fun updateMonthSelection(selectedIndex: Int) {
        selectedMonthIndex = selectedIndex
        setupMonthSelector()
    }

    private fun setupPieChart() {
        val totalExpense = expenseCategories.sumOf { it.amount }
        val totalIncome = 80000.0
        val balance = totalIncome - totalExpense

        val doughnutChart = rootView?.findViewById<com.example.expensesandincomemanager.ui.components.DoughnutChartView>(R.id.doughnut_chart)
        val tvTotalAmount = rootView?.findViewById<android.widget.TextView>(R.id.tv_total_amount)
        val tvBalance = rootView?.findViewById<android.widget.TextView>(R.id.tv_balance)

        doughnutChart?.setData(expenseCategories)
        tvTotalAmount?.text = formatCurrency(totalExpense)

        val balanceText = "Баланс: ${formatCurrency(balance)}"
        tvBalance?.text = balanceText

        if (balance >= 0) {
            tvBalance?.setTextColor(requireContext().getColor(R.color.success))
        } else {
            tvBalance?.setTextColor(requireContext().getColor(R.color.error))
        }
    }

    private fun setupCategoryLegend() {
        val categoryLegend = rootView?.findViewById<android.view.ViewGroup>(R.id.category_legend)
        categoryLegend?.removeAllViews()

        // Добавляем заголовок
        val titleView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, categoryLegend, false)
        val titleTextView = titleView.findViewById<android.widget.TextView>(android.R.id.text1)
        titleTextView.text = "Расходы по категориям"
        titleTextView.textSize = 18f
        titleTextView.setTextColor(requireContext().getColor(R.color.on_surface))
        titleTextView.setTypeface(null, android.graphics.Typeface.BOLD)

        categoryLegend?.addView(titleView)

        // Добавляем категории
        expenseCategories.forEach { category ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.category_legend_item, categoryLegend, false)

            val colorIndicator = itemView.findViewById<android.view.View>(R.id.color_indicator)
            val tvName = itemView.findViewById<android.widget.TextView>(R.id.tv_category_name)
            val tvPercentage = itemView.findViewById<android.widget.TextView>(R.id.tv_category_percentage)
            val tvAmount = itemView.findViewById<android.widget.TextView>(R.id.tv_category_amount)

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
        // TODO
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

    data class MonthData(
        val month: String,
        val year: Int,
        val isSelected: Boolean = false
    )
}