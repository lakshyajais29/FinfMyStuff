package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ✅ Function to handle Sign Up logic
fun signUpUser(
    name: String,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener {
            val user = auth.currentUser
            if (user != null) {
                val userMap = mapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "name" to name
                )

                db.collection("users").document(user.uid)
                    .set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError("Firestore Error: ${e.message}") }
            } else {
                onError("User creation failed.")
            }
        }
        .addOnFailureListener { e -> onError("Sign Up Failed: ${e.message}") }
}

// ✅ UI Composable
@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        color = Color(0xFFF5F9FF)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ Logo
                Image(
                    painter = painterResource(id = R.drawable.findmystuff),
                    contentDescription = "Campus Logo",
                    modifier = Modifier
                        .height(80.dp)
                        .padding(bottom = 24.dp)
                )

                // ✅ Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Create an Account", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            "Join the campus community and never lose an item again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("College Email") },
                            placeholder = { Text("you@kiet.edu") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        error?.let {
                            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val kietEmailRegex = Regex("^[a-zA-Z0-9._%+-]+@kiet\\.edu$")

                                if (!kietEmailRegex.matches(email)) {
                                    error = "Only valid @kiet.edu emails are allowed!"
                                } else if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                                    error = "Please fill all fields."
                                } else if (password.length < 6) {
                                    error = "Password must be at least 6 characters."
                                } else {
                                    error = null
                                    isLoading = true
                                    signUpUser(
                                        name = fullName,
                                        email = email,
                                        password = password,
                                        onSuccess = {
                                            isLoading = false
                                            onNavigateToSignIn() // 🔁 NOW this works perfectly
                                        },
                                        onError = {
                                            isLoading = false
                                            error = it
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFA500),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Sign Up", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = { onNavigateToSignIn() }) {
                            Text("Already have an account? Sign In", color = Color(0xFF1A3C73))
                        }

                        // Optional loading state
                        if (isLoading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(color = Color(0xFFFFA500))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen {
        // Preview only — do nothing
    }
}
