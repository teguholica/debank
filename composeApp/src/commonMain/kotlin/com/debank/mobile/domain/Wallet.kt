package com.debank.mobile.domain

data class KeyPairData(
    val publicKey: String,
    val secretSeed: String
)

data class AccountBalance(
    val assetCode: String,
    val balance: String
)
