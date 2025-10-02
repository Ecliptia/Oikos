package com.ecliptia.oikos

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.ecliptia.oikos.services.GeminiAiService
import com.ecliptia.oikos.data.repository.OikosRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Notification
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date

@AndroidEntryPoint
class OikosNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var geminiAiService: GeminiAiService

    @Inject
    lateinit var oikosRepository: OikosRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    @Serializable
    data class TransactionDetails(
        val type: String, // "INCOME" or "EXPENSE"
        val amount: Double,
        val description: String,
        val category: String? = null,
        val source: String? = null // For income
    )

    private val BANKING_APP_PACKAGES = listOf(
        "com.inter.banking", // Banco Inter
        "com.nubank",       // Nubank
        // Add other banking app package names here
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { notification ->
            val packageName = notification.packageName
            val notificationText = notification.notification.tickerText?.toString()
                ?: notification.notification.extras.getString("android.text")
                ?: notification.notification.extras.getString(android.app.Notification.EXTRA_TEXT)
                ?: ""

            Log.d("OikosNLS", "Notification Posted: Package=$packageName, Text=$notificationText")

            if (BANKING_APP_PACKAGES.contains(packageName) && notificationText.isNotBlank()) {
                serviceScope.launch {
                    val apiKey = oikosRepository.getApiKey().firstOrNull()
                    if (apiKey.isNullOrBlank()) {
                        Log.w("OikosNLS", "Gemini API Key not configured. Cannot process notification.")
                        return@launch
                    }

                    val prompt = """
                        Analise a seguinte notificação bancária e extraia os detalhes da transação.
                        Retorne um objeto JSON com os campos:
                        "type": "INCOME" ou "EXPENSE"
                        "amount": Double (valor da transação)
                        "description": String (descrição breve da transação)
                        "category": String (categoria da despesa, se for EXPENSE, ex: "Alimentação", "Transporte", "Outros")
                        "source": String (fonte da receita, se for INCOME, ex: "Salário", "Transferência", "Outros")

                        Se não for possível extrair todos os dados ou se não for uma transação clara, retorne um JSON vazio: {}

                        Exemplos:
                        - Notificação: "Você recebeu um Pix de R$ 150,00 de João Silva."
                          JSON: {"type": "INCOME", "amount": 150.0, "description": "Pix recebido de João Silva", "source": "Transferência"}
                        - Notificação: "Compra aprovada no valor de R$ 45,50 em Padaria Pão Quente."
                          JSON: {"type": "EXPENSE", "amount": 45.50, "description": "Compra em Padaria Pão Quente", "category": "Alimentação"}
                        - Notificação: "Pagamento de fatura Nubank R$ 1200,00."
                          JSON: {"type": "EXPENSE", "amount": 1200.0, "description": "Pagamento de fatura Nubank", "category": "Contas"}
                        - Notificação: "Seu saldo é R$ 5000,00."
                          JSON: {}

                        Notificação: "$notificationText"
                        JSON:
                    """.trimIndent()

                    try {
                        val geminiResponse = geminiAiService.getFinancialAnalysis(apiKey, prompt)
                        Log.d("OikosNLS", "Gemini Response: $geminiResponse")

                        if (geminiResponse.isNotBlank() && geminiResponse != "{}") {
                            val transactionDetails = Json.decodeFromString<TransactionDetails>(geminiResponse)

                            when (transactionDetails.type) {
                                "INCOME" -> {
                                    val income = Income(
                                        id = "notif_inc_${System.currentTimeMillis()}",
                                        amount = transactionDetails.amount,
                                        date = Date(),
                                        description = transactionDetails.description,
                                        source = transactionDetails.source ?: "Outros"
                                    )
                                    oikosRepository.saveIncome(income, emptyList())
                                    Log.i("OikosNLS", "Saved Income from notification: ${income.description}")
                                }
                                "EXPENSE" -> {
                                    val expense = Expense(
                                        id = "notif_exp_${System.currentTimeMillis()}",
                                        amount = transactionDetails.amount,
                                        date = Date(),
                                        description = transactionDetails.description,
                                        category = transactionDetails.category ?: "Outros"
                                    )
                                    oikosRepository.saveExpense(expense)
                                    Log.i("OikosNLS", "Saved Expense from notification: ${expense.description}")
                                }
                                else -> Log.w("OikosNLS", "Unknown transaction type from Gemini: ${transactionDetails.type}")
                            }
                        } else {
                            Log.d("OikosNLS", "Gemini returned empty or unparseable JSON for notification: $notificationText")
                        }
                    } catch (e: Exception) {
                        Log.e("OikosNLS", "Error processing notification with Gemini: ${e.message}", e)
                        // Optionally save a notification to the user about the failure
                        oikosRepository.saveNotification(
                            Notification(
                                id = "notif_ai_fail_${System.currentTimeMillis()}",
                                message = "Não foi possível processar uma notificação bancária automaticamente. Verifique o Logcat para detalhes.",
                                type = "automation_error"
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            Log.d("OikosNLS", "Notification Removed: Package=${it.packageName}")
        }
    }

    override fun onListenerConnected() {
        Log.d("OikosNLS", "Notification Listener Connected")
    }

    override fun onListenerDisconnected() {
        Log.d("OikosNLS", "Notification Listener Disconnected")
    }
}
