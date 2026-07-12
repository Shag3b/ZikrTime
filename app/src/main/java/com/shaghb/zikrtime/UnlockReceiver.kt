package com.shaghb.zikrtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shaghb.zikrtime.notification.ZikrNotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_USER_PRESENT) {

            val shouldOpenMorning = TimeChecker.shouldOpenMorning(context)
            val shouldOpenEvening = TimeChecker.shouldOpenEvening(context)

            if (shouldOpenMorning || shouldOpenEvening) {
                val isMorning = when {
                    TimeChecker.isInEveningPeriod(context) && !DailyStateManager.isEveningDone(context) -> false
                    TimeChecker.isInMorningPeriod(context) && !DailyStateManager.isMorningDone(context) -> true
                    shouldOpenMorning -> true
                    else -> false
                }

                val prefs = context.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

                val initialNotifKey = if (isMorning) "initial_notif_morning" else "initial_notif_evening"
                val lastInitialNotif = prefs.getString(initialNotifKey, "")

                // Show initial heads-up notification ONCE per period per day
                if (lastInitialNotif != today) {
                    ZikrNotificationHelper.showInitialPeriodNotification(context, isMorning)
                    prefs.edit().putString(initialNotifKey, today).apply()

                    // Start reminder scheduler
                    val scheduledKey = if (isMorning) "scheduled_morning" else "scheduled_evening"
                    val lastScheduled = prefs.getString(scheduledKey, "")

                    if (lastScheduled != today) {
                        if (isMorning) {
                            ReminderScheduler.scheduleMorningReminders(context)
                        } else {
                            ReminderScheduler.scheduleEveningReminders(context)
                        }
                        prefs.edit().putString(scheduledKey, today).apply()
                    }
                } else {
                    ZikrNotificationHelper.showOngoingNotification(context, isMorning)
                }
            }
        }
    }
}
