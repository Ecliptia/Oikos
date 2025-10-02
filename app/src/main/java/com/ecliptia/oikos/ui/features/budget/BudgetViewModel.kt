package com.ecliptia.oikos.ui.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.combine // New import
import com.ecliptia.oikos.data.model.Expense // New import
import com.ecliptia.oikos.data.model.Income // New import
import com.ecliptia.oikos.data.model.Transaction // New import
import java.util.Calendar // New import
import java.util.Date // New import

data class BudgetState(
    val categoryLimits: Map<String, Double> = emptyMap(),
    val currentSpending: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetState())
    val uiState: StateFlow<BudgetState> = _uiState.asStateFlow()

    private fun isCurrentMonth(date: Date): Boolean { // New helper function
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.time = date
        val transactionMonth = calendar.get(Calendar.MONTH)
        val transactionYear = calendar.get(Calendar.YEAR)

        return currentMonth == transactionMonth && currentYear == transactionYear
    }

    init {
        viewModelScope.launch {
            combine(
                repository.getCategoryLimits(),
                repository.getTransactions()
            ) { categoryLimits, transactions ->
                val currentMonthExpenses = transactions
                    .filterIsInstance<Expense>()
                    .filter { isCurrentMonth(it.date) }

                val spendingByCategory = currentMonthExpenses
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                BudgetState(
                    categoryLimits = categoryLimits,
                    currentSpending = spendingByCategory,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}