package com.debank.mobile.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.PinManager
import com.debank.mobile.ui.components.PinPadInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    store: KeyValueStore,
    errorMessage: String? = null,
    onComplete: () -> Unit,
    currentStep: Int = 3,
    totalSteps: Int = 3
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Langkah $currentStep dari $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(24.dp))

            Text(
                if (!isConfirming) "Buat PIN 4-6 digit" else "Masukkan PIN kembali",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(32.dp))

            if (!isConfirming) {
                PinPadInput(
                    pin = pin1,
                    maxLength = 6,
                    onDigit = { d ->
                        if (pin1.length < 6) {
                            pin1 += d.toString()
                            error = null
                        }
                    },
                    onDelete = {
                        if (pin1.isNotEmpty()) pin1 = pin1.dropLast(1)
                    },
                    onComplete = {
                        if (pin1.length < 4) {
                            error = "PIN minimal 4 digit"
                        } else {
                            isConfirming = true
                        }
                    }
                )
            } else {
                PinPadInput(
                    pin = pin2,
                    maxLength = 6,
                    onDigit = { d ->
                        if (pin2.length < 6) {
                            pin2 += d.toString()
                            error = null
                        }
                    },
                    onDelete = {
                        if (pin2.isNotEmpty()) pin2 = pin2.dropLast(1)
                    },
                    onComplete = {
                        if (pin1 != pin2) {
                            error = "PIN tidak cocok"
                            pin2 = ""
                        } else {
                            val hash = PinManager.hash(pin1)
                            store.setString(KeyValueStore.PIN_HASH_KEY, hash)
                            onComplete()
                        }
                    }
                )
            }

            error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
