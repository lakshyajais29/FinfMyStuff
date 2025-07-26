package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MyItemsScreen() {
    val isInPreview = LocalInspectionMode.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var items by remember { mutableStateOf(listOf<PostItem>()) }

    if (!isInPreview) {
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
                }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("My Posted Items", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(2), content = {
            items(items) { item ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        })
    }
}
@Preview(showBackground = true)
@Composable
fun MyItemsScreenPreview() {
    MyItemsScreen()
}


