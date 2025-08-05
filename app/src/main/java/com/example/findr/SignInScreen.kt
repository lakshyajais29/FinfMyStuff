package com.example.findr

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.findr.ui.theme.FindrTheme
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
            onSuccess()
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Login failed")
        }
}

fun sendPasswordReset(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e.message ?: "Failed to send reset email.") }
}

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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
                            "Welcome Back!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            "Sign in with your college email to continue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )


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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val trimmedEmail = email.trim()
                                    if (trimmedEmail.isNotEmpty()) {
                                        error = null // Clear previous errors
                                        sendPasswordReset(
                                            email = trimmedEmail,
                                            onSuccess = {
                                                Toast.makeText(context, "Password reset link sent to $trimmedEmail", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { errorMsg -> error = errorMsg }
                                        )
                                    } else {
                                        error = "Please enter your email first."
                                    }
                                }
                            ) {
                                Text("Forgot Password?")
                            }
                        }


                        error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))


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
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text("Sign In", style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                        TextButton(onClick = onNavigateToSignUp) {
                            Text("Don't have an account? Sign Up", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    FindrTheme {
        SignInScreen(
            onNavigateToSignUp = {},
            onLoginSuccess = {}
        )
    }
}