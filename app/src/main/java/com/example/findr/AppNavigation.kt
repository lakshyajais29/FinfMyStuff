package com.example.findr

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ✅ Determine the starting screen based on Firebase Auth state
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "main" // User is already logged in
    } else {
        "signin" // User is not logged in
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // ✅ Sign In Screen
        composable("signin") {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = {
                    // Navigate to main and clear the back stack so the user can't go back to the login screen
                    navController.navigate("main") {
                        popUpTo("signin") { inclusive = true }
                    }
                }
            )
        }

        // ✅ Sign Up Screen
        composable("signup") {
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.popBackStack()
                }
            )
        }

        // ✅ Main Layout (contains Bottom Nav + inner screens)
        composable("main") {
            MainLayout()
        }
    }
}