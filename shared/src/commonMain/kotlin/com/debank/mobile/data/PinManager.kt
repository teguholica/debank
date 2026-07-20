package com.debank.mobile.data

object PinManager {
    private const val SALT_SIZE = 16
    private const val SEPARATOR = ":"

    fun hash(pin: String): String {
        val salt = CryptoUtils.secureRandomBytes(SALT_SIZE)
        val hash = CryptoUtils.sha256(salt + pin.encodeToByteArray())
        return salt.toHex() + SEPARATOR + hash.toHex()
    }

    fun verify(pin: String, storedValue: String): Boolean {
        val parts = storedValue.split(SEPARATOR)
        if (parts.size != 2) return false
        val salt = parts[0].hexToBytes()
        val expectedHash = parts[1].hexToBytes()
        val actualHash = CryptoUtils.sha256(salt + pin.encodeToByteArray())
        return actualHash.contentEquals(expectedHash)
    }
}
