package com.debank.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.PinManager

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
                    Text("Ya, Hapus")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onShowSeedPhrase,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Lihat Seed Phrase")
            }

            OutlinedButton(
                onClick = onChangePin,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Ganti PIN")
            }

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Logout / Reset")
            }

            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onBack) {
                Text("Kembali")
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
            Text("Masukkan PIN untuk melihat seed phrase")
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 6) {
                        pin = it
                        error = false
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                label = { Text("PIN") },
                isError = error
            )

            if (error) {
                Text("PIN salah", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (pin.isEmpty()) return@Button
                val storedHash = store.getString(KeyValueStore.PIN_HASH_KEY) ?: return@Button
                if (PinManager.verify(pin, storedHash)) onVerifySuccess()
                else error = true
            }) {
                Text("Verifikasi")
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack) {
                Text("Kembali")
            }
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
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            OutlinedButton(onClick = onBack) {
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
            OutlinedTextField(
                value = oldPin,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 6) {
                        oldPin = it; error = ""
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                label = { Text("PIN Lama") },
                modifier = Modifier.fillMaxWidth(0.7f)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = newPin,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 6) {
                        newPin = it; error = ""
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                label = { Text("PIN Baru") },
                modifier = Modifier.fillMaxWidth(0.7f)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPin,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 6) {
                        confirmPin = it; error = ""
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                label = { Text("Konfirmasi PIN Baru") },
                modifier = Modifier.fillMaxWidth(0.7f)
            )

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { validateAndSave() }) {
                Text("Simpan")
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack) {
                Text("Batal")
            }
        }
    }
}
