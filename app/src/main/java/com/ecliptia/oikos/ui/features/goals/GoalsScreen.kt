package com.ecliptia.oikos.ui.features.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.ecliptia.oikos.data.model.SavingsBox
import com.ecliptia.oikos.navigation.AppDestinations
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GoalsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel(),
    showDialog: Boolean,
    onDismissDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var savingsBoxToEdit by remember { mutableStateOf<SavingsBox?>(null) }

    if (showDialog || savingsBoxToEdit != null) {
        AddGoalDialog(
            onDismissRequest = {
                onDismissDialog()
                savingsBoxToEdit = null
            },
            onSave = { name, targetAmount, monthlyContributionTarget ->
                if (savingsBoxToEdit == null) {
                    viewModel.addSavingsBox(name, targetAmount, monthlyContributionTarget)
                } else {
                    viewModel.updateSavingsBox(savingsBoxToEdit!!.copy(
                        name = name,
                        targetAmount = targetAmount,
                        monthlyContributionTarget = monthlyContributionTarget
                    ))
                }
                savingsBoxToEdit = null
            },
            savingsBoxToEdit = savingsBoxToEdit
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.savingsBoxes.isEmpty()) {
            Text("Nenhuma meta definida ainda. Adicione uma meta para começar!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.savingsBoxes) { savingsBox ->
                    GoalItem(
                        savingsBox = savingsBox,
                        onDelete = { viewModel.deleteSavingsBox(savingsBox) },
                        onEdit = { savingsBoxToEdit = it },
                        onClick = { navController.navigate("${AppDestinations.SAVINGS_BOX_HISTORY_ROUTE}/${it.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun GoalItem(savingsBox: SavingsBox, onDelete: () -> Unit, onEdit: (SavingsBox) -> Unit, onClick: (SavingsBox) -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
    val hasTarget = savingsBox.targetAmount > 0
    val progress = if (hasTarget) (savingsBox.currentAmount / savingsBox.targetAmount).toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(savingsBox) },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = savingsBox.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = { onEdit(savingsBox) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Meta", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Meta", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (hasTarget) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progresso: ${currencyFormat.format(savingsBox.currentAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Meta: ${currencyFormat.format(savingsBox.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}% Completo",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            } else {
                Text(
                    text = "Meta Aberta: ${currencyFormat.format(savingsBox.currentAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Contribuição Mensal: ${currencyFormat.format(savingsBox.monthlyContributionTarget)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismissRequest: () -> Unit,
    onSave: (name: String, targetAmount: Double, monthlyContributionTarget: Double) -> Unit,
    savingsBoxToEdit: SavingsBox? = null
) {
    var name by remember { mutableStateOf(savingsBoxToEdit?.name ?: "") }
    var targetAmountText by remember { mutableStateOf(savingsBoxToEdit?.targetAmount?.toString() ?: "") }
    var monthlyContributionTargetText by remember { mutableStateOf(savingsBoxToEdit?.monthlyContributionTarget?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (savingsBoxToEdit == null) "Adicionar Nova Meta" else "Editar Meta") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Meta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Valor Alvo (0 para meta aberta)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("R$") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = monthlyContributionTargetText,
                    onValueChange = { monthlyContributionTargetText = it },
                    label = { Text("Meta de Contribuição Mensal") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("R$") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = targetAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                val monthlyContribution = monthlyContributionTargetText.replace(",", ".").toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && amount >= 0) { // Changed validation to allow 0.0
                    onSave(name, amount, monthlyContribution)
                }
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}