package com.ecliptia.oikos.ui.features.debts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ecliptia.oikos.data.model.Debt

@Composable
fun AddDebtDialog(
    onDismissRequest: () -> Unit,
    onSave: (name: String, totalAmount: Double, interestRate: Double, minimumPayment: Double, settlementOfferAmount: Double?, settlementOfferDetails: String?) -> Unit,
    debtToEdit: Debt? = null
) {
    var name by remember { mutableStateOf(debtToEdit?.name ?: "") }
    var totalAmount by remember { mutableStateOf(debtToEdit?.totalAmount?.toString() ?: "") }
    var interestRate by remember { mutableStateOf(debtToEdit?.interestRate?.toString() ?: "") }
    var minimumPayment by remember { mutableStateOf(debtToEdit?.minimumPayment?.toString() ?: "") }
    var settlementOfferAmount by remember { mutableStateOf(debtToEdit?.settlementOfferAmount?.toString() ?: "") }
    var settlementOfferDetails by remember { mutableStateOf(debtToEdit?.settlementOfferDetails ?: "") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (debtToEdit == null) "Adicionar Nova Dívida" else "Editar Dívida") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Dívida (ex: Cartão)") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Valor Total") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("R$") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Taxa de Juros Anual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("%") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = minimumPayment,
                    onValueChange = { minimumPayment = it },
                    label = { Text("Pagamento Mínimo Mensal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("R$") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = settlementOfferAmount,
                    onValueChange = { settlementOfferAmount = it },
                    label = { Text("Valor da Oferta de Quitação (Opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("R$") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = settlementOfferDetails, // Corrected to settlementOfferDetails
                    onValueChange = { settlementOfferDetails = it },
                    label = { Text("Detalhes da Oferta (Opcional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    name,
                    totalAmount.toDoubleOrNull() ?: 0.0,
                    interestRate.toDoubleOrNull() ?: 0.0,
                    minimumPayment.toDoubleOrNull() ?: 0.0,
                    settlementOfferAmount.toDoubleOrNull(),
                    settlementOfferDetails.ifBlank { null }
                )
                onDismissRequest()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        }
    )
}