package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddExpenseDialog(
    onDismissRequest: () -> Unit,
    onSave: (amount: Double, description: String, category: String, paymentMethod: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Adicionar Nova Despesa") },
        text = {
            Column {
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Método (Cartão, Pix, etc.)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(amount.toDoubleOrNull() ?: 0.0, description, category, paymentMethod)
                    onDismissRequest()
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
