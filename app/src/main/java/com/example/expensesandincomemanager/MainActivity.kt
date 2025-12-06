package com.example.expensesandincomemanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.expensesandincomemanager.ui.screens.home.HomeFragment
import data.initial.InitialData

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
        InitialData.insertInitialData(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
    }

    private fun setupNavigation() {
        findViewById<android.view.View>(R.id.btn_transactions).setOnClickListener {
            navigateToTransactions()
        }

        findViewById<android.view.View>(R.id.btn_report).setOnClickListener {
            // Уже на главной
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add).setOnClickListener {
            navigateToAddTransaction()
        }

        findViewById<android.view.View>(R.id.btn_plan).setOnClickListener {
            navigateToPlan()
        }

        findViewById<android.view.View>(R.id.btn_settings).setOnClickListener {
            navigateToSettings()
        }
    }

    private fun navigateToTransactions() {
        // TODO
    }

    private fun navigateToAddTransaction() {
        // TODO
    }

    private fun navigateToPlan() {
        // TODO
    }

    private fun navigateToSettings() {
        // TODO
    }
}