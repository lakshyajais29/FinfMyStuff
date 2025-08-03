package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    // Fetch the specific post from Firestore
    LaunchedEffect(postId) {
        if (postId.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("posts").document(postId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        post = DetailedPostItem(
                            id = document.id,
                            imageUrl = document.getString("imageUrl") ?: "",
                            description = document.getString("description") ?: "No Description",
                            timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                            postedBy = document.getString("userId") ?: "" // Fetch the poster's user ID
                        )
                    }
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
        // The main content area
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (post == null) {
                // Show a loading indicator while data is being fetched
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                post?.let { item ->
                    // ✅ WRAPPED content in a scrollable Column
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                                fontWeight = FontWeight.Bold,
                                // ✅ CHANGED: Use the theme's text color
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // You can add other details here
                        }
                    }

                    // This Box places the button at the bottom of the screen
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        if (item.postedBy.isNotBlank() && item.postedBy != currentUserId) {
                            Button(
                                onClick = {
                                    if (currentUserId != null) {
                                        val posterId = item.postedBy
                                        val chatId = if (currentUserId < posterId) {
                                            "${currentUserId}_${posterId}_$postId"
                                        } else {
                                            "${posterId}_${currentUserId}_$postId"
                                        }
                                        createChatSession(
                                            chatId = chatId,
                                            postItem = item,
                                            participants = listOf(currentUserId, posterId)
                                        )
                                        navController.navigate("chat/$chatId")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                // ✅ CHANGED: Use the theme's primary color
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "Chat",
                                    // ✅ CHANGED: Use the theme's text color for the icon
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Connect with Owner",
                                    // ✅ CHANGED: Use the theme's text color for the button
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
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