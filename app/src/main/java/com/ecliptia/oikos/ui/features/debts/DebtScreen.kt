package com.ecliptia.oikos.ui.features.debts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.data.model.Debt
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DebtScreen(
    modifier: Modifier = Modifier,
    viewModel: DebtViewModel = hiltViewModel(),
    showDialog: Boolean,
    onDismissDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortedDebts = viewModel.getSortedDebts()
    var debtToEdit by remember { mutableStateOf<Debt?>(null) }

    if (showDialog || debtToEdit != null) {
        AddDebtDialog(
            onDismissRequest = {
                onDismissDialog()
                debtToEdit = null
            },
            onSave = { name, totalAmount, interestRate, minimumPayment, settlementOfferAmount, settlementOfferDetails ->
                if (debtToEdit == null) {
                    viewModel.addDebt(name, totalAmount, interestRate, minimumPayment, settlementOfferAmount, settlementOfferDetails)
                } else {
                    viewModel.updateDebt(debtToEdit!!.copy(
                        name = name,
                        totalAmount = totalAmount,
                        interestRate = interestRate,
                        minimumPayment = minimumPayment,
                        settlementOfferAmount = settlementOfferAmount,
                        settlementOfferDetails = settlementOfferDetails
                    ))
                }
                debtToEdit = null
            },
            debtToEdit = debtToEdit
        )
    }

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (uiState.debts.isEmpty()) {
            EmptyState()
        } else {
            DebtAiInsightCard(state = uiState)
            Spacer(modifier = Modifier.height(16.dp))
            StrategySelector(selectedStrategy = uiState.strategy, onStrategySelected = { viewModel.setStrategy(it) })
            Spacer(modifier = Modifier.height(16.dp))
            DebtPlanList(
                debts = sortedDebts,
                onDelete = { viewModel.deleteDebt(it) },
                onEdit = { debtToEdit = it }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Você não tem dívidas cadastradas.\nClique no botão '+' para adicionar sua primeira dívida e criar um plano de pagamento.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun StrategySelector(selectedStrategy: DebtStrategy, onStrategySelected: (DebtStrategy) -> Unit) {
    Column {
        Text("Escolha sua estratégia:", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.selectableGroup()) {
            StrategyRadioButton(text = "Avalanche", selected = selectedStrategy == DebtStrategy.AVALANCHE, onClick = { onStrategySelected(DebtStrategy.AVALANCHE) })
            Spacer(Modifier.width(16.dp))
            StrategyRadioButton(text = "Bola de Neve", selected = selectedStrategy == DebtStrategy.SNOWBALL, onClick = { onStrategySelected(DebtStrategy.SNOWBALL) })
        }
        val description = when (selectedStrategy) {
            DebtStrategy.AVALANCHE -> "Pague primeiro as dívidas com juros mais altos para economizar mais dinheiro a longo prazo."
            DebtStrategy.SNOWBALL -> "Pague primeiro as menores dívidas para ganhar motivação com vitórias rápidas."
        }
        Text(description, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StrategyRadioButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun DebtPlanList(debts: List<Debt>, onDelete: (Debt) -> Unit, onEdit: (Debt) -> Unit) {
    Text("Sua Ordem de Pagamento:", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(debts) { index, debt ->
            DebtItem(debt = debt, isPriority = index == 0, onDelete = { onDelete(debt) }, onEdit = { onEdit(debt) })
        }
    }
}

@Composable
private fun DebtItem(debt: Debt, isPriority: Boolean, onDelete: (Debt) -> Unit, onEdit: (Debt) -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    val cardColors = if (isPriority) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(debt) }, // Make card clickable for edit
        elevation = CardDefaults.cardElevation(4.dp),
        colors = cardColors
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(debt.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = { onEdit(debt) }) { // Pass debt to onEdit
                        Icon(Icons.Default.Edit, contentDescription = "Editar Dívida", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete(debt) }) { // Pass debt to onDelete
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Dívida", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (isPriority) {
                Text("FOCO ATUAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Valor Total: ${currencyFormat.format(debt.totalAmount)}", style = MaterialTheme.typography.bodyMedium)
            Text("Juros: ${debt.interestRate}% ao ano", style = MaterialTheme.typography.bodyMedium)
            Text("Pagamento Mínimo: ${currencyFormat.format(debt.minimumPayment)}/mês", style = MaterialTheme.typography.bodyMedium)

            debt.settlementOfferAmount?.let { offerAmount ->
                debt.settlementOfferDetails?.let { offerDetails ->
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Oferta de Quitação:", style = MaterialTheme.typography.titleSmall)
                    Text("Valor: ${currencyFormat.format(offerAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Detalhes: $offerDetails", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { /* TODO: Implement logic to accept offer */ }) {
                        Text("Aceitar Oferta")
                    }
                }
            }
        }
    }
}