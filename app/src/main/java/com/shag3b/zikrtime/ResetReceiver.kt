package com.shag3b.zikrtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shag3b.zikrtime.notification.ZikrNotificationHelper

/**
 * Handles resetting zikr completion status at prayer times
 *
 * Morning zikr:
 * - Reset to FALSE at Fajr (period starts)
 * - Reset to TRUE at Asr (period ends)
 *
 * Evening zikr:
 * - Reset to FALSE at Asr (period starts)
 * - Reset to TRUE at Midnight (period ends)
 */
class ResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val period = intent.getStringExtra("period") ?: return
        val setValue = intent.getBooleanExtra("setValue", false)

        val prefs = context.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)

        // FIRST-TIME SETUP PROTECTION
        val currentTime = System.currentTimeMillis()
        val lastSetupTime = prefs.getLong("first_setup_time", 0)
        val timeSinceSetup = currentTime - lastSetupTime

        val morningCompleted = prefs.getBoolean("morningCompleted", false)
        val eveningCompleted = prefs.getBoolean("eveningCompleted", false)

        if (morningCompleted && eveningCompleted && !setValue && timeSinceSetup < 10000) {
            return
        }

        when (period) {
            "morning" -> {
                if (setValue) {
                    val wasCompleted = prefs.getBoolean("morningCompleted", false)
                    if (!wasCompleted) {
                        StatsManager.onMorningForgotten(context)
                    }
                }

                prefs.edit().putBoolean("morningCompleted", setValue).apply()

                if (setValue) {
                    ReminderScheduler.cancelMorningReminders(context)
                    ZikrNotificationHelper.cancelAllNotifications(context)
                } else {
                    prefs.edit().putBoolean("worker_fired_morning_today", false).apply()
                }
            }

            "evening" -> {
                if (setValue) {
                    val wasCompleted = prefs.getBoolean("eveningCompleted", false)
                    if (!wasCompleted) {
                        StatsManager.onEveningForgotten(context)
                    }
                }

                prefs.edit().putBoolean("eveningCompleted", setValue).apply()

                if (setValue) {
                    ReminderScheduler.cancelEveningReminders(context)
                    ZikrNotificationHelper.cancelAllNotifications(context)
                } else {
                    prefs.edit().putBoolean("worker_fired_evening_today", false).apply()
                }
            }
        }
    }
}
