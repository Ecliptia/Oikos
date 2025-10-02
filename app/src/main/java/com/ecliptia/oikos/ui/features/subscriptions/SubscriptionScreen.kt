package com.ecliptia.oikos.ui.features.subscriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.data.model.BillingCycle
import com.ecliptia.oikos.data.model.Subscription
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionScreen(
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel = hiltViewModel(),
    showDialog: Boolean,
    onDismissDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (showDialog) {
        AddSubscriptionDialog(
            onDismissRequest = onDismissDialog,
            onSave = { name, amount, firstBillDate, category, cycle ->
                viewModel.addSubscription(name, amount, firstBillDate, category, cycle)
            }
        )
    }

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.subscriptions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "Você ainda não tem assinaturas cadastradas.\nClique no botão '+' para adicionar.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.subscriptions) { subscription ->
                SubscriptionItem(
                    subscription = subscription,
                    onDelete = { viewModel.deleteSubscription(subscription) }
                )
            }
        }
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription, onDelete: () -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
    
    fun getNextBillingDate(firstBillDate: Long, cycle: BillingCycle): Date {
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

    val nextBillingDate = getNextBillingDate(subscription.firstBillDate, subscription.billingCycle)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(subscription.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${currencyFormat.format(subscription.amount)} / ${if (subscription.billingCycle == BillingCycle.MONTHLY) "mês" else "ano"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Próxima cobrança: ${dateFormat.format(nextBillingDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir Assinatura", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
