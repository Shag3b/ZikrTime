package com.shaghb.zikrtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shaghb.zikrtime.notification.ZikrNotificationHelper

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val period = intent?.getStringExtra("period") ?: return

        // Check if azkar is still pending
        val isPending = when (period) {
            "morning" -> TimeChecker.shouldOpenMorning(context)
            "evening" -> TimeChecker.shouldOpenEvening(context)
            else -> false
        }

        if (!isPending) {
            // Stop reminders if azkar is completed
            if (period == "morning") {
                ReminderScheduler.cancelMorningReminders(context)
            } else {
                ReminderScheduler.cancelEveningReminders(context)
            }
            return
        }

        // Show reminder notification
        val isMorning = period == "morning"
        ZikrNotificationHelper.showReminderNotification(context, isMorning)
    }
}
