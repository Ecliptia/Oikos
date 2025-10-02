package com.ecliptia.oikos.ui.features.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.services.GeminiAiService
import com.ecliptia.oikos.data.model.Allocation
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Transaction
import com.ecliptia.oikos.data.model.Notification
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date
import javax.inject.Inject

data class WalletState(
    val transactions: List<Transaction> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val allocations: List<Allocation> = emptyList(),
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: OikosRepository,
    private val geminiAiService: GeminiAiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletState())
    val uiState: StateFlow<WalletState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getTransactions(),
                repository.getAllocations()
            ) { transactions, allocations ->
                val expenses = transactions.filterIsInstance<Expense>()
                val expensesByCategory = expenses
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                WalletState(
                    transactions = transactions,
                    incomes = transactions.filterIsInstance<Income>(),
                    expenses = expenses,
                    allocations = allocations,
                    expensesByCategory = expensesByCategory,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun addIncome(amount: Double, description: String) {
        viewModelScope.launch {
            val newIncome = Income(
                id = "inc_${System.currentTimeMillis()}",
                amount = amount,
                date = Date(),
                description = description
            )
            repository.saveIncome(newIncome, emptyList())
        }
    }

    fun addExpense(amount: Double, description: String, category: String, paymentMethod: String) {
        viewModelScope.launch {
            val finalCategory = if (category.isBlank()) {
                geminiAiService.categorizeTransaction(repository.getApiKey().firstOrNull() ?: "", description) ?: "Outros"
            } else {
                category
            }

            val currentBalance = (_uiState.value.incomes.sumOf { it.amount } - _uiState.value.expenses.sumOf { it.amount } - _uiState.value.allocations.sumOf { it.amount })
            val categoryLimit = repository.getCategoryLimit(finalCategory).firstOrNull() ?: Double.MAX_VALUE
            val spentInCategory = _uiState.value.expenses.filter { it.category == finalCategory }.sumOf { it.amount }

            if (currentBalance < amount) {
                val prompt = "O usuário está tentando adicionar uma despesa de R$${amount} na categoria '${category}'. O saldo atual dele é de R$${currentBalance}. Isso fará o saldo ficar negativo. Como um gerente financeiro guardião, gere uma mensagem de alerta proativa e construtiva para o usuário, explicando o risco e sugerindo uma ação." 
                val aiMessage = geminiAiService.getFinancialAnalysis(repository.getApiKey().firstOrNull() ?: "", prompt)
                repository.saveNotification(Notification(id = "notif_${System.currentTimeMillis()}", message = aiMessage, type = "expense_alert"))
            } else if (categoryLimit != Double.MAX_VALUE) { // Only check if a limit is actually set
                val newSpentInCategory = spentInCategory + amount
                val percentageSpent = (newSpentInCategory / categoryLimit) * 100

                val alertMessage: String? = when {
                    newSpentInCategory > categoryLimit -> {
                        "O usuário está tentando adicionar uma despesa de R$${amount} na categoria '${finalCategory}'. O limite para esta categoria é de R$${categoryLimit} e ele já gastou R$${spentInCategory}. Isso excederá o limite. Como um gerente financeiro guardião, gere uma mensagem de alerta proativa e construtiva para o usuário, explicando o risco e sugerindo uma ação para evitar o estouro do orçamento."
                    }
                    percentageSpent >= 80 -> { // Nearing limit
                        "O usuário está tentando adicionar uma despesa de R$${amount} na categoria '${finalCategory}'. Ele já gastou R$${spentInCategory} de um limite de R$${categoryLimit}, atingindo ${percentageSpent.toInt()}% do orçamento. Como um gerente financeiro guardião, gere uma mensagem de alerta proativa e construtiva para o usuário, explicando que está se aproximando do limite e sugerindo uma ação para gerenciar o restante do orçamento."
                    }
                    else -> null
                }

                alertMessage?.let { msg ->
                    val aiMessage = geminiAiService.getFinancialAnalysis(
                        repository.getApiKey().firstOrNull() ?: "",
                        msg
                    )
                    repository.saveNotification(
                        Notification(
                            id = "notif_budget_alert_${System.currentTimeMillis()}", // More specific ID
                            message = aiMessage,
                            type = "budget_alert", // More specific type
                            relatedId = finalCategory // Relate to the category
                        )
                    )
                }
            }

            val newExpense = Expense(
                id = "exp_${System.currentTimeMillis()}",
                amount = amount,
                date = Date(),
                description = description,
                category = category,
                paymentMethod = paymentMethod
            )
            repository.saveExpense(newExpense)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllUserData()
        }
    }
}
