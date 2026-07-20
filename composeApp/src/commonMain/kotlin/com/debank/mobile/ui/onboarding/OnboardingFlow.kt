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
    val scope = rememberCoroutineScope()

    when (val currentStep = step) {
        OnboardingStep.GenerateSeed -> GenerateSeedScreen(
            words = seedPhrase,
            onNext = {
                val challenges = generator.createChallenges(seedPhrase)
                step = OnboardingStep.ConfirmSeed(challenges)
            }
        )
        is OnboardingStep.ConfirmSeed -> ConfirmSeedScreen(
            challenges = currentStep.challenges,
            onVerified = { step = OnboardingStep.PinSetup }
        )
        OnboardingStep.PinSetup -> PinSetupScreen(
            store = store,
            onComplete = {
                scope.launch {
                    val kp = repository.createKeyPair()
                    repository.fundTestnetAccount(kp.publicKey)
                    store.setString(KeyValueStore.PUBLIC_KEY_KEY, kp.publicKey)
                    store.setString(KeyValueStore.SECRET_SEED_KEY, kp.secretSeed)
                    onComplete()
                }
            }
        )
    }
}
