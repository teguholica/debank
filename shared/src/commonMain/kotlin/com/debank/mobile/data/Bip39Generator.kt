package com.debank.mobile.data

import com.debank.mobile.domain.Bip39Challenge

class Bip39Generator {

    fun generate(): List<String> {
        val entropy = CryptoUtils.secureRandomBytes(16)
        val hash = CryptoUtils.sha256(entropy)
        val checksumNibble = (hash[0].toInt() and 0xFF) ushr 4

        val bits = IntArray(132)
        var pos = 0
        for (b in entropy) {
            for (j in 7 downTo 0) {
                bits[pos++] = (b.toInt() shr j) and 1
            }
        }
        for (j in 3 downTo 0) {
            bits[pos++] = (checksumNibble shr j) and 1
        }

        return bits.toList().chunked(11).map { chunk ->
            var index = 0
            for (b in chunk) index = (index shl 1) or b
            Bip39Wordlist.words[index]
        }
    }

    fun createChallenges(phrase: List<String>, count: Int = 3): List<Bip39Challenge> {
        val rng = kotlin.random.Random
        val indices = phrase.indices.shuffled(rng).take(count)
        return indices.map { idx ->
            val correct = phrase[idx]
            val wrongOptions = Bip39Wordlist.words
                .filter { it != correct }
                .shuffled(rng)
                .take(3)
            Bip39Challenge(idx, correct, (wrongOptions + correct).shuffled(rng))
        }
    }

    companion object {
        fun verify(challenges: List<Bip39Challenge>, answers: Map<Int, String>): Boolean {
            return challenges.all { answers[it.index] == it.correctWord }
        }
    }
}
