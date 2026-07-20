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
            if (!isConfirming) {
                PinStep(
                    title = "Masukkan PIN 4-6 digit",
                    value = pin1,
                    label = "PIN baru",
                    buttonText = "Lanjut",
                    error = error,
                    onValueChange = { pin1 = it; error = null },
                    onAction = {
                        if (pin1.length < 4) {
                            error = "PIN minimal 4 digit"
                            return@PinStep
                        }
                        isConfirming = true
                    }
                )
            } else {
                PinStep(
                    title = "Masukkan PIN kembali",
                    value = pin2,
                    label = "Konfirmasi PIN",
                    buttonText = "Simpan PIN",
                    error = error,
                    onValueChange = { pin2 = it; error = null },
                    onAction = {
                        if (pin1 != pin2) {
                            error = "PIN tidak cocok"
                            pin2 = ""
                            return@PinStep
                        }
                        val hash = PinManager.hash(pin1)
                        store.setString(KeyValueStore.PIN_HASH_KEY, hash)
                        onComplete()
                    }
                )
            }
        }
    }
}

@Composable
private fun PinStep(
    title: String,
    value: String,
    label: String,
    buttonText: String,
    error: String?,
    onValueChange: (String) -> Unit,
    onAction: () -> Unit
) {
    Text(title)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            if (v.all { it.isDigit() } && v.length <= 6) {
                onValueChange(v)
            }
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        label = { Text(label) }
    )
    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(16.dp))
    Button(onClick = onAction) {
        Text(buttonText)
    }
}
