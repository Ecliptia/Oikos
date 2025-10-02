package com.ecliptia.oikos.ui.features.investments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ecliptia.oikos.data.model.InvestmentType
import com.ecliptia.oikos.data.model.Investment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentDialog(
    onDismissRequest: () -> Unit,
    onSave: (name: String, ticker: String, type: InvestmentType, quantity: Double, purchasePrice: Double, currentPrice: Double) -> Unit,
    investmentToEdit: Investment? = null
) {
    var name by remember { mutableStateOf(investmentToEdit?.name ?: "") }
    var ticker by remember { mutableStateOf(investmentToEdit?.ticker ?: "") }
    var type by remember { mutableStateOf(investmentToEdit?.type ?: InvestmentType.OTHER) }
    var quantity by remember { mutableStateOf(investmentToEdit?.quantity?.toString() ?: "") }
    var purchasePrice by remember { mutableStateOf(investmentToEdit?.purchasePrice?.toString() ?: "") }
    var currentPrice by remember { mutableStateOf(investmentToEdit?.currentPrice?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (investmentToEdit == null) "Adicionar Novo Investimento" else "Editar Investimento") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Ticker/Símbolo") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = type.name,
                        onValueChange = {},
                        label = { Text("Tipo de Investimento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        InvestmentType.values().forEach { investmentType ->
                            DropdownMenuItem(
                                text = { Text(investmentType.name) },
                                onClick = { type = investmentType; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantidade") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Preço de Compra (por unidade)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = currentPrice, onValueChange = { currentPrice = it }, label = { Text("Preço Atual (por unidade)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    name,
                    ticker,
                    type,
                    quantity.toDoubleOrNull() ?: 0.0,
                    purchasePrice.toDoubleOrNull() ?: 0.0,
                    currentPrice.toDoubleOrNull() ?: 0.0
                )
                onDismissRequest()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        }
    )
}
