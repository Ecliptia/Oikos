package com.ecliptia.oikos.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val isCustom: Boolean = true // True for user-defined categories, false for predefined
)
