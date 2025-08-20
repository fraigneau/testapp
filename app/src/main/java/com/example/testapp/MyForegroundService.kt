package com.example.testapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.testapp.data.SpotifyProvider
import com.example.testapp.spotify.AuthManager
import com.example.testapp.spotify.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MyForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "my_channel_id"
        const val NOTIF_ID = 42
        const val ACTION_START = "fg.START"
        const val ACTION_STOP = "fg.STOP"
    }

    private val svcScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var imageLoader: ImageLoader
    private lateinit var provider: SpotifyProvider
    private lateinit var tokenStore: TokenStore
    private lateinit var authManager: AuthManager

    override fun onCreate() {
        super.onCreate()

        imageLoader = ImageLoader(this)
        tokenStore = TokenStore(applicationContext)
        authManager = AuthManager(applicationContext)
        provider = SpotifyProvider(
            appContext = applicationContext,
            authManager = authManager,
            tokenStore = tokenStore
        )

        createNotificationChannel()

        startForeground(NOTIF_ID, buildNotification(title = null, artist = null, largeIcon = null))

        svcScope.launch {
            while (isActive) {
                try {
                    val now = provider.getCurrentlyPlaying().getOrNull()
                    val bmp = loadBitmap(now?.imageUrl)
                    val notif = buildNotification(
                        title = now?.title,
                        artist = now?.artist,
                        largeIcon = bmp
                    )
                    getSystemService(NotificationManager::class.java)
                        .notify(NOTIF_ID, notif)
                } catch (_: Throwable) {
                }
                delay(2_000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        svcScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun loadBitmap(url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        val req = ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false)
            .build()
        val drawable = imageLoader.execute(req).drawable ?: return null
        return drawable.toBitmap()
    }

    private fun buildNotification(
        title: String?,
        artist: String?,
        largeIcon: Bitmap?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val contentPI = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentTitle = listOfNotNull(
            title?.takeIf { it.isNotBlank() },
            artist?.takeIf { it.isNotBlank() }?.let { "— $it" }
        ).joinToString(" ").ifBlank { "Lecture en cours" }

        val stopIntent = Intent(this, MyForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPI = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(contentTitle)
            .setContentText("Spotify")
            .setContentIntent(contentPI)
            .setLargeIcon(largeIcon) // pochette de l'album
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "Stop", stopPI)
            .build()
    }

    private fun createNotificationChannel() {
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Lecture Spotify",
            NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }
}
