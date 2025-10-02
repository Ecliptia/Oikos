package com.ecliptia.oikos.data.model

import java.util.Date

data class Allocation(
    val id: String = "",
    val ruleName: String = "",
    val amount: Double = 0.0,
    val date: Date = Date()
)
