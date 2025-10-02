package com.ecliptia.oikos.data.model

import java.util.Date

data class Notification(
    val id: String = "",
    val message: String = "",
    val timestamp: Long = Date().time,
    val read: Boolean = false,
    val type: String = "", // e.g., "alert", "goal_progress", "insight"
    val relatedId: String? = null // e.g., ID of a goal or category
)