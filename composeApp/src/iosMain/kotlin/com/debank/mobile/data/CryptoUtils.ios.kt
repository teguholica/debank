package com.debank.mobile.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import platform.Security.SecRandomCopyBytes

@OptIn(ExperimentalForeignApi::class)
actual object PlatformSecureRandom {
    actual fun bytes(length: Int): ByteArray = memScoped {
        val bytes = allocArray<UByteVar>(length)
        SecRandomCopyBytes(null, length.toULong(), bytes)
        bytes.readBytes(length)
    }
}
