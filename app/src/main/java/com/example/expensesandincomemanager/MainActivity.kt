package com.example.expensesandincomemanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.expensesandincomemanager.ui.screens.home.AddTransactionFragment
import com.example.expensesandincomemanager.ui.screens.home.HomeFragment
import data.initial.InitialData

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Вставляем начальные данные
        InitialData.insertInitialData(this)

        // Проверяем, не восстанавливаем ли мы состояние
        if (savedInstanceState == null) {
            // Загружаем HomeFragment как начальный экран
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        setupNavigation()
    }

    private fun setupNavigation() {
        // Навигация через нижнее меню
        findViewById<android.view.View>(R.id.btn_transactions)?.setOnClickListener {
            // TODO: Реализовать переход к операциям
            showToast("Операции - в разработке")
        }

        findViewById<android.view.View>(R.id.btn_report)?.setOnClickListener {
            // Возвращаемся на главную
            navigateToHome()
        }

        // Фиолетовая кнопка + для добавления транзакции
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add)?.setOnClickListener {
            navigateToAddTransaction()
        }

        findViewById<android.view.View>(R.id.btn_plan)?.setOnClickListener {
            showToast("Планирование - в разработке")
        }

        findViewById<android.view.View>(R.id.btn_settings)?.setOnClickListener {
            showToast("Настройки - в разработке")
        }
    }

    private fun navigateToHome() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToAddTransaction() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AddTransactionFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}