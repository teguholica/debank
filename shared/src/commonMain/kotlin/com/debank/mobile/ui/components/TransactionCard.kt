package com.debank.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.debank.mobile.domain.TransactionDirection
import com.debank.mobile.domain.TransactionItem

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

@Composable
fun TransactionCard(
    txn: TransactionItem,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isInbound = txn.direction == TransactionDirection.Inbound
    val label = if (isInbound) "Diterima" else "Dikirim"
    val sign = if (isInbound) "+" else "-"
    val amountColor = if (isInbound) Color(0xFF43A047) else MaterialTheme.colorScheme.error
    val icon = if (isInbound) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    Card(
        onClick = onClick ?: {},
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(36.dp),
                tint = amountColor
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    formatCounterparty(txn.counterparty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatTimestamp(txn.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Text(
                "$sign${formatAmount(txn.amount)} IDR",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
