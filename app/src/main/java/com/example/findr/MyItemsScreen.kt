package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

// ✅ UPDATED: The screen now accepts a NavController
@Composable
fun MyItemsScreen(navController: NavController) {
    val isInPreview = LocalInspectionMode.current
    var items by remember { mutableStateOf(listOf<PostItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    if (!isInPreview) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        LaunchedEffect(Unit) {
            FirebaseFirestore.getInstance()
                .collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { doc ->
                        val url = doc.getString("imageUrl") ?: return@mapNotNull null
                        val desc = doc.getString("description") ?: "No description"
                        val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                        PostItem(doc.id, url, desc, timestamp)
                    }
                    items = list
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    } else {
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(
            "My Posted Items",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "You haven't posted any items yet.\nTap the 'Post' button to get started!",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    // ✅ Pass the NavController to each card
                    MyItemCard(item = item, navController = navController)
                }
            }
        }
    }
}

@Composable
fun MyItemCard(item: PostItem, navController: NavController) {
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        // ✅ ADDED: The clickable modifier to navigate to the details screen
        modifier = Modifier.clickable {
            navController.navigate("item_details/${item.id}")
        }
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(item.imageUrl),
                contentDescription = item.description,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.description,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyItemsScreenPreview() {
    // Pass a dummy NavController for the preview
    MyItemsScreen(navController = rememberNavController())
}
