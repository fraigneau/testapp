package com.example.testapp.notif

sealed class PlaybackUiEvent {
    data class NowPlaying(
        val packageName: String,
        val isPlaying: Boolean,
        val title: String,
        val artist: String,
        val uri: String
    ) : PlaybackUiEvent()
    data class Stopped(val packageName: String) : PlaybackUiEvent()
}