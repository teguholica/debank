package com.debank.mobile.ui.receive

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun QrScannerView(
    onResult: (String) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier
)
