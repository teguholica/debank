# DeBank

Dompet self-custodial **Rupiah digital** di Stellar Testnet, dibangun dengan Kotlin Multiplatform + Compose Multiplatform untuk Android & iOS.

> **Status:** MVP / eksperimental. Hanya testnet. Bukan untuk mainnet atau penggunaan riil.

---

## Fitur

| Fitur | Status |
|---|---|
| Generate wallet — seed phrase BIP39 12 kata | ✅ |
| Konfirmasi seed phrase (kuis 4 pilihan) | ✅ |
| PIN 4-6 digit untuk akses harian | ✅ |
| Dashboard — saldo IDR + tombol aksi | ✅ |
| Kirim IDR ke alamat Stellar lain | ✅ |
| Scan QR code alamat tujuan | ✅ |
| Tampilkan QR code alamat sendiri | ✅ |
| Riwayat transaksi inbound/outbound | ✅ |
| Buku kontak (nama + alamat) | ✅ |
| Pilih kontak saat kirim | ✅ |
| Verifikasi PIN sebelum transaksi keluar | ✅ |
| Pull-to-refresh saldo & riwayat | ✅ |
| Lihat seed phrase di pengaturan | ✅ |
| Ganti PIN | ✅ |
| Trustline IDR otomatis | ✅ |
| Faucet IDR test token (di dashboard) | ✅ |

---

## Tech Stack

- **Bahasa:** Kotlin
- **UI:** Compose Multiplatform
- **Platform:** Android (minSdk 24), iOS (15+)
- **Blockchain:** Stellar Testnet (`horizon-testnet.stellar.org`)
- **SDK:** `com.soneso.stellar:stellar-sdk:1.9.0`
- **Asset:** Issued asset `IDR` oleh dedicated testnet issuer
- **Storage:** SharedPreferences (Android) / NSUserDefaults (iOS)
- **Auth:** PIN (SHA-256 + salt) + BIP39 seed phrase
- **QR:** expect/actual — CameraX (Android), AVFoundation (iOS)

---

## Cara Build & Run

### Prasyarat

- Android Studio Ladybug (atau lebih baru)
- JDK 17
- Xcode 16+ (untuk iOS)
- CocoaPods (untuk iOS)

### Android

```bash
./gradlew :composeApp:assembleDebug
```

Atau buka di Android Studio, jalankan dari IDE.

### iOS

```bash
./gradlew :composeApp:iosSimulatorArm64MainBinaries
```

Buka `iosApp/` di Xcode, build & run.

### Test

```bash
./gradlew :composeApp:allTests
```

---

## Script Faucet

Script `scripts/faucet.mjs` digunakan untuk setup issuer testnet dan mengirim IDR test token ke wallet.

```bash
cd scripts
npm install
```

### Setup Issuer Baru

```bash
node faucet.mjs setup
```

Membuat keypair issuer baru, funding via Friendbot, dan menyimpan ke `.issuer.json`. Output berupa konstanta yang perlu dicopy ke `StellarConfig.kt`:

```
const val IDR_ISSUER_PUBLIC_KEY = "G..."
const val IDR_ISSUER_SECRET_SEED = "S..."
```

### Kirim IDR Test ke Wallet

Pastikan wallet tujuan sudah memiliki trustline IDR (buka halaman Kirim di aplikasi — trustline dibuat otomatis).

```bash
node faucet.mjs fund <alamat-stellar> [jumlah]
```

Contoh:

```bash
node faucet.mjs fund GCYOBL6JFP3EY24GO3P57GYCHGHQ6T56I7KVJ6T4XN3PRDHQ6JEV7SXK 5000
```

Jumlah default: `1000.0000000`.

> **Catatan:** Wallet yang baru dibuat otomatis di-funding XLM via Friendbot saat onboarding. Gunakan tombol "Dapatkan IDR Test" di dashboard untuk funding IDR langsung dari aplikasi.

---

## Arsitektur

```
composeApp/
├── src/commonMain/kotlin/com/debank/mobile/
│   ├── App.kt                  # Root composable + navigasi
│   ├── domain/                 # Model & logic murni (tanpa dependensi platform)
│   ├── data/                   # Repository, config, utility
│   └── ui/                     # Layar per fitur (onboarding, dashboard, send, dll)
├── src/androidMain/            # Platform Android (SharedPreferences, CameraX)
└── src/iosMain/                # Platform iOS (NSUserDefaults, AVFoundation)
```

Keputusan arsitektur dan glossary domain ada di `CONTEXT.md` dan `docs/adr/`.

---

## Lisensi

Hak cipta © 2026 Teguh Olica. Lihat file LICENSE untuk detail.

---

## Catatan

- Aplikasi 100% client-side — tidak ada backend server.
- Seed phrase dan secret seed disimpan dalam platform key-value store. Ini adalah kompromi MVP — untuk production gunakan secure enclave / keystore.
- Dibangun untuk eksperimen Stellar issued asset IDR. Bukan produk finansial.
