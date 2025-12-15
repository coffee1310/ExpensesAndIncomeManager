package com.example.expensesandincomemanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.expensesandincomemanager.ui.screens.home.HomeFragment
import com.example.expensesandincomemanager.ui.screens.operations.TransactionsFragment
import com.example.expensesandincomemanager.ui.screens.plan.PlanFragment
import com.example.expensesandincomemanager.ui.screens.transactions.AddTransactionFragment
import data.initial.InitialData

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")

        setupNavigation()
        InitialData.insertInitialData(this)

        if (savedInstanceState == null) {
            // Загружаем HomeFragment как начальный экран
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
    }

    private fun setupNavigation() {
        // Навигация через нижнее меню
        findViewById<android.view.View>(R.id.btn_transactions)?.setOnClickListener {
            navigateToTransactions()
        }

        findViewById<android.view.View>(R.id.btn_report)?.setOnClickListener {
            navigateToHome()
        }

        // Фиолетовая кнопка + для добавления транзакции
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add)?.setOnClickListener {
            navigateToAddTransaction()
        }

        findViewById<android.view.View>(R.id.btn_plan)?.setOnClickListener {
            navigateToPlan()
        }

        findViewById<android.view.View>(R.id.btn_settings)?.setOnClickListener {
            showComingSoon("Настройки")
        }
    }

    private fun navigateToHome() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToTransactions() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, TransactionsFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToAddTransaction() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AddTransactionFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToPlan() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, PlanFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // При возвращении в приложение обновляем данные
        val homeFragment = supportFragmentManager.findFragmentById(R.id.container) as? HomeFragment
        homeFragment?.onResume()
    }

    private fun showComingSoon(featureName: String) {
        android.widget.Toast.makeText(
            this,
            "$featureName - в разработке",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}