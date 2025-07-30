package com.raihan.castfit.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.raihan.castfit.R
import com.raihan.castfit.presentation.main.MainActivity
import com.raihan.castfit.presentation.splashscreen.SplashScreenActivity

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        // Timeout maksimum untuk wake lock
        private const val WAKE_LOCK_TIMEOUT = 10000L // 10 detik
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // Pastikan context dan intent tidak null sebelum melanjutkan
        if (context == null || intent == null) return

        // Mengaktifkan partial wake lock agar proses notifikasi tidak terganggu jika perangkat sedang idle
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::NotificationWakeLock"
        )

        try {
            wakeLock.acquire(WAKE_LOCK_TIMEOUT)

            val title = intent.getStringExtra("title") ?: "Aktivitas Terjadwal"
            val message = intent.getStringExtra("message") ?: "Ada aktivitas yang harus dilakukan hari ini."
            val notificationId = intent.getIntExtra("notificationId", 0)

            // Menyiapkan notification manager dan channel
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "schedule_channel"

            // Buat notification channel jika API >= 26 (Oreo)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Schedule Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setBypassDnd(true)
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Intent untuk membuka aplikasi saat notifikasi ditekan
            val openAppIntent = Intent(context, SplashScreenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Bangun dan tampilkan notifikasi
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_android_castfit_removebg)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setFullScreenIntent(pendingIntent, false)
                .build()

            // Tampilkan notifikasi ke user
            notificationManager.notify(notificationId, notification)

            Log.d("NotificationReceiver", "Notification sent at ${System.currentTimeMillis()}")

        } finally {
            // Melepas wake lock
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
