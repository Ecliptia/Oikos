package com.ecliptia.oikos.services

import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiAiService @Inject constructor() {

        suspend fun getFinancialAnalysis(apiKey: String, prompt: String): String {
            return try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash-lite",
                    apiKey = apiKey
                )
    
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Não foi possível obter uma análise."
        } catch (e: Exception) {
            e.localizedMessage ?: "Ocorreu um erro ao contatar a IA."
        }
    }

    suspend fun categorizeTransaction(apiKey: String, description: String): String? {
        if (apiKey.isBlank() || description.isBlank()) {
            return null
        }

        return try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash-lite",
                apiKey = apiKey
            )

            val prompt = """
                Você é um especialista em categorização de despesas. Dada a descrição de uma transação, retorne APENAS a categoria mais apropriada em português. Se não tiver certeza, retorne "Outros".
                Exemplos:
                - "Uber viagem para o trabalho" -> "Transporte"
                - "Compras no supermercado Pão de Açúcar" -> "Alimentação"
                - "Netflix mensalidade" -> "Entretenimento"
                - "Consulta médica" -> "Saúde"
                - "Pagamento de aluguel" -> "Moradia"
                - "Conta de luz" -> "Moradia"
                - "Jantar com amigos" -> "Lazer"
                - "Café na padaria" -> "Alimentação"
                - "Presente de aniversário" -> "Presentes"
                - "Gasolina posto Shell" -> "Transporte"
                - "Academia mensal" -> "Saúde"
                - "Curso de inglês" -> "Educação"
                - "Salário" -> "Receita"
                - "Transferência para poupança" -> "Poupança"

                Descrição da transação: "$description"
                Categoria:
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val category = response.text?.trim()
            if (category.isNullOrBlank() || category == "Não foi possível obter uma análise.") {
                "Outros" // Default to "Outros" if AI fails or returns empty
            } else {
                category
            }
        } catch (e: Exception) {
            "Outros" // Default to "Outros" if AI call fails
        }
    }

    suspend fun suggestSweepAction(apiKey: String, financialData: String): String? {
        if (apiKey.isBlank() || financialData.isBlank()) {
            return null
        }

        return try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash-lite",
                apiKey = apiKey
            )

            val prompt = """
                Você é Oikos, um assistente financeiro inteligente. Analise os seguintes dados financeiros do usuário e, se houver um saldo positivo considerável (dinheiro "sobrando"), sugira UMA ÚNICA ação concisa e acionável para o usuário alocar esse dinheiro. A sugestão deve ser prática e focada em otimização financeira (ex: poupança, investimento, adiantar dívida). Se não houver saldo positivo ou se a situação for apertada, retorne "Nenhuma sugestão de varredura no momento.".

                Dados Financeiros do Usuário:
                $financialData

                Sugestão de varredura:
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val suggestion = response.text?.trim()
            if (suggestion.isNullOrBlank() || suggestion == "Não foi possível obter uma análise.") {
                null // Return null if AI fails or returns empty/generic error
            } else {
                suggestion
            }
        } catch (e: Exception) {
            null // Return null if AI call fails
        }
    }
}
