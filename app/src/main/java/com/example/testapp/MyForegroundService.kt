package com.example.testapp

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.*

class MyForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "my_channel_id"
        const val NOTIF_ID = 42

        const val ACTION_START = "fg.START"
        const val ACTION_UPDATE = "fg.UPDATE"

        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
        const val EXTRA_IMAGE_URL = "imageUrl"
    }

    private val svcScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        imageLoader = ImageLoader(this)
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification(null, null, null))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> { /* déjà lancé */ }
            ACTION_UPDATE -> {
                val title = intent.getStringExtra(EXTRA_TITLE)
                val artist = intent.getStringExtra(EXTRA_ARTIST)
                val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

                svcScope.launch {
                    val bmp = loadBitmap(imageUrl)
                    val notif = buildNotification(title, artist, bmp)
                    val nm = getSystemService(NotificationManager::class.java)
                    nm.notify(NOTIF_ID, notif)
                }
            }
        }
        return START_STICKY
    }

    private suspend fun loadBitmap(url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        val req = ImageRequest.Builder(this)
            .data(url)
            .allowHardware(false)
            .build()
        val result = imageLoader.execute(req).drawable ?: return null
        return result.toBitmap()
    }

    private fun buildNotification(
        title: String?,
        artist: String?,
        largeIcon: Bitmap?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentTitle = listOfNotNull(title, artist?.let { "— $it" }).joinToString(" ")

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(contentTitle.ifBlank { "Lecture en cours" })
            .setContentText("Spotify") // ou un texte fixe
            .setContentIntent(pi)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setLargeIcon(largeIcon) // pochette
            //.setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Lecture Spotify",
                NotificationManager.IMPORTANCE_LOW
            )
            ch.setShowBadge(false)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        svcScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
