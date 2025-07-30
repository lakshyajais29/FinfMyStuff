package com.example.findr

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainLayout() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = navController) }

            // ✅ CORRECTED: Pass the navController to MyItemsScreen
            composable(Screen.MyItems.route) { MyItemsScreen(navController = navController) }

            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Chats.route) { ConversationsScreen(navController = navController) }

            composable(
                route = "${Screen.Post.route}?itemType={itemType}",
                arguments = listOf(
                    navArgument("itemType") {
                        type = NavType.StringType
                        defaultValue = "Lost"
                    }
                )
            ) { backStackEntry ->
                val itemType = backStackEntry.arguments?.getString("itemType")
                PostItemScreen(
                    itemType = itemType ?: "Lost",
                    onUploadComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
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
}