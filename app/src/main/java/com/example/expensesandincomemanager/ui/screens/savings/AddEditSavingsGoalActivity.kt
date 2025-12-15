package com.example.expensesandincomemanager.ui.screens.savings

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensesandincomemanager.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import data.entities.SavingsGoal
import data.provider.FinanceRepositoryProvider
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddEditSavingsGoalActivity : AppCompatActivity() {

    private var goalId: Int? = null
    private var goal: SavingsGoal? = null
    private var targetDate: Date? = null
    private val repository by lazy { FinanceRepositoryProvider.getRepository(this) }

    // Views
    private lateinit var etGoalName: EditText
    private lateinit var etTargetAmount: EditText
    private lateinit var etCurrentAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvRemainingText: TextView
    private lateinit var tilGoalName: TextInputLayout
    private lateinit var tilTargetAmount: TextInputLayout
    private lateinit var tilCurrentAmount: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_savings_goal)

        initViews()
        setupToolbar()
        setupViews()
        loadGoalData()
        setupListeners()
        setupProgressBar()
    }

    private fun initViews() {
        etGoalName = findViewById(R.id.etGoalName)
        etTargetAmount = findViewById(R.id.etTargetAmount)
        etCurrentAmount = findViewById(R.id.etCurrentAmount)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        progressBar = findViewById(R.id.progressBar)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvRemainingText = findViewById(R.id.tvRemainingText)
        tilGoalName = findViewById(R.id.tilGoalName)
        tilTargetAmount = findViewById(R.id.tilTargetAmount)
        tilCurrentAmount = findViewById(R.id.tilCurrentAmount)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        goalId = intent.getIntExtra("GOAL_ID", -1).takeIf { it != -1 }
        if (goalId != null) {
            supportActionBar?.title = "Редактирование цели"
            btnDelete.visibility = View.VISIBLE
        } else {
            supportActionBar?.title = "Новая цель"
            btnDelete.visibility = View.GONE
        }
    }

    private fun setupViews() {
        // Настройка ввода суммы с форматированием
        setupAmountTextWatcher(etTargetAmount)
        setupAmountTextWatcher(etCurrentAmount)

        // Для отображения сохраненных значений при потере фокуса
        etTargetAmount.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formatAmountField(etTargetAmount)
                updateProgress()
            }
        }

        etCurrentAmount.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formatAmountField(etCurrentAmount)
                updateProgress()
            }
        }
    }

    private fun setupAmountTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                if (s.toString() != current) {
                    isFormatting = true

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        try {
                            val number = cleanString.toLong()
                            val formatted = NumberFormat.getNumberInstance(Locale.getDefault())
                                .apply { maximumFractionDigits = 0 }
                                .format(number)
                            current = formatted
                            s?.replace(0, s.length, formatted)
                        } catch (e: Exception) {
                            current = ""
                            s?.clear()
                        }
                    } else {
                        current = ""
                        s?.clear()
                    }

                    isFormatting = false
                    updateProgress()
                }
            }
        })
    }

    private fun formatAmountField(editText: EditText) {
        val text = editText.text.toString()
        val cleanString = text.replace("[^\\d]".toRegex(), "")

        if (cleanString.isNotEmpty()) {
            try {
                val number = cleanString.toLong()
                val formatted = NumberFormat.getNumberInstance(Locale.getDefault())
                    .apply { maximumFractionDigits = 0 }
                    .format(number)
                editText.setText(formatted)
            } catch (e: Exception) {
                editText.setText("")
            }
        }
    }

    private fun loadGoalData() {
        goalId?.let { id ->
            lifecycleScope.launch {
                goal = repository.getSavingsGoalById(id)
                goal?.let { populateFields(it) }
            }
        }
    }

    private fun populateFields(goal: SavingsGoal) {
        etGoalName.setText(goal.name)

        // Форматируем суммы при отображении
        val targetAmountFormatted = NumberFormat.getNumberInstance(Locale.getDefault())
            .apply { maximumFractionDigits = 0 }
            .format(goal.targetAmount.toLong())
        etTargetAmount.setText(targetAmountFormatted)

        val currentAmountFormatted = NumberFormat.getNumberInstance(Locale.getDefault())
            .apply { maximumFractionDigits = 0 }
            .format(goal.currentAmount.toLong())
        etCurrentAmount.setText(currentAmountFormatted)

        etDescription.setText(goal.description ?: "")

        goal.targetDate?.let { date ->
            targetDate = date
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            etDate.setText(dateFormat.format(date))
        }

        updateProgress()
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveGoal()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        etDate.setOnClickListener {
            showDatePicker()
        }

        // Обработка нажатия на поле текущей суммы
        etCurrentAmount.setOnClickListener {
            showEditCurrentAmountDialog()
        }
    }

    private fun setupProgressBar() {
        // Инициализация прогресс-бара
        updateProgress()
    }

    private fun updateProgress() {
        val targetAmountStr = etTargetAmount.text.toString().replace("[^\\d]".toRegex(), "")
        val currentAmountStr = etCurrentAmount.text.toString().replace("[^\\d]".toRegex(), "")

        if (targetAmountStr.isNotEmpty()) {
            val targetAmount = targetAmountStr.toDoubleOrNull() ?: 0.0
            val currentAmount = if (currentAmountStr.isNotEmpty()) currentAmountStr.toDoubleOrNull() ?: 0.0 else 0.0

            val progress = if (targetAmount > 0) {
                ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            progressBar.progress = progress
            tvProgressPercent.text = "$progress%"

            val remaining = targetAmount - currentAmount
            if (remaining >= 0) {
                tvRemainingText.text = String.format("Осталось: %,.0f ₽", remaining)
            } else {
                tvRemainingText.text = String.format("Превышено на: %,.0f ₽", -remaining)
            }

            // Подсвечиваем поле текущей суммы если превышена цель
            if (currentAmount > targetAmount) {
                tilCurrentAmount.error = "Текущая сумма превышает целевую"
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.error))
            } else {
                tilCurrentAmount.error = null
                progressBar.progressTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
            }
        } else {
            progressBar.progress = 0
            tvProgressPercent.text = "0%"
            tvRemainingText.text = "Осталось: 0 ₽"
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                targetDate = selectedCalendar.time

                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                etDate.setText(dateFormat.format(targetDate))
            },
            year,
            month,
            day
        ).show()
    }

    private fun showEditCurrentAmountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_current_amount, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val tvTarget = dialogView.findViewById<TextView>(R.id.tvTargetAmount)

        val targetAmountStr = etTargetAmount.text.toString().replace("[^\\d]".toRegex(), "")
        val targetAmount = if (targetAmountStr.isNotEmpty()) targetAmountStr.toDouble() else 0.0
        tvTarget.text = String.format("Целевая сумма: %,.0f ₽", targetAmount)

        val currentAmountStr = etCurrentAmount.text.toString().replace("[^\\d]".toRegex(), "")
        if (currentAmountStr.isNotEmpty()) {
            val currentAmount = currentAmountStr.toDouble()
            etAmount.setText(String.format("%,.0f", currentAmount))
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Текущая сумма")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val amountStr = etAmount.text.toString().replace("[^\\d]".toRegex(), "")
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDouble()
                    etCurrentAmount.setText(String.format("%,.0f", amount))
                    updateProgress()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun saveGoal() {
        val name = etGoalName.text.toString().trim()
        val targetAmountStr = etTargetAmount.text.toString().replace("[^\\d]".toRegex(), "")
        val currentAmountStr = etCurrentAmount.text.toString().replace("[^\\d]".toRegex(), "")

        // Валидация
        var hasError = false

        if (name.isEmpty()) {
            tilGoalName.error = "Введите название цели"
            hasError = true
        } else {
            tilGoalName.error = null
        }

        if (targetAmountStr.isEmpty()) {
            tilTargetAmount.error = "Введите целевую сумму"
            hasError = true
        } else {
            tilTargetAmount.error = null
        }

        if (currentAmountStr.isNotEmpty()) {
            val targetAmount = targetAmountStr.toDoubleOrNull() ?: 0.0
            val currentAmount = currentAmountStr.toDoubleOrNull() ?: 0.0

            if (currentAmount > targetAmount) {
                tilCurrentAmount.error = "Текущая сумма не может превышать целевую"
                hasError = true
            } else {
                tilCurrentAmount.error = null
            }
        }

        if (hasError) {
            return
        }

        val targetAmount = targetAmountStr.toDoubleOrNull() ?: 0.0
        val currentAmount = if (currentAmountStr.isNotEmpty()) currentAmountStr.toDoubleOrNull() ?: 0.0 else 0.0

        val description = etDescription.text.toString().trim()
        val isCompleted = currentAmount >= targetAmount

        lifecycleScope.launch {
            try {
                if (goalId != null) {
                    // Редактирование существующей цели
                    goal?.let { existingGoal ->
                        val updatedGoal = existingGoal.copy(
                            name = name,
                            targetAmount = targetAmount,
                            currentAmount = currentAmount,
                            targetDate = targetDate,
                            description = if (description.isNotEmpty()) description else null,
                            isCompleted = isCompleted
                        )
                        repository.updateSavingsGoal(updatedGoal)
                        Toast.makeText(this@AddEditSavingsGoalActivity, "Цель обновлена", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Создание новой цели
                    val newGoal = SavingsGoal(
                        name = name,
                        targetAmount = targetAmount,
                        currentAmount = currentAmount,
                        targetDate = targetDate,
                        description = if (description.isNotEmpty()) description else null,
                        isCompleted = isCompleted
                    )
                    repository.insertSavingsGoal(newGoal)
                    Toast.makeText(this@AddEditSavingsGoalActivity, "Цель создана", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddEditSavingsGoalActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Удаление цели")
            .setMessage("Вы уверены, что хотите удалить эту цель?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    goal?.let {
                        repository.deleteSavingsGoal(it)
                        Toast.makeText(this@AddEditSavingsGoalActivity, "Цель удалена", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}