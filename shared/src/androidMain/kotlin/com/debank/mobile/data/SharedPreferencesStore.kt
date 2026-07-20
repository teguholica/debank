package com.debank.mobile.data

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesStore(context: Context) : KeyValueStore {
    private val prefs: SharedPreferences = context.getSharedPreferences("debank", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun contains(key: String): Boolean = prefs.contains(key)

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
