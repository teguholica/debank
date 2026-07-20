package com.debank.mobile.data

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCopyToClipboard(): (String) -> Unit
