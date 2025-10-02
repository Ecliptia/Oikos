package com.ecliptia.oikos.ui.features.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ecliptia.oikos.navigation.AppDestinations

data class MoreItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MoreScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val moreItems = listOf(
        MoreItem("Relatórios", Icons.Filled.Analytics, AppDestinations.REPORTS_ROUTE),
        MoreItem("Metas", Icons.Filled.Star, AppDestinations.GOALS_ROUTE),
        MoreItem("Oikos", Icons.Filled.EmojiObjects, AppDestinations.AI_ADVISOR_ROUTE),
        MoreItem("Regras de Alocação", Icons.Filled.Rule, AppDestinations.RULES_ROUTE),
        MoreItem("Orçamento", Icons.Filled.Money, AppDestinations.BUDGET_ROUTE),
        MoreItem("Gerenciar Categorias", Icons.Filled.Category, AppDestinations.CATEGORY_MANAGEMENT_ROUTE), // New item
        MoreItem("Gamificação", Icons.Filled.SportsEsports, AppDestinations.GAMIFICATION_ROUTE),
        MoreItem("Assinaturas", Icons.Default.ReceiptLong, AppDestinations.SUBSCRIPTIONS_ROUTE),
        MoreItem("Transações Recorrentes", Icons.Filled.Repeat, AppDestinations.RECURRING_TRANSACTIONS_ROUTE), // New item
        MoreItem("Planejador de Dívidas", Icons.Default.TrendingDown, AppDestinations.DEBTS_ROUTE),
        MoreItem("Investimentos", Icons.Default.TrendingUp, AppDestinations.INVESTMENTS_ROUTE),
        MoreItem("Cartões de Crédito", Icons.Default.CreditCard, AppDestinations.CREDIT_CARD_ROUTE),
        MoreItem("Configurações", Icons.Filled.Settings, AppDestinations.SETTINGS_ROUTE)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mais Opções", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            moreItems.forEach { item ->
                item {
                    MoreItemRow(item = item) {
                        navController.navigate(item.route)
                    }
                }
            }
        }
    }
}

@Composable
fun MoreItemRow(item: MoreItem, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.icon, contentDescription = item.label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(item.label, style = MaterialTheme.typography.bodyLarge)
        }
        Divider()
    }
}
