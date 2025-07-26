// ✅ AppNavigation.kt
package com.example.findr

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "signin") {

        // ✅ Sign In Screen
        composable("signin") {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { navController.navigate("main") }
            )
        }

        // ✅ Sign Up Screen
        composable("signup") {
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.popBackStack()
                    navController.navigate("signin")
                }
            )
        }

        // ✅ Main Layout (contains Bottom Nav + inner screens)
        composable("main") {
            MainLayout()
        }
    }
}
