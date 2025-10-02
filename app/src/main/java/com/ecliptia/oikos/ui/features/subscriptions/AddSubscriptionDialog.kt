package com.ecliptia.oikos.ui.features.subscriptions

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ecliptia.oikos.data.model.BillingCycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionDialog(
    onDismissRequest: () -> Unit,
    onSave: (name: String, amount: Double, firstBillDate: Long, category: String, cycle: BillingCycle) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var expanded by remember { mutableStateOf(false) }

    // Date Picker State
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Nova Assinatura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome (ex: Netflix)") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Valor (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria (ex: Entretenimento)") })

                // Date Picker
                OutlinedTextField(
                    value = dateFormat.format(Date(selectedDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data da primeira cobrança") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data")
                        }
                    }
                )

                // Billing Cycle Dropdown
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = if (cycle == BillingCycle.MONTHLY) "Mensal" else "Anual",
                        onValueChange = {},
                        label = { Text("Ciclo de Cobrança") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Mensal") },
                            onClick = { cycle = BillingCycle.MONTHLY; expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Anual") },
                            onClick = { cycle = BillingCycle.YEARLY; expanded = false }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(name, amount.toDoubleOrNull() ?: 0.0, selectedDate, category, cycle)
                onDismissRequest()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        }
    )
}
