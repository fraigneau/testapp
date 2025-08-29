package com.example.testapp.notif

import android.app.Notification
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PlayerNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        private val INTERESTING_PACKAGES = setOf(
            "com.spotify.music",
            // "deezer.android.app",
            // "com.google.android.apps.youtube.music",
        )
    }

    override fun onListenerConnected() {
        Log.i(TAG, "Notification listener connected")
        activeNotifications.forEach { sbn ->
            handlePosted(sbn, currentRanking)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        handlePosted(sbn, rankingMap)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap, reason: Int) {
        if (!INTERESTING_PACKAGES.contains(sbn.packageName)) return
        Log.i(TAG, "Notification removed from ${sbn.packageName}, reason=$reason")
        NotifBus.emit(PlaybackUiEvent.Stopped(packageName = sbn.packageName))
    }

    private fun handlePosted(sbn: StatusBarNotification, @Suppress("UNUSED_PARAMETER") rankingMap: RankingMap?) {
        val pkg = sbn.packageName ?: return
        if (!INTERESTING_PACKAGES.contains(pkg)) return

        val n = sbn.notification ?: return
        val extras = n.extras

        val notifTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val notifText  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        var title = notifTitle
        var artist = notifText
        var isPlaying = inferIsPlayingFromActions(n)
        var artworkUri = ""

        val token = extras.getParcelable(
            Notification.EXTRA_MEDIA_SESSION,
            android.media.session.MediaSession.Token::class.java
        )


        if (token != null) {
            val controller = MediaController(this, token)
            val md = controller.metadata
            val state = controller.playbackState

            title     = md?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)  ?: title
            artist    = md?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: artist
            artworkUri = md?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART_URI).orEmpty()

            isPlaying = state?.state == PlaybackState.STATE_PLAYING
        }

        NotifBus.emit(
            PlaybackUiEvent.NowPlaying(
                packageName = pkg,
                isPlaying = isPlaying,
                title = title,
                artist = artist,
                uri = artworkUri
            )
        )

        Log.i(TAG, "NowPlaying from $pkg: / playing=$isPlaying / title='$title' / artist='$artist' / uri=${artworkUri.takeIf { it.isNotEmpty() } ?: "<none>"}")
    }

    private fun inferIsPlayingFromActions(n: Notification): Boolean {
        val actions = n.actions ?: return false
        return actions.any { it.title?.toString()?.contains("pause", ignoreCase = true) == true }
    }
}
