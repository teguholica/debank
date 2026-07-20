package com.debank.mobile.data

import com.soneso.stellar.sdk.KeyPair
import com.soneso.stellar.sdk.Network
import com.soneso.stellar.sdk.TransactionBuilder
import com.soneso.stellar.sdk.Account
import com.soneso.stellar.sdk.horizon.HorizonServer
import com.soneso.stellar.sdk.AssetTypeCreditAlphaNum4
import com.soneso.stellar.sdk.ChangeTrustOperation
import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.KeyPairData
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StellarRepositoryImpl(
    private val horizonUrl: String = "https://horizon-testnet.stellar.org",
    private val httpClient: HttpClient = HttpClient()
) : StellarRepository {

    private val server = HorizonServer(horizonUrl)

    override suspend fun createKeyPair(): KeyPairData = withContext(Dispatchers.Default) {
        val kp = KeyPair.random()
        KeyPairData(
            publicKey = kp.getAccountId(),
            secretSeed = kp.getSecretSeed()?.concatToString() ?: ""
        )
    }

    override suspend fun fundTestnetAccount(publicKey: String): String {
        val response = httpClient.get("https://friendbot.stellar.org") {
            url {
                parameters.append("addr", publicKey)
            }
        }
        return response.bodyAsText()
    }

    override suspend fun getAccountBalance(publicKey: String, assetCode: String): AccountBalance = withContext(Dispatchers.Default) {
        val account = server.accounts().account(publicKey)
        val balance = account.balances.find { it.assetCode == assetCode }
        AccountBalance(
            assetCode = assetCode,
            balance = balance?.balance ?: "0.0000000"
        )
    }

    override suspend fun addTrustline(keyPair: KeyPairData, assetCode: String, issuerPublicKey: String) = withContext(Dispatchers.Default) {
        val kp = KeyPair.fromSecretSeed(keyPair.secretSeed.toCharArray())
        val accountResponse = server.accounts().account(keyPair.publicKey)
        val sourceAccount = Account(kp, accountResponse.sequenceNumber)

        val transaction = TransactionBuilder(sourceAccount, Network.TESTNET)
            .setBaseFee(100)
            .addOperation(
                ChangeTrustOperation(AssetTypeCreditAlphaNum4(assetCode, issuerPublicKey))
            )
            .build()

        transaction.sign(kp)
        server.submitTransaction(transaction.toEnvelopeXdrBase64())
        Unit
    }
}
