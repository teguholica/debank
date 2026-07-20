package com.debank.mobile.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.StellarConfig
import com.debank.mobile.data.StellarRepository
import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.KeyPairData
import com.debank.mobile.domain.TransactionItem
import com.debank.mobile.ui.components.BalanceCard
import com.debank.mobile.ui.components.QuickActionItem
import com.debank.mobile.ui.components.QuickActionRow
import com.debank.mobile.ui.components.TransactionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: StellarRepository,
    publicKey: String,
    secretSeed: String,
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    var balance by remember { mutableStateOf<AccountBalance?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var funding by remember { mutableStateOf(false) }
    var fundError by remember { mutableStateOf<String?>(null) }
    var recentTxns by remember { mutableStateOf<List<TransactionItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

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

    fun fetchRecentTransactions() {
        scope.launch {
            try {
                val all = repository.getTransactions(publicKey)
                recentTxns = all.take(5)
            } catch (_: Exception) { }
        }
    }

    fun fundIdr() {
        scope.launch {
            funding = true
            fundError = null
            try {
                repository.fundTestIdr(KeyPairData(publicKey, secretSeed))
                fetchBalance()
            } catch (e: Exception) {
                fundError = "Gagal: ${e.message}"
            }
            funding = false
        }
    }

    LaunchedEffect(publicKey) {
        fetchBalance()
        fetchRecentTransactions()
    }

    PullToRefreshBox(
        isRefreshing = loading,
        onRefresh = {
            fetchBalance()
            fetchRecentTransactions()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BalanceCard(
                    balance = balance?.balance,
                    loading = loading,
                    error = error,
                    onRefresh = { fetchBalance() }
                )
            }

            if (fundError != null) {
                item {
                    Text(
                        fundError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                QuickActionRow(
                    actions = listOf(
                        QuickActionItem(Icons.Default.Send, "Kirim", onSend),
                        QuickActionItem(Icons.Default.Download, "Terima", onReceive),
                        QuickActionItem(
                            Icons.Default.WaterDrop,
                            if (funding) "Memproses..." else "Isi Saldo",
                            { if (!funding) fundIdr() }
                        )
                    )
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
            }

            item {
                Text(
                    "Transaksi Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (recentTxns.isEmpty()) {
                item {
                    Text(
                        "Belum ada transaksi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(recentTxns, key = { it.id }) { txn ->
                    TransactionCard(txn, onClick = onHistory)
                }
            }

        }
    }
}
