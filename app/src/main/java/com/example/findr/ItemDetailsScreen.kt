package com.example.findr

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

// This data class does not need changes.
data class DetailedPostItem(
    val id: String = "",
    val imageUrl: String? = null,
    val description: String = "",
    val location: String = "",
    val itemType: String = "Lost",
    val timestamp: Long = 0L,
    val postedBy: String = ""
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
        if (postId.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("posts").document(postId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        post = DetailedPostItem(
                            id = document.id,
                            imageUrl = document.getString("imageUrl"),
                            description = document.getString("description") ?: "No Description",
                            location = document.getString("location") ?: "No Location Provided",
                            itemType = document.getString("itemType") ?: "Lost",
                            timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                            postedBy = document.getString("userId") ?: ""
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (post == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                post?.let { item ->
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(item.imageUrl),
                                    contentDescription = item.description,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    "No photo provided for this item.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.location,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

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
                                        // This now creates the correct Map structure
                                        createChatSession(
                                            chatId = chatId,
                                            postItem = item,
                                            participants = mapOf(currentUserId to true, posterId to true)
                                        ) { success ->
                                            if (success) {
                                                navController.navigate("chat/$chatId")
                                            } else {
                                                Log.e("ItemDetails", "Could not create chat session.")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Chat",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Connect with Owner",
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


fun createChatSession(
    chatId: String,
    postItem: DetailedPostItem,
    participants: Map<String, Boolean>,
    onComplete: (Boolean) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("chats/$chatId") // Write to the root of the chat
    val session = ChatSession(
        sessionId = chatId,
        postId = postItem.id,
        postImageUrl = postItem.imageUrl ?: "",
        postDescription = postItem.description,
        participants = participants,
        lastMessageTimestamp = System.currentTimeMillis()
    )
    // We now write the entire session object under a "metadata" child
    dbRef.child("metadata").setValue(session)
        .addOnSuccessListener {
            Log.d("ItemDetails", "Chat session created successfully.")
            onComplete(true)
        }
        .addOnFailureListener { e ->
            Log.e("ItemDetails", "Failed to create chat session: ${e.message}")
            onComplete(false)
        }
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsScreenPreview() {
    FindrTheme {
        ItemDetailsScreen(postId = "preview_id", navController = rememberNavController())
    }
}