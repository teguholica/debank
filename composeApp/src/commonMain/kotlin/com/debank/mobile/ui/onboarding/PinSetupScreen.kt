package com.debank.mobile.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.PinManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    store: KeyValueStore,
    onComplete: () -> Unit
) {
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Buat PIN") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (!isConfirming) "Masukkan PIN 4-6 digit"
                else "Masukkan PIN kembali"
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = if (!isConfirming) pin1 else pin2,
                onValueChange = { value ->
                    if (value.all { it.isDigit() } && value.length <= 6) {
                        if (!isConfirming) pin1 = value else pin2 = value
                        error = null
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                label = { Text(if (!isConfirming) "PIN baru" else "Konfirmasi PIN") }
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (!isConfirming) {
                        if (pin1.length < 4) {
                            error = "PIN minimal 4 digit"
                            return@Button
                        }
                        isConfirming = true
                    } else {
                        if (pin1 != pin2) {
                            error = "PIN tidak cocok"
                            pin2 = ""
                            return@Button
                        }
                        val hash = PinManager.hash(pin1)
                        store.setString(KeyValueStore.PIN_HASH_KEY, hash)
                        onComplete()
                    }
                }
            ) {
                Text(if (!isConfirming) "Lanjut" else "Simpan PIN")
            }
        }
    }
}
