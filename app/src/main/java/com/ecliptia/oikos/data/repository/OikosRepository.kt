package com.ecliptia.oikos.data.repository

import com.ecliptia.oikos.data.model.Achievement
import com.ecliptia.oikos.data.model.Allocation
import com.ecliptia.oikos.data.model.AllocationRule
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Notification
import com.ecliptia.oikos.data.model.SavingsBox
import com.ecliptia.oikos.data.model.Transaction
import com.ecliptia.oikos.data.model.Category // New import
import com.ecliptia.oikos.data.model.RecurringTransaction // New import
import kotlinx.coroutines.flow.Flow

interface OikosRepository {
    // Rules
    fun getRules(): Flow<List<AllocationRule>>
    suspend fun saveRule(rule: AllocationRule)
    suspend fun deleteRule(ruleId: String)

    // Transactions
    fun getTransactions(): Flow<List<Transaction>>
    fun getAllocations(): Flow<List<Allocation>>
    suspend fun saveIncome(income: Income, allocations: List<Allocation>)
    suspend fun saveExpense(expense: Expense)
    suspend fun deleteTransaction(transaction: Transaction)

    // Settings
    suspend fun saveApiKey(apiKey: String)
    fun getApiKey(): Flow<String?>
    suspend fun deleteAllUserData()

    // Savings Boxes
    fun getSavingsBoxes(): Flow<List<SavingsBox>>
    suspend fun saveSavingsBox(savingsBox: SavingsBox)
    suspend fun updateSavingsBox(savingsBox: SavingsBox)
    suspend fun deleteSavingsBox(savingsBoxId: String)
    suspend fun addFundsToSavingsBox(expense: Expense, savingsBoxToUpdate: SavingsBox)

    // Category Limits
    suspend fun saveCategoryLimit(category: String, limit: Double)
    fun getCategoryLimit(category: String): Flow<Double?>
    fun getCategoryLimits(): Flow<Map<String, Double>>

    // Reports
    suspend fun exportReports(): String

    // Visual Mode
    suspend fun saveVisualMode(mode: String)
    fun getVisualMode(): Flow<String?>

    // Notifications
    fun getNotifications(): Flow<List<Notification>>
    suspend fun saveNotification(notification: Notification)
    suspend fun markNotificationAsRead(notificationId: String)
    suspend fun deleteNotification(notificationId: String)

    // Achievements
    fun getAchievements(): Flow<List<Achievement>>
    suspend fun saveAchievement(achievement: Achievement)
    suspend fun updateAchievement(achievement: Achievement)

    // Subscriptions
    fun getSubscriptions(): Flow<List<com.ecliptia.oikos.data.model.Subscription>>
    suspend fun saveSubscription(subscription: com.ecliptia.oikos.data.model.Subscription)
    suspend fun deleteSubscription(subscriptionId: String)

    // Debts
    fun getDebts(): Flow<List<com.ecliptia.oikos.data.model.Debt>>
    suspend fun saveDebt(debt: com.ecliptia.oikos.data.model.Debt)
    suspend fun deleteDebt(debtId: String)

    // Investments
    fun getInvestments(): Flow<List<com.ecliptia.oikos.data.model.Investment>>
    suspend fun saveInvestment(investment: com.ecliptia.oikos.data.model.Investment)
    suspend fun deleteInvestment(investmentId: String)

    // Credit Cards
    fun getCreditCards(): Flow<List<com.ecliptia.oikos.data.model.CreditCard>>
    suspend fun saveCreditCard(card: com.ecliptia.oikos.data.model.CreditCard)
    suspend fun deleteCreditCard(cardId: String)

    // Categories
    fun getCategories(): Flow<List<com.ecliptia.oikos.data.model.Category>>
    suspend fun saveCategory(category: com.ecliptia.oikos.data.model.Category)
    suspend fun deleteCategory(categoryId: String)

    // Recurring Transactions
    fun getRecurringTransactions(): Flow<List<com.ecliptia.oikos.data.model.RecurringTransaction>>
    suspend fun saveRecurringTransaction(recurringTransaction: com.ecliptia.oikos.data.model.RecurringTransaction)
    suspend fun updateRecurringTransaction(recurringTransaction: com.ecliptia.oikos.data.model.RecurringTransaction)
    suspend fun deleteRecurringTransaction(recurringTransactionId: String)

    // AI Planner
    suspend fun applyFinancialPlan(plan: com.ecliptia.oikos.data.model.FinancialPlan)
}