package com.example.findr

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import androidx.compose.material3.*
import androidx.compose.ui.Modifier

@Composable
fun MainLayout() {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(navController = bottomNavController) }
            composable("post") { PostItemScreen(onUploadComplete = { }) }
            composable("myitems") { MyItemsScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}
