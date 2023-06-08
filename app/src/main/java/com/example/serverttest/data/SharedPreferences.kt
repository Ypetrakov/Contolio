package com.example.serverttest.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object SharedPreferencesUtil {
    private const val PREFERENCES_NAME = "MyPrefs"
    private const val MAP_KEY = "shortcuts"

    fun saveMap(context: Context, map: Map<String, String>) {
        val json = Gson().toJson(map)
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(MAP_KEY, json)
        editor.apply()
    }

    fun getMap(context: Context): Map<String, String> {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(MAP_KEY, null)
        return if (json != null) {
            Gson().fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
        } else {
            getDefaultMap()
        }
    }

    private fun getDefaultMap(): Map<String, String> {
        return mapOf(
            "ctrl+c" to "Copy",
            "ctrl+v" to "Paste",
            "win+d" to "Go to home screen"
        )
    }
}