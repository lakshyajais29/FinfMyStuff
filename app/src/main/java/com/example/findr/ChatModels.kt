package com.example.findr

// Represents a single message in a chat
data class ChatMessage(
    val messageId: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// Represents a chat session between two users about an item
data class ChatSession(
    val sessionId: String = "",
    val postId: String = "",
    val postImageUrl: String = "",
    val postDescription: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L
)