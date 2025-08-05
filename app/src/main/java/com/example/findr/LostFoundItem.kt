package com.example.findr

data class LostFoundItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "",
    val postedBy: String = ""
)
