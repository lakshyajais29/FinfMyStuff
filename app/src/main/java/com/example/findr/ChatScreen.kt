package com.example.findr

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.findr.ui.theme.FindrTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    verificationImageUrl: String?,
    navController: NavController
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    val dbRef = FirebaseDatabase.getInstance().getReference("chats/$chatId/messages")
    val metadataRef = FirebaseDatabase.getInstance().getReference("chats/$chatId/metadata")

    var showVerificationButton by remember { mutableStateOf(verificationImageUrl != null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            CloudinaryUtil.uploadImage(it,
                onSuccess = { imageUrl ->
                    sendMessage(dbRef, metadataRef, currentUserId, text = null, imageUrl = imageUrl)
                },
                onError = { /* Handle upload error */ }
            )
        }
    }

    DisposableEffect(chatId) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        }
        dbRef.addValueEventListener(listener)
        onDispose { dbRef.removeEventListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom
            ) {
                items(messages) { message ->
                    MessageBubble(message = message, isCurrentUser = message.senderId == currentUserId)
                }
            }

            if (showVerificationButton) {
                Button(
                    onClick = {
                        sendMessage(dbRef, metadataRef, currentUserId, text = null, imageUrl = verificationImageUrl)
                        showVerificationButton = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Share Photo")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Verification Photo")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attach Image")
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            sendMessage(dbRef, metadataRef, currentUserId, text.trim(), imageUrl = null)
                            text = ""
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

fun sendMessage(
    dbRef: DatabaseReference,
    metadataRef: DatabaseReference,
    senderId: String,
    text: String?,
    imageUrl: String?
) {
    val messageId = dbRef.push().key ?: ""
    val timestamp = System.currentTimeMillis()
    // This will now work correctly
    val message = ChatMessage(messageId, text, imageUrl, senderId, timestamp)
    dbRef.child(messageId).setValue(message)

    val lastMessageText = if (!text.isNullOrEmpty()) text else "[Image]"
    metadataRef.updateChildren(
        mapOf(
            "lastMessage" to lastMessageText,
            "lastMessageTimestamp" to timestamp
        )
    )
}

@Composable
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                        bottomEnd = if (isCurrentUser) 0.dp else 16.dp
                    )
                )
                .background(if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Column {
                // This will now work correctly
                message.imageUrl?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = it),
                        contentDescription = "Sent image",
                        modifier = Modifier
                            .sizeIn(maxHeight = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                message.text?.let {
                    Text(
                        text = it,
                        color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    FindrTheme {
        ChatScreen(
            chatId = "preview_chat_id",
            verificationImageUrl = "https://example.com/image.jpg",
            navController = rememberNavController()
        )
    }
}