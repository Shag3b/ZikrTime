package com.shag3b.zikrtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shag3b.zikrtime.utils.PrayerTimeHelper
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Schedule reset alarms for prayer times
            scheduleResetAlarms(context)

            // Schedule WorkManager fallback
            com.shag3b.zikrtime.worker.WorkManagerScheduler.scheduleFallbackChecks(context)
        }
    }

    companion object {
        /**
         * Schedule reset alarms based on prayer times
         */
        fun scheduleResetAlarms(context: Context) {
            val prayerTimes = PrayerTimeHelper.getTodayPrayerTimes(context)

            // Get prayer times in milliseconds
            val fajrTime = prayerTimes.fajr.time
            val asrTime = prayerTimes.asr.time

            // Calculate period start times
            val morningStartTime = fajrTime + (10 * 60 * 1000) // Fajr + 10 minutes
            val eveningStartTime = asrTime + (20 * 60 * 1000) // Asr + 20 minutes

            // Calculate midnight (23:59:59 today)
            val midnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Schedule morning resets: FALSE at Fajr, TRUE at Asr
            ReminderScheduler.scheduleMorningResets(context, fajrTime, asrTime)

            // Schedule evening resets: FALSE at Asr, TRUE at Midnight
            ReminderScheduler.scheduleEveningResets(context, asrTime, midnight)

            // Schedule INITIAL notifications at period start times
            ReminderScheduler.scheduleInitialNotification(context, morningStartTime, true) // Morning
            ReminderScheduler.scheduleInitialNotification(context, eveningStartTime, false) // Evening
        }
    }
}
