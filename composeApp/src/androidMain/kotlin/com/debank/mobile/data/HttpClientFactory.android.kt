package com.debank.mobile.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import okhttp3.Protocol
import java.util.concurrent.TimeUnit

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }
    engine {
        config {
            protocols(listOf(Protocol.HTTP_1_1))
            readTimeout(30, TimeUnit.SECONDS)
            connectTimeout(15, TimeUnit.SECONDS)
        }
    }
}
