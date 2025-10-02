package com.ecliptia.oikos.ui.features.category_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.model.Category
import com.ecliptia.oikos.data.repository.OikosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryManagementState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// Define default categories
private val DEFAULT_CATEGORIES = listOf(
    Category(id = "default_alimentacao", name = "Alimentação", isCustom = false),
    Category(id = "default_transporte", name = "Transporte", isCustom = false),
    Category(id = "default_moradia", name = "Moradia", isCustom = false),
    Category(id = "default_saude", name = "Saúde", isCustom = false),
    Category(id = "default_lazer", name = "Lazer", isCustom = false),
    Category(id = "default_educacao", name = "Educação", isCustom = false),
    Category(id = "default_contas", name = "Contas", isCustom = false),
    Category(id = "default_outros", name = "Outros", isCustom = false)
)

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementState())
    val uiState: StateFlow<CategoryManagementState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCategories().collect { userCategories ->
                val combinedCategories = (DEFAULT_CATEGORIES + userCategories)
                    .distinctBy { it.name } // Ensure unique names, user-defined takes precedence if names conflict
                    .sortedBy { it.name } // Sort alphabetically

                _uiState.value = _uiState.value.copy(
                    categories = combinedCategories,
                    isLoading = false
                )
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            // Check if a default category with the same name exists
            val existingDefault = DEFAULT_CATEGORIES.find { it.name.equals(name, ignoreCase = true) }
            if (existingDefault != null) {
                // If a default category with the same name exists, we might want to prevent adding it
                // or convert the default to a custom one. For now, let's just prevent adding a duplicate name.
                // TODO: Handle this case with user feedback (e.g., show a toast)
                return@launch
            }

            val newCategory = Category(id = "cat_${System.currentTimeMillis()}", name = name, isCustom = true)
            repository.saveCategory(newCategory)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            // Prevent deletion of default categories
            if (DEFAULT_CATEGORIES.any { it.id == categoryId }) {
                // TODO: Handle this case with user feedback (e.g., show a toast)
                return@launch
            }
            repository.deleteCategory(categoryId)
        }
    }
}