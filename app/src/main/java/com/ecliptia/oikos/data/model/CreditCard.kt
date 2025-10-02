package com.ecliptia.oikos.data.model

enum class CardBrand {
    VISA,
    MASTERCARD,
    ELO,
    AMEX,
    HIPERCARD,
    OTHER
}

data class CreditCard(
    val id: String = "",
    val name: String = "",
    val limit: Double = 0.0,
    val closingDay: Int = 1, // Day of the month the invoice closes
    val dueDate: Int = 10,   // Day of the month the invoice is due
    val brand: CardBrand = CardBrand.OTHER
)
