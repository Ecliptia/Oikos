package com.ecliptia.oikos.ui.features.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.data.model.Allocation
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Transaction
import com.ecliptia.oikos.ui.features.dashboard.AddExpenseDialog
import com.ecliptia.oikos.ui.features.dashboard.AddIncomeDialog
import com.ecliptia.oikos.ui.features.dashboard.CategorySpendingBars



import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showIncomeDialog) {
        AddIncomeDialog(
            onDismissRequest = { showIncomeDialog = false },
            onSave = { amount, description ->
                viewModel.addIncome(amount, description)
                showIncomeDialog = false
            }
        )
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            onDismissRequest = { showExpenseDialog = false },
            onSave = { amount, description, category, paymentMethod ->
                viewModel.addExpense(amount, description, category, paymentMethod)
                showExpenseDialog = false
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja excluir TODAS as suas informações financeiras? Esta ação é irreversível.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteAllData()
                    showDeleteAllDialog = false
                }) {
                    Text("Excluir Tudo")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = { showIncomeDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Entrada")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(onClick = { showExpenseDialog = true }) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Nova Saída")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Keep horizontal padding
                .verticalScroll(rememberScrollState()), // Make the whole column scrollable
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Minha Carteira", style = MaterialTheme.typography.titleLarge)

            CategorySpendingBars(expensesByCategory = uiState.expensesByCategory)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showDeleteAllDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Deletar Todas as Informações")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Histórico de Transações", style = MaterialTheme.typography.titleMedium)

            // Compare with last month placeholder
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Botão \"comparar com mês passado\" (em breve)")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.transactions.forEach { transaction ->
                    TransactionItem(transaction = transaction, onDelete = { viewModel.deleteTransaction(transaction) })
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onDelete: () -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f), // Added weight to this Row
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (transaction) {
                    is Income -> Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF008000))
                    is Expense -> Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    // is Allocation -> Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) // Placeholder icon for allocation
                }
                Column {
                    Text(
                        text = transaction.description,
                        fontWeight = FontWeight.Bold,
                        softWrap = true,
                        maxLines = 2
                    )
                    Text(text = dateFormat.format(transaction.date), style = MaterialTheme.typography.bodySmall)
                }
            }
            val (color, sign) = when (transaction) {
                is Income -> Pair(Color(0xFF008000), "+")
                is Expense -> Pair(MaterialTheme.colorScheme.error, "-")
                // is Allocation -> Pair(MaterialTheme.colorScheme.tertiary, "") // Allocations don't have a sign
            }
            Text(text = "$sign ${currencyFormat.format(transaction.amount)}", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir Transação")
            }
        }
    }
}