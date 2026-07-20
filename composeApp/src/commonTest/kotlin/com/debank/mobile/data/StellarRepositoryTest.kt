package com.debank.mobile.data

import com.debank.mobile.domain.AccountBalance
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
    fun `addTrustline marks trustline as added`() = runBlocking {
        val stub = StellarRepositoryStub()
        val keyPair = KeyPairData("GABC123", "SABC123")

        stub.addTrustline(keyPair, "IDR", "GISSUER")

        assertTrue(stub.wasTrustlineAdded())
    }
}
