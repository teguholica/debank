package com.debank.mobile.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.debank.mobile.data.Bip39Generator
import com.debank.mobile.data.KeyValueStore
import com.debank.mobile.data.StellarRepository
import com.debank.mobile.domain.Bip39Challenge
import kotlinx.coroutines.launch

private sealed class OnboardingStep {
    data object GenerateSeed : OnboardingStep()
    data class ConfirmSeed(val challenges: List<Bip39Challenge>) : OnboardingStep()
    data object PinSetup : OnboardingStep()
}

@Composable
fun OnboardingFlow(
    store: KeyValueStore,
    repository: StellarRepository,
    onComplete: () -> Unit
) {
    val generator = remember { Bip39Generator() }
    var seedPhrase by remember { mutableStateOf(generator.generate()) }
    var step by remember { mutableStateOf<OnboardingStep>(OnboardingStep.GenerateSeed) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val stepNumber = when (step) {
        is OnboardingStep.GenerateSeed -> 1
        is OnboardingStep.ConfirmSeed -> 2
        is OnboardingStep.PinSetup -> 3
    }

    when (val currentStep = step) {
        OnboardingStep.GenerateSeed -> GenerateSeedScreen(
            words = seedPhrase,
            currentStep = stepNumber,
            totalSteps = 3,
            onNext = {
                val challenges = generator.createChallenges(seedPhrase)
                step = OnboardingStep.ConfirmSeed(challenges)
            }
        )
        is OnboardingStep.ConfirmSeed -> ConfirmSeedScreen(
            challenges = currentStep.challenges,
            currentStep = stepNumber,
            totalSteps = 3,
            onVerified = { step = OnboardingStep.PinSetup }
        )
        OnboardingStep.PinSetup -> PinSetupScreen(
            store = store,
            currentStep = stepNumber,
            totalSteps = 3,
            errorMessage = errorMessage,
            onComplete = {
                scope.launch {
                    errorMessage = null
                    try {
                        val kp = repository.createKeyPair()
                        store.setString(KeyValueStore.PUBLIC_KEY_KEY, kp.publicKey)
                        store.setString(KeyValueStore.SECRET_SEED_KEY, kp.secretSeed)
                        store.setString(KeyValueStore.SEED_PHRASE_KEY, seedPhrase.joinToString(" "))
                        repository.fundTestnetAccount(kp.publicKey)
                        onComplete()
                    } catch (e: Exception) {
                        errorMessage = "Gagal terhubung ke server. Periksa koneksi internet."
                    }
                }
            }
        )
    }
}
