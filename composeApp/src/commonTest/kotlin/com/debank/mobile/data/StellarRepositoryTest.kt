package com.debank.mobile.data

import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.AssetId
import com.debank.mobile.domain.KeyPairData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class StellarRepositoryTest {
    @Test
    fun `createKeyPair returns keypair data`() = runBlocking {
        val stub = StellarRepositoryStub()
        val expected = KeyPairData("GABC123", "SABC123")
        stub.setKeyPair(expected)

        val result = stub.createKeyPair()

        assertEquals(expected, result)
    }

    @Test
    fun `fundTestnetAccount returns transaction hash`() = runBlocking {
        val stub = StellarRepositoryStub()
        stub.setFundResult("abc123hash")

        val result = stub.fundTestnetAccount("GABC123")

        assertEquals("abc123hash", result)
    }

    @Test
    fun `getAccountBalance returns balance for asset`() = runBlocking {
        val stub = StellarRepositoryStub()
        val expected = AccountBalance("IDR", "100.5000000")
        stub.setBalance(expected)

        val result = stub.getAccountBalance("GABC123", "IDR")

        assertEquals(expected, result)
    }

    @Test
    fun `addTrustline returns transaction hash`() = runBlocking {
        val stub = StellarRepositoryStub()
        val keyPair = KeyPairData("GABC123", "SABC123")
        val assetId = AssetId("IDR", "GISSUER")

        val result = stub.addTrustline(keyPair, assetId)

        assertEquals("tx-hash-IDR", result)
    }

    @Test
    fun `addTrustline records the asset id`() = runBlocking {
        val stub = StellarRepositoryStub()
        val keyPair = KeyPairData("GABC123", "SABC123")
        val assetId = AssetId("IDR", "GISSUER")

        stub.addTrustline(keyPair, assetId)

        assertEquals(assetId, stub.lastTrustlineAsset())
    }

    @Test
    fun `sendPayment returns transaction hash`() = runBlocking {
        val stub = StellarRepositoryStub()
        val keyPair = KeyPairData("GABC123", "SABC123")
        val assetId = AssetId("IDR", "GISSUER")

        val result = stub.sendPayment(keyPair, "GDEST", "50.5000000", assetId)

        assertEquals("tx-hash-send", result)
    }

    @Test
    fun `sendPayment records payment params`() = runBlocking {
        val stub = StellarRepositoryStub()
        val keyPair = KeyPairData("GABC123", "SABC123")
        val assetId = AssetId("IDR", "GISSUER")

        stub.sendPayment(keyPair, "GDEST", "50.5000000", assetId)

        val payment = stub.lastPayment
        assertEquals("GDEST", payment?.destination)
        assertEquals("50.5000000", payment?.amount)
        assertEquals(assetId, payment?.assetId)
    }

    @Test
    fun `sendPayment uses correct keypair`() = runBlocking {
        val stub = StellarRepositoryStub()
        val keyPair = KeyPairData("GABC123", "SABC123")
        val assetId = AssetId("IDR", "GISSUER")

        stub.sendPayment(keyPair, "GDEST", "50.5000000", assetId)

        assertEquals(keyPair, stub.lastPayment?.keyPair)
    }

    @Test
    fun `toString hides secret seed`() {
        val kp = KeyPairData("GABC", "SECRET123")
        val str = kp.toString()

        assertTrue("***" in str)
        assertTrue("SECRET123" !in str)
    }
}
