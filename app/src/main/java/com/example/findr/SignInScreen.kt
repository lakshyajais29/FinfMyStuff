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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth


fun signInUser(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email.trim(), password.trim())
        .addOnSuccessListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.email != null) {
                onSuccess()
            } else {
                onError("Something went wrong. Please try again.")
            }
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Login failed")
        }
}

// ✅ Composable UI
@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

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
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.findmystuff),
                    contentDescription = "Campus Logo",
                    modifier = Modifier
                        .height(80.dp)
                        .padding(bottom = 24.dp)
                )

                // Sign In Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Welcome Back!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            "Sign in with your college email to continue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("College Email") },
                            placeholder = { Text("you@kiet.edu") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error message
                        error?.let {
                            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sign In Button
                        Button(
                            onClick = {
                                val trimmedEmail = email.trim()
                                val trimmedPassword = password.trim()
                                val collegeEmailRegex = Regex("^[a-zA-Z0-9._%+-]+@kiet\\.edu$")

                                when {
                                    trimmedEmail.isEmpty() || trimmedPassword.isEmpty() -> {
                                        error = "Please fill in all fields."
                                    }

                                    !collegeEmailRegex.matches(trimmedEmail) -> {
                                        error = "Only @kiet.edu emails are allowed!"
                                    }

                                    else -> {
                                        error = null
                                        signInUser(
                                            email = trimmedEmail,
                                            password = trimmedPassword,
                                            onSuccess = onLoginSuccess,
                                            onError = { errorMsg -> error = errorMsg }
                                        )
                                    }
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
                            Text("Sign In", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Navigate to Sign Up
                        TextButton(onClick = onNavigateToSignUp) {
                            Text("Don't have an account? Sign Up", color = Color(0xFF1A3C73))
                        }
                    }
                }
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    SignInScreen(
        onNavigateToSignUp = {},
        onLoginSuccess = {}
    )
}
