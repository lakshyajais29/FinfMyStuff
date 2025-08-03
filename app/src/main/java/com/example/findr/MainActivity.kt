package com.example.findr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.findr.ui.theme.FindrTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Call installSplashScreen() BEFORE super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Cloudinary
        CloudinaryUtil.initCloudinary(applicationContext)

        setContent {
            FindrTheme {
                // The AppNavigation composable now determines the start route
                AppNavigation()
            }
        }
    }
}