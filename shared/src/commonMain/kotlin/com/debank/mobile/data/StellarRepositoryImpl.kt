package com.debank.mobile.data

import com.soneso.stellar.sdk.KeyPair
import com.soneso.stellar.sdk.Network
import com.soneso.stellar.sdk.TransactionBuilder
import com.soneso.stellar.sdk.Account
import com.soneso.stellar.sdk.horizon.HorizonServer
import com.soneso.stellar.sdk.horizon.exceptions.BadRequestException
import com.soneso.stellar.sdk.horizon.requests.RequestBuilder
import com.soneso.stellar.sdk.horizon.responses.operations.PaymentOperationResponse
import com.soneso.stellar.sdk.AssetTypeCreditAlphaNum4
import com.soneso.stellar.sdk.ChangeTrustOperation
import com.soneso.stellar.sdk.PaymentOperation
import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.AssetId
import com.debank.mobile.domain.KeyPairData
import com.debank.mobile.domain.TransactionDirection
import com.debank.mobile.domain.TransactionItem
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StellarRepositoryImpl(
    private val horizonUrl: String = StellarConfig.HORIZON_TESTNET,
    private val httpClient: HttpClient = createHttpClient()
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
        val response = httpClient.get(StellarConfig.FRIENDBOT_URL) {
            url {
                parameters.append("addr", publicKey)
            }
        }
        return response.bodyAsText()
    }

    override suspend fun getAccountBalance(publicKey: String, assetCode: String): AccountBalance = withContext(Dispatchers.Default) {
        try {
            val account = server.accounts().account(publicKey)
            val balance = account.balances.find { it.assetCode == assetCode }
            AccountBalance(
                assetCode = assetCode,
                balance = balance?.balance ?: "0.0000000"
            )
        } catch (_: BadRequestException) {
            AccountBalance(assetCode = assetCode, balance = "0.0000000")
        }
    }

    override suspend fun sendPayment(keyPair: KeyPairData, destination: String, amount: String, assetId: AssetId): String = withContext(Dispatchers.Default) {
        val kp = KeyPair.fromSecretSeed(keyPair.secretSeed.toCharArray())
        val accountResponse = server.accounts().account(keyPair.publicKey)
        val sourceAccount = Account(kp, accountResponse.sequenceNumber)

        val transaction = TransactionBuilder(sourceAccount, Network.TESTNET)
            .setBaseFee(100)
            .addOperation(
                PaymentOperation(destination, AssetTypeCreditAlphaNum4(assetId.code, assetId.issuerPublicKey), amount)
            )
            .build()

        transaction.sign(kp)
        val response = server.submitTransaction(transaction.toEnvelopeXdrBase64())
        response.hash
    }

    override suspend fun addTrustline(keyPair: KeyPairData, assetId: AssetId): String = withContext(Dispatchers.Default) {
        val kp = KeyPair.fromSecretSeed(keyPair.secretSeed.toCharArray())
        val accountResponse = server.accounts().account(keyPair.publicKey)
        val sourceAccount = Account(kp, accountResponse.sequenceNumber)

        val transaction = TransactionBuilder(sourceAccount, Network.TESTNET)
            .setBaseFee(100)
            .addOperation(
                ChangeTrustOperation(AssetTypeCreditAlphaNum4(assetId.code, assetId.issuerPublicKey))
            )
            .build()

        transaction.sign(kp)
        val response = server.submitTransaction(transaction.toEnvelopeXdrBase64())
        response.hash
    }

    override suspend fun fundTestIdr(recipientKeyPair: KeyPairData): String = withContext(Dispatchers.Default) {
        val assetId = StellarConfig.idrAssetId()
        try {
            addTrustline(recipientKeyPair, assetId)
        } catch (_: Exception) {
            // trustline may already exist
        }
        val issuerKeyPair = KeyPairData(
            publicKey = StellarConfig.IDR_ISSUER_PUBLIC_KEY,
            secretSeed = StellarConfig.IDR_ISSUER_SECRET_SEED
        )
        sendPayment(issuerKeyPair, recipientKeyPair.publicKey, "1000.0000000", assetId)
    }

    override suspend fun getTransactions(publicKey: String): List<TransactionItem> = withContext(Dispatchers.Default) {
        val page = server.payments()
            .forAccount(publicKey)
            .order(RequestBuilder.Order.DESC)
            .limit(50)
            .execute()

        page.records
            .filterIsInstance<PaymentOperationResponse>()
            .filter { it.assetCode == StellarConfig.IDR_ASSET_CODE && it.assetIssuer == StellarConfig.IDR_ISSUER_PUBLIC_KEY }
            .map { payment ->
                val isOutbound = payment.from == publicKey
                TransactionItem(
                    id = payment.transactionHash.ifEmpty { payment.id },
                    direction = if (isOutbound) TransactionDirection.Outbound else TransactionDirection.Inbound,
                    amount = payment.amount,
                    counterparty = if (isOutbound) payment.to else payment.from,
                    timestamp = parseHorizonDate(payment.createdAt),
                )
            }
    }

    private fun parseHorizonDate(isoDate: String): Long {
        val cleaned = isoDate.substringBefore(".").removeSuffix("Z")
        val parts = cleaned.split("T")
        if (parts.size != 2) return 0L
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        if (dateParts.size != 3 || timeParts.size < 2) return 0L
        val year = dateParts[0].toLongOrNull() ?: return 0L
        val month = dateParts[1].toLongOrNull() ?: return 0L
        val day = dateParts[2].toLongOrNull() ?: return 0L
        val hour = timeParts[0].toLongOrNull() ?: return 0L
        val minute = timeParts[1].toLongOrNull() ?: return 0L
        val second = timeParts.getOrNull(2)?.toLongOrNull() ?: 0L
        return daysFromEpoch(year, month, day) * 86400000L +
            hour * 3600000L + minute * 60000L + second * 1000L
    }

    private fun daysFromEpoch(year: Long, month: Long, day: Long): Long {
        var y = year
        var m = month
        if (m <= 2) { y--; m += 12 }
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = y - era * 400
        val doy = (153L * (m - 3) + 2) / 5 + day - 1
        val doe = yoe * 365L + yoe / 4 - yoe / 100 + doy
        return (era * 146097L + doe - 719468L)
    }
}
