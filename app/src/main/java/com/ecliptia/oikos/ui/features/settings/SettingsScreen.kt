package com.ecliptia.oikos.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ecliptia.oikos.navigation.AppDestinations
import java.text.NumberFormat
import java.util.Locale

import com.ecliptia.oikos.ui.features.category_management.CategoryManagementViewModel // New import

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // Add this line here
    val savedApiKey by viewModel.apiKey.collectAsState()
    var apiKey by remember { mutableStateOf("") }
    val allLimits by viewModel.allCategoryLimits.collectAsState()
    var showAddOrEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(savedApiKey) {
        if (apiKey.isEmpty()) { // Only update if user is not currently typing
            apiKey = savedApiKey ?: ""
        }
    }

    if (showAddOrEditDialog) {
        AddOrEditCategoryLimitDialog(
            settingsViewModel = viewModel, // Pass the correct ViewModel
            categoryViewModel = hiltViewModel(), // Pass the CategoryManagementViewModel
            onDismissRequest = { showAddOrEditDialog = false }
        )
    }

    Scaffold {
        Column(
            modifier = modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Configurações", style = MaterialTheme.typography.titleLarge)

            // Gemini API Key
            Text(text = "Chave de API do Gemini", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Sua Chave de API") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.saveApiKey(apiKey) }) {
                Text("Salvar Chave")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Limits
            Text(text = "Limites por Categoria", style = MaterialTheme.typography.titleMedium)
            Card(elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (allLimits.isEmpty()) {
                        Text("Nenhum limite definido.", modifier = Modifier.padding(8.dp))
                    } else {
                        allLimits.forEach { (category, limit) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                                Text("$category: ${currencyFormat.format(limit)}")
                                IconButton(onClick = { viewModel.saveCategoryLimit(category, 0.0) }) { // Delete by setting to 0
                                    Icon(Icons.Default.Delete, contentDescription = "Remover limite para $category")
                                }
                            }
                        }
                    }
                }
            }
            Button(onClick = { showAddOrEditDialog = true }) {
                Text("Adicionar / Editar Limite")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Mode
            Text(text = "Modo Visual", style = MaterialTheme.typography.titleMedium)
            val visualMode by viewModel.visualMode.collectAsState()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = { viewModel.saveVisualMode("Minimalista") }, enabled = visualMode != "Minimalista") {
                    Text("Minimalista")
                }
                Button(onClick = { viewModel.saveVisualMode("Analítico") }, enabled = visualMode != "Analítico") {
                    Text("Analítico")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Automation
            Text(text = "Automação de Transações", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                // Launch intent to open Notification Listener settings
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                context.startActivity(intent)
            }) {
                Text("Habilitar Escuta de Notificações")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Text(text = "Ações Rápidas", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                val route = "${AppDestinations.DASHBOARD_ROUTE}?showDialog=income&description=Salário"
                navController.navigate(route)
            }) {
                Text("Registrar Salário")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigate(AppDestinations.RULES_ROUTE) }) {
                Text("Gerenciar Regras de Alocação")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Management
            Text(text = "Gerenciamento de Dados", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { viewModel.exportReports() }) {
                Text("Exportar Relatórios")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSignOut) {
                Text("Sair (Logout)")
            }
        }
    }
}