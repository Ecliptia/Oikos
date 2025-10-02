package com.ecliptia.oikos.ui.features.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ecliptia.oikos.navigation.AppDestinations
import com.ecliptia.oikos.data.model.Notification

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Notificações", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.notifications.isEmpty()) {
            Text("Nenhuma notificação no momento.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkAsRead = { viewModel.markNotificationAsRead(notification.id) },
                        onDelete = { viewModel.deleteNotification(notification.id) },
                        onAdjustGoal = { navController.navigate(AppDestinations.GOALS_ROUTE) },
                        onReplanMonth = { navController.navigate(AppDestinations.DASHBOARD_ROUTE) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onMarkAsRead: () -> Unit, onDelete: () -> Unit, onAdjustGoal: () -> Unit, onReplanMonth: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "Há ${((System.currentTimeMillis() - notification.timestamp) / 60000)} minutos", // Simple time ago
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!notification.read) {
                        IconButton(onClick = onMarkAsRead) {
                            Icon(Icons.Default.Check, contentDescription = "Marcar como lida")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir notificação")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAdjustGoal) {
                    Text("Ajustar meta")
                }
                Button(onClick = onReplanMonth) {
                    Text("Replanejar mês")
                }
            }
        }
    }
}