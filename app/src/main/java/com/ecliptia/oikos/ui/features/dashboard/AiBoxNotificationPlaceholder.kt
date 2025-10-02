package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AiBoxNotificationPlaceholder(insights: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Notificações da IA", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (insights.isEmpty()) {
                    Text("Nenhuma notificação da IA no momento.")
                } else {
                    insights.forEach { insight ->
                        Text(text = "⚠️ $insight")
                    }
                }
            }
        }
    }
}