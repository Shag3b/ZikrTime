package com.shag3b.zikrtime.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.shag3b.zikrtime.AzkarProgressManager
import com.shag3b.zikrtime.MainActivity
import com.shag3b.zikrtime.R
import com.shag3b.zikrtime.data.ZikrRepository

object ZikrNotificationHelper {

    // Two separate channels for different notification types
    private const val CHANNEL_URGENT = "zikr_urgent_v2"  // Changed ID to force new channel creation
    private const val CHANNEL_ONGOING = "zikr_ongoing_v2"  // Changed ID to force new channel creation

    // Notification IDs
    private const val NOTIFICATION_ID_ONGOING = 1001
    private const val NOTIFICATION_ID_INITIAL_MORNING = 1100
    private const val NOTIFICATION_ID_INITIAL_EVENING = 1200
    private const val NOTIFICATION_ID_REMINDER_MORNING = 2001
    private const val NOTIFICATION_ID_REMINDER_EVENING = 2002

    /**
     * Check if the app has notification permission
     * Required for Android 13+ (API 33+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, check if notifications are enabled
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Get dynamic notification content based on user progress
     */
    private fun getNotificationContent(context: Context, isMorning: Boolean): NotificationContent {
        val (currentIndex, _) = if (isMorning) {
            AzkarProgressManager.getMorningProgress(context)
        } else {
            AzkarProgressManager.getEveningProgress(context)
        }

        val totalAzkar = if (isMorning) {
            ZikrRepository.loadMorningAzkar(context).size
        } else {
            ZikrRepository.loadEveningAzkar(context).size
        }

        val periodName = if (isMorning) {
            context.getString(R.string.morning_azkar_short)
        } else {
            context.getString(R.string.evening_azkar_short)
        }

        val completedCount = currentIndex
        val remainingCount = totalAzkar - currentIndex

        return when {
            completedCount == 0 -> {
                // Not started yet
                NotificationContent(
                    title = context.getString(R.string.notification_title_not_started, periodName),
                    text = context.getString(R.string.notification_text_not_started),
                    bigText = context.getString(R.string.notification_big_text_not_started, periodName, totalAzkar),
                    progress = Pair(totalAzkar, 0)
                )
            }
            remainingCount > 0 -> {
                // Partially completed
                NotificationContent(
                    title = context.getString(R.string.notification_title_progress, periodName, completedCount, totalAzkar),
                    text = context.getString(R.string.notification_text_remaining, remainingCount),
                    bigText = context.getString(R.string.notification_big_text_continue, completedCount, totalAzkar, remainingCount, periodName),
                    progress = Pair(totalAzkar, completedCount)
                )
            }
            else -> {
                // Just started or default
                NotificationContent(
                    title = context.getString(R.string.notification_title_default, periodName),
                    text = context.getString(R.string.notification_time_to_read),
                    bigText = context.getString(R.string.notification_big_text_default, periodName),
                    progress = Pair(totalAzkar, completedCount)
                )
            }
        }
    }

    /**
     * Data class to hold notification content
     */
    private data class NotificationContent(
        val title: String,
        val text: String,
        val bigText: String,
        val progress: Pair<Int, Int> // (total, current)
    )

    /**
     * Creates both notification channels with proper configuration
     */
    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Channel 1: URGENT - For initial period notification and reminders
        val urgentChannel = NotificationChannel(
            CHANNEL_URGENT,
            "Zikr Reminders",
            NotificationManager.IMPORTANCE_HIGH  // HIGH = Shows heads-up popup
        )
        urgentChannel.description = "Important reminders for Azkar time"
        urgentChannel.setShowBadge(true)
        urgentChannel.enableLights(true)
        urgentChannel.lightColor = android.graphics.Color.GREEN

        // Enable vibration with gentle pattern
        urgentChannel.enableVibration(true)
        urgentChannel.vibrationPattern = longArrayOf(0, 200, 100, 200)  // Short gentle vibration

        // Use default notification sound (short and non-aggressive)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        urgentChannel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)

        manager.createNotificationChannel(urgentChannel)

        // Channel 2: ONGOING - For persistent incomplete status
        val ongoingChannel = NotificationChannel(
            CHANNEL_ONGOING,
            "Azkar Status",
            NotificationManager.IMPORTANCE_LOW  // LOW = No popup, just status bar
        )
        ongoingChannel.description = "Shows when Azkar is incomplete"
        ongoingChannel.setShowBadge(true)
        ongoingChannel.setSound(null, null)  // Silent
        ongoingChannel.enableVibration(false)
        ongoingChannel.enableLights(false)

        manager.createNotificationChannel(ongoingChannel)
    }

    /**
     * Show initial heads-up notification when zikr period starts
     * This appears as a popup and opens directly to the correct zikr screen
     * Now with dynamic content based on user progress
     */
    fun showInitialPeriodNotification(context: Context, isMorning: Boolean) {
        // Check permission first
        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            if (isMorning) 100 else 200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get dynamic content
        val content = getNotificationContent(context, isMorning)

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_stat_misbaha)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.bigText))
            .setProgress(content.progress.first, content.progress.second, false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = if (isMorning) NOTIFICATION_ID_INITIAL_MORNING else NOTIFICATION_ID_INITIAL_EVENING
        manager.notify(notificationId, notification)

        // Track notification time for WorkManager fallback
        val prefs = context.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
        val timestampKey = if (isMorning) "last_morning_notification" else "last_evening_notification"
        prefs.edit().putLong(timestampKey, System.currentTimeMillis()).apply()
    }

    /**
     * Show ongoing persistent notification (cannot be dismissed)
     * Used when user closes app without completing zikr
     * Now with dynamic content showing actual progress
     */
    fun showOngoingNotification(context: Context, isMorning: Boolean) {
        // Check permission first
        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get dynamic content
        val content = getNotificationContent(context, isMorning)

        val notification = NotificationCompat.Builder(context, CHANNEL_ONGOING)
            .setSmallIcon(R.drawable.ic_stat_misbaha)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.bigText))
            .setProgress(content.progress.first, content.progress.second, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_ONGOING, notification)
    }

    /**
     * Show reminder notification (every 15 minutes)
     * Appears as heads-up popup to remind user
     * Now with dynamic content based on progress
     */
    fun showReminderNotification(context: Context, isMorning: Boolean) {
        // Check permission first
        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            if (isMorning) 300 else 400,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get dynamic content
        val content = getNotificationContent(context, isMorning)

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT)
            .setSmallIcon(R.drawable.ic_stat_misbaha)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.bigText)) // BigTextStyle for expanded view
            .setProgress(content.progress.first, content.progress.second, false) // Show progress
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = if (isMorning) NOTIFICATION_ID_REMINDER_MORNING else NOTIFICATION_ID_REMINDER_EVENING
        manager.notify(notificationId, notification)
    }

    /**
     * Cancel all notifications related to zikr
     */
    fun cancelAllNotifications(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cancel all possible notification IDs
        manager.cancel(NOTIFICATION_ID_ONGOING)
        manager.cancel(NOTIFICATION_ID_INITIAL_MORNING)
        manager.cancel(NOTIFICATION_ID_INITIAL_EVENING)
        manager.cancel(NOTIFICATION_ID_REMINDER_MORNING)
        manager.cancel(NOTIFICATION_ID_REMINDER_EVENING)
    }
}
