package com.example.findr

import android.Manifest
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PostItemScreen(
    itemType: String,
    onUploadComplete: (String) -> Unit
) {
    var currentItemType by remember { mutableStateOf(itemType) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ State for the new Bottom Sheet
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // ✅ State and launcher for Camera Permission
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // URI for the camera to save the photo
    val cameraImageUri: Uri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        )
    }

    // Launcher for picking an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    // Launcher for taking a picture with the camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = cameraImageUri
        }
    }

    // --- UI ---
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

        ItemTypeToggle(selectedType = currentItemType, onTypeSelected = { currentItemType = it })

        Spacer(modifier = Modifier.height(16.dp))

        // Image Picker - Now triggers the bottom sheet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .clickable { showBottomSheet = true }, // Show bottom sheet
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
                    Text("Tap to add an image", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        // Display error message if upload fails
        errorMessage?.let {
            Text(
                text = "Upload Failed: $it",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes button to the bottom

        Button(
            onClick = {
                errorMessage = null // Clear previous errors
                if (imageUri != null && description.isNotBlank() && location.isNotBlank()) {
                    isUploading = true
                    CloudinaryUtil.uploadImage(
                        fileUri = imageUri!!,
                        onSuccess = { imageUrl ->
                            savePostToFirestore(imageUrl, description, currentItemType, location) { success ->
                                isUploading = false
                                if (success) {
                                    onUploadComplete(imageUrl)
                                } else {
                                    errorMessage = "Could not save post to database."
                                }
                            }
                        },
                        onError = { error ->
                            Log.e("PostScreen", "Upload failed: $error")
                            errorMessage = error
                            isUploading = false
                        }
                    )
                } else {
                    errorMessage = "Please add an image and fill out all fields."
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

    // ✅ ADDED: The Modal Bottom Sheet for Camera/Gallery choice
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Choose Image Source", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                ListItem(
                    headlineContent = { Text("Take Photo") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = "Camera") },
                    modifier = Modifier.clickable {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                            // Request permission and launch camera
                            if (cameraPermissionState.status.isGranted) {
                                cameraLauncher.launch(cameraImageUri)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text("Choose from Gallery") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery") },
                    modifier = Modifier.clickable {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                            galleryLauncher.launch("image/*")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ItemTypeToggle(selectedType: String, onTypeSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(if (selectedType == "Lost") Color(0xFFF9A825) else Color.White)
                .clickable { onTypeSelected("Lost") }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("I Lost Something", color = if (selectedType == "Lost") Color.White else Color.Black)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(if (selectedType == "Found") Color(0xFF1A3C73) else Color.White)
                .clickable { onTypeSelected("Found") }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("I Found Something", color = if (selectedType == "Found") Color.White else Color.Black)
        }
    }
}

// Updated Firestore function to report success/failure
fun savePostToFirestore(imageUrl: String, description: String, itemType: String, location: String, onComplete: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser ?: return onComplete(false)
    val db = FirebaseFirestore.getInstance()

    val post = mapOf(
        "userId" to user.uid,
        "imageUrl" to imageUrl,
        "description" to description,
        "itemType" to itemType,
        "location" to location,
        "timestamp" to Date()
    )

    db.collection("posts")
        .add(post)
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener {
            Log.e("Firestore", "Post failed: ${it.message}")
            onComplete(false)
        }
}


@Preview(showBackground = true)
@Composable
fun PostItemScreenPreview() {
    PostItemScreen(itemType = "Lost", onUploadComplete = {})
}