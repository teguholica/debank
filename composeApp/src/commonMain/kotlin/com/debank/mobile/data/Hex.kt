package com.debank.mobile.data

internal fun ByteArray.toHex(): String = joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }

internal fun String.hexToBytes(): ByteArray {
    val len = length / 2
    return ByteArray(len) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
