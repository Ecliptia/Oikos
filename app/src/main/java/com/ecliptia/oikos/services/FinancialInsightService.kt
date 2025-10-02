package com.ecliptia.oikos.services

import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.ui.theme.FinancialStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialInsightService @Inject constructor(
    private val repository: OikosRepository,
    private val geminiAiService: GeminiAiService,
    private val db: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    private val currentUser get() = auth.currentUser
    private fun userRootRef() = currentUser?.uid?.let { db.getReference("users").child(it) }

    suspend fun getDashboardInsight(apiKey: String): String {
        if (apiKey.isBlank()) {
            return "Chave de API do Gemini não configurada."
        }

        try {
            val lastAnalysisTimestamp = userRootRef()?.child("insights")?.child("lastDashboardAnalysis")
                ?.get()?.await()?.getValue(Long::class.java) ?: 0L

            val currentTime = System.currentTimeMillis()
            // Only generate new insight if 24 hours have passed
            if ((currentTime - lastAnalysisTimestamp) < 24 * 60 * 60 * 1000) {
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

    suspend fun generateEducationalInsight(apiKey: String, financialStatus: FinancialStatus, financialData: String): String? {
        if (apiKey.isBlank()) {
            return null // No API key, no insight
        }

        val prompt = when (financialStatus) {
            FinancialStatus.BAD -> """
                Você é Oikos, um mentor financeiro. O usuário está em uma situação financeira RUIM. Com base nos dados financeiros abaixo, forneça uma dica educacional urgente e acionável para ajudá-lo a sair dessa situação. Foque em um único conselho prático e direto.
                Dados Financeiros do Usuário:
                $financialData
                Dica:
            """.trimIndent()
            FinancialStatus.WARNING -> """
                Você é Oikos, um mentor financeiro. O usuário está em uma situação financeira de ALERTA. Com base nos dados financeiros abaixo, forneça uma dica educacional importante para ajudá-lo a melhorar sua situação e evitar problemas futuros. Foque em um único conselho prático e direto.
                Dados Financeiros do Usuário:
                $financialData
                Dica:
            """.trimIndent()
            FinancialStatus.GOOD -> """
                Você é Oikos, um mentor financeiro. O usuário está em uma situação financeira BOA. Com base nos dados financeiros abaixo, forneça uma dica educacional para otimizar suas finanças, como começar a investir ou aumentar suas economias. Foque em um único conselho prático e direto.
                Dados Financeiros do Usuário:
                $financialData
                Dica:
            """.trimIndent()
            FinancialStatus.EXCELLENT -> """
                Você é Oikos, um mentor financeiro. O usuário está em uma situação financeira EXCELENTE. Com base nos dados financeiros abaixo, forneça uma dica educacional avançada para manter e expandir sua riqueza, como diversificação de investimentos ou planejamento de longo prazo. Foque em um único conselho prático e direto.
                Dados Financeiros do Usuário:
                $financialData
                Dica:
            """.trimIndent()
            FinancialStatus.NEUTRAL -> null // No specific educational insight for neutral status
        }

        return prompt?.let {
            try {
                geminiAiService.getFinancialAnalysis(apiKey, it)
            } catch (e: Exception) {
                null // Return null if AI call fails
            }
        }
    }

    private suspend fun generateNewDashboardInsight(apiKey: String): String {
        val transactions = repository.getTransactions().firstOrNull() ?: emptyList()
        val totalIncome = transactions.filterIsInstance<Income>().sumOf { it.amount }
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
}