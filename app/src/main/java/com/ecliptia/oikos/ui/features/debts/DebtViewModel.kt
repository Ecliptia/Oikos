package com.ecliptia.oikos.ui.features.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.Debt
import com.ecliptia.oikos.data.model.Notification
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ecliptia.oikos.services.GeminiAiService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

enum class DebtStrategy {
    AVALANCHE, // Prioritizes high-interest debts
    SNOWBALL   // Prioritizes smallest-balance debts
}

data class DebtsState(
    val debts: List<Debt> = emptyList(),
    val strategy: DebtStrategy = DebtStrategy.AVALANCHE,
    val isLoading: Boolean = true,
    val insights: List<String> = emptyList(), // New
    val isLoadingInsight: Boolean = true     // New
)

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val repository: OikosRepository,
    private val geminiAiService: GeminiAiService, // Injected
    private val auth: FirebaseAuth // Injected
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtsState())
    val uiState: StateFlow<DebtsState> = _uiState.asStateFlow()

    private val currentUser get() = auth.currentUser
    private fun userRootRef() = currentUser?.uid?.let { FirebaseDatabase.getInstance().getReference("users").child(it) }


    init {
        viewModelScope.launch {
            repository.getDebts().collect { debts ->
                _uiState.update { it.copy(debts = debts, isLoading = false) }
                // Trigger insight generation after debts are loaded
                repository.getApiKey().collect { apiKey ->
                    _uiState.update { it.copy(isLoadingInsight = true) }
                    if (!apiKey.isNullOrBlank()) {
                        val insight = getDebtInsight(apiKey, debts)
                        _uiState.update { it.copy(insights = listOf(insight), isLoadingInsight = false) }
                    } else {
                        _uiState.update { it.copy(
                            insights = listOf("Configure sua chave de API para receber insights sobre suas dívidas."),
                            isLoadingInsight = false
                        ) }
                    }
                }
            }
        }
    }

    fun addDebt(name: String, totalAmount: Double, interestRate: Double, minimumPayment: Double, settlementOfferAmount: Double?, settlementOfferDetails: String?) {
        viewModelScope.launch {
            val newDebt = Debt(
                id = "debt_${System.currentTimeMillis()}",
                name = name,
                totalAmount = totalAmount,
                interestRate = interestRate,
                minimumPayment = minimumPayment,
                settlementOfferAmount = settlementOfferAmount,
                settlementOfferDetails = settlementOfferDetails
            )
            repository.saveDebt(newDebt)
        }
    }

    fun updateDebt(debt: Debt) {
        viewModelScope.launch {
            repository.saveDebt(debt)
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt.id)
        }
    }

    fun setStrategy(strategy: DebtStrategy) {
        _uiState.update { it.copy(strategy = strategy) }
    }

    fun getSortedDebts(): List<Debt> {
        val debts = _uiState.value.debts
        return when (_uiState.value.strategy) {
            DebtStrategy.AVALANCHE -> debts.sortedByDescending { it.interestRate }
            DebtStrategy.SNOWBALL -> debts.sortedBy { it.totalAmount }
        }
    }

    private suspend fun getDebtInsight(apiKey: String, debts: List<Debt>): String {
        if (apiKey.isBlank()) {
            return "Chave de API do Gemini não configurada."
        }

        val transactions = repository.getTransactions().firstOrNull() ?: emptyList()
        val allocations = repository.getAllocations().firstOrNull() ?: emptyList()
        val savingsBoxes = repository.getSavingsBoxes().firstOrNull() ?: emptyList()

        val totalIncome = transactions.filterIsInstance<Income>().sumOf { it.amount }
        val totalExpense = transactions.filterIsInstance<Expense>().sumOf { it.amount }
        val currentBalance = totalIncome - totalExpense - allocations.sumOf { it.amount }

        val debtData = if (debts.isEmpty()) {
            "Nenhuma dívida cadastrada."
        } else {
            debts.joinToString(separator = "\n") { debt ->
                "- ${debt.name}: Total R$${debt.totalAmount}, Juros ${debt.interestRate}%, Pagamento Mínimo R$${debt.minimumPayment}" +
                        (debt.settlementOfferAmount?.let { ", Oferta de Quitação R$${it} (${debt.settlementOfferDetails ?: "sem detalhes"})" } ?: "")
            }
        }

        val financialContext = """
            Contexto Financeiro Atual:
            - Saldo Atual: R$$currentBalance
            - Receita Total: R$$totalIncome
            - Despesa Total: R$$totalExpense
            - Caixinhas de Poupança: ${savingsBoxes.joinToString { "${it.name} (Guardado: R$${it.currentAmount}, Meta: R$${it.targetAmount})" }}
            - Alocações Ativas: ${allocations.joinToString { "${it.ruleName}: R$${it.amount}" }}
        """.trimIndent()

        val prompt = """
            Você é um consultor financeiro direto e honesto, especializado em dívidas. Analise os seguintes dados de dívidas de um usuário, juntamente com o contexto financeiro geral, e forneça UMA ÚNICA frase de insight acionável e concisa para ser exibida no painel de dívidas. A frase deve ser curta, impactante e fácil de entender. Foque no ponto mais crítico ou na maior oportunidade para o usuário sair das dívidas. Seja direto, até mesmo um "esporro" se necessário, mas sempre construtivo.

            Exemplos de frases que você pode gerar:
            - "Sua dívida de cartão de crédito com 25% de juros é um buraco. Priorize-a!"
            - "Ótimo! Você tem uma oferta de quitação para o empréstimo. Não perca essa chance."
            - "Com ${debts.size} dívidas, focar na menor pode te dar o impulso inicial. Considere a estratégia Bola de Neve."
            - "Nenhuma dívida? Excelente! Mantenha o foco em poupar e investir."
            - "Seu saldo atual é negativo. Resolva isso antes de pensar em quitar dívidas maiores."

            Dados de Dívidas do Usuário:
            $debtData

            $financialContext

            Gere apenas a frase de insight, sem nenhuma introdução ou despedida.
        """.trimIndent()

        return geminiAiService.getFinancialAnalysis(apiKey, prompt)
    }
}
