package com.ecliptia.oikos.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.services.FinancialStatusService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class FinancialStatus {
    NEUTRAL,
    GOOD,
    BAD,
    WARNING,
    EXCELLENT
}

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val financialStatusService: FinancialStatusService
) : ViewModel() {

    val financialStatus: StateFlow<FinancialStatus> = financialStatusService.financialStatus
}
