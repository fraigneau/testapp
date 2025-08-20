package com.example.testapp.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.SpotifyProvider
import com.example.testapp.music.MusicFacade
import com.example.testapp.music.MusicProvider
import com.example.testapp.music.MusicService
import com.example.testapp.music.MusicUi
import com.example.testapp.music.TrackUi
import com.example.testapp.spotify.AuthManager
import com.example.testapp.spotify.TokenStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(app: Application) : AndroidViewModel(app) {

    private val spotify = SpotifyProvider(
        appContext = app.applicationContext,
        authManager = AuthManager(app.applicationContext),
        tokenStore = TokenStore(app.applicationContext)
    )

    private val providers: Map<MusicService, MusicProvider> = mapOf(
        MusicService.Spotify to spotify
    )

    private val facade = MusicFacade(
        context = app.applicationContext,
        providers = providers
    )

    private val _ui = MutableStateFlow(MusicUi(provider = facade.activeService()))
    val uiState = _ui.asStateFlow()

    private var autoJob: Job? = null

    fun switchService(s: MusicService) {
        facade.setService(s)
        _ui.value = _ui.value.copy(provider = s, status = "Service : $s")
        refreshNow()
    }

    fun login() {
        facade.startLogin()
    }

    fun onOAuthRedirect(intent: Intent) {
        if (!facade.canHandleRedirect(intent)) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(status = "Connexion…")
            val res = facade.handleRedirect(intent)
            if (res.isSuccess) {
                _ui.value = _ui.value.copy(
                    provider = facade.activeService(),
                    isLoggedIn = true,
                    status = "Connecté"
                )
                refreshNow()
            } else {
                _ui.value = _ui.value.copy(
                    isLoggedIn = false,
                    status = "Échec auth : ${res.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            val res = facade.getCurrentlyPlaying()
            _ui.value = if (res.isSuccess) {
                val np = res.getOrNull()
                if (np == null) {
                    _ui.value.copy(status = "Rien en cours de lecture", isPlaying = false, track = null)
                } else {
                    _ui.value.copy(
                        status = if (np.isPlaying) "Lecture détectée" else "En pause",
                        isLoggedIn = true,
                        isPlaying = np.isPlaying,
                        track = TrackUi(np.title, np.artist, np.imageUrl)
                    )
                }
            } else {
                _ui.value.copy(status = "Erreur : ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun startAutoRefresh(periodMs: Long = 5000L) {
        autoJob?.cancel()
        autoJob = viewModelScope.launch {
            while (true) {
                refreshNow()
                delay(periodMs)
            }
        }
    }

    fun stopAutoRefresh() {
        autoJob?.cancel()
        autoJob = null
    }

    private fun provider(): MusicProvider = providers.getValue(_ui.value.provider)
}
