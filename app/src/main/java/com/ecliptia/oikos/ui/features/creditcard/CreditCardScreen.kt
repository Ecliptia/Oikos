package com.ecliptia.oikos.ui.features.creditcard

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.data.model.CreditCard
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreditCardScreen(
    modifier: Modifier = Modifier,
    viewModel: CreditCardViewModel = hiltViewModel(),
    showDialog: Boolean,
    onDismissDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cardToEdit by remember { mutableStateOf<CreditCard?>(null) }

    if (showDialog || cardToEdit != null) {
        AddCreditCardDialog(
            onDismissRequest = {
                onDismissDialog()
                cardToEdit = null
            },
            onSave = { name, limit, closingDay, dueDate, brand ->
                if (cardToEdit == null) {
                    viewModel.addCreditCard(name, limit, closingDay, dueDate, brand)
                } else {
                    viewModel.updateCreditCard(cardToEdit!!.copy(
                        name = name,
                        limit = limit,
                        closingDay = closingDay,
                        dueDate = dueDate,
                        brand = brand
                    ))
                }
                cardToEdit = null
            },
            cardToEdit = cardToEdit
        )
    }

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (uiState.creditCards.isEmpty()) {
            EmptyState()
        } else {
            CreditCardList(
                cards = uiState.creditCards,
                onDelete = { viewModel.deleteCreditCard(it) },
                onEdit = { cardToEdit = it }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Você não tem cartões de crédito cadastrados.\nClique no botão '+' para adicionar seu primeiro cartão.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun CreditCardList(cards: List<CreditCard>, onDelete: (CreditCard) -> Unit, onEdit: (CreditCard) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cards) { card ->
            CreditCardItem(
                card = card,
                onDelete = { onDelete(card) },
                onEdit = { onEdit(card) }
            )
        }
    }
}

@Composable
private fun CreditCardItem(card: CreditCard, onDelete: () -> Unit, onEdit: () -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(card.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = { onEdit() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Cartão", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Cartão", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Limite: ${currencyFormat.format(card.limit)}", style = MaterialTheme.typography.bodyMedium)
            Text("Bandeira: ${card.brand.name}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Fechamento: Dia ${card.closingDay}", style = MaterialTheme.typography.bodySmall)
            Text("Vencimento: Dia ${card.dueDate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
