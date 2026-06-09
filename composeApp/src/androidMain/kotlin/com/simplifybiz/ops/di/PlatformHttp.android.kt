package com.simplifybiz.ops.di

import com.simplifybiz.ops.data.CertificatePinning
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import okhttp3.CertificatePinner

actual fun HttpClientConfig<*>.installPlatformSecurity() {
    if (!CertificatePinning.ENABLED || CertificatePinning.PINS.isEmpty()) return

    engine {
        if (this is OkHttpConfig) {
            val pinner = CertificatePinner.Builder().apply {
                CertificatePinning.PINS.forEach { pin ->
                    add(CertificatePinning.HOSTNAME, pin)
                }
            }.build()
            config { certificatePinner(pinner) }
        }
    }
}
