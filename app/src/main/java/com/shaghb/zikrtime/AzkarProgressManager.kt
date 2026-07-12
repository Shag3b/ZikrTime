package com.shaghb.zikrtime

import android.content.Context

object AzkarProgressManager {

    private const val PREFS_NAME = "zikr_prefs"

    // Keys for saving progress
    private const val KEY_MORNING_INDEX = "morning_current_index"
    private const val KEY_MORNING_REPEAT = "morning_repeat_count"
    private const val KEY_EVENING_INDEX = "evening_current_index"
    private const val KEY_EVENING_REPEAT = "evening_repeat_count"

    fun saveMorningProgress(context: Context, currentIndex: Int, repeatCount: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_MORNING_INDEX, currentIndex)
            .putInt(KEY_MORNING_REPEAT, repeatCount)
            .apply()
    }

    fun saveEveningProgress(context: Context, currentIndex: Int, repeatCount: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_EVENING_INDEX, currentIndex)
            .putInt(KEY_EVENING_REPEAT, repeatCount)
            .apply()
    }

    fun getMorningProgress(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_MORNING_INDEX, 0)
        val repeat = prefs.getInt(KEY_MORNING_REPEAT, 1)
        return Pair(index, repeat)
    }

    fun getEveningProgress(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_EVENING_INDEX, 0)
        val repeat = prefs.getInt(KEY_EVENING_REPEAT, 1)
        return Pair(index, repeat)
    }

    fun clearMorningProgress(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_MORNING_INDEX)
            .remove(KEY_MORNING_REPEAT)
            .apply()
    }

    fun clearEveningProgress(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_EVENING_INDEX)
            .remove(KEY_EVENING_REPEAT)
            .apply()
    }
}

