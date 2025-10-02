package com.ecliptia.oikos.ui.features.recurring_transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.RecurringTransaction
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringTransactionsState(
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class RecurringTransactionsViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringTransactionsState())
    val uiState: StateFlow<RecurringTransactionsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRecurringTransactions().collect { transactions ->
                _uiState.value = _uiState.value.copy(
                    recurringTransactions = transactions,
                    isLoading = false
                )
            }
        }
    }

    fun addRecurringTransaction(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            repository.saveRecurringTransaction(recurringTransaction)
        }
    }

    fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            repository.updateRecurringTransaction(recurringTransaction)
        }
    }

    fun deleteRecurringTransaction(recurringTransactionId: String) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(recurringTransactionId)
        }
    }
}