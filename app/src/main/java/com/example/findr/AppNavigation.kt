package com.example.findr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // This LaunchedEffect listens for authentication changes in real-time.
    // This is the key to fixing the logout crash.
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startDestination = "main"
        } else {
            startDestination = "signin"
        }

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                // When user logs out, navigate to signin and clear the entire back stack
                navController.navigate("signin") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            }
        }
        auth.addAuthStateListener(authStateListener)
    }

    // Show a loading screen or nothing until the start destination is determined
    if (startDestination != null) {
        NavHost(navController = navController, startDestination = startDestination!!) {

            composable("signin") {
                SignInScreen(
                    onNavigateToSignUp = { navController.navigate("signup") },
                    onLoginSuccess = {
                        navController.navigate("main") {
                            popUpTo("signin") { inclusive = true }
                        }
                    }
                )
            }

            composable("signup") {
                SignUpScreen(
                    onNavigateToSignIn = {
                        navController.popBackStack()
                    }
                )
            }

            composable("main") {
                MainLayout()
            }
        }
    }
}