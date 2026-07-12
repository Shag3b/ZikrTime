package com.shaghb.zikrtime.utils

import android.content.Context
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar

object PrayerTimeHelper {

    /**
     * Get today's prayer times based on device location
     * Falls back to saved location or default (Cairo) if location is unavailable
     */
    fun getTodayPrayerTimes(context: Context): PrayerTimes {

        // Get location (saved or default)
        val (latitude, longitude) = LocationManager.getLocationSync(context)

        val coordinates = Coordinates(latitude, longitude)

        // Use Egyptian calculation method (can be made configurable)
        val params = CalculationMethod.EGYPTIAN.parameters
        params.madhab = Madhab.SHAFI

        val calendar = Calendar.getInstance()
        val date = DateComponents.from(calendar.time)

        return PrayerTimes(coordinates, date, params)
    }
}
