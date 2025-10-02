package com.ecliptia.oikos.ui.features.recurring_transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.data.model.Frequency
import com.ecliptia.oikos.data.model.RecurringTransaction
import com.ecliptia.oikos.data.model.TransactionType
import com.ecliptia.oikos.ui.features.category_management.CategoryManagementViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecurringTransactionsScreen(
    modifier: Modifier = Modifier,
    viewModel: RecurringTransactionsViewModel = hiltViewModel(),
    showDialog: Boolean, // New parameter
    onDismissDialog: () -> Unit // New parameter
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<RecurringTransaction?>(null) }

    if (showDialog || showAddEditDialog) {
        AddEditRecurringTransactionDialog(
            onDismissRequest = {
                showAddEditDialog = false
                transactionToEdit = null
                onDismissDialog()
            },
            onSave = { name, amount, type, category, source, frequency, nextDueDate, isActive ->
                if (transactionToEdit == null) {
                    viewModel.addRecurringTransaction(
                        RecurringTransaction(
                            id = "rec_${System.currentTimeMillis()}",
                            name = name,
                            amount = amount,
                            type = type,
                            category = category,
                            source = source,
                            frequency = frequency,
                            nextDueDate = nextDueDate,
                            isActive = isActive
                        )
                    )
                } else {
                    viewModel.updateRecurringTransaction(
                        transactionToEdit!!.copy(
                            name = name,
                            amount = amount,
                            type = type,
                            category = category,
                            source = source,
                            frequency = frequency,
                            nextDueDate = nextDueDate,
                            isActive = isActive
                        )
                    )
                }
                transactionToEdit = null
            },
            transactionToEdit = transactionToEdit
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Transações Recorrentes",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.recurringTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhuma transação recorrente definida ainda.\nClique no botão '+' para adicionar sua primeira.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.recurringTransactions) { transaction ->
                    RecurringTransactionItem(
                        transaction = transaction,
                        onEdit = {
                            transactionToEdit = it
                            showAddEditDialog = true
                        },
                        onDelete = { viewModel.deleteRecurringTransaction(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecurringTransactionItem(
    transaction: RecurringTransaction,
    onEdit: (RecurringTransaction) -> Unit,
    onDelete: (RecurringTransaction) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(transaction) },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${currencyFormat.format(transaction.amount)} (${transaction.frequency.name})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Próximo: ${dateFormat.format(Date(transaction.nextDueDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onEdit(transaction) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Transação Recorrente", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onDelete(transaction) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir Transação Recorrente", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringTransactionDialog(
    onDismissRequest: () -> Unit,
    onSave: (name: String, amount: Double, type: TransactionType, category: String?, source: String?, frequency: Frequency, nextDueDate: Long, isActive: Boolean) -> Unit,
    transactionToEdit: RecurringTransaction? = null
) {
    var name by remember { mutableStateOf(transactionToEdit?.name ?: "") }
    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(transactionToEdit?.category ?: "") }
    var source by remember { mutableStateOf(transactionToEdit?.source ?: "") }
    var frequency by remember { mutableStateOf(transactionToEdit?.frequency ?: Frequency.MONTHLY) }
    var nextDueDate by remember { mutableStateOf(transactionToEdit?.nextDueDate ?: Date().time) }
    var isActive by remember { mutableStateOf(transactionToEdit?.isActive ?: true) }

    var typeExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = nextDueDate }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            nextDueDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Fetch categories for dropdown
    val categoryViewModel: CategoryManagementViewModel = hiltViewModel()
    val categoriesState by categoryViewModel.uiState.collectAsState()
    val availableCategories = categoriesState.categories.map { it.name }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (transactionToEdit == null) "Adicionar Transação Recorrente" else "Editar Transação Recorrente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("R$") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Transaction Type Dropdown
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = if (type == TransactionType.INCOME) "Receita" else "Despesa",
                        onValueChange = {},
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        TransactionType.values().forEach { transactionType ->
                            DropdownMenuItem(
                                text = { Text(if (transactionType == TransactionType.INCOME) "Receita" else "Despesa") },
                                onClick = { type = transactionType; typeExpanded = false }
                            )
                        }
                    }
                }

                // Category/Source based on type
                if (type == TransactionType.EXPENSE) {
                    // Category Dropdown (using managed categories)
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = category, // Display selected category name
                            onValueChange = {}, // Read-only
                            label = { Text("Categoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            availableCategories.forEach { catName ->
                                DropdownMenuItem(
                                    text = { Text(catName) },
                                    onClick = { category = catName; categoryExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Fonte") }, modifier = Modifier.fillMaxWidth())
                }

                // Frequency Dropdown
                ExposedDropdownMenuBox(expanded = frequencyExpanded, onExpandedChange = { frequencyExpanded = !frequencyExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = frequency.name,
                        onValueChange = {},
                        label = { Text("Frequência") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) }
                    )
                    ExposedDropdownMenu(expanded = frequencyExpanded, onDismissRequest = { frequencyExpanded = false }) {
                        Frequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.name) },
                                onClick = { frequency = freq; frequencyExpanded = false }
                            )
                        }
                    }
                }

                // Next Due Date Picker
                OutlinedTextField(
                    value = dateFormat.format(Date(nextDueDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Próxima Data de Vencimento") },
                    trailingIcon = { 
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ativo", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    name,
                    amount.toDoubleOrNull() ?: 0.0,
                    type,
                    category.takeIf { type == TransactionType.EXPENSE },
                    source.takeIf { type == TransactionType.INCOME },
                    frequency,
                    nextDueDate,
                    isActive
                )
                onDismissRequest()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        }
    )
}