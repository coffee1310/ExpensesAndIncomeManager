package com.example.expensesandincomemanager.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.expensesandincomemanager.R
import com.example.expensesandincomemanager.databinding.FragmentAddTransactionBinding
import kotlinx.coroutines.launch
import java.util.*

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupTypeSelector()
        setupSaveButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTypeSelector() {
        binding.incomeCard.setOnClickListener {
            selectType("income")
        }

        binding.expenseCard.setOnClickListener {
            selectType("expense")
        }

        // По умолчанию выбираем расход
        selectType("expense")
    }

    private fun selectType(type: String) {
        val primaryColor = requireContext().getColor(R.color.primary)
        val outlineColor = requireContext().getColor(R.color.outline)

        if (type == "income") {
            binding.incomeCard.strokeWidth = 2
            binding.incomeCard.strokeColor = primaryColor
            binding.expenseCard.strokeWidth = 1
            binding.expenseCard.strokeColor = outlineColor
        } else {
            binding.expenseCard.strokeWidth = 2
            binding.expenseCard.strokeColor = primaryColor
            binding.incomeCard.strokeWidth = 1
            binding.incomeCard.strokeColor = outlineColor
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amountText = binding.amountEditText.text.toString()
        val description = binding.descriptionEditText.text.toString()

        if (amountText.isBlank()) {
            binding.amountEditText.error = "Введите сумму"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.amountEditText.error = "Введите корректную сумму"
            return
        }

        // TODO: Здесь будет логика сохранения транзакции в базу данных
        // через ViewModel и Repository

        // После сохранения возвращаемся на предыдущий экран
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}