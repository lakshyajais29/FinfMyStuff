package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.findr.ui.theme.FindrTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun ConversationsScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var conversations by remember { mutableStateOf<List<ChatSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val dbRef = FirebaseDatabase.getInstance().getReference("chats")

    // Listener for user's conversations
    DisposableEffect(currentUserId) {
        if (currentUserId.isBlank()) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }

        // ✅ CORRECTED: This query now fetches all chats and filters on the client side.
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val loadedConversations = snapshot.children.mapNotNull {
                    it.child("metadata").getValue(ChatSession::class.java)
                }.filter {
                    // This finds all chats where the current user is a participant.
                    it.participants.contains(currentUserId)
                }
                conversations = loadedConversations.sortedByDescending { it.lastMessageTimestamp }
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                /* Handle error */
            }
        }
        dbRef.addValueEventListener(listener)

        onDispose { dbRef.removeEventListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            "My Chats",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "You have no active chats.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(conversations) { session ->
                    ConversationCard(session = session) {
                        navController.navigate("chat/${session.sessionId}")
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationCard(session: ChatSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = session.postImageUrl),
                contentDescription = session.postDescription,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    session.postDescription,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    session.lastMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationsScreenPreview() {
    FindrTheme {
        ConversationsScreen(navController = rememberNavController())
    }
}