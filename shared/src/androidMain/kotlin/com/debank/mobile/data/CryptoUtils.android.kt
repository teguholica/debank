package com.debank.mobile.data

import java.security.SecureRandom

actual object PlatformSecureRandom {
    actual fun bytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}
