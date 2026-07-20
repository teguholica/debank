package com.debank.mobile.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.Bip39Challenge
import com.debank.mobile.data.Bip39Generator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmSeedScreen(
    challenges: List<Bip39Challenge>,
    onVerified: () -> Unit
) {
    var answers by remember { mutableStateOf(mapOf<Int, String>()) }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Konfirmasi Seed Phrase") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Pilih kata yang benar untuk setiap nomor:")
            Spacer(Modifier.height(16.dp))

            challenges.forEach { challenge ->
                Text("Kata ke-${challenge.index + 1}:")
                challenge.options.forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = answers[challenge.index] == option,
                            onClick = { answers = answers + (challenge.index to option) }
                        )
                        Text(option)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (showError) {
                Text(
                    "Ada yang salah. Coba lagi.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (answers.size < challenges.size) return@Button
                    if (Bip39Generator().verify(challenges, answers)) {
                        onVerified()
                    } else {
                        showError = true
                    }
                },
                enabled = answers.size == challenges.size
            ) {
                Text("Verifikasi")
            }
        }
    }
}
