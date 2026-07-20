package com.debank.mobile.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.StellarConfig
import com.debank.mobile.data.StellarRepository
import com.debank.mobile.domain.AccountBalance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: StellarRepository,
    publicKey: String,
    onSend: () -> Unit,
    onLogout: () -> Unit
) {
    var balance by remember { mutableStateOf<AccountBalance?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchBalance() {
        scope.launch {
            loading = true
            error = false
            try {
                balance = repository.getAccountBalance(publicKey, StellarConfig.IDR_ASSET_CODE)
            } catch (_: Exception) {
                error = true
            }
            loading = false
        }
    }

    LaunchedEffect(publicKey) { fetchBalance() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DeBank") }) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = { fetchBalance() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("DeBank Wallet", style = MaterialTheme.typography.headlineLarge)

                Spacer(Modifier.height(32.dp))

                if (error) {
                    Text("Gagal memuat saldo", color = MaterialTheme.colorScheme.error)
                } else if (balance != null) {
                    Text(
                        "Saldo IDR",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${balance!!.balance} IDR",
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onSend,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Kirim")
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(onClick = onLogout) {
                    Text("Logout / Reset")
                }
            }
        }
    }
}
