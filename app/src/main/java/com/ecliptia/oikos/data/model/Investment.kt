package com.ecliptia.oikos.data.model

enum class InvestmentType {
    STOCK,
    CRYPTO,
    FUND,
    OTHER
}

data class Investment(
    val id: String = "",
    val name: String = "",
    val ticker: String = "",
    val type: InvestmentType = InvestmentType.OTHER,
    val quantity: Double = 0.0,
    val purchasePrice: Double = 0.0, // Average price per unit
    val currentPrice: Double = 0.0   // Manually updated by user
)
