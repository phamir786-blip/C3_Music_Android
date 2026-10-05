package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Now Playing", Icons.Default.Home)
    object Receivers : Screen("receivers", "Wi-Fi", Icons.Default.Podcasts)
    object Devices : Screen("devices", "Devices", Icons.Default.Podcasts)
    object C3Web : Screen("c3_web", "C3 Music", Icons.Default.Podcasts)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items = listOf(Home, Receivers, Devices, Settings)
    }
}
