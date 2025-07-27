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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun ConversationsScreen(navController: NavController) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var conversations by remember { mutableStateOf<List<ChatSession>>(emptyList()) }
    val dbRef = FirebaseDatabase.getInstance().getReference("chats")

    // Listener for user's conversations
    DisposableEffect(currentUserId) {
        val listener = dbRef.orderByChild("metadata/participants/0").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val loadedConversations = snapshot.children.mapNotNull {
                        it.child("metadata").getValue(ChatSession::class.java)
                    }
                    conversations = loadedConversations.sortedByDescending { it.lastMessageTimestamp }
                }
                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })

        onDispose { dbRef.removeEventListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(
            "My Chats",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You have no active chats.", color = Color.Gray)
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                Text(session.postDescription, fontWeight = FontWeight.Bold)
                Text(session.lastMessage, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationsScreenPreview() {
    // This screen would be empty in preview as it needs a NavController
    // ConversationsScreen(navController = rememberNavController())
}