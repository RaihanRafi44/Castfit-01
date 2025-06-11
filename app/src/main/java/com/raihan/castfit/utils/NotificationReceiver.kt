package com.raihan.castfit.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.raihan.castfit.R
import com.raihan.castfit.presentation.main.MainActivity
import com.raihan.castfit.presentation.splashscreen.SplashScreenActivity

/*
class NotificationReceiver: BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    */
/*override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "CastFit"
        val message = intent.getStringExtra("message") ?: "Aktivitas hari ini menunggumu!"

        val notificationIntent = Intent(context, SplashScreenActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "SCHEDULE_CHANNEL")
            .setSmallIcon(R.drawable.ic_profile)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1002, notification)
    }*//*

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Notifikasi"
        val message = intent.getStringExtra("message") ?: "Ada aktivitas hari ini!"

        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, "SCHEDULE_CHANNEL")
            .setSmallIcon(R.drawable.ic_profile) // Ganti dengan ikon valid
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}*/

/*class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val title = intent.getStringExtra("title") ?: "Aktivitas Terjadwal"
        val message = intent.getStringExtra("message") ?: "Ada aktivitas yang harus dilakukan hari ini."
        val notificationId = intent.getIntExtra("notificationId", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "schedule_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Schedule Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // 👉 Tambahkan ini: intent untuk membuka aplikasi saat notifikasi ditekan
        val openAppIntent = Intent(context, SplashScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_profile) // ganti sesuai icon app kamu
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent) // 🔥 ini yang bikin notifikasimu bisa ditekan
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        *//*val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_profile) // ganti sesuai icon app kamu
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()*//*

        notificationManager.notify(notificationId, notification)
    }
}*/

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val WAKE_LOCK_TIMEOUT = 10000L // 10 detik
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // 5. Acquire wake lock untuk memastikan device bangun
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

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "schedule_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Schedule Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    // 6. Konfigurasi channel untuk bypass DND
                    setBypassDnd(true)
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openAppIntent = Intent(context, SplashScreenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_profile)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                // 7. Tambahan untuk memastikan notifikasi muncul
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setFullScreenIntent(pendingIntent, false) // untuk urgent notifications
                .build()

            notificationManager.notify(notificationId, notification)

            Log.d("NotificationReceiver", "Notification sent at ${System.currentTimeMillis()}")

        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
