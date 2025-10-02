package com.ecliptia.oikos

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ecliptia.oikos.navigation.AppDestinations
import com.ecliptia.oikos.navigation.BottomNavItem
import com.ecliptia.oikos.ui.features.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, AppDestinations.DASHBOARD_ROUTE),
        BottomNavItem("Carteira", Icons.Filled.AccountBalanceWallet, AppDestinations.WALLET_ROUTE),
        BottomNavItem("Mais", Icons.Filled.MoreVert, AppDestinations.MORE_ROUTE)
    )

    val screenTitles = mapOf(
        // AppDestinations.DASHBOARD_ROUTE to "Home", // Title is now inside the screen
        AppDestinations.WALLET_ROUTE to "Carteira",
        AppDestinations.REPORTS_ROUTE to "Relatórios",
        AppDestinations.GOALS_ROUTE to "Metas",
        AppDestinations.DEBTS_ROUTE to "Planejador de Dívidas",
        AppDestinations.INVESTMENTS_ROUTE to "Investimentos",
        AppDestinations.CREDIT_CARD_ROUTE to "Cartões de Crédito",
        AppDestinations.MORE_ROUTE to "Mais",
        AppDestinations.AI_ADVISOR_ROUTE to "Oikos AI",
        AppDestinations.RULES_ROUTE to "Regras de Alocação",
        AppDestinations.GAMIFICATION_ROUTE to "Gamificação",
        AppDestinations.SETTINGS_ROUTE to "Configurações",
        AppDestinations.NOTIFICATIONS_ROUTE to "Notificações"
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route?.substringBefore('?')

    val title = screenTitles[currentRoute]

    Scaffold(
        topBar = {
            if (title != null) {
                TopAppBar(
                    title = { Text(title) },
                    actions = {
                        IconButton(onClick = { navController.navigate(AppDestinations.NOTIFICATIONS_ROUTE) }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notificações")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}
