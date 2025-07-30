package com.raihan.castfit.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.raihan.castfit.data.datasource.schedule.ScheduleActivityDataSourceImpl
import com.raihan.castfit.data.model.ScheduleActivity
import com.raihan.castfit.data.repository.ScheduleActivityRepository
import com.raihan.castfit.data.repository.ScheduleActivityRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.context.GlobalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync() // Menjaga agar proses async bisa tetap berjalan setelah onReceive selesai

        val repository = GlobalContext.get().get<ScheduleActivityRepository>()

        // Menjalankan proses async untuk mengambil dan menjadwalkan ulang notifikasi
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                withTimeout(10000L) { // 25 detik timeout
                    repository.getUserScheduleActivity().collect { resultWrapper ->
                        when (resultWrapper) {
                            is ResultWrapper.Success -> {
                                val schedules = resultWrapper.payload ?: emptyList()
                                Log.d("BootReceiver", "Found ${schedules.size} schedules to process")

                                for (schedule in schedules) {
                                    val dateScheduled = schedule.dateScheduled
                                    if (dateScheduled != null && isToday(dateScheduled)) {
                                        scheduleNotificationAfterBoot(context, schedule)
                                    }
                                }

                                return@collect
                            }
                            is ResultWrapper.Error -> {
                                Log.e("BootReceiver", "Error getting schedules: ${resultWrapper.exception}")
                                return@collect
                            }
                            else -> {  }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error in boot receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    // Mengecek apakah tanggal yang diberikan adalah hari ini
    private fun isToday(dateString: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString)
            val today = Calendar.getInstance()
            val scheduleCal = Calendar.getInstance().apply {
                time = date ?: return false
            }

            today.get(Calendar.YEAR) == scheduleCal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == scheduleCal.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error parsing date: $dateString", e)
            false
        }
    }

    // Menjadwalkan ulang notifikasi untuk aktivitas terjadwal setelah boot
    @SuppressLint("ServiceCast", "ScheduleExactAlarm")
    private fun scheduleNotificationAfterBoot(context: Context, schedule: ScheduleActivity) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try {
            sdf.parse(schedule.dateScheduled)
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error parsing schedule date", e)
            null
        } ?: return

        val startTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endTime = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Menjadwalkan notifikasi tiap 30 menit dari 04:30 sampai 19:00
        while (startTime.before(endTime)) {
            if (startTime.timeInMillis <= System.currentTimeMillis()) {
                startTime.add(Calendar.MINUTE, 30)
                continue
            }

            val requestCode = (schedule.id ?: 0) + startTime.get(Calendar.MINUTE) + startTime.get(Calendar.HOUR_OF_DAY) * 100

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", "Aktivitas Terjadwal Hari Ini")
                putExtra("message", "Ingat untuk melakukan: ${schedule.physicalActivityName}")
                putExtra("notificationId", requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        startTime.timeInMillis,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startTime.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        startTime.timeInMillis,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startTime.timeInMillis,
                        pendingIntent
                    )
                }
            }

            Log.d("BootReceiver", "Rescheduled notification at ${startTime.time} for schedule ${schedule.id}")

            // Lanjut ke waktu berikutnya
            startTime.add(Calendar.MINUTE, 30)
        }
    }
}


