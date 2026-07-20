package com.debank.mobile.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.ContactStore
import com.debank.mobile.domain.Contact

@Composable
fun ContactListScreen(
    contactStore: ContactStore,
    isPicker: Boolean,
    onBack: () -> Unit,
    onContactPicked: (String) -> Unit = {}
) {
    var contacts by remember { mutableStateOf(contactStore.getAll()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<Contact?>(null) }
    var deleteConfirmContact by remember { mutableStateOf<Contact?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    fun refresh() { contacts = contactStore.getAll() }

    val filteredContacts = if (searchQuery.isBlank()) contacts
    else contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.address.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari kontak...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (searchQuery.isNotBlank()) "Kontak tidak ditemukan"
                            else "Belum ada kontak",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isPicker && searchQuery.isBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Text("Tambah Kontak")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isPicker && searchQuery.isBlank()) {
                        item {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tambah Kontak")
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    items(filteredContacts, key = { it.address }) { contact ->
                        ContactCard(
                            contact = contact,
                            isPicker = isPicker,
                            onPick = { onContactPicked(contact.address) },
                            onEdit = { editingContact = contact },
                            onDelete = { deleteConfirmContact = contact }
                        )
                    }
            }
        }
    }

    if (showAddDialog) {
        ContactFormDialog(
            title = "Tambah Kontak",
            initialName = "",
            initialAddress = "",
            onDismiss = { showAddDialog = false },
            onSave = { name, address ->
                contactStore.add(Contact(name, address))
                showAddDialog = false
                refresh()
            }
        )
    }

    editingContact?.let { contact ->
        ContactFormDialog(
            title = "Edit Kontak",
            initialName = contact.name,
            initialAddress = contact.address,
            onDismiss = { editingContact = null },
            onSave = { name, address ->
                contactStore.edit(contact.address, name, address)
                editingContact = null
                refresh()
            }
        )
    }

    deleteConfirmContact?.let { contact ->
        AlertDialog(
            onDismissRequest = { deleteConfirmContact = null },
            title = { Text("Hapus Kontak") },
            text = { Text("Hapus ${contact.name} dari daftar kontak?") },
            confirmButton = {
                TextButton(onClick = {
                    contactStore.delete(contact.address)
                    deleteConfirmContact = null
                    refresh()
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmContact = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun String.getInitial(): String {
    return this.trim().takeIf { it.isNotEmpty() }?.first()?.uppercase() ?: "?"
}

private val avatarColors = listOf(
    Color(0xFF00897B), Color(0xFF00796B), Color(0xFF546E7A),
    Color(0xFF6D4C41), Color(0xFF5C6BC0), Color(0xFF00838F)
)

@Composable
private fun ContactCard(
    contact: Contact,
    isPicker: Boolean,
    onPick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = if (isPicker) onPick else onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colorIndex = contact.name.hashCode().mod(avatarColors.size).let { if (it < 0) it + avatarColors.size else it }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColors[colorIndex]),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    contact.name.getInitial(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = contact.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isPicker) {
                TextButton(onClick = onDelete) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ContactFormDialog(
    title: String,
    initialName: String,
    initialAddress: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var address by remember { mutableStateOf(initialAddress) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Nama") },
                    isError = nameError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                nameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; addressError = null },
                    label = { Text("Alamat Stellar") },
                    placeholder = { Text("G...") },
                    isError = addressError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                addressError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                nameError = if (name.isBlank()) "Nama tidak boleh kosong" else null
                addressError = when {
                    address.isBlank() -> "Alamat tidak boleh kosong"
                    address.length != 56 || !address.startsWith("G") -> "Alamat Stellar tidak valid"
                    else -> null
                }
                if (nameError == null && addressError == null) {
                    onSave(name.trim(), address.trim())
                }
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
