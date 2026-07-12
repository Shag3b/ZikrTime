package com.shaghb.zikrtime

import android.content.Context
import com.shaghb.zikrtime.utils.PrayerTimeHelper
import java.util.Calendar

object TimeChecker {

    /**
     * Check if we're in the morning period (regardless of completion status)
     * Used for navigation to determine which screen to show
     */
    fun isInMorningPeriod(context: Context): Boolean {
        val prayerTimes = PrayerTimeHelper.getTodayPrayerTimes(context)
        val now = Calendar.getInstance()

        val fajr = Calendar.getInstance().apply {
            time = prayerTimes.fajr
            add(Calendar.MINUTE, 10)
        }

        val asr = Calendar.getInstance().apply {
            time = prayerTimes.asr  // Morning ends at Asr, not Dhuhr
        }

        return now.after(fajr) && now.before(asr)
    }

    /**
     * Check if we're in the evening period (regardless of completion status)
     * Used for navigation to determine which screen to show
     */
    fun isInEveningPeriod(context: Context): Boolean {
        val prayerTimes = PrayerTimeHelper.getTodayPrayerTimes(context)
        val now = Calendar.getInstance()

        val asr = Calendar.getInstance().apply {
            time = prayerTimes.asr
            add(Calendar.MINUTE, 20)
        }

        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }


        return now.after(asr) && now.before(midnight)
    }

    /**
     * Check if morning azkar should be opened (time period + not completed)
     * Used for auto-open logic and notifications
     */
    fun shouldOpenMorning(context: Context): Boolean {
        return isInMorningPeriod(context) && !DailyStateManager.isMorningDone(context)
    }

    /**
     * Check if evening azkar should be opened (time period + not completed)
     * Used for auto-open logic and notifications
     */
    fun shouldOpenEvening(context: Context): Boolean {
        return isInEveningPeriod(context) && !DailyStateManager.isEveningDone(context)
    }
}
