package com.ecliptia.oikos.data.model

data class SavingsBox(
    val id: String = "",
    val name: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val monthlyContributionTarget: Double = 0.0
)
