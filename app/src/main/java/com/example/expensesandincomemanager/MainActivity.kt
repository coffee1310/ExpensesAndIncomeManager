package com.example.expensesandincomemanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.expensesandincomemanager.ui.screens.home.HomeFragment
import com.example.expensesandincomemanager.ui.screens.operations.TransactionsFragment
import com.example.expensesandincomemanager.ui.screens.plan.PlanFragment
import com.example.expensesandincomemanager.ui.screens.settings.SettingsFragment
import com.example.expensesandincomemanager.ui.screens.transactions.AddTransactionFragment
import data.initial.InitialData

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    private var currentTab: String = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")

        setupNavigation()
        InitialData.insertInitialData(this)

        if (savedInstanceState == null) {
            navigateToHome()
        }
    }

    private fun setupNavigation() {
        // Находим контейнер нижней навигации
        val bottomNavigation = findViewById<android.view.ViewGroup>(R.id.bottom_navigation)

        // Навигация через нижнее меню
        bottomNavigation.findViewById<android.view.View>(R.id.btn_transactions)?.setOnClickListener {
            navigateToTransactions()
        }

        bottomNavigation.findViewById<android.view.View>(R.id.btn_report)?.setOnClickListener {
            navigateToHome()
        }

        // Фиолетовая кнопка + для добавления транзакции
        bottomNavigation.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add)?.setOnClickListener {
            navigateToAddTransaction()
        }

        bottomNavigation.findViewById<android.view.View>(R.id.btn_plan)?.setOnClickListener {
            navigateToPlan()
        }

        bottomNavigation.findViewById<android.view.View>(R.id.btn_settings)?.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun navigateToHome() {
        setActiveTab("home")
        replaceFragment(HomeFragment(), "home")
    }

    private fun navigateToTransactions() {
        setActiveTab("transactions")
        replaceFragment(TransactionsFragment(), "transactions")
    }

    private fun navigateToAddTransaction() {
        // При открытии формы добавления транзакции сбрасываем выделение вкладки
        setActiveTab(null)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AddTransactionFragment())
            .addToBackStack("add_transaction")
            .commit()
    }

    private fun navigateToPlan() {
        setActiveTab("plan")
        replaceFragment(PlanFragment(), "plan")
    }

    private fun navigateToSettings() {
        setActiveTab("settings")
        replaceFragment(SettingsFragment(), "settings")
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(tag)
            .commit()
    }

    private fun setActiveTab(tab: String?) {
        currentTab = tab ?: ""

        // Находим контейнер нижней навигации
        val bottomNavigation = findViewById<android.view.ViewGroup>(R.id.bottom_navigation)

        // Функция для установки состояния вкладки
        fun setTabState(containerId: Int, isActive: Boolean) {
            val container = bottomNavigation.findViewById<android.view.ViewGroup>(containerId)
            for (i in 0 until container.childCount) {
                val child = container.getChildAt(i)
                when (child) {
                    is android.widget.ImageView -> {
                        child.setColorFilter(ContextCompat.getColor(this,
                            if (isActive) R.color.nav_icon_active else R.color.nav_icon_inactive))
                    }
                    is android.widget.TextView -> {
                        child.setTextColor(ContextCompat.getColor(this,
                            if (isActive) R.color.nav_text_active else R.color.nav_text_inactive))
                    }
                }
            }
        }

        // Сбрасываем все вкладки в неактивное состояние
        setTabState(R.id.btn_transactions, false)
        setTabState(R.id.btn_report, false)
        setTabState(R.id.btn_plan, false)
        setTabState(R.id.btn_settings, false)

        // Устанавливаем активную вкладку
        when (tab) {
            "home" -> setTabState(R.id.btn_report, true)
            "transactions" -> setTabState(R.id.btn_transactions, true)
            "plan" -> setTabState(R.id.btn_plan, true)
            "settings" -> setTabState(R.id.btn_settings, true)
        }
    }

    override fun onBackPressed() {
        // Получаем текущий фрагмент
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

        // Проверяем, является ли текущий фрагмент одним из основных
        when (currentFragment) {
            is HomeFragment -> currentTab = "home"
            is TransactionsFragment -> currentTab = "transactions"
            is PlanFragment -> currentTab = "plan"
            is SettingsFragment -> currentTab = "settings"
            else -> currentTab = ""
        }

        // Обновляем выделение вкладки
        setActiveTab(currentTab)

        // Если в стеке есть фрагменты, используем стандартное поведение
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else {
            // Если стек пуст, выходим из приложения
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // При возвращении в приложение обновляем выделение вкладки
        if (currentTab.isNotEmpty()) {
            setActiveTab(currentTab)
        }

        // Обновляем данные в HomeFragment
        val homeFragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
        homeFragment?.onResume()
    }
}