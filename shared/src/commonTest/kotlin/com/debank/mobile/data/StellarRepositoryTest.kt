package com.debank.mobile.data

import com.debank.mobile.domain.AccountBalance
import com.debank.mobile.domain.AssetId
import com.debank.mobile.domain.KeyPairData
import com.debank.mobile.domain.TransactionDirection
import com.debank.mobile.domain.TransactionItem
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
    fun `getTransactions returns empty list when no transactions`() = runBlocking {
        val stub = StellarRepositoryStub()
        stub.setTransactions(emptyList())

        val result = stub.getTransactions("GABC123")

        assertEquals(emptyList(), result)
    }

    @Test
    fun `getTransactions returns list of transactions`() = runBlocking {
        val stub = StellarRepositoryStub()
        val txns = listOf(
            TransactionItem("tx1", TransactionDirection.Inbound, "100.0000000", "GCOUNTER", 1000L),
            TransactionItem("tx2", TransactionDirection.Outbound, "50.0000000", "GCOUNTER2", 2000L)
        )
        stub.setTransactions(txns)

        val result = stub.getTransactions("GABC123")

        assertEquals(2, result.size)
        assertEquals("tx1", result[0].id)
        assertEquals(TransactionDirection.Inbound, result[0].direction)
        assertEquals("100.0000000", result[0].amount)
        assertEquals(TransactionDirection.Outbound, result[1].direction)
    }

    @Test
    fun `getTransactions preserves transaction order`() = runBlocking {
        val stub = StellarRepositoryStub()
        val txns = listOf(
            TransactionItem("tx1", TransactionDirection.Inbound, "10.0000000", "GX", 100L),
            TransactionItem("tx2", TransactionDirection.Outbound, "20.0000000", "GY", 200L),
            TransactionItem("tx3", TransactionDirection.Inbound, "30.0000000", "GZ", 300L)
        )
        stub.setTransactions(txns)

        val result = stub.getTransactions("GABC123")

        assertEquals("tx1", result[0].id)
        assertEquals("tx2", result[1].id)
        assertEquals("tx3", result[2].id)
    }

    @Test
    fun `toString hides secret seed`() {
        val kp = KeyPairData("GABC", "SECRET123")
        val str = kp.toString()

        assertTrue("***" in str)
        assertTrue("SECRET123" !in str)
    }
}
