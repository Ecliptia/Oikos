package com.ecliptia.oikos.data.model

import java.util.Date

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class RecurringTransaction(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String? = null, // For expenses
    val source: String? = null,   // For incomes
    val frequency: Frequency = Frequency.MONTHLY,
    val lastGeneratedDate: Long = 0L, // Timestamp of the last time this transaction was generated
    val nextDueDate: Long = Date().time, // Timestamp of the next time this transaction is due
    val isActive: Boolean = true
)
