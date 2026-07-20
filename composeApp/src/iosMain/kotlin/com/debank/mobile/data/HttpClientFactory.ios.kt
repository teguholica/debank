package com.debank.mobile.data

import io.ktor.client.HttpClient

actual fun createHttpClient(): HttpClient = HttpClient()
