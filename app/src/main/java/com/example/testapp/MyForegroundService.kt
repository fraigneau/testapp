package com.example.testapp

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MyForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "my_channel_id"
        const val NOTIF_ID = 42
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel() // requis sur Android 8.0+
        startAsForeground()
    }

    private fun startAsForeground() {
        // Intent pour ouvrir l’activité au tap
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Service actif")
            .setContentText("Fonctionne en arrière-plan…")
            .setContentIntent(pi)
            .setOngoing(true)              // empêche le swipe pour la fermer
            .setOnlyAlertOnce(true)        // pas de ding à chaque update
            .build()

        // Important: démarre le service au premier plan avec la notif
        startForeground(NOTIF_ID, notif)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Tâches en cours",
                NotificationManager.IMPORTANCE_LOW // évite le bruit/vibration
            )
            ch.setShowBadge(false)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ton travail long ici (thread/coroutine/Worker…)
        // Si tu veux mettre à jour le texte, rebuild + startForeground(NOTIF_ID, notif) ou notify()
        return START_STICKY
    }

    override fun onDestroy() {
        // Nettoyage si besoin
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
