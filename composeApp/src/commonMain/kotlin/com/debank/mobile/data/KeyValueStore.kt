package com.debank.mobile.data

interface KeyValueStore {
    fun getString(key: String): String?
    fun setString(key: String, value: String)
    fun contains(key: String): Boolean
    fun remove(key: String)

    companion object {
        const val PIN_HASH_KEY = "pin_hash"
        const val PUBLIC_KEY_KEY = "public_key"
        const val SECRET_SEED_KEY = "secret_seed"
    }
}
