package com.ecliptia.oikos.data.model

import java.util.Date

sealed class Transaction {
    abstract val id: String
    abstract val amount: Double
    abstract val date: Date
    abstract val description: String
}

data class Income(
    override val id: String = "",
    override val amount: Double = 0.0,
    override val date: Date = Date(),
    override val description: String = "",
    val source: String = ""
) : Transaction()

data class Expense(
    override val id: String = "",
    override val amount: Double = 0.0,
    override val date: Date = Date(),
    override val description: String = "",
    val category: String = "",
    val paymentMethod: String = "", // e.g., "Credit Card", "Pix", "Cash"
    val relatedSavingsBoxId: String? = null
) : Transaction()
