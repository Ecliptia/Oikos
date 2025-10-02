package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ecliptia.oikos.data.model.AllocationRule
import com.ecliptia.oikos.data.model.AllocationType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AllocationRulesCard(rules: List<AllocationRule>) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Suas Regras Mensais", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            if (rules.isEmpty()) {
                Text(text = "Nenhuma regra de alocação definida.", modifier = Modifier.padding(16.dp))
            } else {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rules.forEach { rule ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = rule.name, fontWeight = FontWeight.Medium)
                            val valueText = if (rule.type == AllocationType.PERCENTAGE) {
                                "${rule.value}%"
                            } else {
                                currencyFormat.format(rule.value)
                            }
                            Text(text = valueText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
