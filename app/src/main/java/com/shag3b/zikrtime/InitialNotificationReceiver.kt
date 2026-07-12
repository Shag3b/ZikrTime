package com.shag3b.zikrtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.shag3b.zikrtime.notification.ZikrNotificationHelper

/**
 * Receives the initial notification trigger at the exact start of zikr period
 * Sends high-priority heads-up notification and starts reminder scheduling
 */
class InitialNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val isMorning = intent.getBooleanExtra("isMorning", true)
        val periodName = if (isMorning) "Morning" else "Evening"

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val now = System.currentTimeMillis()

        Log.d("InitialNotificationReceiver", "═══════════════════════════════════════")
        Log.d("InitialNotificationReceiver", "🔔 $periodName zikr period TRIGGERED!")
        Log.d("InitialNotificationReceiver", "Time: ${dateFormat.format(now)}")
        Log.d("InitialNotificationReceiver", "isMorning flag: $isMorning")

        // CRITICAL: Reschedule for tomorrow (since we can't use setRepeating with Doze-safe alarms)
        val prayerTimes = com.shag3b.zikrtime.utils.PrayerTimeHelper.getTodayPrayerTimes(context)
        val tomorrowTriggerTime = if (isMorning) {
            prayerTimes.fajr.time + (10 * 60 * 1000) + (24 * 60 * 60 * 1000) // Fajr+10 tomorrow
        } else {
            prayerTimes.asr.time + (20 * 60 * 1000) + (24 * 60 * 60 * 1000) // Asr+20 tomorrow
        }
        ReminderScheduler.scheduleInitialNotification(context, tomorrowTriggerTime, isMorning)
        Log.d("InitialNotificationReceiver", "✅ Rescheduled $periodName for tomorrow at ${dateFormat.format(tomorrowTriggerTime)}")

        // Check if already completed today
        val prefs = context.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
        val isCompleted = if (isMorning) {
            prefs.getBoolean("morningCompleted", false)
        } else {
            prefs.getBoolean("eveningCompleted", false)
        }

        Log.d("InitialNotificationReceiver", "$periodName completion status: $isCompleted")

        if (isCompleted) {
            Log.d("InitialNotificationReceiver", "⏭️ $periodName zikr already completed, skipping notification")
            Log.d("InitialNotificationReceiver", "═══════════════════════════════════════")
            return
        }

        // Send HIGH PRIORITY heads-up notification
        Log.d("InitialNotificationReceiver", "📢 Sending high-priority heads-up notification for $periodName")
        ZikrNotificationHelper.showInitialPeriodNotification(context, isMorning)
        Log.d("InitialNotificationReceiver", "✅ High-priority notification sent for $periodName")

        // Start reminder scheduling (every 8 minutes)
        if (isMorning) {
            ReminderScheduler.scheduleMorningReminders(context)
            Log.d("InitialNotificationReceiver", "⏰ Morning reminders scheduled (every 8 minutes)")
        } else {
            ReminderScheduler.scheduleEveningReminders(context)
            Log.d("InitialNotificationReceiver", "⏰ Evening reminders scheduled (every 8 minutes)")
        }

        Log.d("InitialNotificationReceiver", "═══════════════════════════════════════")
    }
}

