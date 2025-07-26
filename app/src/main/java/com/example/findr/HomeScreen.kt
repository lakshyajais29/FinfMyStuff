package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.concurrent.TimeUnit

data class PostItem(
    val id: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val timestamp: Long = 0L
)

// MODIFICATION 1: HomeScreen no longer creates its own Scaffold.
// It now accepts PaddingValues from the parent Scaffold.
@Composable
fun HomeScreen(paddingValues: PaddingValues, navController: NavController? = null) {
    var posts by remember { mutableStateOf(listOf<PostItem>()) }
    var searchText by remember { mutableStateOf("") }

    // 🔥 Fetch posts from Firebase (No changes here)
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    val url = doc.getString("imageUrl") ?: return@mapNotNull null
                    val desc = doc.getString("description") ?: "No description"
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    PostItem(doc.id, url, desc, timestamp)
                }
                posts = list
            }
    }

    // The Scaffold is removed from here. This Column is now the root element.
    Column(
        modifier = Modifier
            .padding(paddingValues) // Apply padding from the parent Scaffold
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Light gray background
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Header with Logo Placeholder and back arrow
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.leftarrow),
                contentDescription = "Back",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                painter = painterResource(id = R.drawable.findmystuff),
                contentDescription = "CampusFind Logo",
                modifier = Modifier.height(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF1A3C73)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔸 Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { /* TODO: Navigate to Lost Item form */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9A825)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("I Lost Something", color = Color.White)
            }
            Button(
                onClick = { /* TODO: Navigate to Found Item form */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C73)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("I Found Something", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Recently Posted Items",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🆕 Vertical Grid for posts
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(posts) { item ->
                PostCard(item = item)
            }
        }
    }
}

@Composable
fun PostCard(item: PostItem) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: navigate to details */ }
    ) {
        Image(
            painter = rememberAsyncImagePainter(item.imageUrl),
            contentDescription = "Item image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )
        Text(
            item.description,
            maxLines = 2,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            timeAgo,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray
    ) {
        val isHomeSelected = true

        NavigationBarItem(
            selected = isHomeSelected,
            onClick = { /* TODO: Navigate to Home */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFF9A825),
                selectedTextColor = Color(0xFFF9A825),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Navigate to Post */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.post),
                    contentDescription = "Post",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Post") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Navigate to My Items */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.items),
                    contentDescription = "My Items",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("My Items") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* TODO: Navigate to Profile */ },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.profile1),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Profile") }
        )
    }
}


// MODIFICATION 2: The preview now provides its own Scaffold
// so we can see how HomeScreen looks within a real screen layout.
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    // The Scaffold is now here, just for the preview
    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) { innerPadding ->
        // Pass the padding from the preview's Scaffold to the HomeScreen
        HomeScreen(paddingValues = innerPadding)
    }
}