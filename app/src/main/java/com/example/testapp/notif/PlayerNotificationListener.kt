package com.example.testapp.notif

import android.app.Notification
import android.media.session.MediaController
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PlayerNotificationListener: NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        private val INTERESTING_PACKAGES = setOf(
            "com.spotify.music"
            // Deezer: "deezer.android.app"
            // Youtube Music: "com.google.android.apps.youtube.music"
        )
    }

    override fun onListenerConnected() {

        Log.i(TAG, "Notification listener connected")
        activeNotifications.forEach { snb -> handlePosted(snb, currentRanking) }

    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {

        handlePosted(sbn, rankingMap)

    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap,
        reason: Int
    ) {

        if (!INTERESTING_PACKAGES.contains(sbn.packageName)) return
        Log.i(TAG, "Notification removed from ${sbn.packageName}, reason=$reason")
        //NotifBus.emit(null)

    }

    private fun handlePosted(sbn: StatusBarNotification, rankingMap: RankingMap?) {
        val pkg = sbn.packageName ?: return
        if (!INTERESTING_PACKAGES.contains(pkg)) return

        val notif = sbn.notification ?: return
        val extras = notif.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val sub   = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()

        val token = extras.getParcelable<android.media.session.MediaSession.Token>(
            Notification.EXTRA_MEDIA_SESSION
        )

        var art = extractArtFromNotification(notif) // fallback par défaut

        if (token != null) {
            val controller = MediaController(this, token)
            val md = controller.metadata
            val state = controller.playbackState

            // essaie d'abord la MediaSession (souvent mieux/plus frais)
            val artFromMd = extractArtFromMetadata(controller)
            if (artFromMd.uri != null || artFromMd.bitmap != null || artFromMd.icon != null) {
                art = artFromMd
            }

            val track  = md?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
            val artist = md?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
            val album  = md?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM)
            val isPlaying = state?.state == android.media.session.PlaybackState.STATE_PLAYING

            NotifBus.emit(
                PlaybackUiEvent.NowPlaying(
                    packageName = pkg,
                    title = track ?: title,
                    artist = artist ?: sub,
                    album = album ?: text,
                    isPlaying = isPlaying,
                    artwork = art  // <-- passe l’Artwork
                )
            )
        } else {
            NotifBus.emit(
                PlaybackUiEvent.NowPlaying(
                    packageName = pkg,
                    title = title,
                    artist = sub,
                    album = text,
                    isPlaying = inferIsPlayingFromStyle(notif),
                    artwork = art
                )
            )
        }

        Log.i(TAG, "Notification from $pkg: '$title' / '$text' / '$sub' / art=$art")
    }

    private fun extractArtFromMetadata(controller: MediaController): Artwork {
        val md = controller.metadata ?: return Artwork()
        // 1) URI d’abord
        val uri = md.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: md.getString(android.media.MediaMetadata.METADATA_KEY_ART_URI)
            ?: md.getString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)

        if (!uri.isNullOrBlank()) return Artwork(uri = uri)

        // 2) Bitmap sinon
        val bmp = md.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: md.getBitmap(android.media.MediaMetadata.METADATA_KEY_ART)
            ?: md.getBitmap(android.media.MediaMetadata.METADATA_KEY_DISPLAY_ICON)

        return Artwork(bitmap = bmp)
    }

    private fun extractArtFromNotification(n: Notification): Artwork {
        val extras = n.extras

        // Icon (API 23+)
        n.getLargeIcon()?.let { icon ->
            return Artwork(icon = icon)
        }

        // Bitmaps selon le style
        (extras.getParcelable<android.graphics.Bitmap>(Notification.EXTRA_LARGE_ICON)
            ?: extras.getParcelable(Notification.EXTRA_LARGE_ICON_BIG)
            ?: extras.getParcelable(Notification.EXTRA_PICTURE)
                )?.let { bmp ->
                return Artwork(bitmap = bmp)
            }

        return Artwork()
    }

    private fun inferIsPlayingFromStyle(n: Notification): Boolean {
        val actions = n.actions ?: return false
        return actions.any { it.title?.toString()?.contains("pause", ignoreCase = true) == true }
    }

}