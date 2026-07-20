package com.debank.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.domain.AppScreen
import com.debank.mobile.ui.dashboard.DashboardScreen
import com.debank.mobile.ui.onboarding.OnboardingFlow
import com.debank.mobile.ui.pin.PinVerifyScreen

@Composable
fun App(store: KeyValueStore) {
    MaterialTheme {
        val startScreen = remember {
            if (store.contains(KeyValueStore.PIN_HASH_KEY)) AppScreen.PinVerify
            else AppScreen.Onboarding
        }
        var screen by remember { mutableStateOf(startScreen) }

        when (screen) {
            AppScreen.Onboarding -> OnboardingFlow(
                store = store,
                onComplete = { screen = AppScreen.Dashboard }
            )
            AppScreen.PinVerify -> PinVerifyScreen(
                store = store,
                onSuccess = { screen = AppScreen.Dashboard }
            )
            AppScreen.Dashboard -> DashboardScreen(
                onLogout = {
                    store.remove(KeyValueStore.PIN_HASH_KEY)
                    screen = AppScreen.Onboarding
                }
            )
        }
    }
}
