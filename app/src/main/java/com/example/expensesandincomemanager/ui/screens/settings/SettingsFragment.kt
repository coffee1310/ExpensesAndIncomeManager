package com.example.expensesandincomemanager.ui.screens.settings


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.expensesandincomemanager.R
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupClickListeners(view)
    }

    private fun setupViews(view: View) {
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserEmail = view.findViewById(R.id.tv_user_email)

        tvUserName.text = "Алексей"
        tvUserEmail.text = "user@example.com"
    }

    private fun setupClickListeners(view: View) {
        // Настройка профиля
        view.findViewById<MaterialCardView>(R.id.card_profile).setOnClickListener {
            // TODO: Перейти на экран редактирования профиля
        }

        // Добавить категорию
        view.findViewById<MaterialCardView>(R.id.card_add_category).setOnClickListener {
            showAddCategoryDialog()
        }

        // Сменить валюту
        view.findViewById<MaterialCardView>(R.id.card_change_currency).setOnClickListener {
            showChangeCurrencyDialog()
        }

        // Тема приложения
        view.findViewById<MaterialCardView>(R.id.card_theme).setOnClickListener {
            showThemeDialog()
        }

        // Уведомления
        view.findViewById<MaterialCardView>(R.id.card_notifications).setOnClickListener {
            showNotificationsDialog()
        }

        // Экспорт данных
        view.findViewById<MaterialCardView>(R.id.card_export_data).setOnClickListener {
            exportData()
        }

        // О приложении
        view.findViewById<MaterialCardView>(R.id.card_about).setOnClickListener {
            showAboutDialog()
        }

        // Выйти
        view.findViewById<MaterialCardView>(R.id.card_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_add_category_dialog, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Добавить категорию")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                // TODO: Обработка добавления категории
                showToast("Категория добавлена")
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    private fun showChangeCurrencyDialog() {
        val currencies = arrayOf(
            "Российский рубль (₽)",
            "Доллар США ($)",
            "Евро (€)",
            "Тенге (₸)",
            "Белорусский рубль (Br)",
            "Гривна (₴)"
        )

        val currencyCodes = arrayOf("RUB", "USD", "EUR", "KZT", "BYN", "UAH")
        val currentCurrency = getCurrentCurrency()
        var selectedIndex = currencyCodes.indexOf(currentCurrency)
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите валюту")
            .setSingleChoiceItems(currencies, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Сохранить") { dialog, _ ->
                if (selectedIndex != -1) {
                    changeCurrency(currencyCodes[selectedIndex])
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun getCurrentCurrency(): String {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getString("currency", "RUB") ?: "RUB"
    }

    private fun changeCurrency(newCurrency: String) {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("currency", newCurrency).apply()
        showToast("Валюта изменена")
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Светлая", "Тёмная", "Системная")
        var currentThemeIndex = getCurrentThemeIndex()

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите тему")
            .setSingleChoiceItems(themes, currentThemeIndex) { _, which ->
                currentThemeIndex = which
            }
            .setPositiveButton("Применить") { dialog, _ ->
                changeTheme(currentThemeIndex)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun getCurrentThemeIndex(): Int {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getInt("theme", 2) // По умолчанию системная
    }

    private fun changeTheme(themeIndex: Int) {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putInt("theme", themeIndex).apply()
        showToast("Тема изменена. Перезапустите приложение")
    }

    private fun showNotificationsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_add_category_dialog, null)

        AlertDialog.Builder(requireContext())
            .setTitle("Настройка уведомлений")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                showToast("Время уведомлений сохранено")
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun exportData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Экспорт данных")
            .setMessage("Выберите формат экспорта:")
            .setPositiveButton("CSV") { _, _ ->
                exportToCSV()
            }
            .setNegativeButton("PDF") { _, _ ->
                exportToPDF()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    private fun exportToCSV() {
        runBlocking {
            launch {
                showToast("Данные экспортированы в CSV")
            }
        }
    }

    private fun exportToPDF() {
        runBlocking {
            launch {
                showToast("Данные экспортированы в PDF")
            }
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("О приложении")
            .setMessage("""
                Учёт расходов и доходов
                
                Версия: 1.0.0
                Разработчик: Ваша компания
                
                Приложение для управления личными финансами.
                Отслеживайте расходы, планируйте бюджет,
                достигайте финансовых целей.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                logout()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        showToast("Вы вышли из аккаунта")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}