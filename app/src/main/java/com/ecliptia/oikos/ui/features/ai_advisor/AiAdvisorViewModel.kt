package com.ecliptia.oikos.ui.features.ai_advisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.FinancialPlan
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.services.GeminiAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val plan: FinancialPlan? = null, // Can hold a structured plan
    val timestamp: Long = System.currentTimeMillis()
)

data class AiAdvisorState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiAdvisorViewModel @Inject constructor(
    private val geminiAiService: GeminiAiService,
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAdvisorState())
    val uiState: StateFlow<AiAdvisorState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun sendMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + ChatMessage(message, true),
            isLoading = true
        )

        viewModelScope.launch {
            val apiKey = repository.getApiKey().firstOrNull()
            if (apiKey.isNullOrEmpty()) {
                addErrorMessage("Chave de API do Gemini não configurada. Por favor, adicione-a nas configurações.")
                return@launch
            }

            val financialData = getFinancialDataContext()
            val userMessage = message.lowercase()
            val isPlanningRequest = listOf("salário", "plano", "orçamento", "ganho", "renda", "organizar").any { userMessage.contains(it) }
            val isEducationalRequest = listOf("o que é", "como", "me explique", "educação", "dica", "melhor método", "primeiros passos").any { userMessage.contains(it) }

            val prompt = when {
                isPlanningRequest -> createPlannerPrompt(message, financialData)
                isEducationalRequest -> createEducationalPrompt(message, financialData)
                else -> createChatPrompt(message, financialData)
            }

            val aiResponse = geminiAiService.getFinancialAnalysis(apiKey, prompt)

            if (isPlanningRequest) {
                try {
                    // Clean the response to get only the JSON part
                    val jsonString = aiResponse.substringAfter("```json").substringBefore("```").trim()
                    val plan = json.decodeFromString<FinancialPlan>(jsonString)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + ChatMessage(text = "", isUser = false, plan = plan),
                        isLoading = false
                    )
                } catch (e: Exception) {
                    // If parsing fails, add the raw response as a text message for debugging
                    addErrorMessage("Não consegui criar um plano estruturado. Aqui está a resposta que recebi: \n$aiResponse")
                }
            } else { // This now covers both educational and general chat requests
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + ChatMessage(aiResponse, false),
                    isLoading = false
                )
            }
        }
    }

    private fun addErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + ChatMessage(message, false),
            isLoading = false
        )
    }

    private suspend fun getFinancialDataContext(): String {
        val allTransactions = repository.getTransactions().firstOrNull() ?: emptyList()
        val rules = repository.getRules().firstOrNull() ?: emptyList()
        val savingsBoxes = repository.getSavingsBoxes().firstOrNull() ?: emptyList()
        val totalIncome = allTransactions.filterIsInstance<com.ecliptia.oikos.data.model.Income>().sumOf { it.amount }
        val totalExpense = allTransactions.filterIsInstance<com.ecliptia.oikos.data.model.Expense>().sumOf { it.amount }
        val currentBalance = totalIncome - totalExpense

        return """
        - Saldo Atual: R$$currentBalance
        - Receita Total: R$$totalIncome
        - Despesa Total: R$$totalExpense
        - Regras de Alocação: ${rules.joinToString { "${it.name}: ${it.value}%" }}
        - Metas de Poupança (Caixinhas): ${savingsBoxes.joinToString { "${it.name} (Alvo: R$${it.targetAmount}, Atual: R$${it.currentAmount})" }}
        """
    }

    private fun createPlannerPrompt(userRequest: String, financialData: String): String {
        return """
        Você é Oikos, um planejador financeiro especialista. Sua tarefa é criar um plano financeiro com base no pedido do usuário. A resposta DEVE ser um objeto JSON válido, sem nenhuma formatação ou texto adicional antes ou depois dele. Use o seguinte schema:

        ```json
        {
          "monthlyIncome": Double,
          "planSummary": "String",
          "deductions": [
            {
              "name": "String",
              "type": "PERCENTAGE | FIXED",
              "value": Double,
              "amount": Double
            }
          ],
          "allocations": [
            {
              "name": "String (Nome de exibição, ex: 'Lazer e Extras')",
              "key": "String (Chave simples, sem espaços/caracteres especiais, ex: 'Lazer')",
              "category": "SAVINGS | SPENDING",
              "amount": Double
            }
          ],
          "projections": {
            "twelveMonthsSavings": Double,
            "projectionSummary": "String"
          }
        }
        ```

        Para o campo 'key' nas alocações, use uma palavra única e simples que resuma a alocação (ex: 'Moradia', 'Transporte', 'Emergencia').

        Pedido do usuário: "$userRequest"
        Dados financeiros atuais para contexto: "$financialData"

        Gere APENAS o objeto JSON.
        """
    }

    private fun createEducationalPrompt(userQuestion: String, financialData: String): String {
        return """
        Você é Oikos, um mentor financeiro paciente e didático, especializado em educar jovens sobre finanças. Sua tarefa é responder à pergunta do usuário de forma clara, simples e encorajadora, como se estivesse ensinando alguém que está dando os primeiros passos. Use analogias se for útil.

        Dados financeiros atuais do usuário para contexto (use-os para personalizar a resposta, se aplicável):
        $financialData

        Pergunta do usuário: "$userQuestion"

        Forneça uma explicação detalhada e conselhos práticos.
        """
    }

    private fun createChatPrompt(userQuestion: String, financialData: String): String {
        return """
        Você é Oikos, um gerente financeiro pessoal e guardião. Responda a pergunta do usuário de forma útil e acionável, usando os dados financeiros fornecidos como contexto.

        Dados financeiros: $financialData
        Pergunta: "$userQuestion"
        """
    }

    fun applyPlan(plan: FinancialPlan) {
        viewModelScope.launch {
            try {
                repository.applyFinancialPlan(plan)
                addErrorMessage("Plano aplicado com sucesso! Novas regras e caixinhas foram criadas.")
            } catch (e: Exception) {
                addErrorMessage("Ocorreu um erro ao aplicar o plano: ${e.message}")
            }
        }
    }
}
