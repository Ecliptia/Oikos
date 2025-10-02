package com.ecliptia.oikos.ui.features.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.data.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GamificationState(
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamificationState())
    val uiState: StateFlow<GamificationState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAchievements().collect { achievements ->
                _uiState.value = GamificationState(
                    achievements = achievements,
                    isLoading = false
                )
            }
        }
    }

    fun trackAchievementProgress(achievementId: String, progress: Float) {
        viewModelScope.launch {
            val currentAchievement = _uiState.value.achievements.find { it.id == achievementId }
            currentAchievement?.let { ach ->
                val updatedAchievement = ach.copy(progress = progress)
                repository.updateAchievement(updatedAchievement)
            }
        }
    }

    fun markAchievementAchieved(achievementId: String) {
        viewModelScope.launch {
            val currentAchievement = _uiState.value.achievements.find { it.id == achievementId }
            currentAchievement?.let { ach ->
                val updatedAchievement = ach.copy(achieved = true, progress = 1f)
                repository.updateAchievement(updatedAchievement)
            }
        }
    }

    // Function to add a new achievement (for initial setup or testing)
    fun addAchievement(name: String, description: String, progress: Float = 0f, achieved: Boolean = false) {
        viewModelScope.launch {
            val newAchievement = Achievement(
                id = "ach_${System.currentTimeMillis()}",
                name = name,
                description = description,
                progress = progress,
                achieved = achieved
            )
            repository.saveAchievement(newAchievement)
        }
    }
}
