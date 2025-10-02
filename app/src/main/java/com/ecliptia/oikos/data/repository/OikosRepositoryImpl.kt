package com.ecliptia.oikos.data.repository

import com.ecliptia.oikos.data.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OikosRepositoryImpl @Inject constructor(
    private val db: FirebaseDatabase,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : OikosRepository {

    private val currentUser get() = auth.currentUser

    private fun userRootRef() = currentUser?.uid?.let { db.getReference("users").child(it) }

    override fun getRules(): Flow<List<AllocationRule>> = callbackFlow {
        val rulesRef = userRootRef()?.child("rules")
        if (rulesRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rules = snapshot.children.mapNotNull { it.getValue(AllocationRule::class.java) }
                trySend(rules)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        rulesRef.addValueEventListener(listener)
        awaitClose { rulesRef.removeEventListener(listener) }
    }

    override suspend fun saveRule(rule: AllocationRule) {
        userRootRef()?.child("rules")?.child(rule.id)?.setValue(rule)?.await()
    }

    override suspend fun deleteRule(ruleId: String) {
        userRootRef()?.child("rules")?.child(ruleId)?.removeValue()?.await()
    }

    override fun getTransactions(): Flow<List<Transaction>> {
        return combine(getIncomesFlow(), getExpensesFlow()) { incomes, expenses ->
            (incomes + expenses).sortedByDescending { it.date }
        }
    }

    override fun getAllocations(): Flow<List<Allocation>> = callbackFlow {
        val allocationsRef = userRootRef()?.child("allocations")
        if (allocationsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Allocation::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        allocationsRef.addValueEventListener(listener)
        awaitClose { allocationsRef.removeEventListener(listener) }
    }

    private fun getIncomesFlow(): Flow<List<Income>> = callbackFlow {
        val incomesRef = userRootRef()?.child("incomes")
        if (incomesRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Income::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        incomesRef.addValueEventListener(listener)
        awaitClose { incomesRef.removeEventListener(listener) }
    }

    private fun getExpensesFlow(): Flow<List<Expense>> = callbackFlow {
        val expensesRef = userRootRef()?.child("expenses")
        if (expensesRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Expense::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        expensesRef.addValueEventListener(listener)
        awaitClose { expensesRef.removeEventListener(listener) }
    }

    override suspend fun saveIncome(income: Income, allocations: List<Allocation>) {
        val root = userRootRef() ?: return
        val updates = mutableMapOf<String, Any?>()
        updates["/incomes/${income.id}"] = income
        allocations.forEach { alloc ->
            updates["/allocations/${alloc.id}"] = alloc
        }
        root.updateChildren(updates).await()
    }

    override suspend fun saveExpense(expense: Expense) {
        userRootRef()?.child("expenses")?.child(expense.id)?.setValue(expense)?.await()
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        val userRef = userRootRef() ?: return
        when (transaction) {
            is Income -> userRef.child("incomes").child(transaction.id).removeValue().await()
            is Expense -> userRef.child("expenses").child(transaction.id).removeValue().await()
        }
    }

    override suspend fun saveApiKey(apiKey: String) {
        userRootRef()?.child("settings")?.child("gemini_api_key")?.setValue(apiKey)?.await()
    }

    override fun getApiKey(): Flow<String?> = callbackFlow {
        val apiKeyRef = userRootRef()?.child("settings")?.child("gemini_api_key")
        if (apiKeyRef == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(String::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        apiKeyRef.addValueEventListener(listener)
        awaitClose { apiKeyRef.removeEventListener(listener) }
    }

    override suspend fun deleteAllUserData() {
        userRootRef()?.removeValue()?.await()
    }

    override fun getSavingsBoxes(): Flow<List<SavingsBox>> = callbackFlow {
        val savingsBoxesRef = userRootRef()?.child("savingsBoxes")
        if (savingsBoxesRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(SavingsBox::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        savingsBoxesRef.addValueEventListener(listener)
        awaitClose { savingsBoxesRef.removeEventListener(listener) }
    }

    override suspend fun saveSavingsBox(savingsBox: SavingsBox) {
        userRootRef()?.child("savingsBoxes")?.child(savingsBox.id)?.setValue(savingsBox)?.await()
    }

    override suspend fun updateSavingsBox(savingsBox: SavingsBox) {
        userRootRef()?.child("savingsBoxes")?.child(savingsBox.id)?.setValue(savingsBox)?.await()
    }

    override suspend fun deleteSavingsBox(savingsBoxId: String) {
        userRootRef()?.child("savingsBoxes")?.child(savingsBoxId)?.removeValue()?.await()
    }

    override suspend fun addFundsToSavingsBox(expense: Expense, savingsBoxToUpdate: SavingsBox) {
        val root = userRootRef() ?: return
        val updates = mutableMapOf<String, Any?>()
        updates["/expenses/${expense.id}"] = expense
        updates["/savingsBoxes/${savingsBoxToUpdate.id}"] = savingsBoxToUpdate
        root.updateChildren(updates).await()
    }

    override suspend fun saveCategoryLimit(category: String, limit: Double) {
        userRootRef()?.child("settings")?.child("category_limits")?.child(category)?.setValue(limit)
            ?.await()
    }

    override fun getCategoryLimit(category: String): Flow<Double?> = callbackFlow {
        val categoryLimitRef =
            userRootRef()?.child("settings")?.child("category_limits")?.child(category)
        if (categoryLimitRef == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Double::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        categoryLimitRef.addValueEventListener(listener)
        awaitClose { categoryLimitRef.removeEventListener(listener) }
    }

    override fun getCategoryLimits(): Flow<Map<String, Double>> = callbackFlow {
        val limitsRef = userRootRef()?.child("settings")?.child("category_limits")
        if (limitsRef == null) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val limits = snapshot.children.mapNotNull { data ->
                    data.key?.let { key ->
                        data.getValue(Double::class.java)?.let { value ->
                            key to value
                        }
                    }
                }.toMap()
                trySend(limits)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        limitsRef.addValueEventListener(listener)
        awaitClose { limitsRef.removeEventListener(listener) }
    }

    override suspend fun exportReports(): String {
        val transactions = getTransactions().firstOrNull() ?: emptyList()
        val stringBuilder = StringBuilder()
        stringBuilder.append("Type,Description,Amount,Date,Category/Source\n")
        transactions.forEach { transaction ->
            when (transaction) {
                is Income -> stringBuilder.append("Income,${transaction.description},${transaction.amount},${transaction.date},${transaction.source}\n")
                is Expense -> stringBuilder.append("Expense,${transaction.description},${transaction.amount},${transaction.date},${transaction.category}\n")
            }
        }
        return stringBuilder.toString()
    }

    override suspend fun saveVisualMode(mode: String) {
        userRootRef()?.child("settings")?.child("visual_mode")?.setValue(mode)?.await()
    }

    override fun getVisualMode(): Flow<String?> = callbackFlow {
        val visualModeRef = userRootRef()?.child("settings")?.child("visual_mode")
        if (visualModeRef == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(String::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        visualModeRef.addValueEventListener(listener)
        awaitClose { visualModeRef.removeEventListener(listener) }
    }

    override fun getNotifications(): Flow<List<Notification>> = callbackFlow {
        val notificationsRef = userRootRef()?.child("notifications")
        if (notificationsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Notification::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        notificationsRef.addValueEventListener(listener)
        awaitClose { notificationsRef.removeEventListener(listener) }
    }

    override suspend fun saveNotification(notification: Notification) {
        userRootRef()?.child("notifications")?.child(notification.id)?.setValue(notification)
            ?.await()
    }

    override suspend fun markNotificationAsRead(notificationId: String) {
        userRootRef()?.child("notifications")?.child(notificationId)?.child("read")?.setValue(true)
            ?.await()
    }

    override suspend fun deleteNotification(notificationId: String) {
        userRootRef()?.child("notifications")?.child(notificationId)?.removeValue()?.await()
    }

    override fun getAchievements(): Flow<List<Achievement>> = callbackFlow {
        val achievementsRef = userRootRef()?.child("achievements")
        if (achievementsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Achievement::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        achievementsRef.addValueEventListener(listener)
        awaitClose { achievementsRef.removeEventListener(listener) }
    }

    override suspend fun saveAchievement(achievement: Achievement) {
        userRootRef()?.child("achievements")?.child(achievement.id)?.setValue(achievement)?.await()
    }

    override suspend fun updateAchievement(achievement: Achievement) {
        userRootRef()?.child("achievements")?.child(achievement.id)?.setValue(achievement)?.await()
    }

    override fun getSubscriptions(): Flow<List<Subscription>> = callbackFlow {
        val subsRef = userRootRef()?.child("subscriptions")
        if (subsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subs = snapshot.children.mapNotNull { it.getValue(Subscription::class.java) }
                trySend(subs)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        subsRef.addValueEventListener(listener)
        awaitClose { subsRef.removeEventListener(listener) }
    }

    override suspend fun saveSubscription(subscription: Subscription) {
        userRootRef()?.child("subscriptions")?.child(subscription.id)?.setValue(subscription)?.await()
    }

    override suspend fun deleteSubscription(subscriptionId: String) {
        userRootRef()?.child("subscriptions")?.child(subscriptionId)?.removeValue()?.await()
    }

    override fun getDebts(): Flow<List<Debt>> = callbackFlow {
        val debtsRef = userRootRef()?.child("debts")
        if (debtsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val debts = snapshot.children.mapNotNull { it.getValue(Debt::class.java) }
                trySend(debts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        debtsRef.addValueEventListener(listener)
        awaitClose { debtsRef.removeEventListener(listener) }
    }

    override suspend fun saveDebt(debt: Debt) {
        userRootRef()?.child("debts")?.child(debt.id)?.setValue(debt)?.await()
    }

    override suspend fun deleteDebt(debtId: String) {
        userRootRef()?.child("debts")?.child(debtId)?.removeValue()?.await()
    }

    override fun getInvestments(): Flow<List<Investment>> = callbackFlow {
        val investmentsRef = userRootRef()?.child("investments")
        if (investmentsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val investments = snapshot.children.mapNotNull { it.getValue(Investment::class.java) }
                trySend(investments)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        investmentsRef.addValueEventListener(listener)
        awaitClose { investmentsRef.removeEventListener(listener) }
    }

    override suspend fun saveInvestment(investment: Investment) {
        userRootRef()?.child("investments")?.child(investment.id)?.setValue(investment)?.await()
    }

    override suspend fun deleteInvestment(investmentId: String) {
        userRootRef()?.child("investments")?.child(investmentId)?.removeValue()?.await()
    }

    override fun getCreditCards(): Flow<List<CreditCard>> = callbackFlow {
        val cardsRef = userRootRef()?.child("creditCards")
        if (cardsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cards = snapshot.children.mapNotNull { it.getValue(CreditCard::class.java) }
                trySend(cards)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        cardsRef.addValueEventListener(listener)
        awaitClose { cardsRef.removeEventListener(listener) }
    }

    override suspend fun saveCreditCard(card: CreditCard) {
        userRootRef()?.child("creditCards")?.child(card.id)?.setValue(card)?.await()
    }

    override suspend fun deleteCreditCard(cardId: String) {
        userRootRef()?.child("creditCards")?.child(cardId)?.removeValue()?.await()
    }

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val categoriesRef = userRootRef()?.child("categories")
        if (categoriesRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        categoriesRef.addValueEventListener(listener)
        awaitClose { categoriesRef.removeEventListener(listener) }
    }

    override suspend fun saveCategory(category: Category) {
        userRootRef()?.child("categories")?.child(category.id)?.setValue(category)?.await()
    }

    override suspend fun deleteCategory(categoryId: String) {
        userRootRef()?.child("categories")?.child(categoryId)?.removeValue()?.await()
    }

    override fun getRecurringTransactions(): Flow<List<RecurringTransaction>> = callbackFlow {
        val recurringTransactionsRef = userRootRef()?.child("recurringTransactions")
        if (recurringTransactionsRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(RecurringTransaction::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        recurringTransactionsRef.addValueEventListener(listener)
        awaitClose { recurringTransactionsRef.removeEventListener(listener) }
    }

    override suspend fun saveRecurringTransaction(recurringTransaction: RecurringTransaction) {
        userRootRef()?.child("recurringTransactions")?.child(recurringTransaction.id)?.setValue(recurringTransaction)?.await()
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        userRootRef()?.child("recurringTransactions")?.child(recurringTransaction.id)?.setValue(recurringTransaction)?.await()
    }

    override suspend fun deleteRecurringTransaction(recurringTransactionId: String) {
        userRootRef()?.child("recurringTransactions")?.child(recurringTransactionId)?.removeValue()?.await()
    }

    override suspend fun applyFinancialPlan(plan: FinancialPlan) {
        val root = userRootRef() ?: return
        val updates = mutableMapOf<String, Any?>()

        val newRules = mutableMapOf<String, AllocationRule>()
        // Process deductions as rules
        plan.deductions.forEach {
            val ruleId = "${it.name.replace(Regex("[^a-zA-Z0-9]"), "")}_${System.currentTimeMillis()}"
            newRules[ruleId] = AllocationRule(
                id = ruleId,
                name = it.name,
                type = if (it.type == "PERCENTAGE") AllocationType.PERCENTAGE else AllocationType.FIXED_AMOUNT,
                value = it.amount
            )
        }
        // Process ALL allocations as rules
        plan.allocations.forEach {
            val ruleId = it.key + "_${System.currentTimeMillis()}"
            newRules[ruleId] = AllocationRule(
                id = ruleId,
                name = it.name,
                type = AllocationType.FIXED_AMOUNT, // All allocations from plan are fixed amounts
                value = it.amount
            )
        }

        // Build the new category limits map for SPENDING allocations
        val newCategoryLimits = plan.allocations.filter { it.category == "SPENDING" }.associate {
            it.key to it.amount
        }

        // Atomically replace the old data with the new maps
        updates["/rules"] = newRules
        updates["/settings/category_limits"] = newCategoryLimits
        updates["/savingsBoxes"] = null // Clear old savings boxes created by the plan

        root.updateChildren(updates).await()
    }
}