package com.example.testapp.music

enum class MusicService { Spotify, Deezer }

data class TrackUi(
    val title: String? = null,
    val artist: String? = null,
    val coverUrl: String? = null
)

data class MusicUi(
    val provider: MusicService = MusicService.Spotify,
    val status: String = "Non connecté",
    val isLoggedIn: Boolean = false,
    val isPlaying: Boolean = false,
    val track: TrackUi? = null
)

data class MusicActions(
    val login: () -> Unit,
    val refresh: () -> Unit,
    val selectProvider: (MusicService) -> Unit
)
