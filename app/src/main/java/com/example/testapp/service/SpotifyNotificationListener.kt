package com.example.testapp.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap

class SpotifyNotificationListener : NotificationListenerService() {

    private var controller: MediaController? = null
    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            pushFromController()
        }
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            pushFromController()
        }
        override fun onSessionDestroyed() {
            controller = null
            NowPlayingRepository.update(null)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.spotify.music") return

        val notif = sbn.notification
        val token = notif.extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
        if (token != null) {
            attachController(token)
            pushFromController()
        } else {
            // Fallback si pas de token (rare) : lire les extras de la notif
            NowPlayingRepository.update(buildFromNotificationFallback(notif))
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.spotify.music") return
        // Quand la notif disparaît, on peut clear l’état si le contrôleur est absent
        if (controller == null) {
            NowPlayingRepository.update(null)
        }
    }

    private fun attachController(token: MediaSession.Token) {
        val current = controller
        if (current != null && current.sessionToken == token) return
        current?.unregisterCallback(controllerCallback)

        controller = MediaController(this, token).also {
            it.registerCallback(controllerCallback)
        }
    }

    private fun pushFromController() {
        val c = controller ?: return
        val md = c.metadata
        val st = c.playbackState

        val title = md?.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = md?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val album = md?.getString(MediaMetadata.METADATA_KEY_ALBUM)
        val duration = md?.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val art: Bitmap? =
            md?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: md?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val isPlaying = st?.state == PlaybackState.STATE_PLAYING
        val position = st?.position

        NowPlayingRepository.update(
            SpotifyNowPlaying(
                isPlaying = isPlaying,
                title = title,
                artist = artist,
                album = album,
                durationMs = duration,
                positionMs = position,
                albumArt = art
            )
        )
    }

    private fun buildFromNotificationFallback(n: Notification): SpotifyNowPlaying {
        val title = n.extras.getString(Notification.EXTRA_TITLE)         // Souvent: "Titre"
        val artistOrAlbum = n.extras.getString(Notification.EXTRA_TEXT)  // Souvent: "Artiste"
        val sub = n.extras.getString(Notification.EXTRA_SUB_TEXT)        // Parfois: "Album"

        // Récupérer la grande icône comme fallback pochette
        val albumArt: Bitmap? = when {
            Build.VERSION.SDK_INT >= 23 -> {
                (n.getLargeIcon() as? Icon)?.loadDrawable(this)?.toBitmap()
            }
            else -> {
                @Suppress("DEPRECATION")
                n.largeIcon
            }
        }

        return SpotifyNowPlaying(
            isPlaying = guessIsPlayingFromActions(n),
            title = title,
            artist = artistOrAlbum,
            album = sub,
            durationMs = null,
            positionMs = null,
            albumArt = albumArt
        )
    }

    private fun guessIsPlayingFromActions(n: Notification): Boolean {
        // Heuristique: Spotify change l’icône Play/Pause. Si action "Pause" présente → en lecture
        val actions = n.actions ?: return false
        return actions.any { it.title?.toString()?.contains("Pause", ignoreCase = true) == true }
    }
}