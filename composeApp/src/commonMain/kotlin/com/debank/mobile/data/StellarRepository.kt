package com.debank.mobile.data

import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.KeyPairData

interface StellarRepository {
    suspend fun createKeyPair(): KeyPairData
    suspend fun fundTestnetAccount(publicKey: String): String
    suspend fun getAccountBalance(publicKey: String, assetCode: String): AccountBalance
    suspend fun addTrustline(keyPair: KeyPairData, assetCode: String, issuerPublicKey: String)
}
