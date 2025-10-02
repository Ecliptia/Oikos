package com.ecliptia.oikos.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    val apiKey = repository.getApiKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            repository.saveApiKey(apiKey)
        }
    }

    fun saveCategoryLimit(category: String, limit: Double) {
        viewModelScope.launch {
            repository.saveCategoryLimit(category, limit)
        }
    }

    val allCategoryLimits = repository.getCategoryLimits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun exportReports() {
        viewModelScope.launch {
            val csvContent = repository.exportReports()
            // TODO: Implement actual file saving/sharing logic
            println("Exported Reports (CSV):\n$csvContent")
        }
    }

    val visualMode = repository.getVisualMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Minimalista") // Default to Minimalista

    fun saveVisualMode(mode: String) {
        viewModelScope.launch {
            repository.saveVisualMode(mode)
        }
    }
}
