package com.ecliptia.oikos.ui.features.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.Locale
import com.ecliptia.oikos.ui.features.settings.AddOrEditCategoryLimitDialog // New import

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel(),
    showDialog: Boolean, // New parameter
    onDismissDialog: () -> Unit // New parameter
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

    if (showDialog) {
        AddOrEditCategoryLimitDialog(
            settingsViewModel = hiltViewModel(), // Pass the correct ViewModel
            categoryViewModel = hiltViewModel(), // Pass the CategoryManagementViewModel
            onDismissRequest = { onDismissDialog() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Orçamento por Categoria",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.categoryLimits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhum limite de categoria definido.\nDefina limites nas Configurações para ver seu orçamento aqui.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.categoryLimits.entries.toList()) { (category, limit) ->
                    val currentSpent = uiState.currentSpending[category] ?: 0.0
                    val progress = if (limit > 0) (currentSpent / limit).toFloat() else 0f
                    val remaining = limit - currentSpent

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = currencyFormat.format(limit),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = when {
                                    progress > 1.0f -> MaterialTheme.colorScheme.error // Over budget
                                    progress > 0.8f -> MaterialTheme.colorScheme.tertiary // Nearing limit
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Gasto: ${currencyFormat.format(currentSpent)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Restante: ${currencyFormat.format(remaining)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}