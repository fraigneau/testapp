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


        val token = extras?.getParcelable<android.media.session.MediaSession.Token>(
            Notification.EXTRA_MEDIA_SESSION
        )

        var controller: MediaController? = null
        if (token != null) {
            controller = MediaController(this, token)
            val metadata = controller.metadata
            val state = controller.playbackState

            val track  = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
            val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
            val album  = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM)
            val isPlaying = state?.state == android.media.session.PlaybackState.STATE_PLAYING

            NotifBus.emit(
                PlaybackUiEvent.NowPlaying(
                    packageName = pkg,
                    title = track ?: title,
                    artist = artist ?: sub,
                    album = album ?: "",
                    isPlaying = isPlaying,
                    // artwork via METADATA_KEY_ALBUM_ART_URI si dispo
                )
            )
        } else {
            NotifBus.emit(
                PlaybackUiEvent.NowPlaying(
                    packageName = pkg,
                    title = title,
                    artist = sub,
                    album = "",
                    isPlaying = inferIsPlayingFromStyle(notif)
                )
            )
        }

        Log.i(TAG, "Notification from $pkg: '$title' / '$text' / '$sub'")
    }

    private fun inferIsPlayingFromStyle(n: Notification): Boolean {
        val actions = n.actions ?: return false
        return actions.any { it.title?.toString()?.contains("pause", ignoreCase = true) == true }
    }

}