package com.debank.mobile.ui.send

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.PinManager
import com.debank.mobile.data.StellarConfig
import com.debank.mobile.data.StellarRepository
import com.debank.mobile.domain.KeyPairData
import com.debank.mobile.ui.components.PinPadInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed class SendStep {
    data object Input : SendStep()
    data class Confirm(val address: String, val amount: String) : SendStep()
    data object Pin : SendStep()
    data object Submitting : SendStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendFlow(
    store: KeyValueStore,
    repository: StellarRepository,
    publicKey: String,
    secretSeed: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onPickContact: () -> Unit,
    prefilledAddress: String = ""
) {
    var step by remember { mutableStateOf<SendStep>(SendStep.Input) }
    var address by remember(prefilledAddress) { mutableStateOf(prefilledAddress) }
    var amount by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var capturedAddress by remember { mutableStateOf("") }
    var capturedAmount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    fun validateAddress(): Boolean {
        addressError = when {
            address.isBlank() -> "Alamat tidak boleh kosong"
            address.length != 56 || !address.startsWith("G") -> "Alamat Stellar tidak valid"
            else -> null
        }
        return addressError == null
    }

    fun validateAmount(): Boolean {
        amountError = when {
            amount.isBlank() -> "Jumlah tidak boleh kosong"
            amount.toDoubleOrNull() == null -> "Format angka tidak valid"
            amount.toDouble() <= 0 -> "Jumlah harus lebih dari 0"
            else -> null
        }
        return amountError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kirim IDR") })
        },
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                val isSuccess = data.visuals.message.startsWith("✓")
                val displayMessage = data.visuals.message.removePrefix("✓").removePrefix("✗")
                Snackbar(
                    shape = RoundedCornerShape(12.dp),
                    containerColor = if (isSuccess) Color(0xFF43A047) else MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(displayMessage)
                    }
                }
            }
        }
    ) { padding ->
        when (val currentStep = step) {
            SendStep.Input -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Kirim IDR", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it; addressError = null },
                        label = { Text("Alamat Stellar tujuan") },
                        placeholder = { Text("G...") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = addressError != null,
                        singleLine = true
                    )
                    addressError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onPickContact,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Dari Kontak")
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it; amountError = null },
                        label = { Text("Jumlah IDR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = amountError != null,
                        singleLine = true
                    )
                    amountError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (validateAddress() && validateAmount()) {
                                step = SendStep.Confirm(address.trim(), amount.trim())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lanjut")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal")
                    }
                }
            }

            is SendStep.Confirm -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Konfirmasi Pengiriman", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tujuan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(currentStep.address, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

                            Spacer(Modifier.height(16.dp))

                            Text("Jumlah", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("${currentStep.amount} IDR", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

                            Spacer(Modifier.height(16.dp))

                            Text("Biaya (fee)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("100 stroops", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            capturedAddress = currentStep.address
                            capturedAmount = currentStep.amount
                            step = SendStep.Pin
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Konfirmasi & Kirim")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { step = SendStep.Input },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembali")
                    }
                }
            }

            SendStep.Pin -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(0.2f))
                    Text("Masukkan PIN", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Masukkan PIN untuk mengirim transaksi", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(32.dp))

                    PinPadInput(
                        pin = pin,
                        onDigit = { pin += it.toString(); pinError = false },
                        onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                        maxLength = 6,
                        onComplete = {
                            val storedHash = store.getString(KeyValueStore.PIN_HASH_KEY) ?: return@PinPadInput
                            if (PinManager.verify(pin, storedHash)) {
                                step = SendStep.Submitting
                                scope.launch {
                                    try {
                                        val keyPair = KeyPairData(publicKey, secretSeed)
                                        val assetId = StellarConfig.idrAssetId()
                                        repository.addTrustline(keyPair, assetId)
                                        repository.sendPayment(
                                            keyPair = keyPair,
                                            destination = capturedAddress,
                                            amount = capturedAmount,
                                            assetId = assetId
                                        )
                                        snackbar.showSnackbar("✓ Transaksi berhasil!")
                                        delay(1500)
                                        onSuccess()
                                    } catch (e: Exception) {
                                        step = SendStep.Input
                                        snackbar.showSnackbar("✗ ${e.message ?: "Gagal mengirim transaksi"}")
                                    }
                                }
                            } else {
                                pinError = true
                                pin = ""
                            }
                        }
                    )

                    if (pinError) {
                        Spacer(Modifier.height(16.dp))
                        Text("PIN salah", color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.weight(0.2f))
                }
            }

            SendStep.Submitting -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Mengirim transaksi...")
                }
            }
        }
    }
}
