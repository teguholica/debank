package com.debank.mobile

import androidx.compose.ui.window.ComposeUIViewController
import com.debank.mobile.data.UserDefaultsStore

fun MainViewController() = ComposeUIViewController { App(store = UserDefaultsStore()) }
