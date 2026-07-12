package com.shag3b.zikrtime

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object StatsManager {

    private const val PREFS_NAME = "zikr_stats"

    // Morning stats keys
    private const val MORNING_TOTAL = "morning_total_completed"
    private const val MORNING_FORGOTTEN = "morning_days_forgotten"
    private const val MORNING_CURRENT_STREAK = "morning_current_streak"
    private const val MORNING_BEST_STREAK = "morning_best_streak"
    private const val MORNING_LAST_DATE = "morning_last_completed_date"

    // Evening stats keys
    private const val EVENING_TOTAL = "evening_total_completed"
    private const val EVENING_FORGOTTEN = "evening_days_forgotten"
    private const val EVENING_CURRENT_STREAK = "evening_current_streak"
    private const val EVENING_BEST_STREAK = "evening_best_streak"
    private const val EVENING_LAST_DATE = "evening_last_completed_date"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    /**
     * Call when user completes morning zikr
     * Only counts once per day
     */
    fun onMorningCompleted(context: Context) {
        val prefs = getPrefs(context)
        val today = getTodayDate()
        val lastDate = prefs.getString(MORNING_LAST_DATE, "") ?: ""

        // Check if already completed today
        if (lastDate == today) {
            return
        }

        // New day completion!
        val total = prefs.getInt(MORNING_TOTAL, 0) + 1
        val currentStreak = prefs.getInt(MORNING_CURRENT_STREAK, 0) + 1
        val bestStreak = prefs.getInt(MORNING_BEST_STREAK, 0)

        prefs.edit().apply {
            putInt(MORNING_TOTAL, total)
            putInt(MORNING_CURRENT_STREAK, currentStreak)
            putInt(MORNING_BEST_STREAK, maxOf(currentStreak, bestStreak))
            putString(MORNING_LAST_DATE, today)
            apply()
        }
    }

    /**
     * Call when user completes evening zikr
     * Only counts once per day
     */
    fun onEveningCompleted(context: Context) {
        val prefs = getPrefs(context)
        val today = getTodayDate()
        val lastDate = prefs.getString(EVENING_LAST_DATE, "") ?: ""

        // Check if already completed today
        if (lastDate == today) {
            return
        }

        // New day completion!
        val total = prefs.getInt(EVENING_TOTAL, 0) + 1
        val currentStreak = prefs.getInt(EVENING_CURRENT_STREAK, 0) + 1
        val bestStreak = prefs.getInt(EVENING_BEST_STREAK, 0)

        prefs.edit().apply {
            putInt(EVENING_TOTAL, total)
            putInt(EVENING_CURRENT_STREAK, currentStreak)
            putInt(EVENING_BEST_STREAK, maxOf(currentStreak, bestStreak))
            putString(EVENING_LAST_DATE, today)
            apply()
        }
    }

    /**
     * Call when morning period ends without completion
     */
    fun onMorningForgotten(context: Context) {
        val prefs = getPrefs(context)
        val today = getTodayDate()
        val lastDate = prefs.getString(MORNING_LAST_DATE, "") ?: ""

        // Only count as forgotten if not completed today
        if (lastDate == today) {
            return
        }

        val forgotten = prefs.getInt(MORNING_FORGOTTEN, 0) + 1

        prefs.edit().apply {
            putInt(MORNING_FORGOTTEN, forgotten)
            putInt(MORNING_CURRENT_STREAK, 0)
            apply()
        }
    }

    /**
     * Call when evening period ends without completion
     */
    fun onEveningForgotten(context: Context) {
        val prefs = getPrefs(context)
        val today = getTodayDate()
        val lastDate = prefs.getString(EVENING_LAST_DATE, "") ?: ""

        // Only count as forgotten if not completed today
        if (lastDate == today) {
            return
        }

        val forgotten = prefs.getInt(EVENING_FORGOTTEN, 0) + 1

        prefs.edit().apply {
            putInt(EVENING_FORGOTTEN, forgotten)
            putInt(EVENING_CURRENT_STREAK, 0)
            apply()
        }
    }

    /**
     * Get morning zikr statistics
     */
    fun getMorningStats(context: Context): ZikrStats {
        val prefs = getPrefs(context)
        return ZikrStats(
            totalCompleted = prefs.getInt(MORNING_TOTAL, 0),
            daysForgotten = prefs.getInt(MORNING_FORGOTTEN, 0),
            currentStreak = prefs.getInt(MORNING_CURRENT_STREAK, 0),
            bestStreak = prefs.getInt(MORNING_BEST_STREAK, 0)
        )
    }

    /**
     * Get evening zikr statistics
     */
    fun getEveningStats(context: Context): ZikrStats {
        val prefs = getPrefs(context)
        return ZikrStats(
            totalCompleted = prefs.getInt(EVENING_TOTAL, 0),
            daysForgotten = prefs.getInt(EVENING_FORGOTTEN, 0),
            currentStreak = prefs.getInt(EVENING_CURRENT_STREAK, 0),
            bestStreak = prefs.getInt(EVENING_BEST_STREAK, 0)
        )
    }

    /**
     * Get combined statistics for both periods
     */
    fun getCombinedStats(context: Context): CombinedStats {
        val morning = getMorningStats(context)
        val evening = getEveningStats(context)

        return CombinedStats(
            morningStats = morning,
            eveningStats = evening,
            totalCompleted = morning.totalCompleted + evening.totalCompleted,
            totalForgotten = morning.daysForgotten + evening.daysForgotten,
            averageStreak = (morning.currentStreak + evening.currentStreak) / 2
        )
    }

    data class ZikrStats(
        val totalCompleted: Int,
        val daysForgotten: Int,
        val currentStreak: Int,
        val bestStreak: Int
    )

    data class CombinedStats(
        val morningStats: ZikrStats,
        val eveningStats: ZikrStats,
        val totalCompleted: Int,
        val totalForgotten: Int,
        val averageStreak: Int
    )
}

