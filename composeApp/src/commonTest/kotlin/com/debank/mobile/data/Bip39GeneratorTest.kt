package com.debank.mobile.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Bip39GeneratorTest {

    private val generator = Bip39Generator()
    private val wordlist = Bip39Wordlist.words

    @Test
    fun `generate returns 12 words`() {
        val phrase = generator.generate()
        assertEquals(12, phrase.size)
    }

    @Test
    fun `all generated words are from BIP39 wordlist`() {
        val phrase = generator.generate()
        phrase.forEach { word ->
            assertTrue(word in wordlist, "Word '$word' not in BIP39 wordlist")
        }
    }

    @Test
    fun `generate produces valid checksum`() {
        repeat(5) {
            val phrase = generator.generate()
            assertTrue(isValidChecksum(phrase), "Invalid BIP39 checksum")
        }
    }

    @Test
    fun `generate produces different phrases each time`() {
        val phrases = (1..5).map { generator.generate() }
        val distinct = phrases.distinct()
        assertTrue(distinct.size > 1, "All phrases identical - randomness broken")
    }

    @Test
    fun `createChallenges returns correct number of challenges`() {
        val phrase = generator.generate()
        val challenges = generator.createChallenges(phrase, count = 3)
        assertEquals(3, challenges.size)
    }

    @Test
    fun `createChallenges uses unique indices`() {
        val phrase = generator.generate()
        val challenges = generator.createChallenges(phrase, count = 3)
        val indices = challenges.map { it.index }
        assertEquals(indices.toSet().size, indices.size)
    }

    @Test
    fun `verify passes for correct answers`() {
        val phrase = generator.generate()
        val challenges = generator.createChallenges(phrase, count = 3)
        val answers = challenges.associate { it.index to it.correctWord }
            assertTrue(Bip39Generator.verify(challenges, answers))
    }

    @Test
    fun `verify fails for incorrect answers`() {
        val phrase = generator.generate()
        val challenges = generator.createChallenges(phrase, count = 1)
        val wrongWord = wordlist.first { it != challenges[0].correctWord }
        val wrongAnswers = mapOf(challenges[0].index to wrongWord)
            assertFalse(Bip39Generator.verify(challenges, wrongAnswers))
    }

    @Test
    fun `challenge options contain correct word`() {
        val phrase = generator.generate()
        val challenges = generator.createChallenges(phrase, count = 1)
        assertTrue(challenges[0].correctWord in challenges[0].options)
    }

    @Test
    fun `challenge options have 4 choices`() {
        val phrase = generator.generate()
        val challenges = generator.createChallenges(phrase, count = 1)
        assertEquals(4, challenges[0].options.size)
    }

    private fun isValidChecksum(phrase: List<String>): Boolean {
        val indexes = phrase.map { wordlist.indexOf(it) }
        require(indexes.all { it >= 0 })

        val entropyBytes = ByteArray(16)
        var bitOffset = 0

        for (idx in indexes.take(11)) {
            for (j in 10 downTo 0) {
                if (bitOffset >= 128) break
                val bit = (idx shr j) and 1
                val byteIdx = bitOffset / 8
                val bitInByte = 7 - (bitOffset % 8)
                entropyBytes[byteIdx] = (entropyBytes[byteIdx].toInt() or (bit shl bitInByte)).toByte()
                bitOffset++
            }
        }

        val lastWordBits = indexes.last() shr 4
        for (j in 6 downTo 0) {
            if (bitOffset >= 128) break
            val bit = (lastWordBits shr j) and 1
            val byteIdx = bitOffset / 8
            val bitInByte = 7 - (bitOffset % 8)
            entropyBytes[byteIdx] = (entropyBytes[byteIdx].toInt() or (bit shl bitInByte)).toByte()
            bitOffset++
        }

        val hash = CryptoUtils.sha256(entropyBytes)
        val expectedChecksum = (hash[0].toInt() and 0xFF) ushr 4
        val actualChecksum = indexes.last() and 0xF

        return expectedChecksum == actualChecksum
    }
}
