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

        composable(
            route = "chat/{chatId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                navController = navController
            )
        }
    }
}
