package com.example.testapp.notif

import android.app.Notification
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PlayerNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "SpotifyNotif"
        private const val SPOTIFY_PKG = "com.spotify.music"
    }

    data class SpotifyNowPlaying(
        val title: String,
        val album: String,
        val artist: String,
        val cover: Bitmap?
    )

    override fun onListenerConnected() {
        Log.i(TAG, "Notification listener connected")
        activeNotifications?.forEach { sbn -> handlePosted(sbn) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        handlePosted(sbn)
    }

    private fun handlePosted(sbn: StatusBarNotification) {
        if (sbn.packageName != SPOTIFY_PKG) return

        val n: Notification = sbn.notification ?: return
        val extras = n.extras

        Log.i(TAG, "Notification Spotify détectée")

        // Récupérer le token MediaSession
        val token = if (Build.VERSION.SDK_INT >= 33) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, android.media.session.MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? android.media.session.MediaSession.Token
        } ?: run {
            Log.w(TAG, "⚠️ Pas de MediaSession.Token trouvé")
            return
        }

        val controller = MediaController(this, token)
        val md: MediaMetadata? = controller.metadata

        if (md == null) {
            Log.w(TAG, "⚠️ Pas de MediaMetadata disponible")
            return
        }

        val title  = md.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
        val album  = md.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty()
        val artist = md.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty()

        Log.i(TAG, "🎵 Metadata récupérée :")
        Log.i(TAG, "   • Titre  = '$title'")
        Log.i(TAG, "   • Album  = '$album'")
        Log.i(TAG, "   • Artiste= '$artist'")

        val cover = fetchCoverBitmap(md, contentResolver)
        if (cover != null) {
            Log.i(TAG, "   • Cover  = Bitmap ${cover.width}x${cover.height}")
        } else {
            Log.w(TAG, "   • Cover  = null")
        }

        val now = SpotifyNowPlaying(title, album, artist, cover)

        Log.i(TAG, "NowPlaying struct → $now")
    }

    private fun fetchCoverBitmap(md: MediaMetadata?, cr: ContentResolver): Bitmap? {
        if (md == null) return null

        // 1) Essayer les bitmaps intégrés
        val direct = listOf(
            MediaMetadata.METADATA_KEY_ALBUM_ART,
            MediaMetadata.METADATA_KEY_ART,
            MediaMetadata.METADATA_KEY_DISPLAY_ICON
        )
        for (k in direct) {
            val bmp = md.getBitmap(k)
            if (bmp != null) {
                Log.i(TAG, "Cover trouvée via clé $k (${bmp.width}x${bmp.height})")
                return bmp
            }
        }

        // 2) Essayer via les URI
        val uriKeys = listOf(
            MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
            MediaMetadata.METADATA_KEY_ART_URI,
            MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
            "android.media.extra.ART_HTTPS_URI"
        )

        for (k in uriKeys) {
            val uriStr = md.getString(k)
            if (!uriStr.isNullOrBlank()) {
                Log.i(TAG, "Tentative de fetch cover via URI $k : $uriStr")
                runCatching {
                    cr.openInputStream(android.net.Uri.parse(uriStr)).use { stream ->
                        if (stream != null) {
                            val bmp = BitmapFactory.decodeStream(stream)
                            if (bmp != null) {
                                Log.i(TAG, "Cover téléchargée depuis $uriStr (${bmp.width}x${bmp.height})")
                                return bmp
                            }
                        }
                    }
                }.onFailure {
                    Log.e(TAG, "Erreur en ouvrant $uriStr", it)
                }
            }
        }

        return null
    }
}
