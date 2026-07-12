package com.shaghb.zikrtime

import android.content.Context

object LanguageManager {

    private const val PREFS_NAME = "zikr_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun setLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, langCode)
            .commit()
    }

    fun getCurrentLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "ar") ?: "ar"
    }

    fun isArabic(context: Context): Boolean {
        return getCurrentLanguage(context) == "ar"
    }

    fun toggleLanguage(context: Context) {
        val currentLang = getCurrentLanguage(context)
        val newLang = if (currentLang == "ar") "en" else "ar"
        setLanguage(context, newLang)
    }
}
