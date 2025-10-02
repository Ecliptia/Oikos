package com.ecliptia.oikos.data.model

import java.util.Date

enum class BillingCycle {
    MONTHLY,
    YEARLY
}

data class Subscription(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val firstBillDate: Long = Date().time, // Store as Long for Firebase
    val category: String = "",
    val isActive: Boolean = true
)
