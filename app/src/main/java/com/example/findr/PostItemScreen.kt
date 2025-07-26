package com.example.findr

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
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
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

// ✅ CORRECTED: The function now accepts the itemType and the onUploadComplete callback.
@Composable
fun PostItemScreen(
    itemType: String,
    onUploadComplete: (String) -> Unit
) {
    // ✅ The state is now initialized with the value from the navigation argument
    var currentItemType by remember { mutableStateOf(itemType) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(
            "Report an Item",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // This now works correctly because currentItemType is properly initialized.
        ItemTypeToggle(selectedType = currentItemType, onTypeSelected = { currentItemType = it })

        Spacer(modifier = Modifier.height(16.dp))

        // Image Picker remains the same
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected item image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Tap to select an image", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TextFields remain the same
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Item Description (e.g., Black Hydro Flask)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Last Known Location (e.g., Library 2nd Floor)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom

        Button(
            onClick = {
                if (imageUri != null && description.isNotBlank()) {
                    isUploading = true
                    // Correctly calls your Cloudinary utility
                    CloudinaryUtil.uploadImage(
                        fileUri = imageUri!!,
                        onSuccess = { imageUrl ->
                            // Correctly calls the Firestore function with the Cloudinary URL
                            savePostToFirestore(imageUrl, description, currentItemType, location) {
                                isUploading = false
                                onUploadComplete(imageUrl)
                            }
                        },
                        onError = {
                            Log.e("PostScreen", "Upload failed: $it")
                            isUploading = false
                        }
                    )
                }
            },
            enabled = !isUploading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C73))
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Submit Post", color = Color.White)
            }
        }
    }
}

// This function is now correct as it's called by the parent composable.
@Composable
fun ItemTypeToggle(selectedType: String, onTypeSelected: (String) -> Unit) {
    // ... (Code for this function remains the same)
}

// This function correctly saves the Cloudinary URL to Firestore, not the image itself.
fun savePostToFirestore(imageUrl: String, description: String, itemType: String, location: String, onComplete: () -> Unit) {
    // ... (Code for this function remains the same)
}


// ✅ CORRECTED: The preview now provides default arguments for the function.
@Preview(showBackground = true)
@Composable
fun PostItemScreenPreview() {
    PostItemScreen(itemType = "Lost", onUploadComplete = {})
}