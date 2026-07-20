package com.debank.mobile.data

import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.AssetId
import com.debank.mobile.domain.KeyPairData
import com.debank.mobile.domain.TransactionItem

interface StellarRepository {
    suspend fun createKeyPair(): KeyPairData
    suspend fun fundTestnetAccount(publicKey: String): String
    suspend fun getAccountBalance(publicKey: String, assetCode: String): AccountBalance
    suspend fun addTrustline(keyPair: KeyPairData, assetId: AssetId): String
    suspend fun sendPayment(keyPair: KeyPairData, destination: String, amount: String, assetId: AssetId): String
    suspend fun getTransactions(publicKey: String): List<TransactionItem>
}
