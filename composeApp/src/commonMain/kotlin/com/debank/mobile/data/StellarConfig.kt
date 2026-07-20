package com.debank.mobile.data

import com.debank.mobile.domain.AssetId

object StellarConfig {
    const val HORIZON_TESTNET = "https://horizon-testnet.stellar.org"
    const val FRIENDBOT_URL = "https://friendbot.stellar.org"
    const val IDR_ASSET_CODE = "IDR"
    const val IDR_ISSUER_PUBLIC_KEY = "GA3ZFHKXPUAJZBCMJJXCJU7O6DAQHISVAC5MQP2DDOA25OWTGU6HHQKN"
    const val IDR_ISSUER_SECRET_SEED = "SBWGJ7A3VHSD7RI6PDYEAF55KKGLBR3WQHFFXY7TY3RAJJQEMBFWMRMW"

    fun idrAssetId() = AssetId(IDR_ASSET_CODE, IDR_ISSUER_PUBLIC_KEY)
}
