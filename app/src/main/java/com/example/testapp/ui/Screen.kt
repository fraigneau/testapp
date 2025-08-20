package com.example.testapp.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Accueil", Icons.Filled.Home)
    data object Profile : Screen("profile", "Profil", Icons.Filled.Person)
    data object Music : Screen("music", "Musique", Icons.Filled.LibraryMusic)
    data object Settings : Screen("settings", "Réglages", Icons.Filled.Settings)
}

val bottomDestinations = listOf(Screen.Home, Screen.Profile, Screen.Music, Screen.Settings)
