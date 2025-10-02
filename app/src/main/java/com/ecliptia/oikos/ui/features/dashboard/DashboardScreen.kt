package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ecliptia.oikos.data.model.SavingsBox
import com.ecliptia.oikos.ui.features.dashboard.NetWorthCard
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.draw.clip

@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    dialogToShow: String? = null,
    dialogDescription: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddFundsDialog by remember { mutableStateOf<SavingsBox?>(null) }
    var incomeDescription by remember { mutableStateOf("") }

    LaunchedEffect(dialogToShow) {
        if (dialogToShow == "income") {
            incomeDescription = dialogDescription ?: ""
            showAddIncomeDialog = true
        }
    }

    if (showAddIncomeDialog) {
        AddIncomeDialog(
            onDismissRequest = { showAddIncomeDialog = false },
            onSave = { amount, description ->
                viewModel.addIncome(amount, description)
                showAddIncomeDialog = false
            },
            initialDescription = incomeDescription
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            onSave = { amount, description, category, paymentMethod ->
                viewModel.addExpense(amount, description, category, paymentMethod)
                showAddExpenseDialog = false
            }
        )
    }

    showAddFundsDialog?.let {
        AddFundsToSavingsBoxDialog(
            boxName = it.name,
            onDismissRequest = { showAddFundsDialog = null },
            onSave = { amount ->
                viewModel.addFundsToSavingsBox(it, amount)
                showAddFundsDialog = null
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Meu Painel", style = MaterialTheme.typography.headlineSmall)

            BalanceCard(state = uiState)

            NetWorthCard(state = uiState) // New

            AiInsightCard(state = uiState)

            QuickAccessShortcuts(
                navController = navController,
                onAddIncome = { showAddIncomeDialog = true },
                onAddExpense = { showAddExpenseDialog = true }
            )

            // New Summary Cards
            InvestmentSummaryCard(state = uiState)
            DebtSummaryCard(state = uiState)
            SubscriptionSummaryCard(state = uiState)

            SavingsBoxesCard(state = uiState, onAddClick = { box -> showAddFundsDialog = box })

            CategorySpendingBars(expensesByCategory = uiState.expensesByCategory)

            TransactionHistoryCard(state = uiState, navController = navController)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BalanceCard(state: DashboardState) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Saldo Atual",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currencyFormat.format(state.currentBalance),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Receitas", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = currencyFormat.format(state.totalIncome),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Despesas", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = currencyFormat.format(state.totalExpense),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(onAddIncome: () -> Unit, onAddExpense: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onAddIncome,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Receita")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Receita")
        }
        Button(
            onClick = onAddExpense,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Adicionar Despesa")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Despesa")
        }
    }
}



@Composable
fun SavingsBoxesCard(state: DashboardState, onAddClick: (SavingsBox) -> Unit) {
    if (state.savingsBoxes.isEmpty()) return

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Minhas Caixinhas", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.savingsBoxes.forEach { box ->
                    key(box.id) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(box.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                TextButton(onClick = { onAddClick(box) }) {
                                    Text("Adicionar")
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val hasTarget = box.targetAmount > 0
                            val progress = if (hasTarget) (box.currentAmount / box.targetAmount).toFloat() else 0f

                            if (hasTarget) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currencyFormat.format(box.currentAmount),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = currencyFormat.format(box.targetAmount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "Meta Aberta: ${currencyFormat.format(box.currentAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Monthly Contribution Target
                            if (box.monthlyContributionTarget > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val monthlyProgress = if (box.monthlyContributionTarget > 0) {
                                    // This is a simplified progress. A real implementation would need to know
                                    // how much has been contributed *this month*
                                    (box.currentAmount % box.monthlyContributionTarget / box.monthlyContributionTarget).toFloat()
                                } else 0f
                                Text(
                                    text = "Meta Mensal: ${currencyFormat.format(box.monthlyContributionTarget)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LinearProgressIndicator(
                                    progress = { monthlyProgress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}