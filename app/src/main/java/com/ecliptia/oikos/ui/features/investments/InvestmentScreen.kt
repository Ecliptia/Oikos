package com.ecliptia.oikos.ui.features.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.data.model.Investment
import java.text.NumberFormat
import java.util.Locale

@Composable
fun InvestmentScreen(
    modifier: Modifier = Modifier,
    viewModel: InvestmentViewModel = hiltViewModel(),
    showDialog: Boolean,
    onDismissDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var investmentToEdit by remember { mutableStateOf<Investment?>(null) }

    if (showDialog || showEditDialog) {
        AddInvestmentDialog(
            onDismissRequest = {
                onDismissDialog()
                showEditDialog = false
                investmentToEdit = null
            },
            onSave = { name, ticker, type, quantity, purchasePrice, currentPrice ->
                if (investmentToEdit == null) {
                    viewModel.addInvestment(name, ticker, type, quantity, purchasePrice, currentPrice)
                } else {
                    viewModel.updateInvestment(investmentToEdit!!.copy(
                        name = name,
                        ticker = ticker,
                        type = type,
                        quantity = quantity,
                        purchasePrice = purchasePrice,
                        currentPrice = currentPrice
                    ))
                }
            },
            investmentToEdit = investmentToEdit
        )
    }

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (uiState.investments.isEmpty()) {
            EmptyState()
        } else {
            InvestmentSummary(investments = uiState.investments)
            Spacer(modifier = Modifier.height(16.dp))
            InvestmentList(
                investments = uiState.investments,
                onEdit = { investment ->
                    investmentToEdit = investment
                    showEditDialog = true
                },
                onDelete = { viewModel.deleteInvestment(it) }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Você ainda não tem investimentos cadastrados.\nClique no botão '+' para adicionar seu primeiro investimento.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun InvestmentSummary(investments: List<Investment>) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    val totalPortfolioValue = investments.sumOf { it.quantity * it.currentPrice }
    val totalInvested = investments.sumOf { it.quantity * it.purchasePrice }
    val totalProfitLoss = totalPortfolioValue - totalInvested

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Valor Total da Carteira:", style = MaterialTheme.typography.titleMedium)
            Text(currencyFormat.format(totalPortfolioValue), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Lucro/Prejuízo Total:", style = MaterialTheme.typography.titleSmall)
            Text(
                currencyFormat.format(totalProfitLoss),
                style = MaterialTheme.typography.titleMedium,
                color = if (totalProfitLoss >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun InvestmentList(investments: List<Investment>, onEdit: (Investment) -> Unit, onDelete: (Investment) -> Unit) {
    Text("Seus Investimentos:", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(investments) {
            InvestmentItem(investment = it, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
private fun InvestmentItem(investment: Investment, onEdit: (Investment) -> Unit, onDelete: (Investment) -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    val investedValue = investment.quantity * investment.purchasePrice
    val currentValue = investment.quantity * investment.currentPrice
    val profitLoss = currentValue - investedValue

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(investment.name, style = MaterialTheme.typography.titleMedium)
                    Text(investment.ticker, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    IconButton(onClick = { onEdit(investment) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Investimento", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete(investment) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Investimento", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Quantidade:", style = MaterialTheme.typography.bodySmall)
                    Text(investment.quantity.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Preço Médio:", style = MaterialTheme.typography.bodySmall)
                    Text(currencyFormat.format(investment.purchasePrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Valor Investido:", style = MaterialTheme.typography.bodySmall)
                    Text(currencyFormat.format(investedValue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valor Atual:", style = MaterialTheme.typography.bodySmall)
                    Text(currencyFormat.format(currentValue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lucro/Prejuízo:", style = MaterialTheme.typography.bodySmall)
                Text(
                    currencyFormat.format(profitLoss),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (profitLoss >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
