package com.example.testapp.music

data class NowPlaying(
    val title: String?,
    val artist: String?,
    val imageUrl: String?,
    val isPlaying: Boolean,
    val progressMs: Long?
)
