package com.ecliptia.oikos.ui.features.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.Investment
import com.ecliptia.oikos.data.model.InvestmentType
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestmentsState(
    val investments: List<Investment> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentsState())
    val uiState: StateFlow<InvestmentsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getInvestments().collect { investments ->
                _uiState.update { it.copy(investments = investments, isLoading = false) }
            }
        }
    }

    fun addInvestment(
        name: String,
        ticker: String,
        type: InvestmentType,
        quantity: Double,
        purchasePrice: Double,
        currentPrice: Double
    ) {
        viewModelScope.launch {
            val newInvestment = Investment(
                id = "inv_${System.currentTimeMillis()}",
                name = name,
                ticker = ticker,
                type = type,
                quantity = quantity,
                purchasePrice = purchasePrice,
                currentPrice = currentPrice
            )
            repository.saveInvestment(newInvestment)
        }
    }

    fun updateInvestment(investment: Investment) {
        viewModelScope.launch {
            repository.saveInvestment(investment) // Save acts as update if ID exists
        }
    }

    fun deleteInvestment(investment: Investment) {
        viewModelScope.launch {
            repository.deleteInvestment(investment.id)
        }
    }
}
