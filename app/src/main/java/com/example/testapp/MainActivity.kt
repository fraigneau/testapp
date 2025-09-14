package com.example.testapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.testapp.data.DataStoreProvider
import com.example.testapp.data.DatabaseProvider
import com.example.testapp.music.NowPlaying
import com.example.testapp.MyForegroundService
import com.example.testapp.spotify.AuthManager
import com.example.testapp.spotify.TokenStore
import com.example.testapp.ui.Menu
import com.example.testapp.ui.PermissionRequestScreen
import com.example.testapp.ui.theme.TestAppTheme
import com.example.testapp.viewmodel.MusicViewModel
import com.example.testapp.viewmodel.TrackViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var dataStoreProvider: DataStoreProvider
    private lateinit var trackViewModel: TrackViewModel
    private lateinit var authManager: AuthManager
    private lateinit var tokenStore: TokenStore

    private val musicVm: MusicViewModel by viewModels()


    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startMyService(this) // pour call la notification

        dataStoreProvider = DataStoreProvider(this)

        DatabaseProvider.init(this@MainActivity)
        trackViewModel = TrackViewModel(DatabaseProvider)

        lifecycleScope.launch(Dispatchers.IO) {
            //! trackViewModel.pushTracksToDatabase()
            trackViewModel.loadTracks()
        }

        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))


        authManager = AuthManager(this)
        tokenStore = TokenStore(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    musicVm.refreshNow()
                    delay(1_000)
                }
            }
        }

        //startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

        enableEdgeToEdge()
        setContent {

            val multiplePermissionsState = rememberMultiplePermissionsState(
                listOf(
                    Manifest.permission.INTERNET,
                )
            )
            TestAppTheme {
                if (multiplePermissionsState.allPermissionsGranted) {

                    val musicUi = musicVm.uiState.collectAsStateWithLifecycle().value
                    LaunchedEffect(Unit) { musicVm.startAutoRefresh(1000) }

                    Menu(
                        dataStoreProvider = dataStoreProvider,
                        trackViewModel = trackViewModel,

                        spotifyStatus = musicUi.status,
                        spotifyTitle = musicUi.track?.title,
                        spotifyArtist = musicUi.track?.artist,
                        spotifyImageUrl = musicUi.track?.coverUrl,

                        onSpotifyLogin = { musicVm.login() },
                        onSpotifyRefresh = { musicVm.refreshNow() }
                    )
                } else {
                    PermissionRequestScreen(multiplePermissionsState)
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        musicVm.onOAuthRedirect(intent)
        setIntent(Intent(this, javaClass))

    }

    override fun onStart() {
        super.onStart()
        intent?.let {
            if (authManager.isRedirect(it)) {
                musicVm.onOAuthRedirect(it)
                intent = Intent(this, javaClass)
            }
        }
    }

    override fun onStop() {
        //stopMyService(this)
        super.onStop()
    }

    fun startMyService(context: Context) {
        val i = Intent(context, MyForegroundService::class.java)
        context.startForegroundService(i)
    }

    fun stopMyService(context: Context) {
        context.stopService(Intent(context, MyForegroundService::class.java))
    }


}
