package com.example.findr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // This listener correctly handles navigating the user when they log in or out.
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        startDestination = if (auth.currentUser != null) "main" else "signin"

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser == null) {
                navController.navigate("signin") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            }
        }
        auth.addAuthStateListener(authStateListener)
    }

    // The NavHost is now at the top level.
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
                    onNavigateToSignIn = { navController.popBackStack() }
                )
            }

            // The MainLayout is now just one of the destinations in the main NavHost.
            composable("main") {
                MainLayout(navController = navController)
            }

            // ✅ ADDED: These "full-screen" destinations are now at the top level,
            // so they will not show the bottom navigation bar.
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
}