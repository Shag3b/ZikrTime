package com.shag3b.zikrtime.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.shag3b.zikrtime.R

/**
 * Centralized manager for notification permissions
 * Handles permission checking, requesting, and settings navigation
 */
object NotificationPermissionManager {

    private const val PREFS_NAME = "notification_permission_prefs"
    private const val KEY_PERMISSION_REQUESTED = "permission_requested"
    private const val KEY_PERMISSION_PERMANENTLY_DENIED = "permission_permanently_denied"

    /**
     * Check if notification permission is granted
     * Works for all Android versions
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Check if notifications are enabled (system-wide check)
     * This is useful for detecting if user disabled notifications in system settings
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Request notification permission (Android 13+)
     * For older versions, this does nothing as permission is automatic
     */
    fun requestNotificationPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Mark that we've requested permission
            setPermissionRequested(activity)

            // Launch permission request
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Check if permission was permanently denied
     * This happens when user clicks "Don't ask again" and denies
     */
    fun isPermissionPermanentlyDenied(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val wasRequested = wasPermissionRequested(activity)
            val hasPermission = hasNotificationPermission(activity)
            val shouldShow = activity.shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS
            )

            // Permanently denied if:
            // - Permission was requested before
            // - Permission is not granted
            // - System doesn't show rationale (user clicked "Don't ask again")
            return wasRequested && !hasPermission && !shouldShow
        }

        // For older Android versions, check if notifications are disabled in settings
        return !areNotificationsEnabled(activity)
    }

    /**
     * Open app notification settings
     * Allows user to manually enable notifications
     */
    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ (API 26+)
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            // Older versions
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Check if this is the first time we're checking permission
     */
    fun isFirstPermissionCheck(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_PERMISSION_REQUESTED, false)
    }

    /**
     * Mark that permission was requested
     */
    private fun setPermissionRequested(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERMISSION_REQUESTED, true)
            .apply()
    }

    /**
     * Check if permission was requested before
     */
    private fun wasPermissionRequested(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PERMISSION_REQUESTED, false)
    }

    /**
     * Reset permission tracking (for testing purposes)
     */
    fun resetPermissionTracking(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    /**
     * Get a user-friendly message explaining why notifications are needed
     */
    fun getPermissionRationaleMessage(context: Context): String {
        return context.getString(R.string.permission_rationale_message)
    }

    /**
     * Get message for when permission is permanently denied
     */
    fun getPermanentlyDeniedMessage(context: Context): String {
        return context.getString(R.string.permission_permanently_denied_message)
    }
}


