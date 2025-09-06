package com.example.findr

// ✅ CORRECTED: This data class is now updated to support image messages.
data class ChatMessage(
    val messageId: String = "",
    val text: String? = null,      // Text is now nullable to allow for image-only messages
    val imageUrl: String? = null, // Added field for the image URL, also nullable
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// This data class is already correct and does not need changes.
data class ChatSession(
    val sessionId: String = "",
    val postId: String = "",
    val postImageUrl: String = "",
    val postDescription: String = "",
    // This change is the key to the solution.
    val participants: Map<String, Boolean> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L
)