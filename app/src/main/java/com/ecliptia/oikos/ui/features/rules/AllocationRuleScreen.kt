package com.ecliptia.oikos.ui.features.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ecliptia.oikos.data.model.AllocationRule
import com.ecliptia.oikos.ui.features.dashboard.DashboardViewModel

@Composable
fun AllocationRuleScreen(
    modifier: Modifier = Modifier,
    viewModel: RulesViewModel = hiltViewModel(),
    showDialog: Boolean,
    onDismissDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.allocationRules.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhuma regra de alocação definida.\nClique no botão '+' para adicionar sua primeira regra.",
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.allocationRules) { rule ->
                RuleItem(rule = rule, onDelete = { viewModel.deleteRule(rule) })
            }
        }
    }

    if (showDialog) {
        AddRuleDialog(
            onDismissRequest = onDismissDialog,
            onSave = {
                viewModel.addRule(it)
                onDismissDialog()
            }
        )
    }
}

@Composable
fun AllocationRuleScreenForDashboard(
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Regras de Alocação (Dashboard)", style = MaterialTheme.typography.titleLarge)
        if (uiState.allocationRules.isEmpty()) {
            Text("Nenhuma regra de alocação definida.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.allocationRules) { rule ->
                    RuleItem(rule = rule) // No onDelete here
                }
            }
        }
    }
}

@Composable
fun RuleItem(rule: AllocationRule, onDelete: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = rule.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "${rule.value} ${if (rule.type == com.ecliptia.oikos.data.model.AllocationType.PERCENTAGE) "%" else "R$"}", style = MaterialTheme.typography.bodySmall)
            }
            onDelete?.let { 
                IconButton(onClick = it) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir Regra")
                }
            }
        }
    }
}