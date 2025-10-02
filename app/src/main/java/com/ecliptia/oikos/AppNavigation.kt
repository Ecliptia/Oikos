package com.ecliptia.oikos

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ecliptia.oikos.navigation.AppDestinations
import com.ecliptia.oikos.ui.features.ai_advisor.AiAdvisorScreen
import com.ecliptia.oikos.ui.features.auth.AuthViewModel
import com.ecliptia.oikos.ui.features.dashboard.DashboardScreen
import com.ecliptia.oikos.ui.features.gamification.GamificationScreen
import com.ecliptia.oikos.ui.features.goals.GoalsScreen
import com.ecliptia.oikos.ui.features.login.LoginScreen
import com.ecliptia.oikos.ui.features.more.MoreScreen
import com.ecliptia.oikos.ui.features.notifications.NotificationsScreen
import com.ecliptia.oikos.ui.features.reports.ReportsScreen
import com.ecliptia.oikos.ui.features.rules.AllocationRuleScreen
import com.ecliptia.oikos.ui.features.settings.SettingsScreen
import com.ecliptia.oikos.ui.features.subscriptions.SubscriptionScreen
import com.ecliptia.oikos.ui.features.debts.DebtScreen
import com.ecliptia.oikos.ui.features.creditcard.CreditCardScreen
import com.ecliptia.oikos.ui.features.investments.InvestmentScreen
import com.ecliptia.oikos.ui.features.savingsbox_history.SavingsBoxHistoryScreen
import com.ecliptia.oikos.ui.features.wallet.WalletScreen
import com.ecliptia.oikos.ui.features.budget.BudgetScreen
import com.ecliptia.oikos.ui.features.category_management.CategoryManagementScreen
import com.ecliptia.oikos.ui.features.recurring_transactions.RecurringTransactionsScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.isLoggedIn) AppDestinations.MAIN_APP_ROUTE else AppDestinations.LOGIN_ROUTE
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(navController = navController)
        }

        navigation(startDestination = AppDestinations.DASHBOARD_ROUTE, route = AppDestinations.MAIN_APP_ROUTE) {
            composable(
                route = "${AppDestinations.DASHBOARD_ROUTE}?showDialog={dialogType}&description={dialogDescription}",
                arguments = listOf(
                    navArgument("dialogType") { type = NavType.StringType; nullable = true },
                    navArgument("dialogDescription") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    DashboardScreen(
                        navController = navController,
                        modifier = innerPadding,
                        dialogToShow = backStackEntry.arguments?.getString("dialogType"),
                        dialogDescription = backStackEntry.arguments?.getString("dialogDescription")
                    )
                }
            }
            composable(AppDestinations.WALLET_ROUTE) {
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    WalletScreen(modifier = innerPadding)
                }
            }
            composable(AppDestinations.REPORTS_ROUTE) {
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    ReportsScreen(modifier = innerPadding)
                }
            }
            composable(AppDestinations.GOALS_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Meta")
                        }
                    }
                ) { innerPadding ->
                    GoalsScreen(
                        navController = navController, // Pass navController
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(AppDestinations.AI_ADVISOR_ROUTE) {
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    AiAdvisorScreen(modifier = innerPadding)
                }
            }
            composable(AppDestinations.NOTIFICATIONS_ROUTE) {
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    NotificationsScreen(navController = navController, modifier = innerPadding)
                }
            }
            composable(AppDestinations.SETTINGS_ROUTE) {
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    SettingsScreen(
                        navController = navController,
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate(AppDestinations.LOGIN_ROUTE) {
                                popUpTo(AppDestinations.MAIN_APP_ROUTE) { inclusive = true }
                            }
                        },
                        modifier = innerPadding
                    )
                }
            }
            composable(AppDestinations.RULES_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Regra")
                        }
                    }
                ) { innerPadding ->
                    AllocationRuleScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(AppDestinations.GAMIFICATION_ROUTE) {
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    GamificationScreen(modifier = innerPadding)
                }
            }
            composable(AppDestinations.MORE_ROUTE) {
                MainScreen(navController = navController, authViewModel = authViewModel) { innerPadding ->
                    MoreScreen(navController = navController, modifier = innerPadding)
                }
            }
            composable(AppDestinations.SUBSCRIPTIONS_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Assinatura")
                        }
                    }
                ) { innerPadding ->
                    SubscriptionScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(AppDestinations.DEBTS_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Dívida")
                        }
                    }
                ) { innerPadding ->
                    DebtScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(AppDestinations.INVESTMENTS_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Investimento")
                        }
                    }
                ) { innerPadding ->
                    InvestmentScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(AppDestinations.CREDIT_CARD_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Cartão")
                        }
                    }
                ) { innerPadding ->
                    CreditCardScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(
                route = "${AppDestinations.SAVINGS_BOX_HISTORY_ROUTE}/{savingsBoxId}",
                arguments = listOf(navArgument("savingsBoxId") { type = NavType.StringType })
            ) { backStackEntry ->
                val savingsBoxId = backStackEntry.arguments?.getString("savingsBoxId")
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel
                ) { innerPadding ->
                    SavingsBoxHistoryScreen(
                        modifier = innerPadding,
                        savingsBoxId = savingsBoxId
                    )
                }
            }
            composable(AppDestinations.BUDGET_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Limite")
                        }
                    }
                ) { innerPadding ->
                    BudgetScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(AppDestinations.CATEGORY_MANAGEMENT_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Categoria")
                        }
                    }
                ) { innerPadding ->
                    CategoryManagementScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
            composable(AppDestinations.RECURRING_TRANSACTIONS_ROUTE) {
                var showDialog by remember { mutableStateOf(false) }
                MainScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Transação Recorrente")
                        }
                    }
                ) { innerPadding ->
                    RecurringTransactionsScreen(
                        modifier = innerPadding,
                        showDialog = showDialog,
                        onDismissDialog = { showDialog = false }
                    )
                }
            }
        }
    }
}
