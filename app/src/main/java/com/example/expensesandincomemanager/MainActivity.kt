package com.example.expensesandincomemanager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensesandincomemanager.ui.screens.home.HomeFragment
import com.example.expensesandincomemanager.ui.screens.operations.TransactionsFragment
import com.example.expensesandincomemanager.ui.screens.transactions.AddTransactionFragment
import data.initial.InitialData
import data.provider.FinanceRepositoryProvider

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")

        // Проверяем контейнер и навигацию
        checkLayout()

        setupNavigation()
        InitialData.insertInitialData(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
    }

    private fun checkLayout() {
        val container = findViewById<ViewGroup>(R.id.container)

        Log.d(TAG, "Контейнер: $container")

        if (container == null) {
            Log.e(TAG, "❌ Контейнер не найден!")
        }

    }

    private fun setupNavigation() {
        findViewById<android.view.View>(R.id.btn_transactions)?.setOnClickListener {
            navigateToTransactions()
        }

        findViewById<android.view.View>(R.id.btn_report)?.setOnClickListener {
            navigateToHome()
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add)?.setOnClickListener {
            navigateToAddTransaction()
        }

        findViewById<android.view.View>(R.id.btn_plan)?.setOnClickListener {
            showComingSoon("Планирование")
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
        Log.d(TAG, "Переход на транзакции")
        try {
            val fragment = TransactionsFragment()
            Log.d(TAG, "Фрагмент создан: ${fragment.javaClass.simpleName}")

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack("transactions")
                .commit()

            Log.d(TAG, "Транзакция коммитнута")

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при переходе на транзакции", e)
        }
    }

    private fun navigateToAddTransaction() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AddTransactionFragment())
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
        Toast.makeText(
            this,
            "$featureName - в разработке",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Очищаем репозиторий
        FinanceRepositoryProvider.cleanup()
    }
}