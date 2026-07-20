package com.debank.mobile.domain

sealed class AppScreen {
    data object Onboarding : AppScreen()
    data object PinVerify : AppScreen()
    data object Dashboard : AppScreen()
}
