package com.debank.mobile.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PinManagerTest {

    @Test
    fun `hash returns non-empty string with separator`() {
        val hash = PinManager.hash("123456")
        assertTrue(hash.contains(":"))
        assertTrue(hash.length > 10)
    }

    @Test
    fun `hash produces different values for same PIN each time`() {
        val hash1 = PinManager.hash("123456")
        val hash2 = PinManager.hash("123456")
        assertNotEquals(hash1, hash2, "Same PIN should produce different hashes (random salt)")
    }

    @Test
    fun `verify returns true for correct PIN`() {
        val pin = "123456"
        val stored = PinManager.hash(pin)
        assertTrue(PinManager.verify(pin, stored))
    }

    @Test
    fun `verify returns false for incorrect PIN`() {
        val stored = PinManager.hash("123456")
        assertFalse(PinManager.verify("654321", stored))
    }

    @Test
    fun `verify returns false for wrong pin length`() {
        val stored = PinManager.hash("123456")
        assertFalse(PinManager.verify("12345", stored))
    }

    @Test
    fun `verify returns false for malformed stored value`() {
        assertFalse(PinManager.verify("123456", "not-a-valid-hash"))
    }

    @Test
    fun `verify returns false for empty stored value`() {
        assertFalse(PinManager.verify("123456", ""))
    }

    @Test
    fun `verify works with 4 digit PIN`() {
        val pin = "1234"
        val stored = PinManager.hash(pin)
        assertTrue(PinManager.verify(pin, stored))
    }

    @Test
    fun `verify works with 6 digit PIN`() {
        val pin = "123456"
        val stored = PinManager.hash(pin)
        assertTrue(PinManager.verify(pin, stored))
    }
}
