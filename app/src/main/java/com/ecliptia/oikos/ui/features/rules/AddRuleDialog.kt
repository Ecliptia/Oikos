package com.ecliptia.oikos.ui.features.rules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ecliptia.oikos.data.model.AllocationRule
import com.ecliptia.oikos.data.model.AllocationType

@Composable
fun AddRuleDialog(
    onDismissRequest: () -> Unit,
    onSave: (AllocationRule) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AllocationType.PERCENTAGE) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Adicionar Nova Regra") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Regra") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    listOf(AllocationType.PERCENTAGE, AllocationType.FIXED_AMOUNT).forEach { allocationType ->
                        Row(
                            Modifier
                                .selectable(
                                    selected = (allocationType == type),
                                    onClick = { type = allocationType }
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (allocationType == type),
                                onClick = { type = allocationType }
                            )
                            Text(text = if (allocationType == AllocationType.PERCENTAGE) "%" else "R$")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rule = AllocationRule(
                        name = name,
                        type = type,
                        value = value.toDoubleOrNull() ?: 0.0
                    )
                    onSave(rule)
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
