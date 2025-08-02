package com.example.findr

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainLayout(navController: NavController) {
    // This inner NavController is ONLY for the screens inside the bottom bar.
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = innerNavController)
        }
    ) { innerPadding ->
        // This NavHost now only contains the screens that have the bottom bar.
        NavHost(
            navController = innerNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ✅ CORRECTED: We pass the main NavController to the HomeScreen
            // so it can navigate OUTSIDE of this layout (to the details screen).
            composable(Screen.Home.route) { HomeScreen(navController = navController) }

            composable(Screen.Chats.route) { ConversationsScreen(navController = navController) }
            composable(Screen.MyItems.route) { MyItemsScreen(navController = navController) }
            composable(Screen.Profile.route) { ProfileScreen() }

            composable(
                route = "${Screen.Post.route}?itemType={itemType}",
                arguments = listOf(navArgument("itemType") { type = NavType.StringType; defaultValue = "Lost" })
            ) { backStackEntry ->
                val itemType = backStackEntry.arguments?.getString("itemType")
                PostItemScreen(
                    itemType = itemType ?: "Lost",
                    onUploadComplete = {
                        innerNavController.popBackStack()
                    }
                )
            }
        }
    }
}