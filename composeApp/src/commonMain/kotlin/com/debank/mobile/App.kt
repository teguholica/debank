package com.debank.mobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.debank.mobile.data.ContactStore
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.StellarRepositoryImpl
import com.debank.mobile.domain.AppScreen
import com.debank.mobile.ui.contact.ContactListScreen
import com.debank.mobile.ui.dashboard.DashboardScreen
import com.debank.mobile.ui.history.HistoryScreen
import com.debank.mobile.ui.onboarding.OnboardingFlow
import com.debank.mobile.ui.pin.PinVerifyScreen
import com.debank.mobile.ui.receive.ReceiveScreen
import com.debank.mobile.ui.send.SendFlow

@Composable
fun App(store: KeyValueStore) {
    MaterialTheme {
        val repository = remember { StellarRepositoryImpl() }
        val contactStore = remember { ContactStore(store) }
        val startScreen = remember {
            if (store.contains(KeyValueStore.PIN_HASH_KEY)) AppScreen.PinVerify
            else AppScreen.Onboarding
        }
        var screen by remember { mutableStateOf(startScreen) }

        when (val currentScreen = screen) {
            AppScreen.Onboarding -> OnboardingFlow(
                store = store,
                repository = repository,
                onComplete = { screen = AppScreen.Dashboard }
            )
            AppScreen.PinVerify -> PinVerifyScreen(
                store = store,
                onSuccess = { screen = AppScreen.Dashboard }
            )
            AppScreen.Dashboard -> DashboardScreen(
                repository = repository,
                publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                onSend = { screen = AppScreen.Send() },
                onReceive = { screen = AppScreen.Receive },
                onHistory = { screen = AppScreen.History },
                onContacts = { screen = AppScreen.ContactList },
                onLogout = {
                    store.remove(KeyValueStore.PIN_HASH_KEY)
                    store.remove(KeyValueStore.PUBLIC_KEY_KEY)
                    store.remove(KeyValueStore.SECRET_SEED_KEY)
                    screen = AppScreen.Onboarding
                }
            )
            is AppScreen.Send -> SendFlow(
                store = store,
                repository = repository,
                publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                secretSeed = store.getString(KeyValueStore.SECRET_SEED_KEY) ?: "",
                onBack = { screen = AppScreen.Dashboard },
                onSuccess = { screen = AppScreen.Dashboard },
                onPickContact = { screen = AppScreen.ContactPicker },
                prefilledAddress = currentScreen.prefilledAddress
            )
            AppScreen.Receive -> ReceiveScreen(
                publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                onScanResult = { address ->
                    screen = AppScreen.Send(prefilledAddress = address)
                },
                onBack = { screen = AppScreen.Dashboard }
            )
            AppScreen.History -> HistoryScreen(
                repository = repository,
                publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                onBack = { screen = AppScreen.Dashboard }
            )
            AppScreen.ContactList -> ContactListScreen(
                contactStore = contactStore,
                isPicker = false,
                onBack = { screen = AppScreen.Dashboard }
            )
            AppScreen.ContactPicker -> ContactListScreen(
                contactStore = contactStore,
                isPicker = true,
                onBack = { screen = AppScreen.Send() },
                onContactPicked = { address ->
                    screen = AppScreen.Send(prefilledAddress = address)
                }
            )
        }
    }
}
