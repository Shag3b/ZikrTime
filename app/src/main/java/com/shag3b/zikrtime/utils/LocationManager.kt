package com.shag3b.zikrtime.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationManager {

    private const val PREF_NAME = "zikr_prefs"
    private const val KEY_LATITUDE = "saved_latitude"
    private const val KEY_LONGITUDE = "saved_longitude"

    // Default location (Cairo, Egypt) used if location is not available
    private const val DEFAULT_LATITUDE = 30.0444
    private const val DEFAULT_LONGITUDE = 31.2357

    /**
     * Get current location or return saved/default location
     */
    suspend fun getLocation(context: Context): Pair<Double, Double> {

        // Check if we have location permission
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val fusedLocationClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)

                val cancellationToken = CancellationTokenSource()

                val location: Location? = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationToken.token
                ).await()

                if (location != null) {
                    // Save the location for future use
                    saveLocation(context, location.latitude, location.longitude)
                    return Pair(location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                // Failed to get location - will use saved location if available
            }
        }

        // If we couldn't get location, try to return saved location
        return getSavedLocation(context)
    }

    /**
     * Get location synchronously (returns saved or default)
     */
    fun getLocationSync(context: Context): Pair<Double, Double> {
        return getSavedLocation(context)
    }

    /**
     * Save location to SharedPreferences
     */
    private fun saveLocation(context: Context, latitude: Double, longitude: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LATITUDE, latitude.toString())
            .putString(KEY_LONGITUDE, longitude.toString())
            .apply()
    }

    /**
     * Get saved location or default location
     */
    private fun getSavedLocation(context: Context): Pair<Double, Double> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val latitude = prefs.getString(KEY_LATITUDE, DEFAULT_LATITUDE.toString())?.toDoubleOrNull() ?: DEFAULT_LATITUDE
        val longitude = prefs.getString(KEY_LONGITUDE, DEFAULT_LONGITUDE.toString())?.toDoubleOrNull() ?: DEFAULT_LONGITUDE
        return Pair(latitude, longitude)
    }

    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

