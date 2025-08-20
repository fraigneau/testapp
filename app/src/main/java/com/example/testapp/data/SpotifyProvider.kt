package com.example.testapp.data

import android.content.Context
import android.content.Intent
import com.example.testapp.music.MusicProvider
import com.example.testapp.music.MusicService
import com.example.testapp.music.NowPlaying
import com.example.testapp.spotify.AuthConfig
import com.example.testapp.spotify.AuthManager
import com.example.testapp.spotify.RetrofitBuilder
import com.example.testapp.spotify.TokenStore
import com.example.testapp.spotify.Tokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpotifyProvider(
    private val appContext: Context,
    private val authManager: AuthManager,
    private val tokenStore: TokenStore
) : MusicProvider {

    override val service: MusicService = MusicService.Spotify

    override fun startLogin(context: Context) {
        authManager.startLogin()
    }

    override fun canHandleRedirect(intent: Intent): Boolean =
        authManager.isRedirect(intent)

    override suspend fun handleRedirect(intent: Intent): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val code = authManager.readAuthCode(intent)
                    ?: return@withContext Result.failure(IllegalStateException("Code OAuth manquant"))

                val verifier = authManager.lastVerifier
                    ?: AuthManager.consumeStoredVerifier(appContext)
                    ?: return@withContext Result.failure(IllegalStateException("Code verifier manquant"))

                val resp = RetrofitBuilder.auth().exchangeCode(
                    clientId = AuthConfig.CLIENT_ID,
                    code = code,
                    redirectUri = AuthConfig.REDIRECT_URI,
                    codeVerifier = verifier
                )

                tokenStore.save(
                    Tokens(
                        accessToken = resp.accessToken,
                        refreshToken = resp.refreshToken ?: "",
                        expiresInSec = resp.expiresIn,
                        obtainedAtMillis = System.currentTimeMillis()
                    )
                )
                Result.success(Unit)

            } catch (e: retrofit2.HttpException) {
                val body = e.response()?.errorBody()?.string()
                android.util.Log.e("SpotifyAuth", "HTTP ${e.code()} ${e.message()} body=$body")
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }


    override suspend fun getCurrentlyPlaying(): Result<NowPlaying?> =
        withContext(Dispatchers.IO) {
            try {
                val tokens = tokenStore.load() ?: return@withContext Result.success(null)

                val access = if (tokenStore.isExpired(tokens)) {
                    val r = RetrofitBuilder.auth().refresh(
                        clientId = AuthConfig.CLIENT_ID,
                        refreshToken = tokens.refreshToken
                    )
                    tokenStore.save(
                        Tokens(
                            accessToken = r.accessToken,
                            refreshToken = tokens.refreshToken,
                            expiresInSec = r.expiresIn,
                            obtainedAtMillis = System.currentTimeMillis()
                        )
                    )
                    r.accessToken
                } else tokens.accessToken

                val res = RetrofitBuilder.api().currentlyPlaying("Bearer $access")
                if (res.code() == 204) return@withContext Result.success(null)
                if (!res.isSuccessful) return@withContext Result.failure(retrofit2.HttpException(res))

                val body = res.body()
                val tr = body?.item
                Result.success(
                    NowPlaying(
                        title = tr?.name,
                        artist = tr?.artists?.joinToString { it.name.orEmpty() },
                        imageUrl = tr?.album?.images?.firstOrNull()?.url,
                        isPlaying = body?.isPlaying == true
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

}