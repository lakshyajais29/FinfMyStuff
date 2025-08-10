package com.example.findr

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState


sealed class Screen(val route: String, val label: String, val iconResId: Int) {
    object Home : Screen("home", "Home", R.drawable.home)
    object Chats : Screen("chats", "Chats", R.drawable.chat)
    object Post : Screen("post", "Post", R.drawable.post)
    object MyItems : Screen("myitems", "My Items", R.drawable.items)
    object Profile : Screen("profile", "Profile", R.drawable.profile1)
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navItems = listOf(
        Screen.Home,
        Screen.Chats,
        Screen.Post,
        Screen.MyItems,
        Screen.Profile
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        navItems.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route?.startsWith(screen.route) == true } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {

                    Icon(
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = screen.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(screen.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF9A825),
                    selectedTextColor = Color(0xFFF9A825),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}