package com.example.findr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.findr.ui.theme.FindrTheme
import com.example.findr.CloudinaryUtil


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Enable edge-to-edge layout
        enableEdgeToEdge()

        // ✅ Initialize Cloudinary with context
        CloudinaryUtil.initCloudinary(applicationContext)

        setContent {
            FindrTheme {
                // ✅ Navigation graph starts here
                AppNavigation()
            }
        }
    }
}
