package com.debank.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.debank.mobile.data.ContactStore
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.StellarRepositoryImpl
import com.debank.mobile.domain.DashboardRoute
import com.debank.mobile.domain.OnboardingRoute
import com.debank.mobile.domain.PinVerifyRoute
import com.debank.mobile.ui.onboarding.OnboardingFlow
import com.debank.mobile.ui.pin.PinVerifyScreen
import com.debank.mobile.ui.theme.DeBankTheme

@Composable
fun App(store: KeyValueStore) {
    DeBankTheme {
        val repository = remember { StellarRepositoryImpl() }
        val contactStore = remember { ContactStore(store) }
        val hasPin = remember { store.contains(KeyValueStore.PIN_HASH_KEY) }
        var entryScreen = remember { mutableStateOf<Any?>(if (hasPin) PinVerifyRoute else OnboardingRoute) }

        val logout = {
            store.remove(KeyValueStore.PIN_HASH_KEY)
            store.remove(KeyValueStore.PUBLIC_KEY_KEY)
            store.remove(KeyValueStore.SECRET_SEED_KEY)
            store.remove(KeyValueStore.SEED_PHRASE_KEY)
            entryScreen.value = OnboardingRoute
        }

        when (entryScreen.value) {
            OnboardingRoute -> OnboardingFlow(
                store = store,
                repository = repository,
                onComplete = { entryScreen.value = null }
            )
            PinVerifyRoute -> PinVerifyScreen(
                store = store,
                onSuccess = { entryScreen.value = null }
            )
            else -> {
                val backStack = remember { mutableStateListOf<Any>(DashboardRoute) }

                NavigationApp(
                    backStack = backStack,
                    store = store,
                    repository = repository,
                    contactStore = contactStore,
                    onLogout = logout
                )
            }
        }
    }
}
