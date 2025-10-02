package com.ecliptia.oikos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FinancialPlan(
    val monthlyIncome: Double,
    val planSummary: String,
    val deductions: List<PlanDeduction>,
    val allocations: List<PlanAllocation>,
    val projections: PlanProjection
)

@Serializable
data class PlanDeduction(
    val name: String,
    val type: String, // "PERCENTAGE" or "FIXED"
    val value: Double,
    val amount: Double
)

@Serializable
data class PlanAllocation(
    val name: String,
    val key: String,
    val category: String, // "SAVINGS" or "SPENDING"
    val amount: Double
)

@Serializable
data class PlanProjection(
    val twelveMonthsSavings: Double,
    val projectionSummary: String
)
