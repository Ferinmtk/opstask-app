package com.simplifybiz.ops.di

import io.ktor.client.HttpClientConfig

actual fun HttpClientConfig<*>.installPlatformSecurity() {
    // no-op: certificate pinning is disabled (CertificatePinning.ENABLED = false).
    // The Darwin engine uses the system trust store by default.
}
