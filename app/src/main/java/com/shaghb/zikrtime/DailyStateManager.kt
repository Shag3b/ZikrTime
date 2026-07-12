package com.shaghb.zikrtime

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailyStateManager {

    private const val PREF_NAME = "zikr_prefs"

    private fun todayKey(): String {
        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return format.format(Date())
    }

    fun markMorningDone(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("morning_${todayKey()}", true)
            .putBoolean("morningCompleted", true)  // Also update legacy flag for consistency
            .apply()
    }

    fun isMorningDone(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("morning_${todayKey()}", false)
    }

    fun markEveningDone(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("evening_${todayKey()}", true)
            .putBoolean("eveningCompleted", true)  // Also update legacy flag for consistency
            .apply()
    }

    fun isEveningDone(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("evening_${todayKey()}", false)
    }
}
