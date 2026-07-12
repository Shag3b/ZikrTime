package com.shaghb.zikrtime.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Manages WorkManager fallback tasks
 * Schedules periodic checks to ensure notifications fire even if AlarmManager fails
 */
object WorkManagerScheduler {

    private const val FALLBACK_WORK_NAME = "notification_fallback_work"
    private const val CHECK_INTERVAL_MINUTES = 60L // Check every 1 hour

    /**
     * Schedule periodic WorkManager checks as fallback for AlarmManager
     * This ensures notifications fire even during deep Doze mode
     */
    fun scheduleFallbackChecks(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NotificationFallbackWorker>(
            CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("notification_fallback")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FALLBACK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancel all WorkManager fallback tasks
     */
    fun cancelFallbackChecks(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(FALLBACK_WORK_NAME)
    }

    /**
     * Check if WorkManager fallback is currently scheduled
     */
    fun isFallbackScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(FALLBACK_WORK_NAME)
            .get()

        return workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }

    /**
     * Trigger an immediate fallback check (useful for testing)
     */
    fun triggerImmediateCheck(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationFallbackWorker>()
            .addTag("notification_fallback_immediate")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
