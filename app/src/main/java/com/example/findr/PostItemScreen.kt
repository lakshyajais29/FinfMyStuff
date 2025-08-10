package com.example.findr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.findr.ui.theme.FindrTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val cameraImageUri: Uri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = cameraImageUri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(cameraImageUri)
        } else {
            Toast.makeText(context, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Report an Item",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        ItemTypeToggle(selectedType = currentItemType, onTypeSelected = { currentItemType = it })

        Spacer(modifier = Modifier.height(16.dp))

        // This UI now changes based on whether the item is Lost or Found
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clickable { showBottomSheet = true },
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    // ✅ Text changes based on context to guide the user
                    Text(
                        text = if (currentItemType == "Lost") "Tap to add a photo (optional)"
                        else "Add a photo for verification (will be kept private)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
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

        errorMessage?.let {
            Text(
                text = "Upload Failed: $it",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                errorMessage = null
                // ✅ MODIFIED: Allow 'Lost' items to be posted without an image
                if (description.isNotBlank() && location.isNotBlank()) {
                    isUploading = true
                    if (imageUri != null) {
                        // If an image is provided, upload it first
                        CloudinaryUtil.uploadImage(
                            fileUri = imageUri!!,
                            onSuccess = { imageUrl ->
                                savePostToFirestore(imageUrl, description, currentItemType, location) { success ->
                                    isUploading = false
                                    if (success) onUploadComplete(imageUrl)
                                    else errorMessage = "Could not save post details."
                                }
                            },
                            onError = { error ->
                                Log.e("PostScreen", "Upload failed: $error")
                                errorMessage = error
                                isUploading = false
                            }
                        )
                    } else {
                        // If no image is provided, save the post directly
                        savePostToFirestore(null, description, currentItemType, location) { success ->
                            isUploading = false
                            if (success) onUploadComplete("")
                            else errorMessage = "Could not save post details."
                        }
                    }
                } else {
                    errorMessage = "Please fill out all fields."
                }
            },
            enabled = !isUploading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Submit Post", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

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
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                    cameraLauncher.launch(cameraImageUri)
                                }
                                else -> {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(if (selectedType == "Lost") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
                .clickable { onTypeSelected("Lost") }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "I Lost Something",
                color = if (selectedType == "Lost") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(if (selectedType == "Found") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                .clickable { onTypeSelected("Found") }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "I Found Something",
                color = if (selectedType == "Found") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ✅ UPDATED: The function now handles a nullable imageUrl to support posts without images
fun savePostToFirestore(imageUrl: String?, description: String, itemType: String, location: String, onComplete: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser ?: return onComplete(false)
    val db = FirebaseFirestore.getInstance()

    val post = mutableMapOf<String, Any>(
        "userId" to user.uid,
        "description" to description,
        "itemType" to itemType,
        "location" to location,
        "timestamp" to Date()
    )

    // Only add the imageUrl to the map if one was provided
    if (imageUrl != null) {
        post["imageUrl"] = imageUrl
    }

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
    FindrTheme {
        PostItemScreen(itemType = "Lost", onUploadComplete = {})
    }
}