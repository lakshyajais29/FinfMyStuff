package com.example.findr

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.findr.ui.theme.FindrTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.TimeUnit

@Composable
fun MyItemsScreen(navController: NavController) {
    val isInPreview = LocalInspectionMode.current
    var items by remember { mutableStateOf(listOf<PostItem>()) }
    var isLoading by remember { mutableStateOf(true) }
    var postToDelete by remember { mutableStateOf<PostItem?>(null) }
    val showDeleteDialog = postToDelete != null
    val context = LocalContext.current

    if (!isInPreview) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        DisposableEffect(userId) {
            if (userId.isBlank()) {
                isLoading = false
                return@DisposableEffect onDispose {}
            }

            val listenerRegistration: ListenerRegistration = FirebaseFirestore.getInstance()
                .collection("posts")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        Log.e("MyItemsScreen", "Listen failed.", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        // ✅ UPDATED: Fetches the new fields correctly
                        val list = snapshot.documents.mapNotNull { doc ->
                            PostItem(
                                id = doc.id,
                                imageUrl = doc.getString("imageUrl"), // Can be null
                                description = doc.getString("description") ?: "No description",
                                itemType = doc.getString("itemType") ?: "Lost", // Fetch itemType
                                timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                            )
                        }
                        items = list
                    }
                    isLoading = false
                }

            onDispose { listenerRegistration.remove() }
        }
    } else {
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            "My Posted Items",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "You haven't posted any items yet.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    MyItemCard(
                        item = item,
                        navController = navController,
                        onDeleteClick = { postToDelete = item }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to permanently delete this post?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        postToDelete?.let { item ->
                            deletePostFromFirestore(item.id) { success ->
                                val message = if (success) "Post deleted" else "Failed to delete post"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        postToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MyItemCard(
    item: PostItem,
    navController: NavController,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val timeAgo = remember(item.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - item.timestamp
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 1440 -> "${TimeUnit.MINUTES.toHours(minutes)}h ago"
            else -> "${TimeUnit.MINUTES.toDays(minutes)}d ago"
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            Column(
                modifier = Modifier.clickable { navController.navigate("item_details/${item.id}") }
            ) {
                // ✅ MODIFIED: This Box now conditionally shows the image or a placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // Only show the image if it's a "Lost" item and an image URL exists
                    if (item.itemType == "Lost" && item.imageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = item.description,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Show a generic search icon for "Found" items to prevent fraud
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Found Item",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = item.description,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

fun deletePostFromFirestore(postId: String, onComplete: (Boolean) -> Unit) {
    if (postId.isBlank()) {
        onComplete(false)
        return
    }
    FirebaseFirestore.getInstance().collection("posts").document(postId)
        .delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Post successfully deleted!")
            onComplete(true)
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error deleting document", e)
            onComplete(false)
        }
}

@Preview(showBackground = true)
@Composable
fun MyItemsScreenPreview() {
    FindrTheme {
        MyItemsScreen(navController = rememberNavController())
    }
}