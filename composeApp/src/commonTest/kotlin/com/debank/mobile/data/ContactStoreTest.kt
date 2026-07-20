package com.debank.mobile.data

import com.debank.mobile.domain.Contact
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContactStoreTest {

    private fun fakeStore(): KeyValueStore = InMemoryStore()

    @Test
    fun `getAll returns empty list when no contacts stored`() {
        val store = ContactStore(fakeStore())
        assertTrue(store.getAll().isEmpty())
    }

    @Test
    fun `add stores a contact`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))

        val all = store.getAll()
        assertEquals(1, all.size)
        assertEquals("Alice", all[0].name)
        assertEquals("GAAAAA1111111111111111111111111111111111111111111111", all[0].address)
    }

    @Test
    fun `add multiple contacts`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))
        store.add(Contact("Bob", "GBBBBB2222222222222222222222222222222222222222222222"))

        val all = store.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun `add duplicate address overwrites`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))
        store.add(Contact("AliceBaru", "GAAAAA1111111111111111111111111111111111111111111111"))

        val all = store.getAll()
        assertEquals(1, all.size)
        assertEquals("AliceBaru", all[0].name)
    }

    @Test
    fun `delete removes contact by address`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))
        store.add(Contact("Bob", "GBBBBB2222222222222222222222222222222222222222222222"))

        store.delete("GAAAAA1111111111111111111111111111111111111111111111")

        val all = store.getAll()
        assertEquals(1, all.size)
        assertEquals("Bob", all[0].name)
    }

    @Test
    fun `delete unknown address does nothing`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))

        store.delete("GUNKNOWN")

        assertEquals(1, store.getAll().size)
    }

    @Test
    fun `edit updates contact name and address`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))

        store.edit("GAAAAA1111111111111111111111111111111111111111111111", "AliceBaru", "GNEWADDR")

        val all = store.getAll()
        assertEquals(1, all.size)
        assertEquals("AliceBaru", all[0].name)
        assertEquals("GNEWADDR", all[0].address)
    }

    @Test
    fun `edit unknown address does nothing`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))

        store.edit("GUNKNOWN", "Baru", "GNEWADDR")

        val all = store.getAll()
        assertEquals(1, all.size)
        assertEquals("Alice", all[0].name)
    }

    @Test
    fun `persists across separate instances`() {
        val kv = fakeStore()
        val store1 = ContactStore(kv)
        store1.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))

        val store2 = ContactStore(kv)
        val all = store2.getAll()
        assertEquals(1, all.size)
    }

    @Test
    fun `handles special characters in name`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Alice (Teman)", "GAAAAA1111111111111111111111111111111111111111111111"))

        val all = store.getAll()
        assertEquals("Alice (Teman)", all[0].name)
    }

    @Test
    fun `getAll returns contacts sorted by name`() {
        val kv = fakeStore()
        val store = ContactStore(kv)
        store.add(Contact("Zara", "GZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ"))
        store.add(Contact("Alice", "GAAAAA1111111111111111111111111111111111111111111111"))
        store.add(Contact("Bob", "GBBBBB2222222222222222222222222222222222222222222222"))

        val all = store.getAll()
        assertEquals("Alice", all[0].name)
        assertEquals("Bob", all[1].name)
        assertEquals("Zara", all[2].name)
    }
}

private class InMemoryStore : KeyValueStore {
    private val map = mutableMapOf<String, String>()

    override fun getString(key: String): String? = map[key]
    override fun setString(key: String, value: String) { map[key] = value }
    override fun contains(key: String): Boolean = key in map
    override fun remove(key: String) { map.remove(key) }
}
