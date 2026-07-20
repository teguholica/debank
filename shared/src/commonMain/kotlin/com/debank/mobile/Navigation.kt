package com.debank.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavKey
import com.debank.mobile.data.ContactStore
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.StellarRepository
import com.debank.mobile.domain.ContactListRoute
import com.debank.mobile.domain.ContactPickerRoute
import com.debank.mobile.domain.DashboardRoute
import com.debank.mobile.domain.HistoryRoute
import com.debank.mobile.domain.ReceiveRoute
import com.debank.mobile.domain.SendRoute
import com.debank.mobile.domain.SettingsRoute
import com.debank.mobile.ui.components.DeBankScaffold
import com.debank.mobile.ui.contact.ContactListScreen
import com.debank.mobile.ui.dashboard.DashboardScreen
import com.debank.mobile.ui.history.HistoryScreen
import com.debank.mobile.ui.receive.ReceiveScreen
import com.debank.mobile.ui.send.SendFlow
import com.debank.mobile.ui.settings.SettingsScreen

@Composable
fun NavigationApp(
    backStack: SnapshotStateList<Any>,
    store: KeyValueStore,
    repository: StellarRepository,
    contactStore: ContactStore,
    onLogout: () -> Unit
) {
    val currentRoute = (backStack.lastOrNull() ?: DashboardRoute) as NavKey
    val showBottomNav = currentRoute is DashboardRoute || currentRoute is SendRoute ||
            currentRoute is HistoryRoute || currentRoute is SettingsRoute

    val entryProvider: (Any) -> NavEntry<Any> = { key ->
        when (key) {
            is DashboardRoute -> NavEntry(key) {
                DashboardScreen(
                    repository = repository,
                    publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                    secretSeed = store.getString(KeyValueStore.SECRET_SEED_KEY) ?: "",
                    onSend = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                        backStack.add(SendRoute())
                    },
                    onReceive = { backStack.add(ReceiveRoute) },
                    onHistory = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                        backStack.add(HistoryRoute)
                    },
                    onSettings = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                        backStack.add(SettingsRoute)
                    }
                )
            }
            is SendRoute -> NavEntry(key) {
                SendFlow(
                    store = store,
                    repository = repository,
                    publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                    secretSeed = store.getString(KeyValueStore.SECRET_SEED_KEY) ?: "",
                    onBack = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                    },
                    onSuccess = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                    },
                    onPickContact = { backStack.add(ContactPickerRoute) },
                    prefilledAddress = key.prefilledAddress
                )
            }
            is ReceiveRoute -> NavEntry(key) {
                ReceiveScreen(
                    publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                    onScanResult = { address ->
                        backStack.clear()
                        backStack.add(DashboardRoute)
                        backStack.add(SendRoute(prefilledAddress = address))
                    },
                    onBack = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                    }
                )
            }
            is HistoryRoute -> NavEntry(key) {
                HistoryScreen(
                    repository = repository,
                    publicKey = store.getString(KeyValueStore.PUBLIC_KEY_KEY) ?: "",
                    onBack = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                    }
                )
            }
            is ContactListRoute -> NavEntry(key) {
                ContactListScreen(
                    contactStore = contactStore,
                    isPicker = false,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            is ContactPickerRoute -> NavEntry(key) {
                ContactListScreen(
                    contactStore = contactStore,
                    isPicker = true,
                    onBack = { backStack.removeLastOrNull() },
                    onContactPicked = { address ->
                        backStack.removeLastOrNull()
                        backStack.add(SendRoute(prefilledAddress = address))
                    }
                )
            }
            is SettingsRoute -> NavEntry(key) {
                SettingsScreen(
                    store = store,
                    onBack = {
                        backStack.clear()
                        backStack.add(DashboardRoute)
                    },
                    onLogout = onLogout
                )
            }
            else -> NavEntry(key) {}
        }
    }

    DeBankScaffold(
        currentRoute = currentRoute,
        onNavigate = { route ->
            backStack.clear()
            backStack.add(DashboardRoute)
            backStack.add(route)
        },
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else {
                backStack.clear()
                backStack.add(DashboardRoute)
            }
        },
        showBottomNav = showBottomNav
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                } else {
                    backStack.clear()
                    backStack.add(DashboardRoute)
                }
            },
            entryProvider = entryProvider
        )
    }
}
