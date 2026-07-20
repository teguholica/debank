package com.debank.mobile.domain

data class Bip39Challenge(
    val index: Int,
    val correctWord: String,
    val options: List<String>
)
