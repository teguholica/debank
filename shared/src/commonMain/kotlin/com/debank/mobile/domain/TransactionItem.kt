package com.debank.mobile.domain

data class TransactionItem(
    val id: String,
    val direction: TransactionDirection,
    val amount: String,
    val counterparty: String,
    val timestamp: Long,
)

enum class TransactionDirection { Inbound, Outbound }
