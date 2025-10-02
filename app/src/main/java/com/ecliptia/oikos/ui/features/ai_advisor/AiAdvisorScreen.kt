package com.ecliptia.oikos.ui.features.ai_advisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ecliptia.oikos.data.model.FinancialPlan
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AiAdvisorScreen(
    modifier: Modifier = Modifier,
    viewModel: AiAdvisorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Oikos: Seu Planejador Financeiro",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            items(uiState.messages.reversed()) { message ->
                if (message.plan != null) {
                    FinancialPlanCard(plan = message.plan, onApply = { viewModel.applyPlan(message.plan) })
                } else {
                    ChatMessageBubble(message = message)
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                label = { Text("Descreva seu salário e metas...") },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 3
            )
            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        viewModel.sendMessage(messageInput)
                        messageInput = ""
                    }
                },
                enabled = !uiState.isLoading
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar Mensagem")
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                color = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FinancialPlanCard(plan: FinancialPlan, onApply: () -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Plano Mensal — ${currencyFormat.format(plan.monthlyIncome)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = plan.planSummary, style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Deductions
            plan.deductions.forEach {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${it.name} (${if (it.type == "PERCENTAGE") "${it.value}%" else "Fixo"})")
                    Text(text = "- ${currencyFormat.format(it.amount)}", color = MaterialTheme.colorScheme.error)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Allocations
            Text("O que sobra:", style = MaterialTheme.typography.titleMedium)
            plan.allocations.forEach {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = it.name)
                    Text(text = currencyFormat.format(it.amount), color = if(it.category == "SAVINGS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Projections
            Text("Projeção em 12 meses:", style = MaterialTheme.typography.titleMedium)
            Text(text = plan.projections.projectionSummary)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar este Plano")
            }
        }
    }
}
