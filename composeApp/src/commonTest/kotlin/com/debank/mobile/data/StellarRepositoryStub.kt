package com.debank.mobile.data

import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.AssetId
import com.debank.mobile.domain.KeyPairData
import com.debank.mobile.domain.TransactionDirection
import com.debank.mobile.domain.TransactionItem

class StellarRepositoryStub : StellarRepository {
    private var keyPairToReturn = KeyPairData("GAAAAA", "SAAAAA")
    private var fundResult = ""
    private var balanceToReturn = AccountBalance("IDR", "0.0000000")
    private var lastTrustline: AssetId? = null
    var lastPayment: PaymentData? = null
    private var paymentResult = "tx-hash-send"
    private var transactionsToReturn: List<TransactionItem> = emptyList()

    fun setKeyPair(kp: KeyPairData) { keyPairToReturn = kp }
    fun setFundResult(hash: String) { fundResult = hash }
    fun setBalance(balance: AccountBalance) { balanceToReturn = balance }
    fun setPaymentResult(hash: String) { paymentResult = hash }
    fun lastTrustlineAsset() = lastTrustline
    fun setTransactions(txns: List<TransactionItem>) { transactionsToReturn = txns }

    override suspend fun createKeyPair(): KeyPairData = keyPairToReturn

    override suspend fun fundTestnetAccount(publicKey: String): String = fundResult

    override suspend fun getAccountBalance(publicKey: String, assetCode: String): AccountBalance = balanceToReturn

    override suspend fun addTrustline(keyPair: KeyPairData, assetId: AssetId): String {
        lastTrustline = assetId
        return "tx-hash-${assetId.code}"
    }

    override suspend fun sendPayment(keyPair: KeyPairData, destination: String, amount: String, assetId: AssetId): String {
        lastPayment = PaymentData(keyPair, destination, amount, assetId)
        return paymentResult
    }

    override suspend fun getTransactions(publicKey: String): List<TransactionItem> = transactionsToReturn

    override suspend fun fundTestIdr(recipientKeyPair: KeyPairData): String = "tx-hash-idr-fund"

    data class PaymentData(
        val keyPair: KeyPairData,
        val destination: String,
        val amount: String,
        val assetId: AssetId
    )
}
