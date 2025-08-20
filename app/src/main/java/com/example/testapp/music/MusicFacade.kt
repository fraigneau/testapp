package com.example.testapp.music

import android.content.Context
import android.content.Intent

class MusicFacade(
    private val context: Context,
    private val providers: Map<MusicService, MusicProvider>,
    initial: MusicService? = null
) {
    @Volatile
    private var active: MusicService = initial ?: autoDetect(context)

    fun activeService(): MusicService = active
    fun setService(s: MusicService) { if (providers.containsKey(s)) active = s }

    fun startLogin() = provider().startLogin(context)

    fun canHandleRedirect(intent: Intent): Boolean =
        providers.values.any { it.canHandleRedirect(intent) }

    suspend fun handleRedirect(intent: Intent): Result<Unit> {
        val p = providers.values.firstOrNull { it.canHandleRedirect(intent) }
            ?: return Result.failure(IllegalStateException("Aucun provider ne peut gérer ce redirect"))
        setService(p.service)
        return p.handleRedirect(intent)
    }

    suspend fun getCurrentlyPlaying() = provider().getCurrentlyPlaying()

    private fun provider(): MusicProvider = providers.getValue(active)

    private fun autoDetect(ctx: Context): MusicService {
        fun installed(pkg: String) = try {
            ctx.packageManager.getPackageInfo(pkg, 0)
            true
        } catch (_: Throwable) { false }

        return when {
            installed("com.spotify.music")  -> MusicService.Spotify
            installed("deezer.android.app") -> MusicService.Deezer
            else -> MusicService.Spotify
        }
    }
}