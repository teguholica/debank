package com.debank.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.PinManager
import com.debank.mobile.ui.components.PinPadInput

private sealed class SettingsStep {
    data object Menu : SettingsStep()
    data class VerifyPinForSeed(val words: List<String>) : SettingsStep()
    data class ShowSeedPhrase(val words: List<String>) : SettingsStep()
    data object ChangePin : SettingsStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    store: KeyValueStore,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var step by remember { mutableStateOf<SettingsStep>(SettingsStep.Menu) }

    when (val currentStep = step) {
        SettingsStep.Menu -> SettingsMenu(
            onBack = onBack,
            onShowSeedPhrase = {
                val raw = store.getString(KeyValueStore.SEED_PHRASE_KEY)
                if (raw != null) {
                    step = SettingsStep.VerifyPinForSeed(raw.split(" "))
                }
            },
            onChangePin = { step = SettingsStep.ChangePin },
            onLogout = onLogout
        )
        is SettingsStep.VerifyPinForSeed -> PinVerifyForSeedScreen(
            store = store,
            onVerifySuccess = { step = SettingsStep.ShowSeedPhrase(currentStep.words) },
            onBack = { step = SettingsStep.Menu }
        )
        is SettingsStep.ShowSeedPhrase -> ShowSeedPhraseScreen(
            words = currentStep.words,
            onBack = { step = SettingsStep.Menu }
        )
        SettingsStep.ChangePin -> ChangePinScreen(
            store = store,
            onSuccess = { step = SettingsStep.Menu },
            onBack = { step = SettingsStep.Menu }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMenu(
    onBack: () -> Unit,
    onShowSeedPhrase: () -> Unit,
    onChangePin: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Logout / Reset") },
            text = { Text("Semua data lokal akan dihapus. Lanjutkan?") },
            confirmButton = {
                TextButton(onClick = onLogout) {
                    Text("Ya, Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pengaturan") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.Key,
                title = "Lihat Seed Phrase",
                subtitle = "12 kata pemulihan wallet",
                onClick = onShowSeedPhrase
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Ganti PIN",
                subtitle = "Ubah PIN 4-6 digit",
                onClick = onChangePin
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Logout / Reset",
                subtitle = "Hapus semua data lokal",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showLogoutConfirm = true }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinVerifyForSeedScreen(
    store: KeyValueStore,
    onVerifySuccess: () -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Verifikasi PIN") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.2f))
            Text("Masukkan PIN untuk melihat seed phrase")
            Spacer(Modifier.height(32.dp))

            PinPadInput(
                pin = pin,
                onDigit = { pin += it.toString(); error = false },
                onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                maxLength = 6,
                onComplete = {
                    if (pin.isEmpty()) return@PinPadInput
                    val storedHash = store.getString(KeyValueStore.PIN_HASH_KEY) ?: return@PinPadInput
                    if (PinManager.verify(pin, storedHash)) onVerifySuccess()
                    else { error = true; pin = "" }
                }
            )

            if (error) {
                Spacer(Modifier.height(16.dp))
                Text("PIN salah", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.weight(0.2f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowSeedPhraseScreen(
    words: List<String>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Seed Phrase") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Jangan screenshot! Catat 12 kata ini di tempat aman.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                words.chunked(3).forEachIndexed { rowIdx, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEachIndexed { colIdx, word ->
                            val idx = rowIdx * 3 + colIdx + 1
                            Text(
                                "$idx. $word",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            TextButton(onClick = onBack) {
                Text("Kembali")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePinScreen(
    store: KeyValueStore,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) }

    fun validateAndSave() {
        if (oldPin.isEmpty()) { error = "Masukkan PIN lama"; return }
        val storedHash = store.getString(KeyValueStore.PIN_HASH_KEY) ?: run {
            error = "Belum ada PIN"
            return
        }
        if (!PinManager.verify(oldPin, storedHash)) {
            error = "PIN lama salah"
            return
        }
        if (newPin.length < 4 || newPin.length > 6 || !newPin.all { it.isDigit() }) {
            error = "PIN baru harus 4-6 digit angka"
            return
        }
        if (newPin != confirmPin) {
            error = "PIN baru tidak cocok"
            return
        }
        store.setString(KeyValueStore.PIN_HASH_KEY, PinManager.hash(newPin))
        onSuccess()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ganti PIN") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                0 -> {
                    Text("Masukkan PIN Lama")
                    Spacer(Modifier.height(24.dp))
                    PinPadInput(
                        pin = oldPin,
                        onDigit = { oldPin += it.toString(); error = "" },
                        onDelete = { if (oldPin.isNotEmpty()) oldPin = oldPin.dropLast(1) },
                        onComplete = { step = 1 }
                    )
                }
                1 -> {
                    Text("Masukkan PIN Baru")
                    Spacer(Modifier.height(24.dp))
                    PinPadInput(
                        pin = newPin,
                        onDigit = {
                            if (newPin.length < 6) { newPin += it.toString(); error = "" }
                        },
                        onDelete = { if (newPin.isNotEmpty()) newPin = newPin.dropLast(1) },
                        onComplete = {
                            if (newPin.length < 4) error = "PIN baru minimal 4 digit"
                            else step = 2
                        }
                    )
                }
                2 -> {
                    Text("Konfirmasi PIN Baru")
                    Spacer(Modifier.height(24.dp))
                    PinPadInput(
                        pin = confirmPin,
                        onDigit = {
                            if (confirmPin.length < 6) { confirmPin += it.toString(); error = "" }
                        },
                        onDelete = { if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1) },
                        onComplete = {
                            if (newPin != confirmPin) {
                                error = "PIN tidak cocok"
                                confirmPin = ""
                            } else {
                                validateAndSave()
                            }
                        }
                    )
                }
            }

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            if (step > 0) {
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { step--; error = "" }) {
                    Text("Kembali")
                }
            }
        }
    }
}
