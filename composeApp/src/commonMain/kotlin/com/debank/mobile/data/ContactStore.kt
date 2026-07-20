package com.debank.mobile.data

import com.debank.mobile.domain.Contact

// ponytail: upgrade to kotlinx.serialization/JSON when >2 entity types need persistence
class ContactStore(
    private val store: KeyValueStore
) {
    private companion object {
        const val KEY = "contacts"
        const val SEP_FIELD = "|"
        const val SEP_RECORD = "\n"
    }

    fun getAll(): List<Contact> {
        val raw = store.getString(KEY) ?: return emptyList()
        return parseAll(raw).sortedBy { it.name.lowercase() }
    }

    fun add(contact: Contact) {
        val all = parseAll(store.getString(KEY) ?: "")
            .filter { it.address != contact.address }
            .toMutableList()
        all.add(contact)
        store.setString(KEY, serializeAll(all))
    }

    fun delete(address: String) {
        val raw = store.getString(KEY) ?: return
        val remaining = parseAll(raw).filter { it.address != address }
        if (remaining.isEmpty()) store.remove(KEY)
        else store.setString(KEY, serializeAll(remaining))
    }

    fun edit(oldAddress: String, newName: String, newAddress: String) {
        val raw = store.getString(KEY) ?: return
        val all = parseAll(raw).map { contact ->
            if (contact.address == oldAddress) Contact(newName, newAddress)
            else contact
        }
        store.setString(KEY, serializeAll(all))
    }

    private fun parseAll(raw: String): MutableList<Contact> {
        val list = mutableListOf<Contact>()
        raw.split(SEP_RECORD).forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = line.split(SEP_FIELD, limit = 2)
            if (parts.size == 2) list.add(Contact(parts[0], parts[1]))
        }
        return list
    }

    private fun serializeAll(contacts: List<Contact>): String =
        contacts.joinToString(SEP_RECORD) { "${it.name}$SEP_FIELD${it.address}" }
}
