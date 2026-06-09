# Simplify Ops Mobile

Kotlin Multiplatform (Android and iOS) client for the Simplify Biz operations workflow.
Lets clients submit tasks, see workflow progress, and read or post Shared Notes —
all backed by the `smplfy-ops-mobile` WordPress plugin running on `ops.simplifybiz.com`.

## Status

This is **v0.8.2** — third pass of audit response. Both High-severity findings
from the v0.7.6 audit now have implementations on both Android and iOS.

- **Android**: builds and runs, encryption verified end-to-end.
- **iOS**: source code written and committed but UNVERIFIED — built without an
  iOS compile-test environment. Build with `./gradlew :composeApp:linkReleaseFrameworkIosArm64`
  to verify before any TestFlight release. The Keychain bridging uses
  well-documented Kotlin/Native CFType interop but Security.framework cinterop
  is famously fiddly; if compile errors appear, file them with the audit trail.

## Architecture

```
Mobile (Compose Multiplatform)
  └─ JWT auth → /wp-json/jwt-auth/v1/token
  └─ Token attached as Bearer on every request
  └─ Calls /wp-json/simplify-ops/v1/* on ops
```

| Layer | What lives here |
|---|---|
| `presentation/` | Compose screens, ViewModels, navigation |
| `data/` | Repositories, models, network, storage |
| `data/SecureStorage` | Encrypted KV store: token + PII (Android Keystore on Android, TEMPORARY NSUserDefaults stub on iOS) |
| `data/cache/PendingQueue` | Mutex-protected offline submission queue |
| `di/` | Koin DI modules (platform actuals supply Settings + SecureStorage) |
| `util/Uuid` | RFC 4122 v4 UUIDs via `kotlin.uuid.Uuid` for idempotency keys |

## Endpoints consumed

| Method | Path | Purpose |
|---|---|---|
| POST | `/wp-json/jwt-auth/v1/token` | Exchange email + password for a 7-day JWT |
| GET | `/wp-json/simplify-ops/v1/auth/me` | Profile + role check |
| GET | `/wp-json/simplify-ops/v1/tasks` | User's tasks (filtered by created_by) |
| GET | `/wp-json/simplify-ops/v1/tasks/{id}` | Single task (ownership-checked) |
| POST | `/wp-json/simplify-ops/v1/tasks` | Submit a new task (idempotency-key supported) |
| GET | `/wp-json/simplify-ops/v1/tasks/{id}/comments` | Shared Notes thread for a task |
| POST | `/wp-json/simplify-ops/v1/tasks/{id}/comments` | Post a Shared Note |
| GET | `/wp-json/simplify-ops/v1/messages` | Cross-task feed of recent Shared Notes |

## Security

The v0.7.6 audit flagged plaintext credentials, world-readable backups, no
release-build minification, and a few smaller items. v0.8.0 fixes:

| Item | Status |
|---|---|
| Auth token + PII in plaintext SharedPreferences | **Android**: EncryptedSharedPreferences + Android Keystore. **iOS**: Keychain Services with `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` (verify by building iOS targets; written without iOS compile-test environment). |
| `android:allowBackup="true"` | Fixed: set to `false` (this is what actually disables backup). The `data_extraction_rules.xml` and `backup_rules.xml` are defense-in-depth — they exclude shared prefs / databases / files if anyone ever flips `allowBackup` back to `true`. |
| Ktor Logging at INFO in release | Fixed: gated via expect/actual `networkLogLevel()` returning `NONE` in release. |
| No R8/ProGuard | Fixed: `isMinifyEnabled = true` with `proguard-rules.pro` keeping serializers and Koin reflection targets. |
| `PendingQueue` race | Fixed: every method runs under a `kotlinx.coroutines.sync.Mutex`. |
| No certificate pinning | **Scaffolded, off by default. There is no MITM protection until enabled.** Code is in place for both Android (OkHttp `CertificatePinner`) and iOS (NSURLSession `didReceiveChallenge` with SPKI SHA-256). To activate, extract fingerprints with the openssl one-liner below, add current + backup pins to `CertificatePinning.PINS`, set `ENABLED = true`, and set a calendar reminder for cert rotation. |
| `generateUuid()` was fake | Fixed: now uses `kotlin.uuid.Uuid.random()` which is CSPRNG-backed and emits proper v4. |
| No tests | Not addressed in this pass. |

### Enabling certificate pinning

```kotlin
// composeApp/src/commonMain/kotlin/com/simplifybiz/ops/data/CertificatePinning.kt
object CertificatePinning {
    const val ENABLED: Boolean = true
    val PINS: List<String> = listOf(
        "sha256/...",   // current
        "sha256/..."    // backup (next cert in the chain)
    )
}
```

Get the fingerprint with:

```bash
openssl s_client -connect ops.simplifybiz.com:443 \
    -servername ops.simplifybiz.com < /dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform DER \
  | openssl dgst -sha256 -binary \
  | openssl enc -base64
```

## Building

```bash
# Debug install on a connected Android device
./gradlew :composeApp:installDebug

# Release APK (signed, requires signing config in local.properties)
./gradlew :composeApp:assembleRelease

# iOS framework for Xcode
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

## Plugin compatibility

| Mobile version | Required plugin version |
|---|---|
| v0.8.x | smplfy-ops-mobile v0.7.0 or newer |
| v0.7.6 (Messages tab) | smplfy-ops-mobile v0.7.0 |
| v0.7.5 (Comments) | smplfy-ops-mobile v0.5.0 - v0.6.0 |
| v0.7.4 (Profile fields) | smplfy-ops-mobile v0.4.0 |
| v0.7.3 (JWT auth) | smplfy-ops-mobile v0.3.7 |
| v0.7.0 - v0.7.2 | smplfy-ops-mobile v0.3.5 - v0.3.7 (Application Password auth) |

## Onboarding a new mobile user

This is what an administrator does on ops to provision a new user:

1. Add the user as a MemberPress member (or create a WP user manually).
2. Set their MemberPress fields:
   - **Organization** (custom field, slug `organization`)
   - **Bill Client Email** (slug `mepr_bill_client_email`)
   - Optionally: Organization Website, Phone, Address.
3. Tick the **Submit Task Mobile App** role on their profile (mobile access is opt-in, even Administrators need this role).
4. Share the app with them.

The user logs into the app with their normal WordPress email and password.
