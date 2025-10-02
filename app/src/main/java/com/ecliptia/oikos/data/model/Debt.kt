package com.ecliptia.oikos.data.model

data class Debt(
    val id: String = "",
    val name: String = "",
    val totalAmount: Double = 0.0,
    val interestRate: Double = 0.0, // Annual Percentage Rate (APR)
    val minimumPayment: Double = 0.0,
    val settlementOfferAmount: Double? = null,
    val settlementOfferDetails: String? = null
)
