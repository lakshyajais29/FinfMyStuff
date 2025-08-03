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
fun MainLayout(navController: NavController) { // This is the main NavController from AppNavigation
    // This inner NavController is ONLY for the screens inside the bottom bar.
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = innerNavController)
        }
    ) { innerPadding ->
        // This NavHost contains only the screens that have the bottom bar.
        NavHost(
            navController = innerNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ✅ CORRECTED: Pass both the main and inner NavControllers to HomeScreen.
            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController,       // For navigating to full screens like Details
                    innerNavController = innerNavController // For navigating to other tabs like Post
                )
            }

            // These screens also need the main controller to navigate to full-screen pages
            composable(Screen.Chats.route) { ConversationsScreen(navController = navController) }
            composable(Screen.MyItems.route) { MyItemsScreen(navController = navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }

            // This composable for the Post screen is correct as is.
            composable(
                route = "${Screen.Post.route}?itemType={itemType}",
                arguments = listOf(navArgument("itemType") { type = NavType.StringType; defaultValue = "Lost" })
            ) { backStackEntry ->
                val itemType = backStackEntry.arguments?.getString("itemType")
                PostItemScreen(
                    itemType = itemType ?: "Lost",
                    onUploadComplete = {
                        // This correctly pops the stack of the inner navigation to return to the previous tab
                        innerNavController.popBackStack()
                    }
                )
            }
        }
    }
}