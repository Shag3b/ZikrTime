package com.shag3b.zikrtime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

object ReminderScheduler {

    private const val REMINDER_INTERVAL = 15 * 60 * 1000L // 15 minutes in milliseconds
    private const val MORNING_REQUEST_CODE = 1001
    private const val EVENING_REQUEST_CODE = 1002

    // Reset alarm request codes
    private const val MORNING_RESET_START_CODE = 2001  // Reset to false at Fajr
    private const val MORNING_RESET_END_CODE = 2002    // Reset to true at Asr
    private const val EVENING_RESET_START_CODE = 2003  // Reset to false at Asr
    private const val EVENING_RESET_END_CODE = 2004    // Reset to true at Midnight

    // Initial notification request codes
    private const val MORNING_INITIAL_NOTIF_CODE = 3001  // Initial notification at Fajr+10
    private const val EVENING_INITIAL_NOTIF_CODE = 3002  // Initial notification at Asr+20

    fun scheduleMorningReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("period", "morning")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            MORNING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use setRepeating for periodic reminders (battery-efficient, Play Store safe)
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + REMINDER_INTERVAL,
            REMINDER_INTERVAL,
            pendingIntent
        )
    }

    fun scheduleEveningReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("period", "evening")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            EVENING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use setRepeating for periodic reminders (battery-efficient, Play Store safe)
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + REMINDER_INTERVAL,
            REMINDER_INTERVAL,
            pendingIntent
        )
    }

    fun cancelMorningReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            MORNING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelEveningReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            EVENING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllReminders(context: Context) {
        cancelMorningReminders(context)
        cancelEveningReminders(context)
    }

    // ========== RESET SCHEDULING ==========

    /**
     * Schedule morning zikr reset alarms:
     * - Reset to FALSE at Fajr time (period starts)
     * - Reset to TRUE at Asr time (period ends)
     *
     * CRITICAL FIX: If times have already passed today, schedule for tomorrow first
     */
    fun scheduleMorningResets(context: Context, fajrTimeInMillis: Long, asrTimeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        // Reset to FALSE at Fajr (morning period starts)
        val resetStartIntent = Intent(context, ResetReceiver::class.java).apply {
            putExtra("period", "morning")
            putExtra("setValue", false)
        }
        val resetStartPendingIntent = PendingIntent.getBroadcast(
            context,
            MORNING_RESET_START_CODE,
            resetStartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reset to TRUE at Asr (morning period ends)
        val resetEndIntent = Intent(context, ResetReceiver::class.java).apply {
            putExtra("period", "morning")
            putExtra("setValue", true)
        }
        val resetEndPendingIntent = PendingIntent.getBroadcast(
            context,
            MORNING_RESET_END_CODE,
            resetEndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // If Fajr time has passed today, schedule for tomorrow
        val fajrAlarmTime = if (fajrTimeInMillis > now) fajrTimeInMillis else fajrTimeInMillis + AlarmManager.INTERVAL_DAY

        // If Asr time has passed today, schedule for tomorrow
        val asrAlarmTime = if (asrTimeInMillis > now) asrTimeInMillis else asrTimeInMillis + AlarmManager.INTERVAL_DAY

        // Schedule daily repeating alarms
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            fajrAlarmTime,
            AlarmManager.INTERVAL_DAY,
            resetStartPendingIntent
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            asrAlarmTime,
            AlarmManager.INTERVAL_DAY,
            resetEndPendingIntent
        )
    }

    /**
     * Schedule evening zikr reset alarms:
     * - Reset to FALSE at Asr time (period starts)
     * - Reset to TRUE at Midnight (period ends)
     *
     * CRITICAL FIX: If times have already passed today, schedule for tomorrow first
     */
    fun scheduleEveningResets(context: Context, asrTimeInMillis: Long, midnightTimeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        // Reset to FALSE at Asr (evening period starts)
        val resetStartIntent = Intent(context, ResetReceiver::class.java).apply {
            putExtra("period", "evening")
            putExtra("setValue", false)
        }
        val resetStartPendingIntent = PendingIntent.getBroadcast(
            context,
            EVENING_RESET_START_CODE,
            resetStartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reset to TRUE at Midnight (evening period ends)
        val resetEndIntent = Intent(context, ResetReceiver::class.java).apply {
            putExtra("period", "evening")
            putExtra("setValue", true)
        }
        val resetEndPendingIntent = PendingIntent.getBroadcast(
            context,
            EVENING_RESET_END_CODE,
            resetEndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // If Asr time has passed today, schedule for tomorrow
        val asrAlarmTime = if (asrTimeInMillis > now) asrTimeInMillis else asrTimeInMillis + AlarmManager.INTERVAL_DAY

        // If Midnight has passed, schedule for tomorrow (should rarely happen unless installing at 11:59 PM)
        val midnightAlarmTime = if (midnightTimeInMillis > now) midnightTimeInMillis else midnightTimeInMillis + AlarmManager.INTERVAL_DAY

        // Schedule daily repeating alarms
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            asrAlarmTime,
            AlarmManager.INTERVAL_DAY,
            resetStartPendingIntent
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            midnightAlarmTime,
            AlarmManager.INTERVAL_DAY,
            resetEndPendingIntent
        )
    }

    /**
     * Cancel all morning reset alarms
     */
    fun cancelMorningResets(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val resetStartIntent = Intent(context, ResetReceiver::class.java)
        val resetStartPendingIntent = PendingIntent.getBroadcast(
            context,
            MORNING_RESET_START_CODE,
            resetStartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resetEndIntent = Intent(context, ResetReceiver::class.java)
        val resetEndPendingIntent = PendingIntent.getBroadcast(
            context,
            MORNING_RESET_END_CODE,
            resetEndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(resetStartPendingIntent)
        alarmManager.cancel(resetEndPendingIntent)
    }

    /**
     * Cancel all evening reset alarms
     */
    fun cancelEveningResets(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val resetStartIntent = Intent(context, ResetReceiver::class.java)
        val resetStartPendingIntent = PendingIntent.getBroadcast(
            context,
            EVENING_RESET_START_CODE,
            resetStartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resetEndIntent = Intent(context, ResetReceiver::class.java)
        val resetEndPendingIntent = PendingIntent.getBroadcast(
            context,
            EVENING_RESET_END_CODE,
            resetEndIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(resetStartPendingIntent)
        alarmManager.cancel(resetEndPendingIntent)
    }

    /**
     * Cancel all reset alarms
     */
    fun cancelAllResets(context: Context) {
        cancelMorningResets(context)
        cancelEveningResets(context)
    }

    /**
     * Schedule initial notification at the exact start of zikr period
     * This triggers immediately when period starts, without needing user to open app
     *
     * CRITICAL FIX: If the trigger time has already passed TODAY, check if zikr is incomplete.
     * If incomplete, trigger the notification NOW, then schedule for tomorrow.
     */
    fun scheduleInitialNotification(context: Context, triggerTimeMillis: Long, isMorning: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, InitialNotificationReceiver::class.java).apply {
            putExtra("isMorning", isMorning)
        }

        val requestCode = if (isMorning) MORNING_INITIAL_NOTIF_CODE else EVENING_INITIAL_NOTIF_CODE

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel any existing alarm
        alarmManager.cancel(pendingIntent)

        val now = System.currentTimeMillis()

        if (triggerTimeMillis > now) {
            // Future time - use setExactAndAllowWhileIdle for Doze mode compatibility
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Failed to schedule exact alarm - permission may not be granted
            }
        } else {
            // Time has already passed - check if we should trigger NOW
            val prefs = context.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
            val isCompleted = if (isMorning) {
                prefs.getBoolean("morningCompleted", false)
            } else {
                prefs.getBoolean("eveningCompleted", false)
            }

            if (!isCompleted) {
                // Zikr is incomplete and we're IN the period - trigger notification NOW
                context.sendBroadcast(intent)

                // Also schedule for tomorrow using Doze-safe method
                val tomorrowTime = triggerTimeMillis + AlarmManager.INTERVAL_DAY
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        tomorrowTime,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    // Failed to schedule exact alarm
                }
            } else {
                // Already completed - schedule for tomorrow using Doze-safe method
                val tomorrowTime = triggerTimeMillis + AlarmManager.INTERVAL_DAY
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        tomorrowTime,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    // Failed to schedule exact alarm
                }
            }
        }
    }
}

