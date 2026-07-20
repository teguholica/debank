package com.debank.mobile

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
import com.debank.mobile.ui.settings.SettingsScreen
import com.debank.mobile.ui.components.DeBankScaffold
import com.debank.mobile.ui.theme.DeBankTheme

@Composable
fun App(store: KeyValueStore) {
    DeBankTheme {
        val repository = remember { StellarRepositoryImpl() }
        val contactStore = remember { ContactStore(store) }
        val startScreen = remember {
            if (store.contains(KeyValueStore.PIN_HASH_KEY)) AppScreen.PinVerify
            else AppScreen.Onboarding
        }
        var screen by remember { mutableStateOf(startScreen) }

        val logout = {
            store.remove(KeyValueStore.PIN_HASH_KEY)
            store.remove(KeyValueStore.PUBLIC_KEY_KEY)
            store.remove(KeyValueStore.SECRET_SEED_KEY)
            store.remove(KeyValueStore.SEED_PHRASE_KEY)
            screen = AppScreen.Onboarding
        }

        when (screen) {
            AppScreen.Onboarding -> OnboardingFlow(
                store = store,
                repository = repository,
                onComplete = { screen = AppScreen.Dashboard }
            )
            AppScreen.PinVerify -> PinVerifyScreen(
                store = store,
                onSuccess = { screen = AppScreen.Dashboard }
            )
            else -> {
                val showBottomNav = screen in setOf(
                    AppScreen.Dashboard,
                    AppScreen.Send(),
                    AppScreen.History,
                    AppScreen.Settings
                )

                fun navigate(target: AppScreen) {
                    screen = target
                }

                fun onBack() {
                    screen = AppScreen.Dashboard
                }

                DeBankScaffold(
                    currentScreen = screen,
                    onNavigate = { navigate(it) },
                    showBottomNav = showBottomNav,
                    topBar = {}
                ) {
                    when (val s = screen) {
                        AppScreen.Dashboard -> DashboardScreen(
                            repository = repository,
                            publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                            secretSeed = store.getString(KeyValueStore.SECRET_SEED_KEY) ?: "",
                            onSend = { navigate(AppScreen.Send()) },
                            onReceive = { navigate(AppScreen.Receive) },
                            onHistory = { navigate(AppScreen.History) },
                            onSettings = { navigate(AppScreen.Settings) }
                        )
                        is AppScreen.Send -> SendFlow(
                            store = store,
                            repository = repository,
                            publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                            secretSeed = store.getString(KeyValueStore.SECRET_SEED_KEY) ?: "",
                            onBack = { onBack() },
                            onSuccess = { onBack() },
                            onPickContact = { navigate(AppScreen.ContactPicker) },
                            prefilledAddress = s.prefilledAddress
                        )
                        AppScreen.Receive -> ReceiveScreen(
                            publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                            onScanResult = { address ->
                                navigate(AppScreen.Send(prefilledAddress = address))
                            },
                            onBack = { onBack() }
                        )
                        AppScreen.History -> HistoryScreen(
                            repository = repository,
                            publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                            onBack = { onBack() }
                        )
                        AppScreen.ContactList -> ContactListScreen(
                            contactStore = contactStore,
                            isPicker = false,
                            onBack = { onBack() }
                        )
                        AppScreen.ContactPicker -> ContactListScreen(
                            contactStore = contactStore,
                            isPicker = true,
                            onBack = { onBack() },
                            onContactPicked = { address ->
                                navigate(AppScreen.Send(prefilledAddress = address))
                            }
                        )
                        AppScreen.Settings -> SettingsScreen(
                            store = store,
                            onBack = { onBack() },
                            onLogout = logout
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}
