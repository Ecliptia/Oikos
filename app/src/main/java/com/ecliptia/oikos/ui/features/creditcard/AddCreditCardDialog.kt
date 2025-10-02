package com.ecliptia.oikos.ui.features.creditcard

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
import com.ecliptia.oikos.data.model.CardBrand
import com.ecliptia.oikos.data.model.CreditCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardDialog(
    onDismissRequest: () -> Unit,
    onSave: (name: String, limit: Double, closingDay: Int, dueDate: Int, brand: CardBrand) -> Unit,
    cardToEdit: CreditCard? = null
) {
    var name by remember { mutableStateOf(cardToEdit?.name ?: "") }
    var limit by remember { mutableStateOf(cardToEdit?.limit?.toString() ?: "") }
    var closingDay by remember { mutableStateOf(cardToEdit?.closingDay?.toString() ?: "") }
    var dueDate by remember { mutableStateOf(cardToEdit?.dueDate?.toString() ?: "") }
    var brand by remember { mutableStateOf(cardToEdit?.brand ?: CardBrand.MASTERCARD) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (cardToEdit == null) "Adicionar Novo Cartão" else "Editar Cartão") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Cartão") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Limite") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), prefix = { Text("R$") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = closingDay, onValueChange = { closingDay = it }, label = { Text("Dia do Fechamento") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Dia do Vencimento") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = brand.name,
                        onValueChange = {},
                        label = { Text("Bandeira") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        CardBrand.values().forEach { cardBrand ->
                            DropdownMenuItem(
                                text = { Text(cardBrand.name) },
                                onClick = { brand = cardBrand; expanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    name,
                    limit.toDoubleOrNull() ?: 0.0,
                    closingDay.toIntOrNull() ?: 1,
                    dueDate.toIntOrNull() ?: 10,
                    brand
                )
                onDismissRequest()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        }
    )
}
