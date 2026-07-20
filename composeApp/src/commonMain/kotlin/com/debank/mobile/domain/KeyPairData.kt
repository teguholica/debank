package com.debank.mobile.domain

data class KeyPairData(
    val publicKey: String,
    val secretSeed: String
) {
    override fun toString(): String = "KeyPairData(publicKey=$publicKey, secretSeed=***)"
}
