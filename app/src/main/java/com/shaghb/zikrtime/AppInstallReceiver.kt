package com.shaghb.zikrtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives broadcast when app is installed or updated
 * This ensures alarms are scheduled even if user never opens the app after install
 *
 * CRITICAL: This receiver enables fully automatic notifications without user action
 */
class AppInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                initializeApp(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                initializeApp(context)
            }
        }
    }

    companion object {
        /**
         * Initialize the app's background services without user action
         * This is called on:
         * - Device boot (ACTION_BOOT_COMPLETED)
         * - App install/update (ACTION_MY_PACKAGE_REPLACED)
         */
        fun initializeApp(context: Context) {
            val prefs = context.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)

            // FIRST-TIME SETUP: If both periods are FALSE, it's a fresh install
            val morningCompleted = prefs.getBoolean("morningCompleted", false)
            val eveningCompleted = prefs.getBoolean("eveningCompleted", false)

            if (!morningCompleted && !eveningCompleted) {
                prefs.edit()
                    .putBoolean("morningCompleted", true)
                    .putBoolean("eveningCompleted", true)
                    .putLong("first_setup_time", System.currentTimeMillis())
                    .apply()
            }

            // Schedule ALL alarms (resets + notifications)
            BootReceiver.scheduleResetAlarms(context)
        }
    }
}
