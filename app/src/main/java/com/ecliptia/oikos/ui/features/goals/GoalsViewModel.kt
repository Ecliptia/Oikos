package com.ecliptia.oikos.ui.features.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.SavingsBox
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsState(
    val savingsBoxes: List<SavingsBox> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsState())
    val uiState: StateFlow<GoalsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSavingsBoxes().collect { savingsBoxes ->
                _uiState.value = GoalsState(
                    savingsBoxes = savingsBoxes,
                    isLoading = false
                )
            }
        }
    }

    fun addSavingsBox(name: String, targetAmount: Double, monthlyContributionTarget: Double) {
        viewModelScope.launch {
            val newSavingsBox = SavingsBox(
                id = "sb_${System.currentTimeMillis()}",
                name = name,
                targetAmount = targetAmount,
                currentAmount = 0.0, // Start with 0
                monthlyContributionTarget = monthlyContributionTarget
            )
            repository.saveSavingsBox(newSavingsBox)
        }
    }

    fun updateSavingsBox(savingsBox: SavingsBox) {
        viewModelScope.launch {
            repository.updateSavingsBox(savingsBox)
        }
    }

    fun deleteSavingsBox(savingsBox: SavingsBox) {
        viewModelScope.launch {
            repository.deleteSavingsBox(savingsBox.id)
        }
    }
}