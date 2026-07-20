package com.debank.mobile.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeKeyValueStore : KeyValueStore {
    val map = mutableMapOf<String, String>()
    override fun getString(key: String): String? = map[key]
    override fun setString(key: String, value: String) { map[key] = value }
    override fun contains(key: String): Boolean = key in map
    override fun remove(key: String) { map.remove(key) }
}

class SeedPhraseTest {

    @Test
    fun `store and retrieve seed phrase`() {
        val store = FakeKeyValueStore()
        val words = "abandon ability able about above absent absorb abstract absurd abuse access accident"
        store.setString(KeyValueStore.SEED_PHRASE_KEY, words)

        val retrieved = store.getString(KeyValueStore.SEED_PHRASE_KEY)
        assertEquals(words, retrieved)
    }

    @Test
    fun `returns null when no seed phrase stored`() {
        val store = FakeKeyValueStore()
        assertNull(store.getString(KeyValueStore.SEED_PHRASE_KEY))
    }

    @Test
    fun `overwrites existing seed phrase`() {
        val store = FakeKeyValueStore()
        store.setString(KeyValueStore.SEED_PHRASE_KEY, "old phrase")
        store.setString(KeyValueStore.SEED_PHRASE_KEY, "new phrase")

        assertEquals("new phrase", store.getString(KeyValueStore.SEED_PHRASE_KEY))
    }

    @Test
    fun `store and retrieve twelve words joined by space`() {
        val store = FakeKeyValueStore()
        val words = listOf(
            "zoo", "young", "xray", "yellow", "wish", "vivid",
            "upset", "topic", "stereo", "rhythm", "quick", "piano"
        )
        store.setString(KeyValueStore.SEED_PHRASE_KEY, words.joinToString(" "))

        val retrieved = store.getString(KeyValueStore.SEED_PHRASE_KEY)
        assertEquals(12, retrieved?.split(" ")?.size)
        assertEquals(words, retrieved?.split(" "))
    }

    @Test
    fun `uses SEED_PHRASE_KEY constant`() {
        val store = FakeKeyValueStore()
        store.setString(KeyValueStore.SEED_PHRASE_KEY, "test phrase")

        // same key via constant
        assertTrue(store.contains(KeyValueStore.SEED_PHRASE_KEY))
        assertEquals("test phrase", store.getString(KeyValueStore.SEED_PHRASE_KEY))
    }

    @Test
    fun `remove clears seed phrase`() {
        val store = FakeKeyValueStore()
        store.setString(KeyValueStore.SEED_PHRASE_KEY, "test phrase")
        store.remove(KeyValueStore.SEED_PHRASE_KEY)

        assertNull(store.getString(KeyValueStore.SEED_PHRASE_KEY))
    }
}
