package com.ecliptia.oikos.ui.features.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all filled icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ecliptia.oikos.navigation.AppDestinations

data class QuickAccessItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun QuickAccessShortcuts(navController: NavController, onAddIncome: () -> Unit, onAddExpense: () -> Unit) {
    val items = listOf(
        QuickAccessItem("Receita", Icons.Default.Add, onAddIncome),
        QuickAccessItem("Despesa", Icons.Default.Remove, onAddExpense),
        QuickAccessItem("Meta", Icons.Default.Star, { navController.navigate(AppDestinations.GOALS_ROUTE) }),
        QuickAccessItem("Assinatura", Icons.Default.ReceiptLong, { navController.navigate(AppDestinations.SUBSCRIPTIONS_ROUTE) }),
        QuickAccessItem("Dívida", Icons.Default.TrendingDown, { navController.navigate(AppDestinations.DEBTS_ROUTE) }),
        QuickAccessItem("Investir", Icons.Default.TrendingUp, { navController.navigate(AppDestinations.INVESTMENTS_ROUTE) }),
        QuickAccessItem("Regras", Icons.Default.Rule, { navController.navigate(AppDestinations.RULES_ROUTE) }),
        QuickAccessItem("Oikos AI", Icons.Default.AutoAwesome, { navController.navigate(AppDestinations.AI_ADVISOR_ROUTE) }),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Acesso Rápido", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(items) { item ->
                QuickAccessButton(item = item)
            }
        }
    }
}

@Composable
fun QuickAccessButton(item: QuickAccessItem) {
    Column(
        modifier = Modifier
            .width(80.dp) // Fixed width for each button
            .clickable(onClick = item.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
