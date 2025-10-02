package com.ecliptia.oikos.ui.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedTextField // New import
import androidx.compose.foundation.layout.fillMaxWidth // Ensure this is present
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ecliptia.oikos.ui.features.category_management.CategoryManagementViewModel // New import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditCategoryLimitDialog(
    settingsViewModel: SettingsViewModel = hiltViewModel(), // Renamed for clarity
    categoryViewModel: CategoryManagementViewModel = hiltViewModel(), // New injected ViewModel
    onDismissRequest: () -> Unit
) {
    val categoriesState by categoryViewModel.uiState.collectAsState() // Fetch categories
    val allLimits by settingsViewModel.allCategoryLimits.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categoriesState.categories.firstOrNull()?.name ?: "") } // Initialize with first category or empty
    var categoryLimitInput by remember { mutableStateOf("") }

    // Update selectedCategory if the initial list is empty and then gets populated
    LaunchedEffect(categoriesState.categories) {
        if (selectedCategory.isBlank() && categoriesState.categories.isNotEmpty()) {
            selectedCategory = categoriesState.categories.first().name
        }
    }

    // Update input field when a category is selected and its limit is found
    LaunchedEffect(selectedCategory, allLimits) {
        val limit = allLimits[selectedCategory]
        categoryLimitInput = limit?.toString() ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Adicionar ou Editar Limite") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = false,
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        categoriesState.categories.forEach { category -> // Use fetched categories
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.name
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = categoryLimitInput,
                    onValueChange = { categoryLimitInput = it },
                    label = { Text("Limite (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sanitizedInput = categoryLimitInput.filter { it.isDigit() || it == ',' || it == '.' }.replace(",", ".")
                    val limit = sanitizedInput.toDoubleOrNull() ?: 0.0
                    if (selectedCategory.isNotBlank()) {
                    settingsViewModel.saveCategoryLimit(selectedCategory, limit) // Corrected to settingsViewModel
                    }
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