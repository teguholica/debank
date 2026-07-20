package com.debank.mobile.domain

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey

@Serializable
data object OnboardingRoute : Route

@Serializable
data object PinVerifyRoute : Route

@Serializable
data object DashboardRoute : Route

@Serializable
data class SendRoute(val prefilledAddress: String = "") : Route

@Serializable
data object ReceiveRoute : Route

@Serializable
data object HistoryRoute : Route

@Serializable
data object ContactListRoute : Route

@Serializable
data object ContactPickerRoute : Route

@Serializable
data object SettingsRoute : Route
