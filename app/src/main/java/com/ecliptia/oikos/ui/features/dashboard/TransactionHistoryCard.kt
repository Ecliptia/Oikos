package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Transaction
import com.ecliptia.oikos.navigation.AppDestinations
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionHistoryCard(state: DashboardState, navController: NavController) {
    if (state.transactions.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Histórico Recente", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            if (state.transactions.isEmpty()) {
                Text(text = "Nenhuma transação registrada.", modifier = Modifier.padding(16.dp))
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    state.transactions.take(10).forEachIndexed { index, transaction ->
                        TransactionItem(transaction = transaction)
                        if (index < 4 && index < state.transactions.size - 1) { // Add divider between items
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
                // "Ver Tudo" button
                if (state.transactions.size > 10) { // Only show if there are more transactions than displayed
                    TextButton(
                        onClick = { navController.navigate(AppDestinations.WALLET_ROUTE) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Ver Tudo")
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault()) // Shortened date format

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f), // Added weight to this Row
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val (icon, color) = when (transaction) {
                is Income -> Pair(Icons.Default.ArrowDownward, MaterialTheme.colorScheme.primary)
                is Expense -> Pair(Icons.Default.ArrowUpward, MaterialTheme.colorScheme.error)
            }
            Icon(imageVector = icon, contentDescription = null, tint = color)

            Column(modifier = Modifier.weight(1f)) { // This Column still needs weight to expand within its parent Row
                Text(
                    text = transaction.description,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    softWrap = true,
                    maxLines = 2
                )
                val subtext = when (transaction) {
                    is Expense -> transaction.category
                    is Income -> if(transaction.source.isNotBlank()) transaction.source else ""
                }
                if (subtext.isNotBlank()) {
                    Text(text = subtext, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val (color, sign) = when (transaction) {
                is Income -> Pair(MaterialTheme.colorScheme.primary, "+")
                is Expense -> Pair(MaterialTheme.colorScheme.error, "-")
            }
            Text(
                text = "$sign ${currencyFormat.format(transaction.amount)}",
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = dateFormat.format(transaction.date), style = MaterialTheme.typography.bodySmall)
        }
    }
}