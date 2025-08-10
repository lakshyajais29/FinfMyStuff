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

    // This correctly determines the start destination based on login state.
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        "main"
    } else {
        "signin"
    }

    NavHost(navController = navController, startDestination = startDestination) {

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
                onNavigateToSignIn = { navController.popBackStack() }
            )
        }

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

        // ✅ CORRECTED: The chat route now accepts an optional verificationImageUrl
        composable(
            route = "chat/{chatId}?verificationImageUrl={verificationImageUrl}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("verificationImageUrl") {
                    type = NavType.StringType
                    nullable = true // This makes the argument optional
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            // Decode the URL back to its original form, as it was encoded before navigation
            val encodedUrl = backStackEntry.arguments?.getString("verificationImageUrl")
            val imageUrl = encodedUrl?.let { java.net.URLDecoder.decode(it, "UTF-8") }

            ChatScreen(
                chatId = chatId,
                verificationImageUrl = imageUrl, // Pass the URL to the screen
                navController = navController
            )
        }
    }
}