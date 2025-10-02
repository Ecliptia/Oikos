package com.ecliptia.oikos.services

import com.ecliptia.oikos.data.model.Allocation
import com.ecliptia.oikos.data.model.Debt
import com.ecliptia.oikos.data.model.Expense
import com.ecliptia.oikos.data.model.Income
import com.ecliptia.oikos.data.model.Investment
import com.ecliptia.oikos.data.model.SavingsBox
import com.ecliptia.oikos.data.model.Subscription
import com.ecliptia.oikos.data.model.Transaction
import com.ecliptia.oikos.data.repository.OikosRepository
import com.ecliptia.oikos.ui.theme.FinancialStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialStatusService @Inject constructor(
    private val repository: OikosRepository
) {
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private fun isCurrentMonth(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.time = date
        val transactionMonth = calendar.get(Calendar.MONTH)
        val transactionYear = calendar.get(Calendar.YEAR)

        return currentMonth == transactionMonth && currentYear == transactionYear
    }

    val financialStatus: StateFlow<FinancialStatus> = combine(
        listOf(
            repository.getTransactions(),
            repository.getAllocations(),
            repository.getDebts(),
            repository.getSavingsBoxes(),
            repository.getInvestments(),
            repository.getSubscriptions()
        )
    ) { flowsArray ->
        @Suppress("UNCHECKED_CAST")
        val transactions = flowsArray[0] as List<Transaction>
        val allocations = flowsArray[1] as List<Allocation>
        val debts = flowsArray[2] as List<Debt>
        val savingsBoxes = flowsArray[3] as List<SavingsBox>
        val investments = flowsArray[4] as List<Investment>
        val subscriptions = flowsArray[5] as List<Subscription>

        val currentMonthTransactions = transactions.filter { isCurrentMonth(it.date) }
        val monthlyIncome = currentMonthTransactions.filterIsInstance<Income>().sumOf { it.amount }
        val monthlyExpense = currentMonthTransactions.filterIsInstance<Expense>().sumOf { it.amount }
        val totalAllocated = allocations.sumOf { it.amount } // Assuming allocations are monthly
        val currentBalance = monthlyIncome - monthlyExpense - totalAllocated

        val totalDebtAmount = debts.sumOf { it.totalAmount }
        val totalSavingsAmount = savingsBoxes.sumOf { it.currentAmount }
        val totalInvestmentValue = investments.sumOf { it.quantity * it.currentPrice }
        val upcomingSubscriptionPayments = subscriptions.sumOf { it.amount } // Assuming this is the monthly sum of all subscriptions

        when {
            // BAD (Red Theme)
            currentBalance < 0.0 ||
            (monthlyExpense + upcomingSubscriptionPayments) > monthlyIncome ||
            totalDebtAmount > (monthlyIncome * 3) ||
            (totalSavingsAmount + totalInvestmentValue > 0 && totalDebtAmount > (totalSavingsAmount + totalInvestmentValue) * 0.5) -> FinancialStatus.BAD

            // WARNING (Orange Theme)
            totalDebtAmount > (monthlyIncome * 1.5) ||
            (monthlyExpense + upcomingSubscriptionPayments) > (monthlyIncome * 0.9) ||
            (totalInvestmentValue < totalDebtAmount * 0.5) -> FinancialStatus.WARNING

            // EXCELLENT (Gold Theme)
            totalSavingsAmount >= (monthlyIncome * 6) &&
            totalDebtAmount == 0.0 &&
            (monthlyIncome > monthlyExpense + upcomingSubscriptionPayments) &&
            totalInvestmentValue > (monthlyIncome * 12) -> FinancialStatus.EXCELLENT

            // GOOD (Blue Theme)
            (monthlyIncome > monthlyExpense + upcomingSubscriptionPayments) &&
            totalSavingsAmount > 0.0 &&
            totalDebtAmount <= monthlyIncome &&
            totalInvestmentValue > 0.0 -> FinancialStatus.GOOD

            // NEUTRAL (Green Theme)
            else -> FinancialStatus.NEUTRAL
        }
    }.stateIn(
        scope = serviceScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialStatus.NEUTRAL
    )
}
