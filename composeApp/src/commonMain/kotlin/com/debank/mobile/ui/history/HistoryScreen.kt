package com.debank.mobile.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.debank.mobile.data.StellarRepository
import com.debank.mobile.domain.TransactionDirection
import com.debank.mobile.domain.TransactionItem
import kotlinx.coroutines.launch

private fun formatAmount(raw: String): String {
    val trimmed = raw.trimEnd('0').trimEnd('.')
    return if (trimmed.isEmpty()) "0" else trimmed
}

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis == 0L) return "-"
    val totalDays = epochMillis / 86400000L
    var y = (totalDays + 719468L) / 146097L * 400L
    var temp = (totalDays + 719468L) % 146097L
    if (temp < 0) { temp += 146097L; y -= 400L }
    val yoe = if (temp == 146096L) 399 else (temp.toInt() % 36525 * 100 / 36525 + (temp.toInt() % 36525 * 100 + 36500) / 36525 - 1)
    y += yoe
    val doy = temp.toInt() - (yoe * 365 + yoe / 4 - yoe / 100)
    val mp = (doy * 5 + 2) / 153
    val d = doy - (mp * 153 + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    val monthStr = if (m < 10) "0$m" else "$m"
    val dayStr = if (d < 10) "0$d" else "$d"
    return "$dayStr/$monthStr/$y"
}

private fun formatCounterparty(address: String): String {
    if (address.length <= 12) return address
    return "${address.take(6)}...${address.takeLast(4)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: StellarRepository,
    publicKey: String,
    onBack: () -> Unit
) {
    var transactions by remember { mutableStateOf<List<TransactionItem>>(emptyList()) }
    var initialLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun fetchTransactions() {
        scope.launch {
            isRefreshing = true
            error = false
            try {
                transactions = repository.getTransactions(publicKey)
            } catch (_: Exception) {
                error = true
            }
            isRefreshing = false
            initialLoading = false
        }
    }

    LaunchedEffect(publicKey) { fetchTransactions() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Riwayat Transaksi") })
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { fetchTransactions() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                initialLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error && transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Gagal memuat riwayat transaksi",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = onBack) {
                                Text("Kembali")
                            }
                        }
                    }
                }
                transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Belum ada transaksi",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = onBack) {
                                Text("Kembali")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions, key = { it.id }) { txn ->
                            TransactionCard(txn)
                        }
                        item {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text("Kembali")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(txn: TransactionItem) {
    val isInbound = txn.direction == TransactionDirection.Inbound
    val label = if (isInbound) "Diterima" else "Dikirim"
    val sign = if (isInbound) "+" else "-"
    val color = if (isInbound) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$sign${formatAmount(txn.amount)} IDR",
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatCounterparty(txn.counterparty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(txn.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
