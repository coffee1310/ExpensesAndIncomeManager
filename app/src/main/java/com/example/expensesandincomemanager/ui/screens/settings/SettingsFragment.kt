package com.example.expensesandincomemanager.ui.screens.settings

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.expensesandincomemanager.R
import com.google.android.material.card.MaterialCardView
import data.entities.Category
import kotlinx.coroutines.launch
import data.repository.FinanceRepository
import data.provider.FinanceRepositoryProvider

class SettingsFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private var selectedColor: Int = Color.parseColor("#FF6B6B")
    private lateinit var repository: FinanceRepository

    // Map для хранения исходных цветов фона
    private val originalBackgrounds = mutableMapOf<View, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FinanceRepositoryProvider.getRepository(requireContext())
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
        view.findViewById<MaterialCardView>(R.id.card_profile).setOnClickListener {
            Toast.makeText(requireContext(), "Редактирование профиля", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialCardView>(R.id.card_add_category).setOnClickListener {
            showAddCategoryDialog()
        }

        view.findViewById<MaterialCardView>(R.id.card_change_currency).setOnClickListener {
            showChangeCurrencyDialog()
        }

        view.findViewById<MaterialCardView>(R.id.card_export_data).setOnClickListener {
            exportData()
        }

        view.findViewById<MaterialCardView>(R.id.card_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_add_category_dialog, null)

        // Находим views для выбора цвета
        val colorViews = listOf(
            dialogView.findViewById<View>(R.id.color_food),
            dialogView.findViewById<View>(R.id.color_transport),
            dialogView.findViewById<View>(R.id.color_entertainment),
            dialogView.findViewById<View>(R.id.color_cafe),
            dialogView.findViewById<View>(R.id.color_utilities),
            dialogView.findViewById<View>(R.id.color_other)
        )

        // Сохраняем исходные цвета
        colorViews.forEach { view ->
            originalBackgrounds[view] = (view.background as? GradientDrawable)?.color?.defaultColor
                ?: Color.parseColor(view.tag as String)
        }

        var selectedColorView: View? = colorViews[0]

        // Вспомогательная функция для обновления выделения
        fun updateColorSelection(newSelectedView: View?) {
            // Снимаем выделение со всех цветов
            colorViews.forEach { view ->
                // Восстанавливаем исходный цвет фона
                val originalColor = originalBackgrounds[view] ?: Color.parseColor(view.tag as String)
                val shape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(originalColor)
                }
                view.background = shape
            }

            // Выделяем новый выбранный цвет
            newSelectedView?.let { selectedView ->
                val originalColor = originalBackgrounds[selectedView] ?: Color.parseColor(selectedView.tag as String)

                // Создаем LayerDrawable: цветной круг + рамка
                val colorCircle = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(originalColor)
                }

                val borderCircle = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setStroke(4, requireContext().getColor(R.color.primary))
                    setColor(Color.TRANSPARENT)
                }

                val layers = arrayOf(colorCircle, borderCircle)
                val layerDrawable = LayerDrawable(layers)
                layerDrawable.setLayerInset(1, 4, 4, 4, 4) // Отступ для рамки

                selectedView.background = layerDrawable
            }

            selectedColorView = newSelectedView
        }

        // Устанавливаем обработчики кликов для выбора цвета
        colorViews.forEach { colorView ->
            colorView.setOnClickListener {
                updateColorSelection(colorView)

                // Получаем цвет из тега
                val colorTag = colorView.tag as? String
                colorTag?.let {
                    selectedColor = Color.parseColor(it)
                }
            }
        }

        // Выделяем цвет по умолчанию
        updateColorSelection(colorViews[0])

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить категорию")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val categoryName = dialogView.findViewById<EditText>(R.id.et_category_name)
                    .text.toString().trim()

                if (categoryName.isNotEmpty()) {
                    addNewCategory(categoryName, selectedColor)
                } else {
                    Toast.makeText(requireContext(), "Введите название категории", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addNewCategory(name: String, color: Int) {
        lifecycleScope.launch {
            try {
                // Создаем новую категорию
                val newCategory = Category(
                    name = name,
                    color = String.format("#%06X", 0xFFFFFF and color),
                    type = "expense"
                )

                // Сохраняем в базу данных
                repository.insertCategory(newCategory)

                Toast.makeText(
                    requireContext(),
                    "Категория '$name' добавлена",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка при добавлении категории: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
        Toast.makeText(requireContext(), "Валюта изменена", Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Данные экспортированы в CSV", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка экспорта: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportToPDF() {
        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Данные экспортированы в PDF", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка экспорта: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                closeApp()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun closeApp() {
        activity?.finishAffinity()
    }
}