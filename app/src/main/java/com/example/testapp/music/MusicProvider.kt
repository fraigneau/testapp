package com.example.testapp.music

import android.content.Context
import android.content.Intent

interface MusicProvider {
    val service: MusicService
    fun startLogin(context: Context)
    fun canHandleRedirect(intent: Intent): Boolean
    suspend fun handleRedirect(intent: Intent): Result<Unit>
    suspend fun getCurrentlyPlaying(): Result<NowPlaying?>

}