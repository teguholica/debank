# DeBank — Glossary

## Domain

| Term | Definisi |
|---|---|
| **Wallet** | Self-custodial wallet untuk Stellar testnet. Seed phrase + PIN sebagai akses. |
| **IDR** | Issued asset di Stellar testnet, mewakili Rupiah. Diterbitkan oleh issuer account testnet. |
| **Seed Phrase** | 12 kata BIP-39 untuk derivasi keypair Stellar (path `m/44'/148'/0'`). |
| **PIN** | 4-6 digit, hash SHA-256 dengan salt, untuk otorisasi transaksi keluar. |
| **Trustline** | On-chain authorization untuk hold/kirim issued asset. Auto-establish via ChangeTrustOperation. |
| **Friendbot** | Layanan Stellar testnet untuk funding XLM gratis. |
| **Faucet** | Script Node.js untuk setup issuer dan kirim IDR test ke wallet. |

## Teknis

| Term | Definisi |
|---|---|
| **KMP** | Kotlin Multiplatform — shared code Android + iOS. |
| **Compose Multiplatform** | Shared UI framework. |
| **Stellar SDK** | `com.soneso.stellar:stellar-sdk:1.9.0` untuk interaksi Horizon. |
| **Horizon** | Stellar network API endpoint (`https://horizon-testnet.stellar.org`). |
| **StellarConfig** | Object singleton berisi konstanta network, asset IDR, issuer keys. |
| **StellarRepository** | Interface untuk semua operasi Stellar (balance, payment, fund, trustline). |
