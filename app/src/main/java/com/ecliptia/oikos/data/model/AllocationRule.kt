package com.ecliptia.oikos.data.model

enum class AllocationType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class AllocationRule(
    val id: String = "",
    val name: String = "",
    val type: AllocationType = AllocationType.PERCENTAGE,
    val value: Double = 0.0
)
