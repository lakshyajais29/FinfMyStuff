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
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = innerNavController)
        }
    ) { innerPadding ->

        NavHost(
            navController = innerNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    innerNavController = innerNavController
                )
            }


            composable(Screen.Chats.route) { ConversationsScreen(navController = navController) }
            composable(Screen.MyItems.route) { MyItemsScreen(navController = navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }

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