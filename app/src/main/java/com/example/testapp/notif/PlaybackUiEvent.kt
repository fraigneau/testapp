package com.example.testapp.notif

sealed class PlaybackUiEvent {
    data class NowPlaying(
        val packageName: String,
        val title: String,
        val artist: String,
        val album: String,
        val isPlaying: Boolean,
        val artwork: Artwork
    ) : PlaybackUiEvent()
    data class Stopped(val packageName: String) : PlaybackUiEvent()
}