package com.example.findr

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.findr.CloudinaryUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun PostItemScreen(
    onUploadComplete: (String) -> Unit = {}
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Choose Image")
        }

        imageUri?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (imageUri != null && description.isNotBlank()) {
                    isUploading = true
                    CloudinaryUtil.uploadImage(
                        fileUri = imageUri!!,
                         // ✅ Add your actual unsigned preset here
                        onSuccess = { imageUrl ->
                            savePostToFirestore(imageUrl, description) {
                                isUploading = false
                                onUploadComplete(imageUrl)
                            }
                        },
                        onError = {
                            Log.e("Cloudinary", "Upload failed: $it")
                            isUploading = false
                        }
                    )
                }
            },
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C73))
        ) {
            Text(if (isUploading) "Uploading..." else "Post Item", color = Color.White)
        }
    }
}

// 🔥 Firestore logic to save posted item
fun savePostToFirestore(imageUrl: String, description: String, onComplete: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()

    val post = mapOf(
        "userId" to user.uid,
        "imageUrl" to imageUrl,
        "description" to description,
        "timestamp" to Date()
    )

    db.collection("posts")
        .add(post)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { Log.e("Firestore", "Post failed: ${it.message}") }
}

