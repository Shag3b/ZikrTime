package com.shag3b.zikrtime

import android.content.Context

object FirstLaunchManager {

    private const val PREFS = "zikr_prefs"
    private const val KEY_LANG_SELECTED = "lang_selected"
    private const val KEY_INITIALIZED = "app_initialized"

    fun isLanguageSelected(context: Context): Boolean {
        return context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_LANG_SELECTED, false)
    }

    fun setLanguageSelected(context: Context) {
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LANG_SELECTED, true)
            .apply()
    }

    /**
     * Check if this is the first time the app is being initialized
     * Used to set initial completion status to avoid first-setup bug
     */
    fun isFirstTimeSetup(context: Context): Boolean {
        return !context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_INITIALIZED, false)
    }

    /**
     * Initialize app on first launch
     * Sets both morning and evening as "completed" initially
     * They will be reset to false at the next prayer time automatically
     */
    fun initializeApp(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Mark both periods as completed initially
        // This prevents showing notifications for both periods on first install
        // The reset logic will set them to false at the appropriate prayer times
        prefs.edit()
            .putBoolean("morningCompleted", true)
            .putBoolean("eveningCompleted", true)
            .putBoolean(KEY_INITIALIZED, true)
            .apply()
    }
}
