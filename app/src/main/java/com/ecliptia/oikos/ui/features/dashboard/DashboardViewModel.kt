package com.ecliptia.oikos.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.*
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.services.GeminiAiService
import com.ecliptia.oikos.services.FinancialInsightService
import com.ecliptia.oikos.services.FinancialStatusService
import com.ecliptia.oikos.ui.theme.FinancialStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: OikosRepository,
    private val geminiAiService: GeminiAiService,
    private val db: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val financialInsightService: FinancialInsightService,
    private val financialStatusService: FinancialStatusService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private val currentUser get() = auth.currentUser
    private fun userRootRef() = currentUser?.uid?.let { db.getReference("users").child(it) }

    private fun isCurrentMonth(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.time = date
        val transactionMonth = calendar.get(Calendar.MONTH)
        val transactionYear = calendar.get(Calendar.YEAR)

        return currentMonth == transactionMonth && currentYear == transactionYear
    }

    init {
        // Collector for main financial data and new feature summaries
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                listOf(
                    repository.getTransactions(),
                    repository.getAllocations(),
                    repository.getSavingsBoxes(),
                    repository.getRules(),
                    repository.getInvestments(),
                    repository.getDebts(),
                    repository.getSubscriptions()
                )
            ) { flows ->
                @Suppress("UNCHECKED_CAST")
                val transactions = flows[0] as List<Transaction>
                val allocations = flows[1] as List<Allocation>
                val savingsBoxes = flows[2] as List<SavingsBox>
                val rules = flows[3] as List<AllocationRule>
                val investments = flows[4] as List<Investment>
                val debts = flows[5] as List<Debt>
                val subscriptions = flows[6] as List<Subscription>

                val totalIncome = transactions.filterIsInstance<Income>().sumOf { it.amount }
                val expenses = transactions.filterIsInstance<Expense>()
                val totalExpense = expenses.sumOf { it.amount }
                val totalAllocated = allocations.sumOf { it.amount }
                val expensesByCategory = expenses
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                // Investment Summary
                val totalInvestmentValue = investments.sumOf { it.quantity * it.currentPrice }
                val totalInvestmentProfitLoss = investments.sumOf { (it.currentPrice - it.purchasePrice) * it.quantity }

                // Debt Summary
                val totalDebtAmount = debts.sumOf { it.totalAmount }

                // Subscription Summary
                val upcomingSubscriptionPayments = subscriptions.sumOf { it.amount }
                val nextSubscriptionPaymentDate = subscriptions.minByOrNull { sub ->
                    getNextBillingDate(sub.firstBillDate, sub.billingCycle).time
                }?.let { sub ->
                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(getNextBillingDate(sub.firstBillDate, sub.billingCycle))
                }

                // Net Worth Calculation
                val totalSavingsAmount = savingsBoxes.sumOf { it.currentAmount }
                val netWorth = (totalIncome - totalExpense - totalAllocated) + totalSavingsAmount + totalInvestmentValue - totalDebtAmount

                _uiState.value = _uiState.value.copy(
                    transactions = transactions,
                    allocations = allocations,
                    savingsBoxes = savingsBoxes,
                    allocationRules = rules,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    totalAllocated = totalAllocated,
                    currentBalance = totalIncome - totalExpense - totalAllocated,
                    expensesByCategory = expensesByCategory,
                    isLoading = false, // Set isLoading to false here
                    totalInvestmentValue = totalInvestmentValue,
                    totalInvestmentProfitLoss = totalInvestmentProfitLoss,
                    totalDebtAmount = totalDebtAmount,
                    upcomingSubscriptionPayments = upcomingSubscriptionPayments,
                    nextSubscriptionPaymentDate = nextSubscriptionPaymentDate,
                    netWorth = netWorth // New
                )
            }.collect()
        }

        // Collector for API key and insight generation
        viewModelScope.launch {
            repository.getApiKey().collect { apiKey ->
                _uiState.update { it.copy(apiKey = apiKey) }
                if (!apiKey.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoadingInsight = true) }
                    val insight = getDashboardInsight(apiKey)
                    _uiState.update { it.copy(insights = listOf(insight), isLoadingInsight = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            insights = listOf("Configure sua chave de API para receber insights."),
                            isLoadingInsight = false
                        )
                    }
                }
            }
        }

        // Collector for educational insights
        viewModelScope.launch {
            combine(
                repository.getApiKey(),
                financialStatusService.financialStatus, // Get financial status from service
                repository.getTransactions(), // Need transactions for financialData
                repository.getAllocations(),
                repository.getDebts(),
                repository.getSavingsBoxes(),
                repository.getInvestments(),
                repository.getSubscriptions()
            ) { flowsArray ->
                @Suppress("UNCHECKED_CAST")
                val apiKey = flowsArray[0] as String?
                val financialStatus = flowsArray[1] as FinancialStatus
                val transactions = flowsArray[2] as List<Transaction>
                val allocations = flowsArray[3] as List<Allocation>
                val debts = flowsArray[4] as List<Debt>
                val savingsBoxes = flowsArray[5] as List<SavingsBox>
                val investments = flowsArray[6] as List<Investment>
                val subscriptions = flowsArray[7] as List<Subscription>

                if (apiKey.isNullOrBlank()) {
                    return@combine // Skip if no API key
                }

                // Cooldown mechanism: only generate new insight if last one was more than X hours ago
                val lastEducationalInsightTimestamp = userRootRef()?.child("insights")?.child("lastEducationalInsight")
                    ?.get()?.await()?.getValue(Long::class.java) ?: 0L
                val currentTime = System.currentTimeMillis()
                val COOLDOWN_PERIOD_MS = 24 * 60 * 60 * 1000L // 24 hours

                if ((currentTime - lastEducationalInsightTimestamp) < COOLDOWN_PERIOD_MS) {
                    return@combine // Skip if still in cooldown
                }

                // Construct financialData string for the AI
                val currentMonthTransactions = transactions.filter { isCurrentMonth(it.date) }
                val monthlyIncome = currentMonthTransactions.filterIsInstance<Income>().sumOf { it.amount }
                val monthlyExpense = currentMonthTransactions.filterIsInstance<Expense>().sumOf { it.amount }
                val totalAllocated = allocations.sumOf { it.amount }
                val currentBalance = monthlyIncome - monthlyExpense - totalAllocated

                val totalDebtAmount = debts.sumOf { it.totalAmount }
                val totalSavingsAmount = savingsBoxes.sumOf { it.currentAmount }
                val totalInvestmentValue = investments.sumOf { it.quantity * it.currentPrice }
                val upcomingSubscriptionPayments = subscriptions.sumOf { it.amount }

                val financialData = """
                    - Saldo Atual: R$$currentBalance
                    - Receita Total (mês): R$$monthlyIncome
                    - Despesa Total (mês): R$$monthlyExpense
                    - Dívida Total: R$$totalDebtAmount
                    - Poupança Total: R$$totalSavingsAmount
                    - Investimento Total: R$$totalInvestmentValue
                    - Assinaturas Mensais: R$$upcomingSubscriptionPayments
                """.trimIndent()

                val educationalInsight = financialInsightService.generateEducationalInsight(apiKey, financialStatus, financialData)

                educationalInsight?.let { insight ->
                    repository.saveNotification(
                        Notification(
                            id = "notif_edu_${System.currentTimeMillis()}",
                            message = insight,
                            type = "educational_insight"
                        )
                    )
                    // Update last educational insight timestamp
                    userRootRef()?.child("insights")?.child("lastEducationalInsight")?.setValue(currentTime)
                }
            }.collect()
        }

        // Collector for sweep suggestions
        viewModelScope.launch {
            combine(
                repository.getApiKey(),
                _uiState, // Observe UI state for currentBalance
                repository.getTransactions(),
                repository.getAllocations(),
                repository.getDebts(),
                repository.getSavingsBoxes(),
                repository.getInvestments(),
                repository.getSubscriptions()
            ) { flowsArray ->
                @Suppress("UNCHECKED_CAST")
                val apiKey = flowsArray[0] as String?
                val uiState = flowsArray[1] as DashboardState
                val transactions = flowsArray[2] as List<Transaction>
                val allocations = flowsArray[3] as List<Allocation>
                val debts = flowsArray[4] as List<Debt>
                val savingsBoxes = flowsArray[5] as List<SavingsBox>
                val investments = flowsArray[6] as List<Investment>
                val subscriptions = flowsArray[7] as List<Subscription>

                if (apiKey.isNullOrBlank()) {
                    return@combine // Skip if no API key
                }

                // Cooldown mechanism: only generate new insight if last one was more than X hours ago
                val lastSweepSuggestionTimestamp = userRootRef()?.child("insights")?.child("lastSweepSuggestion")
                    ?.get()?.await()?.getValue(Long::class.java) ?: 0L
                val currentTime = System.currentTimeMillis()
                val COOLDOWN_PERIOD_MS = 24 * 60 * 60 * 1000L // 24 hours

                if ((currentTime - lastSweepSuggestionTimestamp) < COOLDOWN_PERIOD_MS) {
                    return@combine // Skip if still in cooldown
                }

                // Only suggest sweep if there's a positive current balance
                if (uiState.currentBalance <= 0) {
                    return@combine
                }

                // Construct financialData string for the AI
                val currentMonthTransactions = transactions.filter { isCurrentMonth(it.date) }
                val monthlyIncome = currentMonthTransactions.filterIsInstance<Income>().sumOf { it.amount }
                val monthlyExpense = currentMonthTransactions.filterIsInstance<Expense>().sumOf { it.amount }
                val totalAllocated = allocations.sumOf { it.amount }
                val currentBalance = monthlyIncome - monthlyExpense - totalAllocated

                val totalDebtAmount = debts.sumOf { it.totalAmount }
                val totalSavingsAmount = savingsBoxes.sumOf { it.currentAmount }
                val totalInvestmentValue = investments.sumOf { it.quantity * it.currentPrice }
                val upcomingSubscriptionPayments = subscriptions.sumOf { it.amount }

                val financialData = """
                    - Saldo Atual: R$$currentBalance
                    - Receita Total (mês): R$$monthlyIncome
                    - Despesa Total (mês): R$$monthlyExpense
                    - Dívida Total: R$$totalDebtAmount
                    - Poupança Total: R$$totalSavingsAmount
                    - Investimento Total: R$$totalInvestmentValue
                    - Assinaturas Mensais: R$$upcomingSubscriptionPayments
                """.trimIndent()

                val sweepSuggestion = geminiAiService.suggestSweepAction(apiKey, financialData)

                sweepSuggestion?.let { suggestion ->
                    repository.saveNotification(
                        Notification(
                            id = "notif_sweep_${System.currentTimeMillis()}",
                            message = suggestion,
                            type = "sweep_suggestion"
                        )
                    )
                    // Update last sweep suggestion timestamp
                    userRootRef()?.child("insights")?.child("lastSweepSuggestion")?.setValue(currentTime)
                }
            }.collect()
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

    fun addFundsToSavingsBox(box: SavingsBox, amount: Double) {
        viewModelScope.launch {
            if (amount <= 0) return@launch

            val currentBalance = _uiState.value.currentBalance
            if (currentBalance < amount) {
                // TODO: Show error notification to user
                return@launch
            }

            val newExpense = Expense(
                id = "exp_transfer_${System.currentTimeMillis()}",
                amount = amount,
                date = Date(),
                description = "Transferência para a caixinha '${box.name}'",
                category = "Transferência",
                relatedSavingsBoxId = box.id // Set the relatedSavingsBoxId
            )

            val updatedBox = box.copy(currentAmount = box.currentAmount + amount)

            repository.addFundsToSavingsBox(newExpense, updatedBox)
        }
    }

    fun addExpense(amount: Double, description: String, category: String, paymentMethod: String) {
        viewModelScope.launch {
            val finalCategory = if (category.isBlank()) {
                geminiAiService.categorizeTransaction(repository.getApiKey().firstOrNull() ?: "", description) ?: "Outros"
            } else {
                category
            }

            val currentBalance =
                (_uiState.value.totalIncome - _uiState.value.totalExpense - _uiState.value.totalAllocated)
            val categoryLimit =
                repository.getCategoryLimit(finalCategory).firstOrNull() ?: Double.MAX_VALUE
            val spentInCategory =
                _uiState.value.transactions.filterIsInstance<Expense>()
                    .filter { it.category == finalCategory }
                    .sumOf { it.amount }

            if (currentBalance < amount) {
                val prompt =
                    "O usuário está tentando adicionar uma despesa de R$${amount} na categoria '${category}'. O saldo atual dele é de R$${currentBalance}. Isso fará o saldo ficar negativo. Como um gerente financeiro guardião, gere uma mensagem de alerta proativa e construtiva para o usuário, explicando o risco e sugerindo uma ação."
                val aiMessage = geminiAiService.getFinancialAnalysis(
                    repository.getApiKey().firstOrNull() ?: "",
                    prompt
                )
                repository.saveNotification(
                    Notification(
                        id = "notif_${System.currentTimeMillis()}",
                        message = aiMessage,
                        type = "expense_alert"
                    )
                )
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

    private suspend fun getDashboardInsight(apiKey: String): String {
        if (apiKey.isBlank()) {
            return "Chave de API do Gemini não configurada."
        }

        try {
            val lastAnalysisTimestamp = userRootRef()?.child("insights")?.child("lastDashboardAnalysis")
                ?.get()?.await()?.getValue(Long::class.java) ?: 0L

            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastAnalysisTimestamp) < 24 * 60 * 60 * 1000) { // 24 hours
                val cachedInsight = userRootRef()?.child("insights")?.child("dashboardInsight")
                    ?.get()?.await()?.getValue(String::class.java)
                if (!cachedInsight.isNullOrBlank()) {
                    return cachedInsight
                }
            }

            val insight = generateNewDashboardInsight(apiKey)

            userRootRef()?.child("insights")?.updateChildren(mapOf(
                "dashboardInsight" to insight,
                "lastDashboardAnalysis" to currentTime
            ))

            return insight
        } catch (e: Exception) {
            return "Não foi possível gerar um insight da IA no momento."
        }
    }

    private suspend fun generateNewDashboardInsight(apiKey: String): String {
        val transactions = repository.getTransactions().firstOrNull() ?: emptyList()
        val totalIncome = transactions.filterIsInstance<Income>().sumOf { it.amount }

        if (transactions.isEmpty() && totalIncome == 0.0) {
            return "Primeiro passo: arrumar um emprego."
        }

        val totalExpense = transactions.filterIsInstance<Expense>().sumOf { it.amount }
        val currentBalance = totalIncome - totalExpense - (repository.getAllocations().firstOrNull()?.sumOf { it.amount } ?: 0.0)
        val savingsBoxes = repository.getSavingsBoxes().firstOrNull() ?: emptyList()
        val rules = repository.getRules().firstOrNull() ?: emptyList()

        val financialData = """
            - Saldo Atual: R$$currentBalance
            - Receita Total (período): R$$totalIncome
            - Despesa Total (período): R$$totalExpense
            - Metas de Poupança: ${savingsBoxes.joinToString { "${it.name} (Guardado: R$${it.currentAmount}, Meta: R$${it.targetAmount})" }}
            - Regras de Alocação: ${rules.joinToString { "${it.name} (${it.value}${if (it.type == com.ecliptia.oikos.data.model.AllocationType.PERCENTAGE) "%" else " R$"})" }}
            - Últimas 5 Transações: ${transactions.take(5).joinToString { "${it.description} (R$${it.amount})" }}
        """.trimIndent()

        val prompt = """
            Você é um consultor financeiro amigável e direto ao ponto. Analise os seguintes dados financeiros de um usuário e forneça UMA ÚNICA frase de insight acionável e concisa para ser exibida no painel principal. A frase deve ser curta, impactante e fácil de entender. Foque no ponto mais crítico ou na maior oportunidade.

            Exemplos de frases que você pode gerar:
            - "Você está gastando mais do que ganha. Vamos rever suas despesas."
            - "Ótimo trabalho guardando dinheiro! Que tal aumentar um pouco a meta 'Viagem'?"
            - "A maior parte das suas despesas é com 'Comida'. Há espaço para economizar aí."
            - "Você não tem uma regra de alocação para emergências. Considere criar uma."

            Dados Financeiros do Usuário:
            $financialData

            Gere apenas a frase de insight, sem nenhuma introdução ou despedida.
        """.trimIndent()

        return geminiAiService.getFinancialAnalysis(apiKey, prompt)
    }

    private fun getNextBillingDate(firstBillDate: Long, cycle: BillingCycle): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = firstBillDate
        val now = Calendar.getInstance()

        if (calendar.after(now)) {
            return calendar.time
        }

        when (cycle) {
            BillingCycle.MONTHLY -> {
                while (calendar.before(now)) {
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            BillingCycle.YEARLY -> {
                while (calendar.before(now)) {
                    calendar.add(Calendar.YEAR, 1)
                }
            }
        }
        return calendar.time
    }
}