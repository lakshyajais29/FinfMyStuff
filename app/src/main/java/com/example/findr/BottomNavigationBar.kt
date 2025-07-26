package com.example.findr

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute = currentRoute(navController)

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == "post",
            onClick = { navController.navigate("post") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.post),
                    contentDescription = "Post"
                )
            },
            label = { Text("Post") }
        )

        NavigationBarItem(
            selected = currentRoute == "myitems",
            onClick = { navController.navigate("myitems") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.items),
                    contentDescription = "My Items"
                )
            },
            label = { Text("My Items") }
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.profile1),
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") }
        )
    }
}

// 🔍 Utility function to detect the current screen
@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
