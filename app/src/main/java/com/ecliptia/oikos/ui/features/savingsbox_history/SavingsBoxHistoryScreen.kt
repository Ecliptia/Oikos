package com.ecliptia.oikos.ui.features.savingsbox_history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.ui.features.dashboard.TransactionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

data class SavingsBoxHistoryState(
    val savingsBoxName: String = "Carregando...",
    val transactions: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SavingsBoxHistoryViewModel @Inject constructor(
    private val repository: OikosRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savingsBoxId: String = checkNotNull(savedStateHandle["savingsBoxId"])

    private val _uiState = MutableStateFlow(SavingsBoxHistoryState())
    val uiState: StateFlow<SavingsBoxHistoryState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getSavingsBoxes().map { it.firstOrNull { box -> box.id == savingsBoxId } },
                repository.getTransactions().map { it.filterIsInstance<Expense>().filter { expense -> expense.relatedSavingsBoxId == savingsBoxId } }
            ) { savingsBox, transactions ->
                SavingsBoxHistoryState(
                    savingsBoxName = savingsBox?.name ?: "Caixinha não encontrada",
                    transactions = transactions.sortedByDescending { it.date },
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}

@Composable
fun SavingsBoxHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: SavingsBoxHistoryViewModel = hiltViewModel(),
    savingsBoxId: String? // Accept savingsBoxId as parameter
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Histórico de ${uiState.savingsBoxName}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhuma transação registrada para esta caixinha ainda.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactions) { expense ->
                    TransactionItem(transaction = expense) // Reusing TransactionItem from dashboard
                }
            }
        }
    }
}