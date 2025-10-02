package com.ecliptia.oikos.ui.features.creditcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.CardBrand
import com.ecliptia.oikos.data.model.CreditCard
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreditCardState(
    val creditCards: List<CreditCard> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditCardState())
    val uiState: StateFlow<CreditCardState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCreditCards().collect { cards ->
                _uiState.update { it.copy(creditCards = cards, isLoading = false) }
            }
        }
    }

    fun addCreditCard(
        name: String,
        limit: Double,
        closingDay: Int,
        dueDate: Int,
        brand: CardBrand
    ) {
        viewModelScope.launch {
            val newCard = CreditCard(
                id = "cc_${System.currentTimeMillis()}",
                name = name,
                limit = limit,
                closingDay = closingDay,
                dueDate = dueDate,
                brand = brand
            )
            repository.saveCreditCard(newCard)
        }
    }

    fun updateCreditCard(card: CreditCard) {
        viewModelScope.launch {
            repository.saveCreditCard(card)
        }
    }

    fun deleteCreditCard(card: CreditCard) {
        viewModelScope.launch {
            repository.deleteCreditCard(card.id)
        }
    }
}
