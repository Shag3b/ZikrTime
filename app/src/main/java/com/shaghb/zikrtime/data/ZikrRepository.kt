package com.shaghb.zikrtime.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ZikrRepository {

    fun loadMorningAzkar(context: Context): List<Zikr> {
        val jsonString = context.assets
            .open("morning.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<Zikr>>() {}.type
        return Gson().fromJson(jsonString, type)
    }
    fun loadEveningAzkar(context: Context): List<Zikr> {
        val jsonString = context.assets
            .open("evening.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<Zikr>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

}
