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
            // This call remains correct
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home, MyItems, and Profile screens remain the same
            composable(Screen.Home.route) { HomeScreen(navController = navController) }
            composable(Screen.MyItems.route) { MyItemsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }

            // ✅ CORRECTED: This single composable now handles all navigation to the Post screen.
            composable(
                // The route now includes an optional argument for itemType
                route = "${Screen.Post.route}?itemType={itemType}",
                arguments = listOf(
                    navArgument("itemType") {
                        type = NavType.StringType
                        defaultValue = "Lost" // Default to "Lost" if no argument is passed (e.g., from bottom nav)
                    }
                )
            ) { backStackEntry ->
                // Extract the argument. It will use the defaultValue if not provided.
                val itemType = backStackEntry.arguments?.getString("itemType")
                PostItemScreen(
                    itemType = itemType ?: "Lost", // Pass the argument to the screen
                    onUploadComplete = {
                        navController.popBackStack() // Go back after upload is complete
                    }
                )
            }
        }
    }
}