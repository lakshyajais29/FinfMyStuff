package com.example.findr

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ✅ CORRECTED: Determine the start destination before building the NavHost.
    // This removes the need for a composable splash screen.
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "main" // User is already logged in, go to the main layout
    } else {
        "signin" // User is not logged in, go to the sign-in screen
    }

    // The NavHost now starts directly at the correct screen.
    NavHost(navController = navController, startDestination = startDestination) {

        // The old "splash" route has been removed.

        composable("signin") {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = {
                    // Navigate to main and clear the back stack so the user can't go back
                    navController.navigate("main") {
                        popUpTo("signin") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onNavigateToSignIn = { navController.popBackStack() }
            )
        }

        // The MainLayout now accepts the top-level NavController to handle
        // navigation to full-screen destinations like ItemDetails and Chat.
        composable("main") {
            MainLayout(navController = navController)
        }

        composable(
            route = "item_details/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            ItemDetailsScreen(postId = postId, navController = navController)
        }

        composable(
            route = "chat/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(chatId = chatId, navController = navController)
        }
    }
}