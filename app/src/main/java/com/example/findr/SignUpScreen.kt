package com.example.findr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.findr.ui.theme.FindrTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

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

                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates)


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
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()), // Make the screen scrollable
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.findmystuff),
                    contentDescription = "Campus Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1200f / 500f)
                        .padding(bottom = 24.dp)
                )


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Create an Account",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Join the campus community and never lose an item again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val kietEmailRegex = Regex("^[a-zA-Z0-9._%+-]+@kiet\\.edu$")
                                val trimmedEmail = email.trim()
                                val trimmedPassword = password.trim()
                                val trimmedFullName = fullName.trim()

                                if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || trimmedFullName.isBlank()) {
                                    error = "Please fill in all fields."
                                } else if (!kietEmailRegex.matches(trimmedEmail)) {
                                    error = "Only valid @kiet.edu emails are allowed!"
                                } else if (trimmedPassword.length < 6) {
                                    error = "Password must be at least 6 characters."
                                } else {
                                    error = null
                                    isLoading = true
                                    signUpUser(
                                        name = trimmedFullName,
                                        email = trimmedEmail,
                                        password = trimmedPassword,
                                        onSuccess = {
                                            isLoading = false
                                            onNavigateToSignIn()
                                        },
                                        onError = { errorMsg ->
                                            isLoading = false
                                            error = errorMsg
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text("Sign Up", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = onNavigateToSignIn) {
                            Text("Already have an account? Sign In", color = MaterialTheme.colorScheme.primary)
                        }

                        if (isLoading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
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
    FindrTheme {
        SignUpScreen {}
    }
}