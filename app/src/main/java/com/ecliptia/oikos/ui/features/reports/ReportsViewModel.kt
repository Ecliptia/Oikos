package com.ecliptia.oikos.ui.features.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Transaction
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class ReportsState(
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val monthlyExpenses: Map<String, Double> = emptyMap(),
    val monthlyIncome: Map<String, Double> = emptyMap(),
    val previousMonthlyExpenses: Map<String, Double> = emptyMap(), // New
    val previousMonthlyIncome: Map<String, Double> = emptyMap(),   // New
    val projections: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsState())
    val uiState: StateFlow<ReportsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTransactions().collect { transactions ->
                val expenses = transactions.filterIsInstance<Expense>()
                val incomes = transactions.filterIsInstance<Income>()

                val expensesByCategory = expenses
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val currentMonth = Calendar.getInstance().monthYearString()
                val previousMonth = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                }.monthYearString()

                val monthlyExpenses = expenses
                    .filter { it.monthYear() == currentMonth }
                    .groupBy { it.monthYear() }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val monthlyIncome = incomes
                    .filter { it.monthYear() == currentMonth }
                    .groupBy { it.monthYear() }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val previousMonthlyExpenses = expenses
                    .filter { it.monthYear() == previousMonth }
                    .groupBy { it.monthYear() }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val previousMonthlyIncome = incomes
                    .filter { it.monthYear() == previousMonth }
                    .groupBy { it.monthYear() }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                _uiState.value = ReportsState(
                    expensesByCategory = expensesByCategory,
                    monthlyExpenses = monthlyExpenses,
                    monthlyIncome = monthlyIncome,
                    previousMonthlyExpenses = previousMonthlyExpenses,
                    previousMonthlyIncome = previousMonthlyIncome,
                    projections = calculateProjections(monthlyIncome, monthlyExpenses), // Placeholder
                    isLoading = false
                )
            }
        }
    }

    private fun Transaction.monthYear(): String {
        val calendar = Calendar.getInstance()
        calendar.time = this.date
        return "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    private fun Calendar.monthYearString(): String {
        return "${this.get(Calendar.MONTH) + 1}/${this.get(Calendar.YEAR)}"
    }

    private fun calculateProjections(monthlyIncome: Map<String, Double>, monthlyExpenses: Map<String, Double>): Map<String, Double> {
        // Basic projection logic for now
        // This will be expanded later
        return emptyMap()
    }
}