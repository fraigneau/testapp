package com.example.testapp.ui

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.testapp.data.DataStoreProvider
import com.example.testapp.ui.component.section.ChartsSection
import com.example.testapp.ui.component.section.DataSection
import com.example.testapp.ui.component.section.MusicSection
import com.example.testapp.viewmodel.TrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(
    dataStoreProvider: DataStoreProvider,
    trackViewModel: TrackViewModel,

    spotifyStatus: String,
    spotifyTitle: String?,
    spotifyArtist: String?,
    spotifyImageUrl: String?,
    onSpotifyLogin: () -> Unit,
    onSpotifyRefresh: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomDestinations.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
                if (spotifyImageUrl != null) {
                    Log.i("ImageUri", "Spotify Image URL: $spotifyImageUrl")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { ChartsSection() }
            composable(Screen.Profile.route) { DataSection(dataStoreProvider, trackViewModel) }
            composable(Screen.Music.route) { MusicSection(spotifyStatus, spotifyTitle, spotifyArtist, spotifyImageUrl, onSpotifyLogin, onSpotifyRefresh) }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
@Composable fun SettingsScreen(){ Text("Page Réglages",modifier = Modifier.padding(16.dp)) }
