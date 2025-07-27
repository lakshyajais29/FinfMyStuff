package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

// We need a more complete Post data class for this screen
data class DetailedPostItem(
    val id: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val postedBy: String = "" // The user ID of the person who posted
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    postId: String,
    navController: NavController
) {
    var post by remember { mutableStateOf<DetailedPostItem?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(postId) {
        FirebaseFirestore.getInstance().collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    post = DetailedPostItem(
                        id = document.id,
                        imageUrl = document.getString("imageUrl") ?: "",
                        description = document.getString("description") ?: "",
                        timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                        postedBy = document.getString("userId") ?: "" // Fetch the poster's user ID
                    )
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            if (post == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                post?.let { item ->
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUrl),
                        contentDescription = item.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Only show the button if the viewer is not the one who posted it
                    if (item.postedBy.isNotBlank() && item.postedBy != currentUserId) {
                        Button(
                            onClick = {
                                if (currentUserId != null) {
                                    // ✅ CORRECTED: Removed the backslashes before the underscores
                                    val posterId = item.postedBy
                                    val chatId = if (currentUserId < posterId) {
                                        "${currentUserId}_${posterId}_$postId"
                                    } else {
                                        "${posterId}_${currentUserId}_$postId"
                                    }

                                    // Create a session in Realtime DB so it appears in chat lists
                                    createChatSession(
                                        chatId = chatId,
                                        postItem = item,
                                        participants = listOf(currentUserId, posterId)
                                    )

                                    // Navigate to the chat screen
                                    navController.navigate("chat/$chatId")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C73))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect with Owner", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Helper function to create the chat session metadata in Realtime Database
fun createChatSession(chatId: String, postItem: DetailedPostItem, participants: List<String>) {
    val dbRef = FirebaseDatabase.getInstance().getReference("chats/$chatId/metadata")
    val session = ChatSession(
        sessionId = chatId,
        postId = postItem.id,
        postImageUrl = postItem.imageUrl,
        postDescription = postItem.description,
        participants = participants
    )
    dbRef.setValue(session)
}