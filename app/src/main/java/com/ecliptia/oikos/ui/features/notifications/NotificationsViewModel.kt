package com.ecliptia.oikos.ui.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.data.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: OikosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsState())
    val uiState: StateFlow<NotificationsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getNotifications().collect { notifications ->
                _uiState.value = NotificationsState(
                    notifications = notifications,
                    isLoading = false
                )
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }

    // Function to add a new notification (for internal use or testing)
    fun addNotification(message: String, type: String = "", relatedId: String? = null) {
        viewModelScope.launch {
            val newNotification = Notification(
                id = "notif_${System.currentTimeMillis()}",
                message = message,
                type = type,
                relatedId = relatedId
            )
            repository.saveNotification(newNotification)
        }
    }
}
