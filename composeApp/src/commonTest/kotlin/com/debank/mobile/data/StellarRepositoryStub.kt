package com.debank.mobile.data

import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.AssetId
import com.debank.mobile.domain.KeyPairData

class StellarRepositoryStub : StellarRepository {
    private var keyPairToReturn = KeyPairData("GAAAAA", "SAAAAA")
    private var fundResult = ""
    private var balanceToReturn = AccountBalance("IDR", "0.0000000")
    private var lastTrustline: AssetId? = null

    fun setKeyPair(kp: KeyPairData) { keyPairToReturn = kp }
    fun setFundResult(hash: String) { fundResult = hash }
    fun setBalance(balance: AccountBalance) { balanceToReturn = balance }
    fun lastTrustlineAsset() = lastTrustline

    override suspend fun createKeyPair(): KeyPairData = keyPairToReturn

    override suspend fun fundTestnetAccount(publicKey: String): String = fundResult

    override suspend fun getAccountBalance(publicKey: String, assetCode: String): AccountBalance = balanceToReturn

    override suspend fun addTrustline(keyPair: KeyPairData, assetId: AssetId): String {
        lastTrustline = assetId
        return "tx-hash-${assetId.code}"
    }
}
