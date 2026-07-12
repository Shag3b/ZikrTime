package com.shaghb.zikrtime.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shaghb.zikrtime.ReminderScheduler
import com.shaghb.zikrtime.notification.ZikrNotificationHelper
import com.shaghb.zikrtime.utils.PrayerTimeHelper
import java.util.Calendar

/**
 * WorkManager fallback worker
 * Checks if AlarmManager notifications were missed and triggers them if needed
 * This ensures notifications fire even if AlarmManager gets blocked by Doze mode
 */
class NotificationFallbackWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val prayerTimes = PrayerTimeHelper.getTodayPrayerTimes(applicationContext)

        // Calculate period times
        val fajrTime = prayerTimes.fajr.time
        val asrTime = prayerTimes.asr.time
        val morningStartTime = fajrTime + (10 * 60 * 1000)
        val morningEndTime = asrTime
        val eveningStartTime = asrTime + (20 * 60 * 1000)
        val midnightTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        // Check Morning Period
        val isInMorningPeriod = now >= morningStartTime && now < morningEndTime
        val morningCompleted = prefs.getBoolean("morningCompleted", false)

        if (isInMorningPeriod && !morningCompleted) {
            val lastMorningNotif = prefs.getLong("last_morning_notification", 0)
            val timeSinceLastNotif = now - lastMorningNotif
            val workerFiredToday = prefs.getBoolean("worker_fired_morning_today", false)

            if (timeSinceLastNotif > 60 * 60 * 1000 && !workerFiredToday) {
                ZikrNotificationHelper.showInitialPeriodNotification(applicationContext, true)
                prefs.edit()
                    .putLong("last_morning_notification", now)
                    .putBoolean("worker_fired_morning_today", true)
                    .apply()

                ReminderScheduler.scheduleMorningReminders(applicationContext)
            }
        }

        // Check Evening Period
        val isInEveningPeriod = now >= eveningStartTime && now <= midnightTime
        val eveningCompleted = prefs.getBoolean("eveningCompleted", false)

        if (isInEveningPeriod && !eveningCompleted) {
            val lastEveningNotif = prefs.getLong("last_evening_notification", 0)
            val timeSinceLastNotif = now - lastEveningNotif
            val workerFiredToday = prefs.getBoolean("worker_fired_evening_today", false)

            if (timeSinceLastNotif > 60 * 60 * 1000 && !workerFiredToday) {
                ZikrNotificationHelper.showInitialPeriodNotification(applicationContext, false)
                prefs.edit()
                    .putLong("last_evening_notification", now)
                    .putBoolean("worker_fired_evening_today", true)
                    .apply()

                ReminderScheduler.scheduleEveningReminders(applicationContext)
            }
        }

        return Result.success()
    }
}
