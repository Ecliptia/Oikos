package com.ecliptia.oikos.data.model

data class Achievement(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val achieved: Boolean = false,
    val progress: Float = 0f // 0.0 to 1.0
)