package com.ecliptia.oikos.ui.features.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.AllocationRule
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RulesState(
    val allocationRules: List<AllocationRule> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulesState())
    val uiState: StateFlow<RulesState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRules().collect { rules ->
                _uiState.value = RulesState(
                    allocationRules = rules,
                    isLoading = false
                )
            }
        }
    }

    fun addRule(rule: AllocationRule) {
        viewModelScope.launch {
            repository.saveRule(rule.copy(id = "rule_${System.currentTimeMillis()}"))
        }
    }

    fun deleteRule(rule: AllocationRule) {
        viewModelScope.launch {
            repository.deleteRule(rule.id)
        }
    }
}