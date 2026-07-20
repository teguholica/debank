package com.debank.mobile.data

import platform.Foundation.NSUserDefaults

class UserDefaultsStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun setString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun contains(key: String): Boolean = defaults.objectForKey(key) != null

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
