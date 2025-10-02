package com.ecliptia.oikos.ui.features.dashboard

import com.ecliptia.oikos.data.model.Allocation
import com.ecliptia.oikos.data.model.AllocationRule
import com.ecliptia.oikos.data.model.SavingsBox
import com.ecliptia.oikos.data.model.Transaction
import com.ecliptia.oikos.data.model.Investment
import com.ecliptia.oikos.data.model.Debt
import com.ecliptia.oikos.data.model.Subscription

data class DashboardState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalAllocated: Double = 0.0,
    val currentBalance: Double = 0.0,
    val allocations: List<Allocation> = emptyList(),
    val allocationRules: List<AllocationRule> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val savingsBoxes: List<SavingsBox> = emptyList(),
    val expensesByCategory: Map<String, Double> = emptyMap(),
    val categoryLimits: Map<String, Double> = emptyMap(),
    val insights: List<String> = emptyList(),
    val apiKey: String? = null,
    val isLoading: Boolean = true,
    val isLoadingInsight: Boolean = true,
    // New summary data for other features
    val totalInvestmentValue: Double = 0.0,
    val totalInvestmentProfitLoss: Double = 0.0,
    val totalDebtAmount: Double = 0.0,
    val upcomingSubscriptionPayments: Double = 0.0,
    val nextSubscriptionPaymentDate: String? = null,
    val netWorth: Double = 0.0 // New
)