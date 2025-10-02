package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AddFundsToSavingsBoxDialog(
    boxName: String,
    onDismissRequest: () -> Unit,
    onSave: (amount: Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Adicionar à caixinha \"$boxName\"") },
        text = {
            Column {
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("R$ ") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(amount.replace(",", ".").toDoubleOrNull() ?: 0.0)
                    onDismissRequest()
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
